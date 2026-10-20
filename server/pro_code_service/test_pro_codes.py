"""Local API tests: python -m unittest test_pro_codes.py (requires requirements.txt)."""
from __future__ import annotations

import hashlib
import tempfile
import unittest
from pathlib import Path

from fastapi.testclient import TestClient

import app as service


class ProCodeServiceTests(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory()
        service.DB_PATH = Path(self.temp.name) / "test.sqlite3"
        self.client = TestClient(service.app)

    def tearDown(self) -> None:
        self.temp.cleanup()

    def insert_code(self, code: str, expires_at_ms: int | None = None) -> None:
        conn = service.db()
        conn.execute(
            "INSERT INTO codes(code_hash, expires_at_ms, redeemed_at_ms, installation_id) VALUES(?,?,NULL,NULL)",
            (hashlib.sha256(code.strip().upper().encode("utf-8")).hexdigest(), expires_at_ms),
        )
        conn.close()

    def test_code_activates_once_and_entitlement_can_be_refreshed(self) -> None:
        code = "ZING-TEST-ONE-TIME"
        device = "a1c3b5d7-e9f1-4a2b-8c4d-6e8f0a2b4c6d"
        other_device = "b2d4f6a8-e0c2-4b6d-8e0f-2a4c6e8f0b1d"
        self.insert_code(code)

        first = self.client.post("/api/v1/redeem", json={"code": code, "installation_id": device})
        repeat = self.client.post("/api/v1/redeem", json={"code": code, "installation_id": other_device})
        refresh = self.client.post("/api/v1/entitlement", json={"installation_id": device})

        self.assertTrue(first.json()["active"])
        self.assertGreater(first.json()["activated_at_ms"], 0)
        self.assertEqual(repeat.json()["code"], "already_redeemed")
        self.assertTrue(refresh.json()["active"])

    def test_entitlement_endpoint_is_safe_before_any_code_is_issued(self) -> None:
        result = self.client.post(
            "/api/v1/entitlement",
            json={"installation_id": "a1c3b5d7-e9f1-4a2b-8c4d-6e8f0a2b4c6d"},
        )
        self.assertEqual(result.status_code, 200)
        self.assertFalse(result.json()["active"])

    def test_unknown_code_is_rejected(self) -> None:
        result = self.client.post(
            "/api/v1/redeem",
            json={"code": "ZING-NOT-REAL", "installation_id": "a1c3b5d7-e9f1-4a2b-8c4d-6e8f0a2b4c6d"},
        )
        self.assertFalse(result.json()["active"])
        self.assertEqual(result.json()["code"], "invalid_code")

    def test_expired_code_is_rejected(self) -> None:
        code = "ZING-EXPIRED-CODE"
        self.insert_code(code, expires_at_ms=1)
        result = self.client.post(
            "/api/v1/redeem",
            json={"code": code, "installation_id": "a1c3b5d7-e9f1-4a2b-8c4d-6e8f0a2b4c6d"},
        )
        self.assertFalse(result.json()["active"])
        self.assertEqual(result.json()["code"], "expired")


if __name__ == "__main__":
    unittest.main()
