package com.flightfare.model;

import java.sql.Timestamp;

/**
 * Model representing a comparison between two flight tickets.
 */
public class FlightComparison {
    private int comparisonId;
    private String route;
    private String flight1Number;
    private String flight1Airline;
    private double flight1Price;
    private String flight2Number;
    private String flight2Airline;
    private double flight2Price;
    private double priceDifference;
    private String cheaperFlight;
    private String durationDifference;
    private String verdict;
    private Timestamp comparedAt;

    public FlightComparison() {}

    public FlightComparison(String route, String flight1Number, String flight1Airline, double flight1Price,
                            String flight2Number, String flight2Airline, double flight2Price,
                            double priceDifference, String cheaperFlight, String durationDifference,
                            String verdict) {
        this.route = route;
        this.flight1Number = flight1Number;
        this.flight1Airline = flight1Airline;
        this.flight1Price = flight1Price;
        this.flight2Number = flight2Number;
        this.flight2Airline = flight2Airline;
        this.flight2Price = flight2Price;
        this.priceDifference = priceDifference;
        this.cheaperFlight = cheaperFlight;
        this.durationDifference = durationDifference;
        this.verdict = verdict;
    }

    public int getComparisonId() { return comparisonId; }
    public void setComparisonId(int comparisonId) { this.comparisonId = comparisonId; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public String getFlight1Number() { return flight1Number; }
    public void setFlight1Number(String flight1Number) { this.flight1Number = flight1Number; }

    public String getFlight1Airline() { return flight1Airline; }
    public void setFlight1Airline(String flight1Airline) { this.flight1Airline = flight1Airline; }

    public double getFlight1Price() { return flight1Price; }
    public void setFlight1Price(double flight1Price) { this.flight1Price = flight1Price; }

    public String getFlight2Number() { return flight2Number; }
    public void setFlight2Number(String flight2Number) { this.flight2Number = flight2Number; }

    public String getFlight2Airline() { return flight2Airline; }
    public void setFlight2Airline(String flight2Airline) { this.flight2Airline = flight2Airline; }

    public double getFlight2Price() { return flight2Price; }
    public void setFlight2Price(double flight2Price) { this.flight2Price = flight2Price; }

    public double getPriceDifference() { return priceDifference; }
    public void setPriceDifference(double priceDifference) { this.priceDifference = priceDifference; }

    public String getCheaperFlight() { return cheaperFlight; }
    public void setCheaperFlight(String cheaperFlight) { this.cheaperFlight = cheaperFlight; }

    public String getDurationDifference() { return durationDifference; }
    public void setDurationDifference(String durationDifference) { this.durationDifference = durationDifference; }

    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }

    public Timestamp getComparedAt() { return comparedAt; }
    public void setComparedAt(Timestamp comparedAt) { this.comparedAt = comparedAt; }

    @Override
    public String toString() {
        return String.format("Comparison [%s]: %s ($%.2f) vs %s ($%.2f) | Diff: $%.2f | Cheaper: %s | Verdict: %s",
                route, flight1Number, flight1Price, flight2Number, flight2Price, priceDifference, cheaperFlight, verdict);
    }
}
