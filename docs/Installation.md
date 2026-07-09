# Installation

# Installation

## Raspberry Pi OS

Run from the repository root:

```bash
chmod +x scripts/install.sh
./scripts/install.sh
```

The installer:

- Installs Java 17, Maven, Node.js, Chromium, nginx, and supporting packages.
- Builds the Spring Boot backend.
- Builds the Vite frontend.
- Publishes frontend files to `/opt/burnmetrix-dashboard/frontend`.
- Publishes the backend jar to `/opt/burnmetrix-dashboard/backend`.
- Creates systemd services for the backend and kiosk browser.
- Installs manual commands: `burnmetrix-start` and `burnmetrix-stop`.
- Configures Chromium to open `http://localhost` when started manually.

## Docker

```bash
docker compose up --build
```

## Troubleshooting

Start the dashboard:

```bash
burnmetrix-start
```

Stop the dashboard:

```bash
burnmetrix-stop
```

Check services:

```bash
systemctl status burnmetrix-backend
systemctl status burnmetrix-kiosk
```

Check backend logs:

```bash
journalctl -u burnmetrix-backend -f
```
