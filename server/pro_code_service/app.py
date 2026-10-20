"""Small first-party backend for one-time Zing Pro codes.

Deploy behind HTTPS. The Android app intentionally refuses custom codes when no HTTPS
base URL is configured. Code plaintext is only shown by the issuance CLI; SQLite stores hashes.
"""
from __future__ import annotations
import hashlib
import os
import sqlite3
import time
from pathlib import Path
from typing import Optional
from fastapi import FastAPI
from pydantic import BaseModel, Field

DB_PATH = Path(os.environ.get("ZING_CODES_DB", "./zing_codes.sqlite3"))
DB_PATH.parent.mkdir(parents=True, exist_ok=True)
app = FastAPI(title="Zing Pro Code Service", docs_url=None, redoc_url=None)


def db() -> sqlite3.Connection:
    conn = sqlite3.connect(DB_PATH, timeout=10, isolation_level=None)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA journal_mode=WAL")
    conn.execute("""CREATE TABLE IF NOT EXISTS codes (
        code_hash TEXT PRIMARY KEY,
        expires_at_ms INTEGER,
        redeemed_at_ms INTEGER,
        installation_id TEXT
    )""")
    conn.execute("""CREATE TABLE IF NOT EXISTS entitlements (
        installation_id TEXT PRIMARY KEY,
        activated_at_ms INTEGER NOT NULL,
        code_hash TEXT NOT NULL UNIQUE,
        active INTEGER NOT NULL DEFAULT 1
    )""")
    return conn


def hash_code(code: str) -> str:
    return hashlib.sha256(code.strip().upper().encode("utf-8")).hexdigest()


class RedeemRequest(BaseModel):
    code: str = Field(min_length=1, max_length=64)
    installation_id: str = Field(min_length=16, max_length=80, pattern=r"^[a-zA-Z0-9-]+$")


class EntitlementRequest(BaseModel):
    installation_id: str = Field(min_length=16, max_length=80, pattern=r"^[a-zA-Z0-9-]+$")


@app.get("/health")
def health():
    return {"ok": True}


@app.post("/api/v1/redeem")
def redeem(req: RedeemRequest):
    code_hash = hash_code(req.code)
    now = int(time.time() * 1000)
    conn = db()
    try:
        conn.execute("BEGIN IMMEDIATE")
        row = conn.execute("SELECT * FROM codes WHERE code_hash=?", (code_hash,)).fetchone()
        if row is None:
            conn.execute("ROLLBACK")
            return {"ok": False, "active": False, "code": "invalid_code"}
        expires = row["expires_at_ms"]
        if expires is not None and int(expires) < now:
            conn.execute("ROLLBACK")
            return {"ok": False, "active": False, "code": "expired"}
        if row["redeemed_at_ms"] is not None:
            if row["installation_id"] == req.installation_id:
                ent = conn.execute("SELECT activated_at_ms, active FROM entitlements WHERE installation_id=? AND code_hash=?", (req.installation_id, code_hash)).fetchone()
                conn.execute("COMMIT")
                return {"ok": True, "active": bool(ent and ent["active"]), "code": "already_redeemed_here", "activated_at_ms": ent["activated_at_ms"] if ent else row["redeemed_at_ms"]}
            conn.execute("ROLLBACK")
            return {"ok": False, "active": False, "code": "already_redeemed"}
        conn.execute("UPDATE codes SET redeemed_at_ms=?, installation_id=? WHERE code_hash=? AND redeemed_at_ms IS NULL", (now, req.installation_id, code_hash))
        conn.execute("INSERT INTO entitlements(installation_id, activated_at_ms, code_hash, active) VALUES(?,?,?,1) ON CONFLICT(installation_id) DO UPDATE SET activated_at_ms=excluded.activated_at_ms, code_hash=excluded.code_hash, active=1", (req.installation_id, now, code_hash))
        conn.execute("COMMIT")
        return {"ok": True, "active": True, "code": "activated", "activated_at_ms": now}
    except Exception:
        try: conn.execute("ROLLBACK")
        except sqlite3.Error: pass
        raise
    finally:
        conn.close()


@app.post("/api/v1/entitlement")
def entitlement(req: EntitlementRequest):
    conn = db()
    try:
        row = conn.execute("SELECT active, activated_at_ms FROM entitlements WHERE installation_id=?", (req.installation_id,)).fetchone()
        return {"ok": True, "active": bool(row and row["active"]), "code": "active" if row and row["active"] else "inactive", "activated_at_ms": row["activated_at_ms"] if row else None}
    finally:
        conn.close()
