"""Run on the server: python issue_codes.py --count 10 [--expires-days 90].
Keep the output private; each plaintext code is shown once, while only its hash is stored.
"""
import argparse, hashlib, os, secrets, sqlite3, time
from pathlib import Path

parser = argparse.ArgumentParser()
parser.add_argument('--count', type=int, default=1)
parser.add_argument('--expires-days', type=int, default=0, help='0 means no expiry')
args = parser.parse_args()
if not 1 <= args.count <= 500:
    raise SystemExit('--count must be between 1 and 500')
db_path = Path(os.environ.get('ZING_CODES_DB', './zing_codes.sqlite3'))
conn = sqlite3.connect(db_path)
conn.execute('CREATE TABLE IF NOT EXISTS codes (code_hash TEXT PRIMARY KEY, expires_at_ms INTEGER, redeemed_at_ms INTEGER, installation_id TEXT)')
now = int(time.time() * 1000)
expires = now + args.expires_days * 86400000 if args.expires_days > 0 else None
for _ in range(args.count):
    code = 'ZING-' + '-'.join(secrets.token_hex(2).upper() for _ in range(3))
    digest = hashlib.sha256(code.encode()).hexdigest()
    conn.execute('INSERT INTO codes(code_hash, expires_at_ms, redeemed_at_ms, installation_id) VALUES(?,?,NULL,NULL)', (digest, expires))
    print(code)
conn.commit(); conn.close()
