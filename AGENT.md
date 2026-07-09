# BurnMetrix Dashboard Agent Notes

## Project Overview

BurnMetrix Dashboard is a Raspberry Pi touchscreen kiosk dashboard for cycling, weather, calendar, system status, and Strava metabolic calorie analysis.

The main app lives in this repository:

```text
C:\Users\zacha\Documents\Codex\2026-07-02\files-mentioned-by-the-user-project\burnmetrix-dashboard
```

## Stack

- Frontend: React, TypeScript, Vite, Material UI, React Router, TanStack Query, Recharts, Axios
- Backend: Java, Spring Boot 3.3.x, Maven, Spring Web, Spring Security, Spring Data JPA
- Database: SQLite with Flyway migrations
- Raspberry Pi runtime: nginx, systemd, Chromium kiosk mode

## Useful Commands

Run frontend tests:

```powershell
cd frontend
npm run test
```

Build frontend:

```powershell
cd frontend
npm run build
```

Run backend tests using the local Maven install:

```powershell
& 'C:\Users\zacha\Documents\Codex\2026-07-02\files-mentioned-by-the-user-project\work\tools\apache-maven-3.9.16\bin\mvn.cmd' '-Dmaven.repo.local=C:\Users\zacha\Documents\Codex\2026-07-02\files-mentioned-by-the-user-project\work\m2' -f backend\pom.xml test
```

Run backend locally:

```powershell
& 'C:\Users\zacha\Documents\Codex\2026-07-02\files-mentioned-by-the-user-project\work\tools\apache-maven-3.9.16\bin\mvn.cmd' '-Dmaven.repo.local=C:\Users\zacha\Documents\Codex\2026-07-02\files-mentioned-by-the-user-project\work\m2' -f backend\pom.xml spring-boot:run
```

Run frontend locally:

```powershell
cd frontend
npm run dev
```

## Maven

Maven is installed locally for this workspace, not globally on the Windows PATH:

```text
C:\Users\zacha\Documents\Codex\2026-07-02\files-mentioned-by-the-user-project\work\tools\apache-maven-3.9.16
```

Use the workspace-local Maven repository under:

```text
C:\Users\zacha\Documents\Codex\2026-07-02\files-mentioned-by-the-user-project\work\m2
```

This avoids depending on the user's global Maven setup.

## Important Implementation Notes

- The dashboard should not start automatically on the Raspberry Pi. The installer creates manual controls instead.
- Manual Pi commands are `burnmetrix-start` and `burnmetrix-stop`.
- The kiosk close button calls the backend endpoint that closes Chromium.
- The Calories page includes the Strava metabolic analysis workflow ported from:

```text
C:\Users\zacha\Documents\Codex\2026-06-17\id-like-to-create-an-app\outputs\strava-metabolic-app
```

- Strava credentials are configured with `STRAVA_CLIENT_ID` and `STRAVA_CLIENT_SECRET`.
- Keep SQLite JDBC URLs quoted in YAML because values like `jdbc:sqlite::memory:` contain colons.
- Do not add `org.flywaydb:flyway-database-sqlite`; it was unavailable for the Spring Boot-managed Flyway version used here. `flyway-core` works with the current backend tests.

## Verification Status

Known good checks:

```text
npm run test
npm run build
mvn test via the local Maven install
```

The backend Maven test suite passed after the YAML quoting and Flyway dependency cleanup.
