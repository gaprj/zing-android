# Zing Pro single-use code service

This backend is source code only; it has **not** been deployed. The Android build uses an empty
`ZING_PRO_REDEMPTION_BASE_URL` unless configured with a Gradle property, so it will not unlock Pro
with a locally hardcoded code or accept custom codes while the service is unavailable.

## Deployment outline

1. Use a maintained Linux VPS. Create a dedicated unprivileged service account and a private,
   backed-up directory for `zing_codes.sqlite3` with restricted file permissions.
2. Install Python 3.11+ and create a virtual environment. Install `requirements.txt`.
3. Start the app behind a reverse proxy with a valid TLS certificate, e.g. Caddy or Nginx + ACME.
   Do not expose the raw Uvicorn port to the public internet. Configure rate limiting on `/api/v1/redeem`;
   `nginx-rate-limit.example.conf` contains a snippet to adapt to the real HTTPS virtual host. `/health` is the minimal health check.
4. Set `ZING_CODES_DB=/var/lib/zing-pro-codes/zing_codes.sqlite3` for both the API and issuance CLI.
5. Issue private codes on the server with `python issue_codes.py --count 10`. To add expiry, use
   `--expires-days 90`. Plaintext codes are printed once; share them privately and do not commit them.
6. Configure Android builds with `-PzingProRedemptionBaseUrl=https://YOUR-REAL-HOSTNAME` when
   running Gradle. A real hostname, TLS configuration, firewall, backups, and deployment are the
   operator's responsibility; no URL or credentials are fabricated in this project.

Endpoints:

- `POST /api/v1/redeem`: body `{ "code": "ZING-...", "installation_id": "..." }`. Code redemption
  is atomic in SQLite and a code is bound to the first installation ID that redeems it.
- `POST /api/v1/entitlement`: body `{ "installation_id": "..." }`. Refreshes the activation state.

## Security and limitation

Only the code hash is stored. Redemption is one-time and performed server-side. The installation ID
is an app-generated random local identifier, not user authentication or an unforgeable device identity.
A determined modified client can spoof its own identifier/API calls; use Google Play purchases as the
stronger account/device-restorable route. For stronger fraud resistance, extend this service with
an authenticated account or a server-side Google Play purchase verification flow. Keep this API on
HTTPS; the Android client deliberately rejects non-HTTPS URLs.

## Run local API tests

From this directory, install `requirements.txt` and run `python -m unittest -v test_pro_codes.py`. Tests use a temporary SQLite database; they do not touch the configured production database.
