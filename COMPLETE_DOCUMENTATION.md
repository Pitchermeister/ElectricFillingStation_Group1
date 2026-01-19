# EV Charging Station MVP — Concise Docs

## Overview
- Status: complete MVP (5/5 business requirements)
- Date: 2026-01-19 | Version: 1.2
- In-memory demo (no DB)

## Requirements (all done)
1) Customer registration + prepaid balance
2) Billing: AC/DC × kWh + minutes (price frozen at session start)
3) Dynamic per-location pricing
4) Network status: prices + charger state per location
5) Invoice status: chronological items + balance

## Run
```bash
mvn exec:java -Dexec.mainClass=org.example.Main   # demo
mvn test -Dtest=RunCucumberTest                  # all BDD tests
```

## Project structure
```
src/main/java/org/example/
  domain/ (Client, ClientAccount, ChargingSession, InvoiceEntry, Charger, Location, PriceConfiguration)
  Management/ (ClientManager, StationManager, BillingManager, ChargingService, ChargingManager)
  Main.java (demo)
src/test/java/org/example/ (step definitions)
src/test/resources/org/example/ (feature files)
```

## Key APIs
- Registration: `ClientManager.registerClient(id, name, email)`
- Prepaid: `ClientAccount.topUp(amount)`, `debit(amount)`, `getBalance()`
- Start/finish session: `ChargingService.startSession(...)`, `finishSession(...)`
- Pricing per location: `StationManager.updateLocationPricing(...)`
- Status reports: `getNetworkStatusReport()`, `getNetworkStatusQuickView()`
- Invoice reports: `BillingManager.getDetailedInvoiceReport(clientId, clientManager)`, `getQuickInvoiceView(...)`

## Billing formula
```
price = (kWh × pricePerKWh[mode]) + (minutes × pricePerMinute[mode])
AC: €0.45/kWh + €0.20/min | DC: €0.65/kWh + €0.20/min
```

## Demo data
- 3 customers (Alice, Bob, Carol)
- 10 locations / 16 chargers (AC/DC mix)
- Sample sessions with invoices and balances

## Test coverage
- 50+ BDD scenarios across registration, billing, pricing, status, invoices

## Next steps (suggested)
- Add persistence (e.g., PostgreSQL)
- Expose REST API
- Frontend / mobile apps
