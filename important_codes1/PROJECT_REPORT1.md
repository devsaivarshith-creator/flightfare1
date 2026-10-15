# ✈️ FlightFare Pro — Comprehensive Project Report & System Documentation

**Author / Developer**: Varshith ([devsaivarshith-creator](https://github.com/devsaivarshith-creator))  
**Project Name**: FlightFare Pro (Flight Ticket Search, Intelligent Comparison & MySQL Engine)  
**Repository**: [https://github.com/devsaivarshith-creator/flightfare1](https://github.com/devsaivarshith-creator/flightfare1)  
**Version**: 2.0.0 (Production Release)  
**Target Environment**: Java SE 17+ / Java 21, Eclipse IDE, VS Code / Antigravity IDE, MySQL 8.0+  

---

## 📑 Table of Contents
1. [Executive Summary & Abstract](#1-executive-summary--abstract)
2. [System Architecture](#2-system-architecture)
3. [Technology Stack & Prerequisites](#3-technology-stack--prerequisites)
4. [Database Design & Relational Schema](#4-database-design--relational-schema)
5. [Core Software Modules](#5-core-software-modules)
6. [Authentic API Integration & Quota Governance](#6-authentic-api-integration--quota-governance)
7. [Intelligent Comparison & Recommendation Engine](#7-intelligent-comparison--recommendation-engine)
8. [Historical Price Fluctuation & Cost Tracker](#8-historical-price-fluctuation--cost-tracker)
9. [User Interface Design System (GUI Walkthrough)](#9-user-interface-design-system-gui-walkthrough)
10. [Step-by-Step Setup, Compilation & Execution](#10-step-by-step-setup-compilation--execution)
11. [MySQL Workbench Querying Guide](#11-mysql-workbench-querying-guide)
12. [Verification, Quality Assurance & Troubleshooting](#12-verification-quality-assurance--troubleshooting)
13. [Future Enhancements & Conclusion](#13-future-enhancements--conclusion)

---

## 1. Executive Summary & Abstract

**FlightFare Pro** is an enterprise-grade desktop and web-compatible flight intelligence application engineered in Java and backed by MySQL. Modern air travelers and booking systems face severe fragmentation across flight aggregators, inconsistent fare fluctuations, and unpredictable pricing spikes. 

FlightFare Pro addresses these challenges by providing:
- **Authentic Multi-Date Flight Inventory**: Integration of real-world flight itineraries harvested via **FlightAPI.io** and **AviationStack**, randomized and distributed across a comprehensive multi-week calendar schedule (25 distinct upcoming departure dates).
- **Intelligent Dual-Flight Comparison Engine**: An algorithmic evaluation module that contrasts flight options on price deltas, layover stops, travel duration, and cabin class to deliver clear, data-driven booking verdicts (*"Clear Winner"*, *"Fastest Route"*, *"Budget Choice"*, etc.).
- **Historical Price Fluctuation & Cost Tracker**: A dedicated audit and telemetry sub-system that tracks fare movements across API refresh sessions, flagging price drops (🟢) versus price hikes (🔴), historical low points, and dynamic volatility.
- **Strict API Quota & Credit Governance**: An automated safety mechanism that limits expensive third-party REST API queries to a hard ceiling of 30 credits, preserving tokens while delivering synchronized live fare updates.
- **Modern Swing & Responsive Web Interface**: A custom design system boasting high-contrast typography, anti-aliased rounded cards, real-time KPI metrics, and tabbed workflow navigation.

---

## 2. System Architecture

FlightFare Pro follows a decoupled **Layered MVC (Model-View-Controller / Service-DAO)** architecture:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                              │
│   FlightAppGUI (Swing Modern UI)   │   index.jsp (Web Servlet Client) │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
┌────────────────────────────────────▼───────────────────────────────────┐
│                          SERVICE LAYER                                 │
│  FlightComparisonService  │  LiveFlightApiService  │  ApiCreditManager │
│  DataImporter             │                                            │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
┌────────────────────────────────────▼───────────────────────────────────┐
│                     DATA ACCESS LAYER (DAO)                            │
│           FlightDAO (CRUD, Search, Comparison & Price Audit)           │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
┌────────────────────────────────────▼───────────────────────────────────┐
│                       PERSISTENCE LAYER                                │
│       DbConfig (JDBC Connection Pooling & Automatic DDL Migration)      │
│       MySQL 8.0 Database: `flight_db` (flights, comparisons, history)  │
└────────────────────────────────────────────────────────────────────────┘
```

### Component Interaction Flow
1. **Startup**: `FlightAppMain` boots, reads `db.properties`, calls `DbConfig.initDatabase()` to ensure MySQL schema and tables exist.
2. **Ingestion**: `DataImporter` checks if MySQL contains flights; if empty, it ingests authentic cached API payloads from `flightapi_cache.json` and `aviationstack_cache.json`, assigning realistic calendar dates, authentic airport names, and durations.
3. **Seeding**: `FlightDAO.seedInitialPriceHistoryIfEmpty()` registers baseline fare records to prime the Cost Tracker.
4. **Display**: `FlightAppGUI` launches with 5 tabs, pulling data via `FlightDAO` into Swing tables styled with custom `ModernUI` cell renderers.
5. **Comparison**: When a user selects two flights, `FlightComparisonService` computes metrics and commits the comparison into `flight_comparisons`.
6. **Live Refresh**: If requested in Tab 5, `LiveFlightApiService` queries live endpoints, updates `flights`, records delta records in `price_history`, and decrements credits via `ApiCreditManager`.

---

## 3. Technology Stack & Prerequisites

| Layer / Aspect | Technology / Library | Version / Detail |
| :--- | :--- | :--- |
| **Programming Language** | Java (JDK) | Java SE 17 LTS (compiled with `--release 17` for backwards compatibility with 17, 21, and 25) |
| **GUI Framework** | Java Swing & AWT | Custom `ModernUI` component toolkit with Java 2D anti-aliasing |
| **Relational Database** | MySQL Server | MySQL 8.0+ Community Server / Workbench |
| **Database Driver** | MySQL Connector/J | `mysql-connector-j-8.3.0.jar` (JDBC Type 4) |
| **JSON Serialization** | Google Gson | `gson-2.10.1.jar` |
| **External REST APIs** | FlightAPI.io & AviationStack | Dual real-time airfare & schedule endpoints |
| **Web Container (Optional)** | Apache Tomcat / Servlet | Compatible with Tomcat 9/10 (`Dynamic Web Module 4.0`) |
| **Version Control** | Git & GitHub | Remote repository at `devsaivarshith-creator/flightfare1` |

---

## 4. Database Design & Relational Schema

The database `flight_db` is created automatically on application launch if it does not already exist. It consists of three normalized tables:

```mermaid
erDiagram
    FLIGHTS {
        int id PK
        varchar flight_number
        varchar airline
        varchar origin_code
        varchar origin_airport
        varchar destination_code
        varchar destination_airport
        datetime departure_time
        datetime arrival_time
        int duration
        int stops
        varchar cabin_class
        decimal price
        varchar currency
        varchar data_source
        varchar status
        timestamp created_at
    }

    FLIGHT_COMPARISONS {
        int comparison_id PK
        varchar route
        varchar flight1_number
        varchar flight1_airline
        decimal flight1_price
        varchar flight2_number
        varchar flight2_airline
        decimal flight2_price
        decimal price_difference
        decimal percent_difference
        varchar cheaper_flight
        varchar duration_comparison
        varchar verdict
        timestamp created_at
    }

    PRICE_HISTORY {
        int history_id PK
        int token_batch
        varchar flight_number
        varchar airline
        varchar route
        decimal old_price
        decimal new_price
        decimal price_change
        decimal percent_change
        varchar change_type
        timestamp recorded_at
        varchar source
    }

    FLIGHTS ||--o{ FLIGHT_COMPARISONS : "referenced by route and flight_number"
    FLIGHTS ||--o{ PRICE_HISTORY : "tracks price changes over time"
```

### Table 1: `flights`
Stores complete flight schedule, route, airport, and pricing records.
```sql
CREATE TABLE IF NOT EXISTS flights (
    id INT AUTO_INCREMENT PRIMARY KEY,
    flight_number VARCHAR(20) NOT NULL,
    airline VARCHAR(100) NOT NULL,
    origin_code VARCHAR(10) NOT NULL,
    origin_airport VARCHAR(150),
    destination_code VARCHAR(10) NOT NULL,
    destination_airport VARCHAR(150),
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME NOT NULL,
    duration INT DEFAULT 0,              -- duration in minutes
    stops INT DEFAULT 0,
    cabin_class VARCHAR(50) DEFAULT 'Economy',
    price DECIMAL(10,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'USD',
    data_source VARCHAR(50) DEFAULT 'FlightAPI.io',
    status VARCHAR(50) DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Table 2: `flight_comparisons`
Stores the permanent audit log of all side-by-side flight evaluations made by users or background routines.
```sql
CREATE TABLE IF NOT EXISTS flight_comparisons (
    comparison_id INT AUTO_INCREMENT PRIMARY KEY,
    route VARCHAR(30) NOT NULL,
    flight1_number VARCHAR(20) NOT NULL,
    flight1_airline VARCHAR(100) NOT NULL,
    flight1_price DECIMAL(10,2) NOT NULL,
    flight2_number VARCHAR(20) NOT NULL,
    flight2_airline VARCHAR(100) NOT NULL,
    flight2_price DECIMAL(10,2) NOT NULL,
    price_difference DECIMAL(10,2) NOT NULL,
    percent_difference DECIMAL(5,2) NOT NULL,
    cheaper_flight VARCHAR(100) NOT NULL,
    duration_comparison VARCHAR(255),
    verdict VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Table 3: `price_history`
Stores historical price delta records across refresh sessions, logging price drops, hikes, and new fares.
```sql
CREATE TABLE IF NOT EXISTS price_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    token_batch INT DEFAULT 1,
    flight_number VARCHAR(20) NOT NULL,
    airline VARCHAR(100) NOT NULL,
    route VARCHAR(30) NOT NULL,
    old_price DECIMAL(10,2) NOT NULL,
    new_price DECIMAL(10,2) NOT NULL,
    price_change DECIMAL(10,2) NOT NULL,
    percent_change DECIMAL(6,2) NOT NULL,
    change_type VARCHAR(20) NOT NULL,     -- DROPPED, INCREASED, UNCHANGED, NEW_FLIGHT
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    source VARCHAR(100) DEFAULT 'Live API Refresh'
);
```

---

## 5. Core Software Modules

### 1. `com.flightfare.FlightAppMain`
- Main execution entry point.
- Coordinates database initialization, sample query verification, CLI mode detection (`--cli`), and Swing GUI thread dispatching.

### 2. `com.flightfare.config.DbConfig`
- Manages MySQL JDBC connection lifecycle.
- Reads connection properties from `db.properties` with fallback defaults (`localhost:3306`, `flight_db`, `root`/`root`).
- Automatically creates `flight_db` and executes `CREATE TABLE IF NOT EXISTS` for all tables.

### 3. `com.flightfare.dao.FlightDAO`
- Encapsulates all SQL CRUD and analytical queries.
- Key queries:
  - Multi-attribute search (`searchFlights` with dynamic `WHERE` filtering).
  - Batch insertion (`saveFlightsBatch`).
  - Dropdown aggregators (`getDistinctOrigins`, `getDistinctDestinations`, `getDistinctAirlines`).
  - Historical price tracking (`recordPriceChange`, `getAllPriceHistory`, `getPriceHistoryByTokenBatch`).

### 4. `com.flightfare.model.*`
- `Flight.java`: POJO representing flight itinerary with formatted time and duration utilities (`getFormattedDuration()`).
- `FlightComparison.java`: Captures side-by-side comparison metrics, price deltas, and generated verdicts.
- `PriceHistory.java`: Captures individual price changes, percentage swings, and formatting pills.

### 5. `com.flightfare.service.DataImporter`
- Converts raw cached JSON payloads from FlightAPI.io and AviationStack into normalized Java models.
- **Multi-Date Distribution**: Distributes flight schedules realistically across 25 upcoming calendar dates between October 15, 2026, and December 12, 2026.
- **Airport Resolution**: Maps IATA codes (`MEL`, `SYD`, `JFK`, `LHR`, `DXB`, etc.) to full official airport terminal names.
- Computes arrival times dynamically using departure time + authentic flight duration minutes.

### 6. `com.flightfare.service.FlightComparisonService`
- Compares any two flight objects.
- Analyzes price difference: `Math.abs(f1.getPrice() - f2.getPrice())`.
- Analyzes duration difference: `Math.abs(f1.getDuration() - f2.getDuration())`.
- Generates smart booking advice (e.g., whether paying extra is worth saving layover time).

### 7. `com.flightfare.service.ApiCreditManager`
- Enforces API consumption limits.
- Tracks used vs remaining credits against the strict 30-credit ceiling.
- Automatically persists quota usage in `api_credits.properties`.

### 8. `com.flightfare.service.LiveFlightApiService`
- Handles live HTTP REST calls to FlightAPI.io and AviationStack using `HttpURLConnection`.
- `refreshAllPlatforms()` updates fares across both platforms concurrently in one click.

### 9. `com.flightfare.ui.ModernUI`
- Houses custom Swing look-and-feel constants:
  - Dark background (`#0F172A`), Card panels (`#1E293B`), Accent Blue (`#3B82F6`), Success Emerald (`#10B981`), Amber Warning (`#F59E0B`), Danger Rose (`#F43F5E`).
  - Custom buttons with rounded borders, anti-aliased font rendering, and hover animations.
  - Table cell renderers for colored badges, price formatting, and alignment.

### 10. `com.flightfare.ui.FlightAppGUI`
- Main visual interface organized into 5 functional tabs with top KPI dashboard cards.

---

## 6. Authentic API Integration & Quota Governance

### Dual REST API Strategy
1. **FlightAPI.io (`https://api.flightapi.io`)**: Provides commercial airline fares, multi-leg segments, stopover counts, and ticket prices in USD.
2. **AviationStack (`https://api.aviationstack.com/v1/flights`)**: Provides real-time flight telemetry, airline ICAO/IATA identifiers, departure/arrival terminals, and live flight statuses.

### Quota Governance & Anti-Overage Protection
- Both APIs impose strict usage caps on free developer tiers.
- To prevent accidental billing or token exhaustion:
  1. Authentic API payloads were initially fetched and saved to local JSON caches (`flightapi_cache.json` and `aviationstack_cache.json`).
  2. The application boots instantaneously using these authentic records without draining API calls.
  3. A global `ApiCreditManager` maintains a persistent credit counter capped at **30 credits max**.
  4. Any call to `refreshAllPlatforms()` checks quota availability before issuing network requests.

### Realistic Multi-Date Calendar Generation
Instead of clustering all flights onto arbitrary identical dates, `DataImporter` spreads the 156 authentic flights across **25 distinct calendar dates** spanning October to December 2026:
- 10-15, 10-17, 10-20, 10-22, 10-25, 10-28, 10-31
- 11-03, 11-06, 11-09, 11-12, 11-15, 11-18, 11-21, 11-24, 11-27, 11-30
- 12-02, 12-05, 12-08, 12-10, 12-12
Departure times maintain realistic separation throughout the day, and arrival times are computed from departure timestamp + exact flight duration minutes.

---

## 7. Intelligent Comparison & Recommendation Engine

When two flights are compared in Tab 2, `FlightComparisonService` applies the following decision matrix:

| Condition | Verdict Output | Recommendation Logic |
| :--- | :--- | :--- |
| **Identical Price & Duration** | `⚖️ Equal Fare` | Compare based on airline reputation or terminal convenience. |
| **Cheaper AND Shorter/Equal Duration** | `🏆 Clear Winner: [Airline]` | Save money AND time; no trade-offs required. |
| **Cheaper but Longer Duration** | `💰 Budget Choice: [Airline]` | Lower fare, but involves more flight/layover time. |
| **More Expensive but Shorter Duration** | `⚡ Express Choice: [Airline]` | Premium fare to save travel time. |
| **Price Delta < $15** | `⏱️ Comparable Fares` | Choose based on preferred departure schedule. |

All comparison metrics (delta dollars, percentage difference, winning airline, verdict) are automatically saved to MySQL table `flight_comparisons` for historical analysis.

---

## 8. Historical Price Fluctuation & Cost Tracker

Tab 3 provides a dedicated **Cost Tracker & Price Trends** dashboard:
- **KPI Metrics**:
  - *Largest Price Drop*: Displays the highest recorded dollar savings across all flights.
  - *Fluctuation Events Logged*: Total number of fare adjustment records in `price_history`.
  - *Smart Booking Advice*: Algorithmic signal recommending whether to book now or wait based on recent market trends.
- **Audit Table**:
  - Displays Flight #, Airline, Route, Old Price, New Price, Change Badge, Change Type (`DROPPED`, `INCREASED`, `NEW_FLIGHT`), Timestamp, and Source.
  - Price drops are highlighted in vibrant emerald green (`-$XX.XX (-YY%)`).
  - Price increases are highlighted in rose red (`+$XX.XX (+YY%)`).
- **Interactive Simulation**:
  - Allows users to simulate market fluctuations on specific routes to preview cost trends before executing live updates.

---

## 9. User Interface Design System (GUI Walkthrough)

FlightFare Pro features a custom dark-mode theme inspired by modern enterprise travel platforms:

### 1. Header Banner & Global KPI Ribbon
- Application branding with status indicator: `MySQL Connected | Database: flight_db`.
- Quick action button: `"⚡ Refresh Live Data"` (executes dual-platform sync).
- KPI Cards: Total Flights in DB (156), Active Routes, Tracked Airlines, Best Fare Available.

### 2. Tab 1: 🔍 Browse & Search Flights
- **Search Panel**: Filter by Origin City/Airport, Destination City/Airport, Airline, Cabin Class, and Max Price slider.
- **Live Flights Table**: Color-coded columns displaying Flight Number, Airline, Origin Airport & IATA, Destination Airport & IATA, Departure Time, Arrival Time, Duration, Stops, Price ($ USD), and Status.

### 3. Tab 2: ⚖️ Compare Flights Studio
- Two dynamic dropdown selectors populated from active flights.
- **Side-by-Side Comparison Cards**: Visual side-by-side cards comparing Airline logo/name, Route, Departure, Duration, Layover stops, and Ticket Price.
- **Verdict Banner**: Highlighted card displaying Price Difference, Percentage Savings, and the Smart Verdict.
- **Save Action**: One-click button to record the comparison directly into MySQL.

### 4. Tab 3: 📈 Price Fluctuation History & Cost Tracker
- Visual trend metrics and session filters.
- Detailed audit grid tracking historical price movements.
- Simulation controls for previewing price volatility across specific flight corridors.

### 5. Tab 4: 🗄️ MySQL Database & Table Browser
- Direct database inspection panel without leaving the application.
- Allows switching between `flights`, `flight_comparisons`, and `price_history`.
- Includes an interactive SQL Query Console allowing users to execute custom `SELECT` queries against MySQL with instantaneous grid visualization.

### 6. Tab 5: ⚡ API Quota & Live Refresh
- Real-time visual progress bar tracking API usage against the 30-credit ceiling.
- Token refresh controls: Updates live fares across all routes from both FlightAPI.io and AviationStack concurrently.
- Safety lock: Automatically prevents further queries when 30 credits have been exhausted.

---

## 10. Step-by-Step Setup, Compilation & Execution

### Prerequisites
1. **Java JDK**: JDK 17 LTS or JDK 21 installed.
2. **MySQL Server 8.0+**: Running locally on port `3306` with user `root` / password `root` (or configured via `db.properties`).

### Method 1: Running in Eclipse IDE
1. Open Eclipse IDE.
2. Click **File &rarr; Open Projects from File System...**
3. Select `c:\Users\varsh\OneDrive\Desktop\flightfare\flightfare1`.
4. Navigate to `src/main/java/com/flightfare/FlightAppMain.java`.
5. Right-click `FlightAppMain.java` &rarr; **Run As &rarr; Java Application** (or `Shift + Alt + X, J`).

### Method 2: Running in Antigravity / VS Code IDE
1. Open the project folder in Antigravity IDE / VS Code.
2. Open [FlightAppMain.java](file:///c:/Users/varsh/OneDrive/Desktop/flightfare/flightfare1/src/main/java/com/flightfare/FlightAppMain.java).
3. Click the **Run** button at the top right (or press `Shift + F10`).

### Method 3: Running from Command Prompt / PowerShell
Compile using Java 17 release target to ensure universal bytecode compatibility:
```powershell
cd "c:\Users\varsh\OneDrive\Desktop\flightfare\flightfare1"

# 1. Compile all Java sources
$sources = (Get-ChildItem -Recurse -Filter *.java -Path src/main/java).FullName
javac --release 17 -cp "lib/*" -d build/classes $sources

# 2. Copy resources
Copy-Item src/main/resources/db.properties build/classes/

# 3. Launch GUI Application
java -cp "build/classes;lib/*" com.flightfare.FlightAppMain
```

### Method 4: Running in Headless / CLI Mode
To run without launching a graphical window:
```powershell
java -cp "build/classes;lib/*" com.flightfare.FlightAppMain --cli
```

---

## 11. MySQL Workbench Querying Guide

To inspect and analyze the project's data directly in **MySQL Workbench**:
1. Open MySQL Workbench.
2. Connect to **Local instance MySQL80** (`localhost:3306`, user: `root`).
3. Open a new SQL Query tab and execute:

```sql
-- 1. Switch to the FlightFare database
USE flight_db;

-- 2. View all 156 flights ordered by price
SELECT id, flight_number, airline, origin_code, origin_airport, 
       destination_code, destination_airport, departure_time, 
       duration, stops, price, data_source
FROM flights
ORDER BY price ASC;

-- 3. Find cheapest flights grouped by route
SELECT origin_code, destination_code, 
       MIN(price) AS lowest_price, 
       AVG(price) AS avg_price, 
       COUNT(*) AS total_flights
FROM flights
GROUP BY origin_code, destination_code
ORDER BY lowest_price ASC;

-- 4. View all saved flight comparison records
SELECT comparison_id, route, 
       flight1_number, flight1_airline, flight1_price, 
       flight2_number, flight2_airline, flight2_price, 
       price_difference, cheaper_flight, verdict
FROM flight_comparisons
ORDER BY comparison_id DESC;

-- 5. View price drop history (Cost Tracker)
SELECT history_id, token_batch, flight_number, airline, route, 
       old_price, new_price, price_change, percent_change, change_type, recorded_at
FROM price_history
WHERE change_type = 'DROPPED'
ORDER BY price_change ASC;

-- 6. Check flight schedule distribution across dates
SELECT DATE(departure_time) AS flight_date, COUNT(*) AS flights_scheduled
FROM flights
GROUP BY DATE(departure_time)
ORDER BY flight_date ASC;
```

---

## 12. Verification, Quality Assurance & Troubleshooting

### Diagnostic Matrix

| Problem / Symptom | Root Cause | Solution Implemented |
| :--- | :--- | :--- |
| `UnsupportedClassVersionError: class file version 65.0... recognizes up to 61.0` | Bytecode was compiled with JDK 21 target (65.0), whereas runtime/Eclipse expects Java 17 (61.0). | Recompiled all source files with `javac --release 17`, generating Java 17 bytecode (61.0) compatible across JDK 17, 21, and 25. |
| `Unresolved compilation problems: PriceHistory cannot be resolved to a type` | `FlightDAO.java` previously contained a shadowed static inner class while `FlightAppGUI.java` imported `model.PriceHistory`. | Extracted `PriceHistory.java` into a standalone model class in `com.flightfare.model`, imported it into `FlightDAO`, and removed the inner class. |
| Data looked synthetic or identical in departure times | Early test data had static mock timestamps. | Replaced with authentic API records spread across 25 distinct dates with calculated arrival times based on flight duration. |
| High live API token consumption | Frequent REST calls drained free tier quotas. | Implemented persistent caching and `ApiCreditManager` with a 30-credit hard ceiling and dual-platform 1-click sync. |

---

## 13. Future Enhancements & Conclusion

### Future Roadmap
1. **Passenger Booking & Ticketing Simulation**: Allow users to reserve seats, generate simulated boarding passes (PDF), and assign PNR numbers.
2. **Email & Webhook Alerts**: Send real-time notifications when a flight price drops below a user-defined threshold.
3. **Machine Learning Price Forecasting**: Implement a regression model to predict whether airfares will increase or drop within 7 days of departure.
4. **Interactive Route Map**: Embed an interactive visual map (using Leaflet or OpenLayers) plotting flight arcs between origin and destination airports.

### Conclusion
**FlightFare Pro** successfully combines authentic aviation data harvesting, intelligent comparison algorithms, historical cost tracking, and relational database persistence in a unified, modern Java application. With 0 compilation errors, clean Java 17 bytecode compatibility, a resilient multi-date schedule, and strict API quota governance, the system is fully operational, thoroughly tested, and ready for evaluation.

---
*Report generated and committed to GitHub: [https://github.com/devsaivarshith-creator/flightfare1](https://github.com/devsaivarshith-creator/flightfare1)*
