# 🎙️ FlightFare Pro — 3-Person Team Presentation Script

**Project**: FlightFare Pro (Flight Ticket Search, Intelligent Comparison & MySQL Engine)  
**Total Estimated Time**: 8 – 10 Minutes (with Live Demo)  
**Roles Division**:
- **Speaker 1**: Introduction, Problem Statement, Architecture & Flight Search Demo (Tab 1)
- **Speaker 2**: Database Schema, Dual API Governance & Comparison Engine Demo (Tabs 2 & 4)
- **Speaker 3**: Cost Tracker, Live Sync, Technical Solutions & Conclusion (Tabs 3 & 5)

---

## ⏱️ Timeline Overview

| Speaker | Section / Topic | Duration | UI / Live Action |
| :--- | :--- | :--- | :--- |
| **Speaker 1** | Introduction, Motivation & Flight Search | 3 min | Launch App, show Console logs, Tab 1 filtering |
| **Speaker 2** | Database Architecture, Dual API & Comparison Engine | 3 min | Tab 2 side-by-side comparison, Tab 4 SQL viewer |
| **Speaker 3** | Cost Tracker, Live Refresh, Troubleshooting & Q&A | 3 min | Tab 3 trend audit, Tab 5 quota sync, Wrap up |

---

## 🎬 Detailed Script

---

### 👤 SPEAKER 1: Introduction, Problem Statement & Flight Search (0:00 – 3:00)

**[Visual: Title Slide or Project Repository / Start on Desktop]**

> **Speaker 1**:  
> *"Good morning / afternoon everyone! Today, our team is excited to present **FlightFare Pro** — an intelligent flight comparison and price tracking system built using Java and MySQL.*
>
> *When booking flights, passengers constantly struggle with fragmented ticket aggregators, erratic price swings, and hidden trade-offs between price and layover duration. Our goal was to build a full-stack Java system that brings transparency to flight fares by harvesting authentic aviation data, running automated comparison algorithms, and tracking historical price drops over time.*
>
> *Under the hood, FlightFare Pro is powered by **Java SE 17**, backed by a relational **MySQL 8.0** database via JDBC, and presented through a custom anti-aliased **Swing ModernUI** desktop interface.*
>
> *Let me demonstrate by launching the application directly from our IDE."*

**[Action: Speaker 1 clicks Run on `FlightAppMain.java` / Launches App]**

> **Speaker 1**:  
> *"Notice our console output: upon launch, the application automatically establishes a connection to MySQL, verifies our schema, and loads **156 authentic flights** spread across a realistic 25-date upcoming calendar schedule.*
>
> *(Points to Tab 1: Browse & Search Flights)*  
> *Here on our main dashboard, you can see global KPI cards tracking our active flights, routes, and airlines. In the search pane, users can filter dynamically by Origin, Destination, Airline, or Maximum Budget.*
>
> *Notice our airport tags: unlike basic mock apps, every flight features authentic airport terminal names and IATA codes like Melbourne International, Sydney Kingsford Smith, and Adelaide. Arrival timestamps are dynamically calculated based on authentic flight durations.*
>
> *Now, I will pass it over to **Speaker 2**, who will explain our database architecture and our intelligent flight comparison engine."*

---

### 👤 SPEAKER 2: Database Schema, API Governance & Comparison Engine (3:00 – 6:00)

**[Action: Switch to Tab 2: Compare Flights Studio]**

> **Speaker 2**:  
> *"Thank you, Speaker 1! Behind this intuitive interface lies a robust relational database schema hosted on MySQL called `flight_db`. It consists of three normalized tables:*
> 1. *`flights`: Storing all itinerary details, departure/arrival schedules, stops, and current fares.*
> 2. *`flight_comparisons`: Serving as an immutable audit log of side-by-side comparisons.*
> 3. *`price_history`: Tracking historical price trends and fluctuations across refresh batches.*
>
> *To populate this system responsibly, we integrated **two real-world REST APIs**: **FlightAPI.io** for commercial fares and **AviationStack** for live flight telemetry. Because third-party API quotas are expensive, we implemented a custom `ApiCreditManager` that enforces a **strict 30-credit hard ceiling** and persists token caches locally to prevent overage charges.*
>
> *(Points to Tab 2: Compare Flights Studio)*  
> *Now, let's look at our **Intelligent Comparison Engine**. A traveler often has to decide: should I pay $30 more to save 2 hours of layover time?*
>
> *Let's select two flights on the same route — for example, Melbourne to Adelaide."*

**[Action: Select two flights in Tab 2 and click Compare]**

> **Speaker 2**:  
> *"Watch how our algorithm evaluates both options in real time. It calculates:*
> - *The exact price delta in dollars and percentage.*
> - *The travel time difference in hours and minutes.*
> - *And generates a smart verdict — such as **'🏆 Clear Winner'** when a flight is both cheaper and faster, or **'💰 Budget Choice'** when you save money with a trade-off on duration.*
>
> *With one click on **'Save Comparison to MySQL'**, this record is immediately written into the database.*
>
> *(Switches to Tab 4: MySQL Database Browser)*  
> *In Tab 4, we have an embedded SQL Query Runner and Table Inspector. You can query `flight_comparisons` right here or in MySQL Workbench, confirming instant persistence.*
>
> *Now, I will hand over to **Speaker 3** to demonstrate our Cost Tracker and Live API Sync."*

---

### 👤 SPEAKER 3: Cost Tracker, Live Refresh & Technical Solutions (6:00 – 9:00)

**[Action: Switch to Tab 3: Price Fluctuation History & Cost Tracker]**

> **Speaker 3**:  
> *"Thank you, Speaker 2! One of the standout features of FlightFare Pro is our **Historical Price Fluctuation & Cost Tracker**.*
>
> *Airlines change ticket prices dynamically based on demand. In Tab 3, our system monitors these shifts across refresh sessions.*
>
> *Notice our KPI cards at the top:*
> - *The **Largest Price Drop** observed in the market.*
> - *The total **Fluctuation Events Logged**.*
> - *And algorithmic **Smart Booking Advice** advising whether to book now or hold.*
>
> *In the audit table below, price drops are highlighted with emerald green badges — such as `-$21.03 (-16.8%)` — while price increases are tagged in red. This enables travelers to recognize whether today's price is genuinely a bargain.*
>
> *(Switches to Tab 5: Live API Quota & Dual Refresh)*  
> *In Tab 5, we provide full transparency into API consumption. You can see the visual quota progress bar tracking our 30-credit allowance.*
>
> *When we click **'⚡ Refresh Live Data'**, our background service queries both FlightAPI.io and AviationStack concurrently, updating fares across routes and logging new records into `price_history`.*
>
> *(Technical Engineering Highlight)*  
> *From an engineering standpoint, we solved critical deployment challenges:*
> - *We ensured universal bytecode compatibility by compiling to **Java 17 target bytecode (class version 61.0)**, allowing seamless execution across Java 17, 21, and modern IDEs without `UnsupportedClassVersionError`.*
> - *We decoupled our models into clean POJOs to eliminate compiler resolution errors across Eclipse and VS Code.*
> - *And our entire repository is version-controlled and deployed on GitHub with automated Git tracking.*
>
> *In conclusion, FlightFare Pro demonstrates how Java SE, MySQL, and REST APIs can be combined to deliver a high-performance, cost-aware travel platform.*
>
> *Thank you for your time, and we now welcome any questions!"*

---

## 💡 Quick Q&A Preparation Sheet

**Q1: How do you prevent hitting API rate limits or incurring costs?**  
> *Answer*: *"We implemented a local JSON cache and a singleton `ApiCreditManager` with a persistent hard ceiling of 30 credits. Every refresh requires user confirmation and checks remaining credits before dispatching network requests."*

**Q2: How does the application decide the 'Verdict' when comparing flights?**  
> *Answer*: *"Our `FlightComparisonService` uses a multi-factor decision matrix that evaluates both price delta and duration delta. If Flight A is cheaper and faster, it's a 'Clear Winner'. If it's cheaper but longer, it's labeled 'Budget Choice'. If fares are within $15, it suggests deciding based on departure schedule."*

**Q3: Can this be used in a web browser instead of Swing?**  
> *Answer*: *"Yes! The project includes `index.jsp` and a `web.xml` configuration, making it ready for deployment as a Dynamic Web Project on Apache Tomcat."*
