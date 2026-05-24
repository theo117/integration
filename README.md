# Business Operations Hub

Business Operations Hub is a portfolio-grade Spring Boot application that demonstrates how a small business can centralise fragmented operational workflows into a single dashboard.

## What it includes

- Lead capture through manual entry and simulated webhook ingestion
- CSV lead import for spreadsheet-style intake
- Integration event log for inbound, outbound, failed, and skipped integration activity
- Mini CRM pipeline with status updates from `NEW` to `WON` or `LOST`
- Invoice tracking with pending, paid, and overdue visibility
- Notifications for new leads, status movement, and overdue invoices
- A polished dashboard UI built with Thymeleaf, CSS, and vanilla JavaScript
- A monthly reporting page for lead, conversion, and invoice visibility
- Email automation hooks with safe local fallback logging
- Authentication with admin and operations access
- Admin user management with database-backed account creation and activation controls
- Password reset and self-service password change flows

## Tech stack

- Java 21+
- Spring Boot
- Spring MVC + Thymeleaf
- Spring Data JPA
- H2 database for fast local setup

## Run locally

You can run the project in either of these ways:

### Option 1: Maven already installed

```bash
mvn spring-boot:run
```

### Option 2: Use the included Windows bootstrap wrapper

If Maven is not installed globally, this repo includes a lightweight Windows bootstrap script that downloads Maven locally into `.mvn/apache-maven` the first time you run it:

```bat
mvnw.cmd spring-boot:run
```

### Option 3: Run with the dev profile on port 8081

If port `8080` is already in use on your machine, start the app with the included dev profile:

```bat
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

Email automation is disabled by default in local development, so email events are logged instead of sent.

Open:

- Dashboard: `http://localhost:8080/dashboard`
- Reporting: `http://localhost:8080/reports`
- Integrations: `http://localhost:8080/integrations`
- H2 Console: `http://localhost:8080/h2-console`

When using the dev profile:

- Dashboard: `http://localhost:8081/dashboard`
- Reporting: `http://localhost:8081/reports`
- Integrations: `http://localhost:8081/integrations`
- H2 Console: `http://localhost:8081/h2-console`

H2 connection settings:

- JDBC URL: `jdbc:h2:mem:integrationhub`
- Username: `sa`
- Password: leave blank

## API endpoints

Browser-driven write requests are protected by Spring Security CSRF tokens. The Thymeleaf pages expose the token as
metadata and `dashboard.js` attaches it to JSON and CSV upload requests automatically.

- `GET /api/dashboard`
- `GET /api/reports`
- `GET /api/integrations/events`
- `GET /api/leads`
- `POST /api/leads`
- `POST /api/leads/webhook`
- `POST /api/leads/import/csv`
- `PATCH /api/leads/{id}/status`
- `GET /api/invoices`
- `POST /api/invoices`
- `PATCH /api/invoices/{id}/status`

## Integration demo

The project now demonstrates integrations through explicit adapter-style entry points and a visible event log.

- Website/API lead intake records a `WEBSITE_LEAD` event.
- Signed webhook intake records accepted and rejected `WEBHOOK_LEAD` events.
- CSV import records a `CSV_IMPORT` event with imported and skipped counts.
- Lead status and invoice workflow changes record operational events.
- Email automation records `SENT`, `SKIPPED`, or `FAILED` outbound events, including retry timing for failures.

View recent integration activity at:

- `GET /integrations`
- `GET /api/integrations/events`

Download the header-only CSV import template at:

- `GET /samples/lead-import-template.csv`

Webhook requests are intended for external systems, so they are CSRF-exempt and protected with `X-Integration-Key` instead:

```bash
curl -X POST http://localhost:8080/api/leads/webhook \
  -H "Content-Type: application/json" \
  -H "X-Integration-Key: local-webhook-key" \
  -d '{
    "name": "Contact Name",
    "email": "contact@example.com",
    "phone": "+27000000000",
    "company": "Company Name",
    "source": "WEBHOOK",
    "notes": "External system handoff"
  }'
```

Change the local webhook key in `application.properties`:

```properties
businesshub.integration.webhook-api-key=local-webhook-key
```

## Authentication

The app uses Spring Security with a custom login page and database-backed users.

On first startup, the application seeds bootstrap accounts into the `app_users` table using the configured defaults:

- `admin / Admin@123`
- `ops / Ops@123`

Access rules:

- `ADMIN`: dashboard, reports, and operational APIs
- `OPS`: dashboard, integrations, and operational APIs
- reports are restricted to admins
- user management is restricted to admins

You can change the bootstrap credentials in `application.properties`:

```properties
businesshub.security.admin.username=admin
businesshub.security.admin.password=Admin@123
businesshub.security.ops.username=ops
businesshub.security.ops.password=Ops@123
```

After users have been seeded once, changing those properties will not retroactively overwrite existing database passwords.

Admins can also manage users from the application at:

- `GET /admin/users`

This screen supports:

- creating new internal users
- assigning `ADMIN` or `OPS` roles
- activating or disabling accounts
- resetting passwords for internal users

Signed-in users can also manage their own password at:

- `GET /account/security`

This screen supports:

- verifying the current password
- updating the password for the active session account

## Email automation

Automated email triggers are included for:

- new lead received
- lead status changed
- invoice created
- invoice paid
- invoice overdue

Local default:

- `businesshub.email.enabled=false`
- events are logged only

To enable real SMTP delivery, configure:

```properties
businesshub.email.enabled=true
businesshub.email.from=you@example.com
businesshub.email.ops-inbox=ops@example.com
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=you@example.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## Deploy to Render

Render natively supports a fixed set of language runtimes, and Java apps are deployed there via Docker. This project is already prepared for that path.

Files included for deployment:

- `Dockerfile`
- `render.yaml`
- `src/main/resources/application-render.properties`

### Recommended deployment path

1. Push this repo to GitHub.
2. In Render, create a new Blueprint deployment from the repo.
3. Render will provision:
   - a Docker-based web service
   - a PostgreSQL database
4. The service will run with the `render` profile.
5. After the first deploy, check the generated admin and ops passwords in the Render environment variable panel.

### Render production behavior

- Uses PostgreSQL instead of H2
- Binds to Render's `PORT` environment variable
- Exposes `/actuator/health` for health checks
- Keeps email automation disabled unless you turn it on with SMTP settings
- Seeds secure bootstrap passwords for the default admin and ops users

### If you want real email in production

Add these environment variables in Render:

```properties
BUSINESSHUB_EMAIL_ENABLED=true
BUSINESSHUB_EMAIL_FROM=you@example.com
BUSINESSHUB_EMAIL_OPS_INBOX=ops@example.com
BUSINESSHUB_INTEGRATION_WEBHOOK_API_KEY=replace-with-a-long-random-key
SPRING_MAIL_HOST=smtp.example.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=you@example.com
SPRING_MAIL_PASSWORD=your-password
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
```

## Project docs

- Architecture notes: [ARCHITECTURE.md](./ARCHITECTURE.md)
- Expansion roadmap: [ROADMAP.md](./ROADMAP.md)

## Portfolio positioning

Instead of presenting this as a generic CRUD system, position it as:

> An integration-focused operations dashboard for SMEs that unifies lead intake, workflow tracking, invoicing, and alerts in one place.
