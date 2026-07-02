# BurnMetrix Backend

Spring Boot 3 backend for BurnMetrix Dashboard.

## Responsibilities

- Own business logic and integration boundaries.
- Expose REST APIs consumed by the React frontend.
- Persist local dashboard data in SQLite.
- Provide mock services until external providers are integrated.

## Initial Endpoints

- `GET /api/weather/current`
- `GET /api/calendar/events`
- `GET /api/cycling/today`
- `GET /api/cycling/week`
- `GET /api/cycling/month`
- `GET /api/cycling/year`
- `GET /api/cycling/lifetime`
- `GET /api/cycling/rides/{rideId}`
- `GET /api/settings`

