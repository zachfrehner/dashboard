# Installation

# Installation

## Raspberry Pi OS

Run from the repository root:

```bash
chmod +x scripts/install.sh
./scripts/install.sh
```

The installer:

- Installs Java 21, Maven, Node.js, npm, Chromium, nginx, and supporting packages.
- Builds the Spring Boot backend.
- Builds the Vite frontend.
- Publishes frontend files to `/opt/burnmetrix-dashboard/frontend`.
- Publishes the backend jar to `/opt/burnmetrix-dashboard/backend`.
- Creates systemd services for the backend and kiosk browser.
- Configures Chromium to open `http://localhost`.

## Docker

```bash
docker compose up --build
```

## Troubleshooting

Check services:

```bash
systemctl status burnmetrix-backend
systemctl status burnmetrix-kiosk
```

Check backend logs:

```bash
journalctl -u burnmetrix-backend -f
```
