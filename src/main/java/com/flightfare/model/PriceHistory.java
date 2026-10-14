package com.flightfare.model;

import java.sql.Timestamp;

/**
 * Model representing a historical price change for a flight or route.
 */
public class PriceHistory {
    private int historyId;
    private int tokenBatch = 1;
    private String flightNumber;
    private String airline;
    private String route;
    private double oldPrice;
    private double newPrice;
    private double priceChange;
    private double percentChange;
    private String changeType;
    private Timestamp recordedAt;
    private String source;

    public PriceHistory() {}

    public PriceHistory(String flightNumber, String airline, String route,
                        double oldPrice, double newPrice, String changeType, String source) {
        this(1, flightNumber, airline, route, oldPrice, newPrice, changeType, source);
    }

    public PriceHistory(int tokenBatch, String flightNumber, String airline, String route,
                        double oldPrice, double newPrice, String changeType, String source) {
        this.tokenBatch = tokenBatch;
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.route = route;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.priceChange = newPrice - oldPrice;
        this.percentChange = oldPrice > 0 ? ((newPrice - oldPrice) / oldPrice) * 100.0 : 0.0;
        this.changeType = changeType;
        this.recordedAt = new Timestamp(System.currentTimeMillis());
        this.source = source != null ? source : "Live API";
    }

    public int getHistoryId() { return historyId; }
    public void setHistoryId(int historyId) { this.historyId = historyId; }

    public int getTokenBatch() { return tokenBatch; }
    public void setTokenBatch(int tokenBatch) { this.tokenBatch = tokenBatch; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public double getOldPrice() { return oldPrice; }
    public void setOldPrice(double oldPrice) { this.oldPrice = oldPrice; }

    public double getNewPrice() { return newPrice; }
    public void setNewPrice(double newPrice) { this.newPrice = newPrice; }

    public double getPriceChange() { return priceChange; }
    public void setPriceChange(double priceChange) { this.priceChange = priceChange; }

    public double getPercentChange() { return percentChange; }
    public void setPercentChange(double percentChange) { this.percentChange = percentChange; }

    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }

    public Timestamp getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Timestamp recordedAt) { this.recordedAt = recordedAt; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getFormattedPriceChange() {
        if ("NEW_FLIGHT".equalsIgnoreCase(changeType)) {
            return String.format("NEW FARE: $%.2f", newPrice);
        }
        if (priceChange < 0) {
            return String.format("-$%.2f (%.1f%%)", Math.abs(priceChange), percentChange);
        } else if (priceChange > 0) {
            return String.format("+$%.2f (+%.1f%%)", priceChange, percentChange);
        } else {
            return "$0.00 (0.0%)";
        }
    }
}
