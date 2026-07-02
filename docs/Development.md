# Development

# Development

## Frontend

```bash
cd frontend
npm install
npm run dev
npm run test
npm run build
```

## Backend

```bash
cd backend
mvn test
mvn spring-boot:run
```

## Database

SQLite database files live under `database/data/` and are ignored by Git. Schema changes should be added as Flyway migrations in `backend/src/main/resources/db/migration`.

## API Strategy

The UI consumes typed API helpers in `frontend/src/api`. Backend controllers should remain thin and delegate business logic to services.
