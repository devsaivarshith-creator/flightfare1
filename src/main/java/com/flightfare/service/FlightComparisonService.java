package com.flightfare.service;

import com.flightfare.model.Flight;
import com.flightfare.model.FlightComparison;

import java.util.Comparator;
import java.util.List;

/**
 * Service providing algorithms to compare flight tickets and generate actionable recommendations.
 */
public class FlightComparisonService {

    /**
     * Compares two flight tickets and produces a detailed comparison record.
     */
    public FlightComparison compare(Flight f1, Flight f2) {
        if (f1 == null || f2 == null) {
            throw new IllegalArgumentException("Both flights must be non-null for comparison");
        }

        String route = f1.getOriginCode() + " \u27A1 " + f1.getDestinationCode();
        double p1 = f1.getPrice();
        double p2 = f2.getPrice();
        double diff = Math.abs(p1 - p2);

        String cheaper;
        if (p1 < p2) {
            double pct = ((p2 - p1) / p2) * 100.0;
            cheaper = String.format("%s (%s) - Saves $%.2f (%.1f%%)", f1.getFlightNumber(), f1.getAirline(), diff, pct);
        } else if (p2 < p1) {
            double pct = ((p1 - p2) / p1) * 100.0;
            cheaper = String.format("%s (%s) - Saves $%.2f (%.1f%%)", f2.getFlightNumber(), f2.getAirline(), diff, pct);
        } else {
            cheaper = "Both tickets are identical in price ($" + p1 + ")";
        }

        int m1 = parseDurationToMinutes(f1.getDuration());
        int m2 = parseDurationToMinutes(f2.getDuration());
        String durDiff;
        String fasterFlight = null;
        if (m1 > 0 && m2 > 0) {
            int dMin = Math.abs(m1 - m2);
            int dH = dMin / 60;
            int dM = dMin % 60;
            String timeStr = (dH > 0 ? dH + "h " : "") + dM + "m";

            if (m1 < m2) {
                fasterFlight = f1.getFlightNumber();
                durDiff = String.format("%s is %s faster", f1.getFlightNumber(), timeStr);
            } else if (m2 < m1) {
                fasterFlight = f2.getFlightNumber();
                durDiff = String.format("%s is %s faster", f2.getFlightNumber(), timeStr);
            } else {
                durDiff = "Both flights have equal duration (" + f1.getDuration() + ")";
            }
        } else {
            durDiff = f1.getDuration() + " vs " + f2.getDuration();
        }

        // Generate Smart Recommendation Verdict
        StringBuilder verdict = new StringBuilder();
        if (p1 < p2 && m1 <= m2 && f1.getStops() <= f2.getStops()) {
            verdict.append("\uD83C\uDFC6 Clear Winner: ").append(f1.getFlightNumber()).append(" (")
                    .append(f1.getAirline()).append(") is cheaper by $").append(String.format("%.2f", diff))
                    .append(" AND faster/direct!");
        } else if (p2 < p1 && m2 <= m1 && f2.getStops() <= f1.getStops()) {
            verdict.append("\uD83C\uDFC6 Clear Winner: ").append(f2.getFlightNumber()).append(" (")
                    .append(f2.getAirline()).append(") is cheaper by $").append(String.format("%.2f", diff))
                    .append(" AND faster/direct!");
        } else if (p1 < p2) {
            verdict.append("\uD83D\uDCB0 Budget Choice: ").append(f1.getFlightNumber()).append(" saves $")
                    .append(String.format("%.2f", diff));
            if (m2 < m1) {
                verdict.append(", but ").append(f2.getFlightNumber()).append(" saves travel time.");
            } else {
                verdict.append(".");
            }
        } else if (p2 < p1) {
            verdict.append("\uD83D\uDCB0 Budget Choice: ").append(f2.getFlightNumber()).append(" saves $")
                    .append(String.format("%.2f", diff));
            if (m1 < m2) {
                verdict.append(", but ").append(f1.getFlightNumber()).append(" saves travel time.");
            } else {
                verdict.append(".");
            }
        } else {
            verdict.append("\u2696\uFE0F Equal Fare: Compare based on convenience (").append(durDiff).append(").");
        }

        return new FlightComparison(route, f1.getFlightNumber(), f1.getAirline(), f1.getPrice(),
                f2.getFlightNumber(), f2.getAirline(), f2.getPrice(),
                diff, cheaper, durDiff, verdict.toString());
    }

    public static int parseDurationToMinutes(String duration) {
        if (duration == null || duration.trim().isEmpty()) return 0;
        try {
            int total = 0;
            String lower = duration.toLowerCase().trim();
            if (lower.contains("h")) {
                String[] parts = lower.split("h");
                total += Integer.parseInt(parts[0].replaceAll("[^0-9]", "")) * 60;
                if (parts.length > 1 && parts[1].contains("m")) {
                    total += Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
                }
            } else if (lower.contains("m")) {
                total += Integer.parseInt(lower.replaceAll("[^0-9]", ""));
            }
            return total;
        } catch (Exception e) {
            return 0;
        }
    }

    public Flight findCheapest(List<Flight> flights) {
        return flights.stream().min(Comparator.comparingDouble(Flight::getPrice)).orElse(null);
    }

    public Flight findFastest(List<Flight> flights) {
        return flights.stream()
                .min(Comparator.comparingInt(f -> parseDurationToMinutes(f.getDuration()) == 0 ? 9999 : parseDurationToMinutes(f.getDuration())))
                .orElse(null);
    }
}
