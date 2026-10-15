<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.flightfare.dao.FlightDAO" %>
<%@ page import="com.flightfare.model.Flight" %>
<%@ page import="com.flightfare.model.FlightComparison" %>
<%@ page import="com.flightfare.service.FlightComparisonService" %>
<%
    FlightDAO dao = new FlightDAO();
    List<Flight> flights = dao.getAllFlights();
    List<FlightComparison> comparisons = dao.getAllComparisons();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>FlightFare Pro - Flight Ticket Comparison & Analytics</title>
    <style>
        :root {
            --primary: #184c78;
            --primary-light: #ebf4fc;
            --accent: #007acc;
            --success: #228b22;
            --bg: #f8fafc;
            --card: #ffffff;
            --text: #1e293b;
            --border: #e2e8f0;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', system-ui, -apple-system, sans-serif; }
        body { background-color: var(--bg); color: var(--text); padding: 24px; }
        .header { background: var(--card); border: 1px solid var(--border); border-radius: 12px; padding: 20px 24px; margin-bottom: 24px; box-shadow: 0 2px 4px rgba(0,0,0,0.02); display: flex; justify-content: space-between; align-items: center; }
        .header h1 { font-size: 22px; color: var(--primary); display: flex; align-items: center; gap: 8px; }
        .header p { font-size: 13px; color: #64748b; margin-top: 4px; }
        .badges { display: flex; gap: 10px; }
        .badge { padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: 600; color: #fff; }
        .badge-primary { background: var(--primary); }
        .badge-success { background: var(--success); }
        .badge-warning { background: #d97706; }
        
        .grid { display: grid; grid-template-columns: 2fr 1fr; gap: 24px; }
        .card { background: var(--card); border: 1px solid var(--border); border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); margin-bottom: 24px; }
        .card h2 { font-size: 17px; margin-bottom: 14px; color: var(--primary); display: flex; align-items: center; justify-content: space-between; }
        
        table { width: 100%; border-collapse: collapse; font-size: 13px; margin-top: 10px; }
        th, td { padding: 10px 12px; text-align: left; border-bottom: 1px solid var(--border); }
        th { background: var(--primary-light); color: var(--primary); font-weight: 600; }
        tr:hover { background-color: #f1f5f9; }
        .price { font-weight: 700; color: var(--success); font-size: 14px; }
        .direct { color: #0284c7; font-weight: 600; }
        
        .comp-item { background: #f8fafc; border: 1px solid var(--border); border-radius: 8px; padding: 12px 14px; margin-bottom: 10px; }
        .comp-route { font-weight: 700; color: var(--primary); font-size: 14px; }
        .comp-verdict { font-size: 12px; color: #0f766e; font-weight: 600; margin-top: 4px; background: #ccfbf1; padding: 4px 8px; border-radius: 4px; display: inline-block; }
        
        .sql-box { background: #1e1e1e; color: #9cdcfe; font-family: Consolas, monospace; font-size: 12px; padding: 12px; border-radius: 6px; overflow-x: auto; margin-top: 8px; }
    </style>
</head>
<body>

    <div class="header">
        <div>
            <h1>✈️ FlightFare Pro — Real-time Flight Ticket Comparison</h1>
            <p>Database: <b>MySQL 8.0 (flight_db)</b> &bull; Synchronized with MySQL Workbench &bull; Eclipse Java Web Project</p>
        </div>
        <div class="badges">
            <span class="badge badge-primary">Flights: <%= flights.size() %></span>
            <span class="badge badge-success">MySQL: Connected</span>
            <span class="badge badge-warning">API Mode: Protected (1-Time)</span>
        </div>
    </div>

    <div class="grid">
        <!-- Flights Table -->
        <div class="card">
            <h2><span>📋 Flight Tickets Catalog</span> <span style="font-size:12px; color:#64748b;">Source: MySQL Table 'flights'</span></h2>
            <table>
                <thead>
                    <tr>
                        <th>Flight</th>
                        <th>Airline</th>
                        <th>Route</th>
                        <th>Time</th>
                        <th>Duration</th>
                        <th>Stops</th>
                        <th>Price</th>
                        <th>Source</th>
                    </tr>
                </thead>
                <tbody>
                    <% for(Flight f : flights) { %>
                    <tr>
                        <td><b><%= f.getFlightNumber() %></b></td>
                        <td><%= f.getAirline() %></td>
                        <td><%= f.getRoute() %></td>
                        <td><%= f.getDepartureTime() %></td>
                        <td><%= f.getDuration() %></td>
                        <td class="<%= f.getStops() == 0 ? "direct" : "" %>"><%= f.getStopsDescription() %></td>
                        <td class="price">$<%= String.format("%.2f", f.getPrice()) %></td>
                        <td style="font-size:11px; color:#64748b;"><%= f.getDataSource() %></td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>

        <!-- Sidebar: Comparisons & Workbench Guide -->
        <div>
            <div class="card">
                <h2><span>⚖️ Saved Ticket Comparisons</span></h2>
                <% if(comparisons.isEmpty()) { %>
                    <p style="color:#64748b; font-size:13px;">No comparisons saved yet. Run comparison in Java GUI or Eclipse.</p>
                <% } else { %>
                    <% for(FlightComparison c : comparisons) { %>
                    <div class="comp-item">
                        <div class="comp-route"><%= c.getRoute() %></div>
                        <div style="font-size:13px; margin: 4px 0;">
                            <b><%= c.getFlight1Number() %></b> ($<%= c.getFlight1Price() %>) vs <b><%= c.getFlight2Number() %></b> ($<%= c.getFlight2Price() %>)
                        </div>
                        <div style="font-size:12px; color:#dc2626;">Diff: $<%= String.format("%.2f", c.getPriceDifference()) %></div>
                        <div class="comp-verdict"><%= c.getVerdict() %></div>
                    </div>
                    <% } %>
                <% } %>
            </div>

            <div class="card">
                <h2><span>💻 MySQL Workbench Instructions</span></h2>
                <p style="font-size:13px; line-height: 1.5; color:#475569;">
                    Open <b>MySQL Workbench</b> &rarr; Connect to <b>Local instance 3306</b> (User: <code>root</code>, Pass: <code>root</code>) and run:
                </p>
                <div class="sql-box">
USE flight_db;<br>
SELECT * FROM flights ORDER BY price ASC;<br>
SELECT * FROM flight_comparisons;
                </div>
            </div>
        </div>
    </div>

</body>
</html>
