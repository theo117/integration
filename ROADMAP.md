# Professional Roadmap

## Phase 1: Foundation

Status: complete

- Spring Boot application setup
- Leads domain and pipeline flow
- Invoice tracking and overdue logic
- Notification stream
- Dashboard UI
- Demo seed data
- REST endpoints

## Phase 2: Portfolio polish

Recommended next

- Add tests for services and controllers
- Add DTO responses instead of returning entities directly
- Add a global layout fragment and navigation pages
- Add filtering for leads and invoices
- Add a dashboard KPI for conversion rate
- Add audit timestamps to API responses in a friendlier format

## Phase 3: Real integration credibility

Best "one real integration" options

- Email notification on new lead or invoice status change
- CSV upload that imports multiple leads
- Public REST sync to a mock CRM service
- Export invoices to CSV for accounting handoff

## Phase 4: Advanced business value

- Authentication and roles
- Team activity feed
- Reporting page with monthly trends
- Client profile view
- Notes and follow-up tasks
- Dashboard charts

## Recommended order

1. Add automated tests
2. Add DTOs and mapper layer
3. Add CSV import
4. Add one real email integration
5. Add auth
6. Add reporting

## Interview positioning

When speaking about the project, frame it as:

> A business operations dashboard that centralises lead intake, workflow progression, invoicing, and alerts to reduce fragmentation for SMEs.

Then explain the technical progression:

- MVP with simulated integrations to validate the model
- Modular service architecture to support future integrations
- Planned expansion into real external systems
