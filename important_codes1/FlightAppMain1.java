package com.flightfare;

import com.flightfare.config.DbConfig;
import com.flightfare.dao.FlightDAO;
import com.flightfare.model.Flight;
import com.flightfare.model.FlightComparison;
import com.flightfare.service.DataImporter;
import com.flightfare.service.FlightComparisonService;
import com.flightfare.ui.FlightAppGUI;

import javax.swing.*;
import java.awt.GraphicsEnvironment;
import java.util.List;

/**
 * Main Entry Point for running the FlightFare application in Java Eclipse or CLI.
 * 
 * To run in Eclipse:
 * Right-click FlightAppMain.java -> Run As -> Java Application
 */
public class FlightAppMain {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  FLIGHTFARE PRO - FLIGHT COMPARISON & MYSQL ENGINE");
        System.out.println("==================================================================");
        System.out.println("[Main] Starting application...");

        // 1. Initialize Database schema
        System.out.println("[Main] Connecting to MySQL database at: " + DbConfig.getDbUrl());
        boolean dbOk = DbConfig.initDatabase();
        if (!dbOk) {
            System.err.println("[Main] Warning: Could not connect to MySQL. Ensure MySQL service is running.");
        }

        boolean forceRefresh = false;
        for (String a : args) {
            if ("--refresh".equalsIgnoreCase(a)) forceRefresh = true;
        }

        // 2. Import cached API data & seed MySQL tables
        try {
            DataImporter importer = new DataImporter();
            importer.initializeData(forceRefresh);
        } catch (Exception e) {
            System.err.println("[Main] Error during data seeding: " + e.getMessage());
        }

        // 3. Print MySQL Workbench instructions to console
        printWorkbenchGuide();

        // 4. Print sample console comparisons
        printSampleComparison();

        // 5. Launch GUI if graphical display is available
        boolean isHeadless = GraphicsEnvironment.isHeadless() || (args.length > 0 && "--cli".equalsIgnoreCase(args[0]));
        if (!isHeadless) {
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}

                FlightAppGUI gui = new FlightAppGUI();
                gui.setVisible(true);
                System.out.println("[Main] FlightAppGUI window launched successfully!");
            });
        } else {
            System.out.println("[Main] Running in CLI mode (headless display).");
        }
    }

    private static void printWorkbenchGuide() {
        System.out.println("\n------------------------------------------------------------------");
        System.out.println("  MYSQL WORKBENCH ACCESS GUIDE");
        System.out.println("------------------------------------------------------------------");
        System.out.println("To view and query this data in MySQL Workbench:");
        System.out.println("1. Open MySQL Workbench on your desktop.");
        System.out.println("2. Open your MySQL connection (root / localhost:3306).");
        System.out.println("3. Run the following queries:");
        System.out.println("     USE flight_db;");
        System.out.println("     SELECT * FROM flights;");
        System.out.println("     SELECT * FROM flight_comparisons;");
        System.out.println("------------------------------------------------------------------\n");
    }

    private static void printSampleComparison() {
        FlightDAO dao = new FlightDAO();
        List<Flight> flights = dao.getAllFlights();
        System.out.println("[Main] Current Flights in MySQL: " + flights.size());

        if (flights.size() >= 2) {
            Flight f1 = flights.get(0);
            Flight f2 = flights.get(1);
            FlightComparisonService cs = new FlightComparisonService();
            FlightComparison comp = cs.compare(f1, f2);
            System.out.println("\n[Main] Sample Comparison Computed & Synced to MySQL:");
            System.out.println("  Route: " + comp.getRoute());
            System.out.println("  Flight 1: " + f1.getFlightNumber() + " (" + f1.getAirline() + ") - $" + f1.getPrice());
            System.out.println("  Flight 2: " + f2.getFlightNumber() + " (" + f2.getAirline() + ") - $" + f2.getPrice());
            System.out.println("  Cheaper: " + comp.getCheaperFlight());
            System.out.println("  Duration: " + comp.getDurationDifference());
            System.out.println("  Verdict: " + comp.getVerdict());
            System.out.println("------------------------------------------------------------------\n");
        }
    }
}
