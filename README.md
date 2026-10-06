# backend

## Aiven MySQL keep-alive on Render

The backend exposes `GET /health/db` as a database-only health check. It opens a connection to Aiven MySQL and executes `SELECT 1`, without calling the AI service.

Create a Render Cron Job that runs every 10 minutes with this command, replacing the URL with the deployed backend URL:

```bash
curl --fail --silent --show-error https://YOUR-BACKEND.onrender.com/health/db
```

Use the same schedule for an existing Render cron job if one already calls `/health`. A cron request can reduce inactivity, but it cannot override Aiven free-tier suspension or Render service sleeping policies.