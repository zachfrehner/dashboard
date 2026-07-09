# Architecture

# Architecture

BurnMetrix Dashboard is split into a React frontend and Spring Boot backend.

## Frontend

- React and TypeScript application built with Vite.
- Material UI dark theme tuned for a 1280x800 touchscreen.
- React Router owns pages: Home, Calendar, Weather, Cycling, Ride Detail, Settings.
- TanStack Query and Axios own API reads.
- Mock fallback data keeps the kiosk usable when backend services are unavailable.

## Backend

- Spring Boot 3 application on Java 17 for Raspberry Pi OS package compatibility.
- REST controllers expose `/api/**`.
- Controllers depend on service interfaces.
- Mock services implement the initial behavior.
- JPA repositories and entities prepare for persistent SQLite data.
- Flyway owns database migrations.
- Spring Security is configured but authentication is disabled by default.

## Data Flow

```text
Touchscreen UI -> React Query -> Axios -> Spring REST API -> Services -> SQLite / Integrations
```

The frontend never reads SQLite directly. External integrations will be added behind backend service interfaces.
