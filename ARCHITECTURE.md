# Architecture Overview

## Purpose

Business Operations Hub is designed as an integration-style command centre for small and medium businesses. The goal is to centralise operational signals from fragmented tools into one dashboard that supports visibility and action.

## Current modules

- `LeadService`
  Handles lead capture, source tracking, and status movement.
- `InvoiceService`
  Handles invoice creation, status changes, and overdue detection.
- `NotificationService`
  Generates operational alerts from business events.
- `DashboardService`
  Aggregates data into a single dashboard view model for the UI and API.

## Layering

- `domain`
  JPA entities and enums.
- `repository`
  Database access using Spring Data JPA.
- `service`
  Business logic and orchestration.
- `web`
  Thymeleaf MVC controller and REST API controllers.
- `config`
  Bootstrapping and demo seed data.

## Request flow

1. A lead or invoice enters through a form or API request.
2. The relevant service persists the data.
3. A notification is generated from the business event.
4. `DashboardService` aggregates counts, pipeline state, open revenue, and recent activity.
5. The dashboard renders the latest operational picture.

## Integration strategy

The project uses simulated integrations first to prove the architecture and business value without depending on paid APIs.

Current input modes:

- Manual dashboard entry
- Webhook lead ingestion at `/api/leads/webhook`
- CSV-style source modelling through `LeadSource.CSV_IMPORT`

Future integration candidates:

- Email notifications
- Real CRM sync
- Accounting sync
- WhatsApp or messaging event ingestion

## Why this architecture works well for a portfolio

- It shows separation of concerns instead of controller-heavy CRUD.
- It demonstrates business-driven domain design.
- It leaves clear extension points for real integrations later.
- It supports both server-rendered UI and JSON APIs.
