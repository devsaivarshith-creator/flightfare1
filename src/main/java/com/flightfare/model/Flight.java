package com.flightfare.model;

import java.sql.Timestamp;

/**
 * Model representing a flight ticket.
 */
public class Flight {
    private int id;
    private String flightNumber;
    private String airline;
    private String originCode;
    private String originAirport;
    private String destinationCode;
    private String destinationAirport;
    private String departureTime;
    private String arrivalTime;
    private String duration;
    private int stops;
    private String cabinClass;
    private double price;
    private String currency;
    private String dataSource;
    private String status;
    private Timestamp createdAt;

    public Flight() {
        this.currency = "USD";
        this.cabinClass = "Economy";
        this.status = "Scheduled";
        this.stops = 0;
    }

    public Flight(String flightNumber, String airline, String originCode, String originAirport,
                  String destinationCode, String destinationAirport, String departureTime,
                  String arrivalTime, String duration, int stops, String cabinClass,
                  double price, String currency, String dataSource, String status) {
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.originCode = originCode;
        this.originAirport = originAirport;
        this.destinationCode = destinationCode;
        this.destinationAirport = destinationAirport;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.duration = duration;
        this.stops = stops;
        this.cabinClass = cabinClass;
        this.price = price;
        this.currency = currency;
        this.dataSource = dataSource;
        this.status = status;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }

    public String getOriginCode() { return originCode; }
    public void setOriginCode(String originCode) { this.originCode = originCode; }

    public String getOriginAirport() { return originAirport; }
    public void setOriginAirport(String originAirport) { this.originAirport = originAirport; }

    public String getDestinationCode() { return destinationCode; }
    public void setDestinationCode(String destinationCode) { this.destinationCode = destinationCode; }

    public String getDestinationAirport() { return destinationAirport; }
    public void setDestinationAirport(String destinationAirport) { this.destinationAirport = destinationAirport; }

    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public int getStops() { return stops; }
    public void setStops(int stops) { this.stops = stops; }

    public String getCabinClass() { return cabinClass; }
    public void setCabinClass(String cabinClass) { this.cabinClass = cabinClass; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getRoute() {
        return originCode + " \u2192 " + destinationCode;
    }

    public String getStopsDescription() {
        if (stops == 0) return "Direct (Non-stop)";
        if (stops == 1) return "1 Stop";
        return stops + " Stops";
    }

    public String getFormattedPrice() {
        return String.format("$%.2f %s", price, currency != null ? currency : "USD");
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s -> %s | %s | $%.2f",
                flightNumber, airline, originCode, destinationCode, getStopsDescription(), price);
    }
}
