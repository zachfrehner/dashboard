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

## Strava Metabolic Analysis

The Calories page can connect to Strava and analyze recent activities against the bundled lab CSV.

Set these values in `/etc/burnmetrix-dashboard/backend.env` before starting the app:

```bash
STRAVA_CLIENT_ID=your-client-id
STRAVA_CLIENT_SECRET=your-client-secret
STRAVA_REDIRECT_URI=http://localhost:8080/api/metabolic/auth/callback
```

In Strava, set the app authorization callback domain to `localhost`.

## iCalendar

Set your shared calendar subscription URL in `/etc/burnmetrix-dashboard/backend.env`:

```bash
CALENDAR_ICAL_URL="https://your-private-calendar-feed.ics"
```

If the link starts with `webcal://`, change only the scheme to `https://`.

After editing the file, restart the dashboard:

```bash
burnmetrix-stop
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
