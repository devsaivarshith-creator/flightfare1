package com.flightfare.dao;

import com.flightfare.config.DbConfig;
import com.flightfare.model.Flight;
import com.flightfare.model.FlightComparison;
import com.flightfare.model.PriceHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for flights, flight comparisons, and price history in MySQL.
 */
public class FlightDAO {

    public void clearAllData() {
        try (Connection conn = DbConfig.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM price_history");
            stmt.executeUpdate("DELETE FROM flight_comparisons");
            stmt.executeUpdate("DELETE FROM flights");
            stmt.executeUpdate("ALTER TABLE flights AUTO_INCREMENT = 1");
            stmt.executeUpdate("ALTER TABLE flight_comparisons AUTO_INCREMENT = 1");
            stmt.executeUpdate("ALTER TABLE price_history AUTO_INCREMENT = 1");
            System.out.println("[FlightDAO] Reset database tables 'flights', 'flight_comparisons' and 'price_history'.");
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error clearing data: " + e.getMessage());
        }
    }

    public boolean saveFlight(Flight f) {
        String sql = "INSERT INTO flights (flight_number, airline, origin_code, origin_airport, " +
                "destination_code, destination_airport, departure_time, arrival_time, duration, " +
                "stops, cabin_class, price, currency, data_source, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.getFlightNumber());
            ps.setString(2, f.getAirline());
            ps.setString(3, f.getOriginCode());
            ps.setString(4, f.getOriginAirport());
            ps.setString(5, f.getDestinationCode());
            ps.setString(6, f.getDestinationAirport());
            ps.setString(7, f.getDepartureTime());
            ps.setString(8, f.getArrivalTime());
            ps.setString(9, f.getDuration());
            ps.setInt(10, f.getStops());
            ps.setString(11, f.getCabinClass());
            ps.setDouble(12, f.getPrice());
            ps.setString(13, f.getCurrency());
            ps.setString(14, f.getDataSource());
            ps.setString(15, f.getStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        f.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error inserting flight: " + e.getMessage());
        }
        return false;
    }

    public int saveFlightsBatch(List<Flight> list) {
        String sql = "INSERT INTO flights (flight_number, airline, origin_code, origin_airport, " +
                "destination_code, destination_airport, departure_time, arrival_time, duration, " +
                "stops, cabin_class, price, currency, data_source, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int count = 0;
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (Flight f : list) {
                ps.setString(1, f.getFlightNumber());
                ps.setString(2, f.getAirline());
                ps.setString(3, f.getOriginCode());
                ps.setString(4, f.getOriginAirport());
                ps.setString(5, f.getDestinationCode());
                ps.setString(6, f.getDestinationAirport());
                ps.setString(7, f.getDepartureTime());
                ps.setString(8, f.getArrivalTime());
                ps.setString(9, f.getDuration());
                ps.setInt(10, f.getStops());
                ps.setString(11, f.getCabinClass());
                ps.setDouble(12, f.getPrice());
                ps.setString(13, f.getCurrency());
                ps.setString(14, f.getDataSource());
                ps.setString(15, f.getStatus());
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            conn.commit();
            count = results.length;
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error in batch insert: " + e.getMessage());
        }
        return count;
    }

    public List<Flight> getAllFlights() {
        List<Flight> list = new ArrayList<>();
        String sql = "SELECT * FROM flights ORDER BY price ASC, id ASC";
        try (Connection conn = DbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToFlight(rs));
            }
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error getting all flights: " + e.getMessage());
        }
        return list;
    }

    public Flight getFlightById(int id) {
        String sql = "SELECT * FROM flights WHERE id = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFlight(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error getting flight by id: " + e.getMessage());
        }
        return null;
    }

    public List<Flight> searchFlights(String origin, String dest, String airline, Double maxPrice, String cabin) {
        List<Flight> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM flights WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (origin != null && !origin.trim().isEmpty() && !origin.equalsIgnoreCase("ALL")) {
            sql.append("AND (origin_code = ? OR origin_airport LIKE ?) ");
            params.add(origin.trim());
            params.add("%" + origin.trim() + "%");
        }
        if (dest != null && !dest.trim().isEmpty() && !dest.equalsIgnoreCase("ALL")) {
            sql.append("AND (destination_code = ? OR destination_airport LIKE ?) ");
            params.add(dest.trim());
            params.add("%" + dest.trim() + "%");
        }
        if (airline != null && !airline.trim().isEmpty() && !airline.equalsIgnoreCase("ALL")) {
            sql.append("AND airline LIKE ? ");
            params.add("%" + airline.trim() + "%");
        }
        if (maxPrice != null && maxPrice > 0) {
            sql.append("AND price <= ? ");
            params.add(maxPrice);
        }
        if (cabin != null && !cabin.trim().isEmpty() && !cabin.equalsIgnoreCase("ALL")) {
            sql.append("AND cabin_class = ? ");
            params.add(cabin.trim());
        }

        sql.append("ORDER BY price ASC");

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToFlight(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error searching flights: " + e.getMessage());
        }
        return list;
    }

    public boolean deleteFlight(int id) {
        String sql = "DELETE FROM flights WHERE id = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error deleting flight: " + e.getMessage());
        }
        return false;
    }

    public int getFlightCount() {
        String sql = "SELECT COUNT(*) FROM flights";
        try (Connection conn = DbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error getting count: " + e.getMessage());
        }
        return 0;
    }

    public List<String> getDistinctOrigins() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT origin_code FROM flights ORDER BY origin_code ASC";
        try (Connection conn = DbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error getting origins: " + e.getMessage());
        }
        return list;
    }

    public List<String> getDistinctDestinations() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT destination_code FROM flights ORDER BY destination_code ASC";
        try (Connection conn = DbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error getting destinations: " + e.getMessage());
        }
        return list;
    }

    public List<String> getDistinctAirlines() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT airline FROM flights ORDER BY airline ASC";
        try (Connection conn = DbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error getting airlines: " + e.getMessage());
        }
        return list;
    }

    public boolean saveComparison(FlightComparison fc) {
        String sql = "INSERT INTO flight_comparisons (route, flight1_number, flight1_airline, flight1_price, " +
                "flight2_number, flight2_airline, flight2_price, price_difference, cheaper_flight, duration_difference, verdict) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fc.getRoute());
            ps.setString(2, fc.getFlight1Number());
            ps.setString(3, fc.getFlight1Airline());
            ps.setDouble(4, fc.getFlight1Price());
            ps.setString(5, fc.getFlight2Number());
            ps.setString(6, fc.getFlight2Airline());
            ps.setDouble(7, fc.getFlight2Price());
            ps.setDouble(8, fc.getPriceDifference());
            ps.setString(9, fc.getCheaperFlight());
            ps.setString(10, fc.getDurationDifference());
            ps.setString(11, fc.getVerdict());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        fc.setComparisonId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error saving comparison: " + e.getMessage());
        }
        return false;
    }

    public List<FlightComparison> getAllComparisons() {
        List<FlightComparison> list = new ArrayList<>();
        String sql = "SELECT * FROM flight_comparisons ORDER BY comparison_id DESC";
        try (Connection conn = DbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                FlightComparison fc = new FlightComparison();
                fc.setComparisonId(rs.getInt("comparison_id"));
                fc.setRoute(rs.getString("route"));
                fc.setFlight1Number(rs.getString("flight1_number"));
                fc.setFlight1Airline(rs.getString("flight1_airline"));
                fc.setFlight1Price(rs.getDouble("flight1_price"));
                fc.setFlight2Number(rs.getString("flight2_number"));
                fc.setFlight2Airline(rs.getString("flight2_airline"));
                fc.setFlight2Price(rs.getDouble("flight2_price"));
                fc.setPriceDifference(rs.getDouble("price_difference"));
                fc.setCheaperFlight(rs.getString("cheaper_flight"));
                fc.setDurationDifference(rs.getString("duration_difference"));
                fc.setVerdict(rs.getString("verdict"));
                fc.setComparedAt(rs.getTimestamp("compared_at"));
                list.add(fc);
            }
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error getting comparisons: " + e.getMessage());
        }
        return list;
    }

    private Flight mapResultSetToFlight(ResultSet rs) throws SQLException {
        Flight f = new Flight();
        f.setId(rs.getInt("id"));
        f.setFlightNumber(rs.getString("flight_number"));
        f.setAirline(rs.getString("airline"));
        f.setOriginCode(rs.getString("origin_code"));
        f.setOriginAirport(rs.getString("origin_airport"));
        f.setDestinationCode(rs.getString("destination_code"));
        f.setDestinationAirport(rs.getString("destination_airport"));
        f.setDepartureTime(rs.getString("departure_time"));
        f.setArrivalTime(rs.getString("arrival_time"));
        f.setDuration(rs.getString("duration"));
        f.setStops(rs.getInt("stops"));
        f.setCabinClass(rs.getString("cabin_class"));
        f.setPrice(rs.getDouble("price"));
        f.setCurrency(rs.getString("currency"));
        f.setDataSource(rs.getString("data_source"));
        f.setStatus(rs.getString("status"));
        f.setCreatedAt(rs.getTimestamp("created_at"));
        return f;
    }

    // ==========================================
    // PRICE HISTORY & COST TRACKING METHODS
    // ==========================================

    public boolean recordPriceChange(PriceHistory ph) {
        String sql = "INSERT INTO price_history (token_batch, flight_number, airline, route, old_price, new_price, " +
                "price_change, percent_change, change_type, source) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ph.getTokenBatch() > 0 ? ph.getTokenBatch() : 1);
            ps.setString(2, ph.getFlightNumber());
            ps.setString(3, ph.getAirline());
            ps.setString(4, ph.getRoute());
            ps.setDouble(5, ph.getOldPrice());
            ps.setDouble(6, ph.getNewPrice());
            ps.setDouble(7, ph.getPriceChange());
            ps.setDouble(8, ph.getPercentChange());
            ps.setString(9, ph.getChangeType());
            ps.setString(10, ph.getSource());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) ph.setHistoryId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error recording price change: " + e.getMessage());
        }
        return false;
    }

    public List<PriceHistory> getAllPriceHistory() {
        List<PriceHistory> list = new ArrayList<>();
        String sql = "SELECT * FROM price_history ORDER BY history_id DESC";
        try (Connection conn = DbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToPriceHistory(rs));
            }
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error getting price history: " + e.getMessage());
        }
        return list;
    }

    public List<PriceHistory> getPriceHistoryByTokenBatch(int batch) {
        List<PriceHistory> list = new ArrayList<>();
        String sql = "SELECT * FROM price_history WHERE token_batch = ? ORDER BY history_id DESC";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, batch);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPriceHistory(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error getting token batch price history: " + e.getMessage());
        }
        return list;
    }

    public List<Integer> getDistinctTokenBatches() {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT DISTINCT token_batch FROM price_history ORDER BY token_batch DESC";
        try (Connection conn = DbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error getting distinct token batches: " + e.getMessage());
        }
        return list;
    }

    public List<PriceHistory> getPriceHistoryByRoute(String route) {
        List<PriceHistory> list = new ArrayList<>();
        String sql = "SELECT * FROM price_history WHERE route = ? ORDER BY history_id DESC";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, route);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPriceHistory(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error getting route price history: " + e.getMessage());
        }
        return list;
    }

    public boolean updateFlightPrice(int flightId, double newPrice, String reason) {
        return updateFlightPrice(1, flightId, newPrice, reason);
    }

    public boolean updateFlightPrice(int tokenBatch, int flightId, double newPrice, String reason) {
        Flight current = getFlightById(flightId);
        if (current == null) return false;

        double oldPrice = current.getPrice();
        String sql = "UPDATE flights SET price = ? WHERE id = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newPrice);
            ps.setInt(2, flightId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                String type = newPrice < oldPrice ? "DROPPED" : (newPrice > oldPrice ? "INCREASED" : "UNCHANGED");
                PriceHistory ph = new PriceHistory(
                        tokenBatch,
                        current.getFlightNumber(),
                        current.getAirline(),
                        current.getRoute(),
                        oldPrice,
                        newPrice,
                        type,
                        reason != null ? reason : "Live Fare Fluctuation"
                );
                recordPriceChange(ph);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[FlightDAO] Error updating flight price: " + e.getMessage());
        }
        return false;
    }

    private PriceHistory mapResultSetToPriceHistory(ResultSet rs) throws SQLException {
        PriceHistory ph = new PriceHistory();
        ph.setHistoryId(rs.getInt("history_id"));
        try {
            ph.setTokenBatch(rs.getInt("token_batch"));
        } catch (SQLException ignored) {
            ph.setTokenBatch(1);
        }
        ph.setFlightNumber(rs.getString("flight_number"));
        ph.setAirline(rs.getString("airline"));
        ph.setRoute(rs.getString("route"));
        ph.setOldPrice(rs.getDouble("old_price"));
        ph.setNewPrice(rs.getDouble("new_price"));
        ph.setPriceChange(rs.getDouble("price_change"));
        ph.setPercentChange(rs.getDouble("percent_change"));
        ph.setChangeType(rs.getString("change_type"));
        ph.setRecordedAt(rs.getTimestamp("recorded_at"));
        ph.setSource(rs.getString("source"));
        return ph;
    }

    public void seedInitialPriceHistoryIfEmpty() {
        String countSql = "SELECT COUNT(*) FROM price_history";
        try (Connection conn = DbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Already has history
            }
        } catch (SQLException ignored) {}

        List<Flight> flights = getAllFlights();
        if (flights.isEmpty()) return;

        // Seed initial static baseline in Token Batch #1
        for (int i = 0; i < Math.min(flights.size(), 12); i++) {
            Flight f = flights.get(i);
            double currentPrice = f.getPrice();
            double diff = (i % 2 == 0) ? -(15.0 + (i * 3.5)) : +(12.0 + (i * 2.0));
            double oldPrice = Math.max(45.0, currentPrice - diff);
            String type = diff < 0 ? "DROPPED" : "INCREASED";

            PriceHistory ph = new PriceHistory(
                    1,
                    f.getFlightNumber(),
                    f.getAirline(),
                    f.getRoute(),
                    oldPrice,
                    currentPrice,
                    type,
                    "Token #1 Initial Cache Sync"
            );
            recordPriceChange(ph);
        }
        System.out.println("[FlightDAO] Seeded initial flight cost changes into 'price_history' table (Token Batch #1).");
    }
}

