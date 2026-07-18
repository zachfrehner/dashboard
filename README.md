# BurnMetrix Dashboard

BurnMetrix Dashboard is a Raspberry Pi kiosk dashboard for cycling, weather, calendar, and system status data.

The app is designed for a Raspberry Pi 4 connected to a 10-inch touchscreen in kiosk mode. It can also run locally for development.

## Planned Stack

- React, TypeScript, Vite, Material UI, React Router, TanStack Query, Recharts, Axios
- Java 17, Spring Boot 3, Maven, Spring Security, Spring Scheduling, Spring Web, Jackson
- SQLite
- Docker, Docker Compose, native Raspberry Pi install script, systemd, Chromium kiosk mode

## Repository Layout

```text
burnmetrix-dashboard/
  frontend/
  backend/
  database/
  docs/
  scripts/
  docker/
  .github/
```

## Quick Start

### Frontend Only

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. The frontend falls back to mock data if the backend is not running.

### Backend

```bash
cd backend
mvn spring-boot:run
```

The backend runs on `http://localhost:8080`.

### Docker Compose

```bash
docker compose up --build
```

Frontend: `http://localhost:5173`

Backend: `http://localhost:8080`

### Raspberry Pi

```bash
chmod +x scripts/install.sh
./scripts/install.sh
```

The installer builds the app and configures systemd services. It does not start the kiosk automatically.

Start manually on the Pi:

```bash
burnmetrix-start
```

Stop manually:

```bash
burnmetrix-stop
```

Clear Chromium cache and restart:

```bash
burnmetrix-clear-cache
```

The Calories page includes the Strava metabolic analysis workflow from the earlier BurnMetrix app. Configure `STRAVA_CLIENT_ID` and `STRAVA_CLIENT_SECRET` for the backend, then connect Strava from the dashboard.

For shared calendar events, set `CALENDAR_ICAL_URL` in `/etc/burnmetrix-dashboard/backend.env` to a private iCalendar `.ics` subscription URL.
