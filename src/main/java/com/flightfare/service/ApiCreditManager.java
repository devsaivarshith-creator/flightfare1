package com.flightfare.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Manages and strictly enforces the 30-credit quota limit for external Flight APIs.
 * Persists credit consumption to disk to guarantee usage never exceeds the 30 credit ceiling.
 */
public class ApiCreditManager {

    public static final int MAX_CREDITS = 30;
    private static final String CREDITS_FILE = "api_credits.properties";

    private static final ApiCreditManager INSTANCE = new ApiCreditManager();

    private int usedCredits = 1; // Default: 1 used during initial setup
    private final List<CreditUsageListener> listeners = new ArrayList<>();

    public interface CreditUsageListener {
        void onCreditUpdated(int used, int remaining);
    }

    private ApiCreditManager() {
        loadCredits();
    }

    public static ApiCreditManager getInstance() {
        return INSTANCE;
    }

    public synchronized void addListener(CreditUsageListener listener) {
        listeners.add(listener);
        listener.onCreditUpdated(usedCredits, getRemainingCredits());
    }

    public synchronized int getMaxCredits() {
        return MAX_CREDITS;
    }

    public synchronized int getUsedCredits() {
        return usedCredits;
    }

    public synchronized int getRemainingCredits() {
        return Math.max(0, MAX_CREDITS - usedCredits);
    }

    public synchronized boolean canMakeApiCall() {
        return usedCredits < MAX_CREDITS;
    }

    /**
     * Attempts to consume 1 API credit.
     * @return true if credit was successfully consumed; false if credit limit reached.
     */
    public synchronized boolean consumeCredit(String apiName, String details) {
        if (!canMakeApiCall()) {
            System.err.println("[ApiCreditManager] BLOCKED: Cannot consume credit. Limit reached (" + usedCredits + "/" + MAX_CREDITS + ")");
            return false;
        }

        usedCredits++;
        saveCredits(apiName, details);
        notifyListeners();
        System.out.println("[ApiCreditManager] 1 Credit consumed for: " + apiName + " | Remaining: " + getRemainingCredits() + "/" + MAX_CREDITS);
        return true;
    }

    private void notifyListeners() {
        int remaining = getRemainingCredits();
        for (CreditUsageListener l : listeners) {
            try {
                l.onCreditUpdated(usedCredits, remaining);
            } catch (Exception ignored) {}
        }
    }

    private synchronized void loadCredits() {
        File file = new File(CREDITS_FILE);
        if (file.exists()) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
                String usedStr = props.getProperty("api.credits.used", "1");
                usedCredits = Integer.parseInt(usedStr.trim());
            } catch (Exception e) {
                System.err.println("[ApiCreditManager] Could not load credits file: " + e.getMessage());
                usedCredits = 1;
            }
        } else {
            // First run: 1 initial fetch used
            usedCredits = 1;
            saveCredits("Initial Ingestion", "First-time setup cache fetch");
        }
    }

    private synchronized void saveCredits(String apiName, String details) {
        Properties props = new Properties();
        props.setProperty("api.credits.max", String.valueOf(MAX_CREDITS));
        props.setProperty("api.credits.used", String.valueOf(usedCredits));
        props.setProperty("api.credits.remaining", String.valueOf(getRemainingCredits()));
        props.setProperty("api.last_request.api", apiName != null ? apiName : "Unknown");
        props.setProperty("api.last_request.details", details != null ? details : "N/A");
        props.setProperty("api.last_request.time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        try (FileOutputStream fos = new FileOutputStream(CREDITS_FILE)) {
            props.store(fos, "FlightAPI & AviationStack Credit Quota Tracker (Strict 30-Credit Ceiling)");
        } catch (IOException e) {
            System.err.println("[ApiCreditManager] Failed to persist credits: " + e.getMessage());
        }
    }
}
