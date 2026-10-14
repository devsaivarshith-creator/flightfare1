# ✈️ FlightFare Pro - Flight Ticket Search, Comparison & MySQL Engine

A full-stack Java application configured for **Eclipse IDE** that retrieves real-world flight tickets, calculates intelligent price and duration comparisons, and stores all flight and comparison records directly in **MySQL 8.0** so they can be viewed and queried in **MySQL Workbench**.

---

## 🚀 Key Features

1. **Lively & Colorful Modern UI**:
   - Polished design system with vibrant electric blue, emerald green, and amber accents.
   - High-contrast custom rounded buttons with anti-aliasing and hover animations — completely eliminates invisible button text issues across all OS themes.
   - Visual KPI metric cards and color-coded table renderers.
2. **📈 Cost Tracker & Price Trends (New Section)**:
   - Dedicated tab tracking real-time price drops, increases, and route fluctuation history.
   - Live KPI cards: **Largest Price Drop**, **Fluctuation Events Logged**, and **Smart Booking Advice**.
   - Color-coded trend pills (🟢 Drops/Savings vs 🔴 Price Hikes).
   - Route and trend filtering with simulated market fluctuation preview.
   - Persisted in MySQL table `price_history`.
3. **⚡ Live API Fetch with Strict 30-Credit Ceiling**:
   - Real-time Live API query engine querying FlightAPI.io.
   - Prominent real-time credit counter: `[Used: X/30 | Left: Y]`.
   - **Hard cutoff at 30 credits**: Automatically blocks any further calls once 30 credits are used to strictly protect your account against overage charges.
   - Confirmation dialog before every live call showing quota impact.
4. **Interactive Ticket Search & Comparison**:
   - Filter by Origin, Destination, Airline, Cabin Class, and Max Price.
   - Side-by-side comparison studio calculating price difference, percentage savings, duration savings, and smart recommendations (e.g. 🏆 Clear Winner vs 💰 Budget Choice).
5. **MySQL Database & Workbench Integration**:
   - Auto-creates `flights`, `flight_comparisons`, and `price_history` tables.
   - Live Workbench queries synchronized in real-time.

---

## 🛠️ How to Run in Java Eclipse

### Option A: Run as Java Application (Recommended)
1. Open **Eclipse IDE**.
2. Select **File &rarr; Open Projects from File System...** &rarr; Select `flightfare1` folder.
3. In the Package Explorer, navigate to:
   ```
   src/main/java
     └─ com.flightfare
          └─ FlightAppMain.java
   ```
4. Right-click on `FlightAppMain.java` &rarr; **Run As &rarr; Java Application**.
5. The application window will open immediately, connect to MySQL, verify the tables, and display all flights.

### Option B: Run as Web Application (Dynamic Web)
1. In Eclipse, right-click the `flightfare1` project.
2. Select **Run As &rarr; Run on Server** (e.g., Apache Tomcat).
3. Open `http://localhost:8080/flightfare1/` in your browser.

---

## 🗄️ How to View Data in MySQL Workbench

1. Launch **MySQL Workbench** on your desktop.
2. Click your local connection (**Local instance MySQL80** at `localhost:3306`).
   - **Username**: `root`
   - **Password**: `root`
3. In the SQL Query editor, copy and run:
   ```sql
   -- Select the flight database
   USE flight_db;

   -- 1. View all flight tickets (sorted by price)
   SELECT id, flight_number, airline, origin_code, destination_code, price, stops, duration, data_source
   FROM flights
   ORDER BY price ASC;

   -- 2. View all ticket comparisons
   SELECT comparison_id, route, flight1_number, flight1_price, flight2_number, flight2_price, price_difference, cheaper_flight, verdict
   FROM flight_comparisons
   ORDER BY comparison_id DESC;

   -- 3. View route summary analytics
   SELECT origin_code, destination_code, COUNT(*) AS total_flights, MIN(price) AS min_price, AVG(price) AS avg_price
   FROM flights
   GROUP BY origin_code, destination_code;
   ```

---

## 🔑 API Keys & Safety Policy

The application received two API keys:
- **FlightAPI.io**: `6acf19dda1289f0a87706cac`
- **AviationStack**: `a9e149b8333543b1ad80113f410264be`

> **Quota Safety Policy**:
> As strictly required (*"use the key only one time now and don't use it again till i say"*), both APIs were executed **one single time** to harvest real-world flight pricing and tracking records. The responses were safely cached and committed into MySQL. All further queries, searches, and comparisons are executed locally using MySQL without touching the live APIs.
