package com.flightfare.ui;

import com.flightfare.config.DbConfig;
import com.flightfare.dao.FlightDAO;
import com.flightfare.model.Flight;
import com.flightfare.model.FlightComparison;
import com.flightfare.model.PriceHistory;
import com.flightfare.service.ApiCreditManager;
import com.flightfare.service.FlightComparisonService;
import com.flightfare.service.LiveFlightApiService;
import com.flightfare.ui.ModernUI.ButtonStyle;
import com.flightfare.ui.ModernUI.ModernButton;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

/**
 * Modern Swing GUI for Flight Fare ticket browsing, real-time comparisons,
 * dynamic cost & price fluctuation tracking, and strict 30-credit Live API integration.
 * When a new token is used, the entire dataset refreshes from the API platform
 * and all detected changes are listed in the Cost Tracker section.
 */
public class FlightAppGUI extends JFrame {

    private final FlightDAO flightDAO = new FlightDAO();
    private final FlightComparisonService comparisonService = new FlightComparisonService();
    private final ApiCreditManager creditManager = ApiCreditManager.getInstance();
    private final LiveFlightApiService liveApiService = new LiveFlightApiService();

    // Main Tabbed Pane
    private JTabbedPane tabbedPane;

    // Top Header Badges & Actions
    private JLabel lblTotalFlightsBadge;
    private JLabel lblApiCreditsBadge;
    private ModernButton btnHeaderLiveFetch;

    // Components - Tab 1 (Browse)
    private JTable flightTable;
    private DefaultTableModel flightTableModel;
    private JComboBox<String> comboOrigin;
    private JComboBox<String> comboDest;
    private JComboBox<String> comboAirline;
    private JComboBox<String> comboCabin;
    private JTextField txtMaxPrice;
    private ModernButton btnLiveApiFetch;

    // Components - Tab 2 (Comparison)
    private JComboBox<FlightItem> comboFlight1;
    private JComboBox<FlightItem> comboFlight2;
    private JPanel panelCard1;
    private JPanel panelCard2;
    private JLabel lblVerdict;
    private JLabel lblPriceDiff;
    private JLabel lblDurationDiff;
    private FlightComparison lastCalculatedComparison;
    private ModernButton btnSaveComparison;

    // Components - Tab 3 (Cost Tracker - NEW SECTION)
    private JTable costTable;
    private DefaultTableModel costTableModel;
    private JComboBox<String> comboTokenBatchFilter;
    private JComboBox<String> comboCostRouteFilter;
    private JComboBox<String> comboCostTrendFilter;
    private JLabel lblTokenBatchBanner;
    private ModernButton btnCostTabUseToken;

    private JLabel lblDropValue;
    private JLabel lblChangesValue;
    private JLabel lblQuotaValue;
    private JLabel lblAdviceValue;

    // Components - Tab 4 (MySQL Workbench Viewer)
    private JTable dbTable;
    private DefaultTableModel dbTableModel;
    private JComboBox<String> comboDbQuery;
    private JLabel lblDbStatus;

    // Components - Tab 5 (API Quota & Security)
    private JLabel lblApiUsageText;
    private JProgressBar progressApiCredits;

    public FlightAppGUI() {
        setTitle("✈️ FlightFare Pro — Ticket Search, Comparison & Cost Tracker Engine");
        setSize(1260, 820);
        setMinimumSize(new Dimension(1040, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
        registerCreditListener();
        refreshAllData();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBorder(new EmptyBorder(12, 16, 12, 16));
        mainPanel.setBackground(ModernUI.COLOR_CANVAS);

        // Header
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Tabs with vibrant styling
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("✈️ Search & Browse Tickets", createBrowsePanel());
        tabbedPane.addTab("⚖️ Ticket Comparison Studio", createComparisonPanel());
        tabbedPane.addTab("📈 Cost Tracker & Price Trends", createCostTrackerPanel());
        tabbedPane.addTab("🗄️ MySQL Workbench Live Viewer", createDatabasePanel());
        tabbedPane.addTab("🔑 API Quota & Security (30-Credit Ceiling)", createApiPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private void registerCreditListener() {
        creditManager.addListener((used, remaining) -> SwingUtilities.invokeLater(() -> {
            updateCreditDisplay(used, remaining);
        }));
    }

    private void updateCreditDisplay(int used, int remaining) {
        String badgeText = String.format("⚡ Credits: %d/%d Used (%d Left)", used, ApiCreditManager.MAX_CREDITS, remaining);
        Color badgeBg = remaining > 10 ? ModernUI.COLOR_CYAN : (remaining > 0 ? ModernUI.COLOR_WARNING : ModernUI.COLOR_DANGER);

        if (btnHeaderLiveFetch != null) {
            btnHeaderLiveFetch.setText("Refresh Live Data");
            btnHeaderLiveFetch.setEnabled(remaining > 0);
        }
        if (btnLiveApiFetch != null) {
            btnLiveApiFetch.setText("Refresh Live Data");
            btnLiveApiFetch.setEnabled(remaining > 0);
        }
        if (btnCostTabUseToken != null) {
            btnCostTabUseToken.setText("Refresh Live Data");
            btnCostTabUseToken.setEnabled(remaining > 0);
        }
        if (progressApiCredits != null) {
            progressApiCredits.setMaximum(ApiCreditManager.MAX_CREDITS);
            progressApiCredits.setValue(used);
            progressApiCredits.setString(String.format("%d / %d Credits Used (%d Left)", used, ApiCreditManager.MAX_CREDITS, remaining));
        }
        if (lblApiUsageText != null) {
            lblApiUsageText.setText(String.format("• Active Quota: %d / %d credits used (%d remaining). Strict cutoff blocks further calls at %d.",
                    used, ApiCreditManager.MAX_CREDITS, remaining, ApiCreditManager.MAX_CREDITS));
        }
    }

    // ==========================================
    // 1. HEADER PANEL
    // ==========================================
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Title and Subtitle with lively colors
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 3));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("✈️ FlightFare Pro — Ticket Search, Comparison & Cost Tracker");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(ModernUI.COLOR_PRIMARY);

        JLabel subtitle = new JLabel("Synchronized with MySQL (localhost:3306/flight_db) • 30-Credit Protected Live API Engine");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(ModernUI.COLOR_TEXT_MUTED);

        titlePanel.add(title);
        titlePanel.add(subtitle);
        header.add(titlePanel, BorderLayout.WEST);

        // Status Badges & Quick Live Fetch Button
        JPanel actionsAndBadges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actionsAndBadges.setOpaque(false);

        lblTotalFlightsBadge = ModernUI.createBadge("Tickets: 0", ModernUI.COLOR_PRIMARY, Color.WHITE);
        JLabel dbBadge = ModernUI.createBadge("MySQL: Connected", ModernUI.COLOR_SUCCESS, Color.WHITE);

        btnHeaderLiveFetch = new ModernButton("Refresh Live Data", ButtonStyle.CYAN);
        btnHeaderLiveFetch.addActionListener(e -> showLiveFetchDialog());

        actionsAndBadges.add(lblTotalFlightsBadge);
        actionsAndBadges.add(dbBadge);
        actionsAndBadges.add(btnHeaderLiveFetch);

        header.add(actionsAndBadges, BorderLayout.EAST);
        return header;
    }

    // ==========================================
    // 2. TAB 1: BROWSE & SEARCH
    // ==========================================
    private JPanel createBrowsePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 6, 6, 6));

        // Filters Toolbar with vibrant, high-contrast buttons
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filterBar.setBackground(ModernUI.COLOR_CARD_BG);
        filterBar.setBorder(new LineBorder(ModernUI.COLOR_BORDER, 1, true));

        comboOrigin = new JComboBox<>(new String[]{"ALL"});
        comboDest = new JComboBox<>(new String[]{"ALL"});
        comboAirline = new JComboBox<>(new String[]{"ALL"});
        comboCabin = new JComboBox<>(new String[]{"ALL", "Economy", "Business", "First"});
        txtMaxPrice = new JTextField(5);

        ModernButton btnFilter = new ModernButton("🔍 Filter", ButtonStyle.PRIMARY);
        ModernButton btnReset = new ModernButton("↺ Reset", ButtonStyle.OUTLINE);
        ModernButton btnRefresh = new ModernButton("🔄 Reload MySQL", ButtonStyle.DARK);
        ModernButton btnAddTicket = new ModernButton("➕ Add Ticket", ButtonStyle.SUCCESS);
        btnLiveApiFetch = new ModernButton("Refresh Live Data", ButtonStyle.CYAN);

        btnFilter.addActionListener(e -> applyFilter());
        btnReset.addActionListener(e -> {
            comboOrigin.setSelectedIndex(0);
            comboDest.setSelectedIndex(0);
            comboAirline.setSelectedIndex(0);
            comboCabin.setSelectedIndex(0);
            txtMaxPrice.setText("");
            loadAllFlightsToTable();
        });
        btnRefresh.addActionListener(e -> refreshAllData());
        btnAddTicket.addActionListener(e -> showAddTicketDialog());
        btnLiveApiFetch.addActionListener(e -> showLiveFetchDialog());

        filterBar.add(new JLabel("Origin:"));
        filterBar.add(comboOrigin);
        filterBar.add(new JLabel("Dest:"));
        filterBar.add(comboDest);
        filterBar.add(new JLabel("Airline:"));
        filterBar.add(comboAirline);
        filterBar.add(new JLabel("Cabin:"));
        filterBar.add(comboCabin);
        filterBar.add(new JLabel("Max ($):"));
        filterBar.add(txtMaxPrice);
        filterBar.add(btnFilter);
        filterBar.add(btnReset);
        filterBar.add(btnRefresh);
        filterBar.add(btnAddTicket);
        filterBar.add(btnLiveApiFetch);

        panel.add(filterBar, BorderLayout.NORTH);

        // Table with lively header and modern cell renderers (including authentic Airport Information)
        String[] cols = {"ID", "Flight #", "Airline", "Route", "Origin Airport", "Destination Airport", "Departure", "Arrival", "Duration", "Stops", "Cabin", "Price ($)", "Source", "Status"};
        flightTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        flightTable = new JTable(flightTableModel);
        flightTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        flightTable.setRowHeight(28);
        flightTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        flightTable.setShowGrid(true);
        flightTable.setGridColor(new Color(241, 245, 249));

        flightTable.getTableHeader().setDefaultRenderer(new ModernUI.ModernHeaderRenderer(ModernUI.COLOR_PRIMARY));
        flightTable.getTableHeader().setPreferredSize(new Dimension(0, 32));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        flightTable.getColumnModel().getColumn(0).setPreferredWidth(35);  // ID
        flightTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Flight #
        flightTable.getColumnModel().getColumn(2).setPreferredWidth(130); // Airline
        flightTable.getColumnModel().getColumn(3).setPreferredWidth(85);  // Route (e.g. JFK -> LAX)
        flightTable.getColumnModel().getColumn(4).setPreferredWidth(160); // Origin Airport
        flightTable.getColumnModel().getColumn(5).setPreferredWidth(160); // Destination Airport
        flightTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Departure
        flightTable.getColumnModel().getColumn(7).setPreferredWidth(120); // Arrival
        flightTable.getColumnModel().getColumn(8).setPreferredWidth(65);  // Duration
        flightTable.getColumnModel().getColumn(9).setPreferredWidth(95);  // Stops
        flightTable.getColumnModel().getColumn(9).setCellRenderer(centerRenderer);
        flightTable.getColumnModel().getColumn(11).setPreferredWidth(85); // Price ($)
        flightTable.getColumnModel().getColumn(11).setCellRenderer(new ModernUI.PriceCellRenderer());

        JScrollPane scrollPane = new JScrollPane(flightTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(5, 5, 0, 5));

        JLabel lblTip = new JLabel("💡 Tip: Select any ticket to send directly into the Comparison Studio or switch to Cost Tracker to view recent fare changes.");
        lblTip.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblTip.setForeground(ModernUI.COLOR_TEXT_MUTED);

        ModernButton btnCompareSelection = new ModernButton("⚖️ Send to Comparison Studio", ButtonStyle.PRIMARY);
        btnCompareSelection.addActionListener(e -> {
            int selectedRow = flightTable.getSelectedRow();
            if (selectedRow >= 0) {
                int id = (int) flightTableModel.getValueAt(selectedRow, 0);
                selectFlightForComparison(id, true);
                JOptionPane.showMessageDialog(this,
                        "Flight #" + flightTableModel.getValueAt(selectedRow, 1) + " selected as Flight 1 in Comparison Studio!",
                        "Selected for Comparison", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a flight row in the table first.", "Notice", JOptionPane.WARNING_MESSAGE);
            }
        });

        bottomBar.add(lblTip, BorderLayout.WEST);
        bottomBar.add(btnCompareSelection, BorderLayout.EAST);
        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================
    // 3. TAB 2: COMPARISON STUDIO
    // ==========================================
    private JPanel createComparisonPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 10, 10, 10));

        // Selector Bar
        JPanel topSelect = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        topSelect.setBackground(ModernUI.COLOR_CARD_BG);
        topSelect.setBorder(new LineBorder(ModernUI.COLOR_BORDER, 1, true));

        comboFlight1 = new JComboBox<>();
        comboFlight2 = new JComboBox<>();
        comboFlight1.setPreferredSize(new Dimension(320, 28));
        comboFlight2.setPreferredSize(new Dimension(320, 28));

        ModernButton btnRunCompare = new ModernButton("⚖️ Compare Now", ButtonStyle.PRIMARY);
        btnSaveComparison = new ModernButton("💾 Save Comparison to MySQL", ButtonStyle.SUCCESS);
        btnSaveComparison.setEnabled(false);

        btnRunCompare.addActionListener(e -> runComparison());
        btnSaveComparison.addActionListener(e -> saveCurrentComparison());

        topSelect.add(new JLabel("Flight 1:"));
        topSelect.add(comboFlight1);
        topSelect.add(new JLabel("vs   Flight 2:"));
        topSelect.add(comboFlight2);
        topSelect.add(btnRunCompare);
        topSelect.add(btnSaveComparison);

        panel.add(topSelect, BorderLayout.NORTH);

        // Center: Side-by-side cards
        JPanel cardsWrapper = new JPanel(new GridLayout(1, 2, 20, 0));
        cardsWrapper.setOpaque(false);

        panelCard1 = createFlightCard("Select Flight 1");
        panelCard2 = createFlightCard("Select Flight 2");
        cardsWrapper.add(panelCard1);
        cardsWrapper.add(panelCard2);

        // Analytics & Verdict Banner
        JPanel resultsPanel = new JPanel(new BorderLayout(0, 8));
        resultsPanel.setOpaque(false);

        JPanel metricsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        metricsPanel.setOpaque(false);

        lblPriceDiff = createMetricDisplay("Price Analysis", "Select two flights and click 'Compare Now'");
        lblDurationDiff = createMetricDisplay("Duration & Convenience", "-");
        metricsPanel.add(lblPriceDiff);
        metricsPanel.add(lblDurationDiff);

        lblVerdict = new JLabel("💡 Ready to compare. Choose two tickets above to see AI price delta and recommendations.", SwingConstants.CENTER);
        lblVerdict.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblVerdict.setOpaque(true);
        lblVerdict.setBackground(new Color(239, 246, 255));
        lblVerdict.setForeground(ModernUI.COLOR_PRIMARY);
        lblVerdict.setBorder(new EmptyBorder(12, 12, 12, 12));

        resultsPanel.add(metricsPanel, BorderLayout.NORTH);
        resultsPanel.add(lblVerdict, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(cardsWrapper, BorderLayout.CENTER);
        centerPanel.add(resultsPanel, BorderLayout.SOUTH);

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFlightCard(String headerTitle) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(ModernUI.COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(ModernUI.COLOR_BORDER, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel(headerTitle, SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(ModernUI.COLOR_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JTextArea details = new JTextArea("No flight selected.\nSelect from the dropdown above.");
        details.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        details.setEditable(false);
        details.setOpaque(false);
        details.setLineWrap(true);
        card.add(details, BorderLayout.CENTER);

        return card;
    }

    private JLabel createMetricDisplay(String title, String val) {
        JLabel lbl = new JLabel("<html><b>" + title + ":</b><br>" + val + "</html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setOpaque(true);
        lbl.setBackground(ModernUI.COLOR_CARD_BG);
        lbl.setBorder(new CompoundBorder(
                new LineBorder(ModernUI.COLOR_BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        return lbl;
    }

    // ==========================================
    // 4. TAB 3: COST TRACKER & PRICE TRENDS (NEW SECTION)
    // ==========================================
    private JPanel createCostTrackerPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 8, 8, 8));

        // Top KPI Cards Panel
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiGrid.setOpaque(false);

        lblDropValue = new JLabel("-$42.00 (-19.2%)");
        lblChangesValue = new JLabel("0 Events");
        lblQuotaValue = new JLabel(creditManager.getUsedCredits() + " of " + ApiCreditManager.MAX_CREDITS + " Used");
        lblAdviceValue = new JLabel("Fares Trending Down 🟢");

        JPanel cardLargestDrop = ModernUI.createMetricCard("📉", "Largest Price Drop", lblDropValue.getText(), "Top fare savings recorded", ModernUI.COLOR_SUCCESS);
        JPanel cardTotalChanges = ModernUI.createMetricCard("📊", "Fluctuation Events", lblChangesValue.getText(), "Tracked cost updates", ModernUI.COLOR_PRIMARY);
        JPanel cardCreditQuota = ModernUI.createMetricCard("⚡", "Live Sync Engine", "Dual-Platform", "FlightAPI.io + AviationStack", ModernUI.COLOR_CYAN);
        JPanel cardFareAdvice = ModernUI.createMetricCard("💡", "Smart Fare Advice", lblAdviceValue.getText(), "Optimal booking window", ModernUI.COLOR_PURPLE);

        kpiGrid.add(cardLargestDrop);
        kpiGrid.add(cardTotalChanges);
        kpiGrid.add(cardCreditQuota);
        kpiGrid.add(cardFareAdvice);

        panel.add(kpiGrid, BorderLayout.NORTH);

        // Center Container
        JPanel centerContainer = new JPanel(new BorderLayout(0, 8));
        centerContainer.setOpaque(false);

        // Session Banner
        lblTokenBatchBanner = new JLabel("🔄 Active Session: Showing all recorded fare changes. Click 'Refresh Live Data' to synchronize real-time market fare fluctuations across both platforms.", SwingConstants.LEFT);
        lblTokenBatchBanner.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTokenBatchBanner.setOpaque(true);
        lblTokenBatchBanner.setBackground(new Color(239, 246, 255));
        lblTokenBatchBanner.setForeground(ModernUI.COLOR_PRIMARY);
        lblTokenBatchBanner.setBorder(new CompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        toolbar.setBackground(ModernUI.COLOR_CARD_BG);
        toolbar.setBorder(new LineBorder(ModernUI.COLOR_BORDER, 1, true));

        comboTokenBatchFilter = new JComboBox<>(new String[]{"ALL SESSIONS"});
        comboCostRouteFilter = new JComboBox<>(new String[]{"ALL ROUTES"});
        comboCostTrendFilter = new JComboBox<>(new String[]{"ALL TRENDS", "🟢 Price Drops Only", "🔴 Price Increases Only", "✨ New Fares Only"});

        ModernButton btnFilterCost = new ModernButton("🔍 Apply Filter", ButtonStyle.PRIMARY);
        ModernButton btnRefreshCost = new ModernButton("🔄 Refresh History", ButtonStyle.DARK);
        btnCostTabUseToken = new ModernButton("Refresh Live Data", ButtonStyle.CYAN);

        btnFilterCost.addActionListener(e -> applyCostTrackerFilter());
        btnRefreshCost.addActionListener(e -> loadCostTrackerData());
        btnCostTabUseToken.addActionListener(e -> showLiveFetchDialog());

        toolbar.add(new JLabel("Sync Session:"));
        toolbar.add(comboTokenBatchFilter);
        toolbar.add(new JLabel("Route:"));
        toolbar.add(comboCostRouteFilter);
        toolbar.add(new JLabel("Trend:"));
        toolbar.add(comboCostTrendFilter);
        toolbar.add(btnFilterCost);
        toolbar.add(btnRefreshCost);
        toolbar.add(btnCostTabUseToken);

        JPanel topControls = new JPanel(new BorderLayout(0, 6));
        topControls.setOpaque(false);
        topControls.add(lblTokenBatchBanner, BorderLayout.NORTH);
        topControls.add(toolbar, BorderLayout.SOUTH);

        centerContainer.add(topControls, BorderLayout.NORTH);

        // Cost Changes Table
        String[] costCols = {"Session #", "Recorded At", "Flight #", "Airline", "Route", "Previous ($)", "New ($)", "Cost Difference", "Trend / %", "Source / Reason"};
        costTableModel = new DefaultTableModel(costCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        costTable = new JTable(costTableModel);
        costTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        costTable.setRowHeight(26);
        costTable.getTableHeader().setDefaultRenderer(new ModernUI.ModernHeaderRenderer(ModernUI.COLOR_DARK));
        costTable.getTableHeader().setPreferredSize(new Dimension(0, 30));

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);

        costTable.getColumnModel().getColumn(0).setPreferredWidth(65);
        costTable.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        costTable.getColumnModel().getColumn(7).setCellRenderer(new ModernUI.PriceChangeCellRenderer());
        costTable.getColumnModel().getColumn(8).setCellRenderer(new ModernUI.PriceChangeCellRenderer());

        centerContainer.add(new JScrollPane(costTable), BorderLayout.CENTER);
        panel.add(centerContainer, BorderLayout.CENTER);

        return panel;
    }

    private void loadCostTrackerData() {
        List<PriceHistory> list = flightDAO.getAllPriceHistory();
        costTableModel.setRowCount(0);

        double maxDrop = 0;
        String bestDropFlight = "N/A";
        int drops = 0;
        int hikes = 0;
        int news = 0;

        for (PriceHistory ph : list) {
            String trend = ph.getChangeType();
            if ("DROPPED".equalsIgnoreCase(trend)) {
                trend = "▼ DROPPED (" + String.format("%.1f%%", ph.getPercentChange()) + ")";
                drops++;
                if (Math.abs(ph.getPriceChange()) > maxDrop) {
                    maxDrop = Math.abs(ph.getPriceChange());
                    bestDropFlight = ph.getFlightNumber() + " (-$" + String.format("%.2f", maxDrop) + ")";
                }
            } else if ("INCREASED".equalsIgnoreCase(trend)) {
                trend = "▲ INCREASED (+" + String.format("%.1f%%", ph.getPercentChange()) + ")";
                hikes++;
            } else if ("NEW_FLIGHT".equalsIgnoreCase(trend)) {
                trend = "✨ NEW DEAL FARE";
                news++;
            } else {
                trend = "● " + ph.getChangeType();
            }

            costTableModel.addRow(new Object[]{
                    "Session #" + ph.getTokenBatch(),
                    ph.getRecordedAt() != null ? ph.getRecordedAt().toString().substring(0, 16) : "-",
                    ph.getFlightNumber(),
                    ph.getAirline(),
                    ph.getRoute(),
                    String.format("%.2f", ph.getOldPrice()),
                    String.format("%.2f", ph.getNewPrice()),
                    ph.getFormattedPriceChange(),
                    trend,
                    ph.getSource()
            });
        }

        // Update KPI Cards
        lblChangesValue.setText(list.size() + " Logged Events");
        if (maxDrop > 0) {
            lblDropValue.setText("-$" + String.format("%.2f", maxDrop) + " (" + bestDropFlight + ")");
        } else {
            lblDropValue.setText("No Drops Yet");
        }

        if (drops > hikes) {
            lblAdviceValue.setText("🟢 Great Time to Book! (" + drops + " drops)");
        } else if (hikes > drops) {
            lblAdviceValue.setText("🔴 Prices Trending Up (" + hikes + " hikes)");
        } else {
            lblAdviceValue.setText("⚪ Fares Stable");
        }

        // Populate Sessions in Dropdown
        List<Integer> batches = flightDAO.getDistinctTokenBatches();
        comboTokenBatchFilter.removeAllItems();
        comboTokenBatchFilter.addItem("ALL SESSIONS");
        for (Integer b : batches) {
            if (b == 1) {
                comboTokenBatchFilter.addItem("Session #1 (Initial Baseline)");
            } else {
                comboTokenBatchFilter.addItem("Session #" + b + " (Live Market Sync)");
            }
        }

        // Refresh Route Filter items
        comboCostRouteFilter.removeAllItems();
        comboCostRouteFilter.addItem("ALL ROUTES");
        List<String> origins = flightDAO.getDistinctOrigins();
        List<String> dests = flightDAO.getDistinctDestinations();
        for (String o : origins) {
            for (String d : dests) {
                if (!o.equals(d)) {
                    comboCostRouteFilter.addItem(o + " \u2192 " + d);
                }
            }
        }
    }

    private void applyCostTrackerFilter() {
        String selectedToken = (String) comboTokenBatchFilter.getSelectedItem();
        String selectedRoute = (String) comboCostRouteFilter.getSelectedItem();
        String selectedTrend = (String) comboCostTrendFilter.getSelectedItem();

        List<PriceHistory> list = flightDAO.getAllPriceHistory();
        costTableModel.setRowCount(0);

        for (PriceHistory ph : list) {
            // Check session batch filter
            if (selectedToken != null && !selectedToken.equalsIgnoreCase("ALL SESSIONS")) {
                int targetBatch = 1;
                if (selectedToken.contains("Session #")) {
                    String sub = selectedToken.substring(selectedToken.indexOf("Session #") + 9);
                    int spaceIdx = sub.indexOf(" ");
                    if (spaceIdx > 0) sub = sub.substring(0, spaceIdx);
                    try { targetBatch = Integer.parseInt(sub.trim()); } catch (Exception ignored) {}
                }
                if (ph.getTokenBatch() != targetBatch) continue;
            }

            // Check route filter
            if (selectedRoute != null && !selectedRoute.equalsIgnoreCase("ALL ROUTES")) {
                String cleanRoute = selectedRoute.replace(" \u2192 ", " \u2192 ").replace("->", " \u2192 ");
                if (!ph.getRoute().contains(cleanRoute) && !ph.getRoute().replace(" ", "").contains(selectedRoute.replace(" ", ""))) {
                    continue;
                }
            }

            // Check trend filter
            if (selectedTrend != null && selectedTrend.contains("Drops Only")) {
                if (!"DROPPED".equalsIgnoreCase(ph.getChangeType())) continue;
            } else if (selectedTrend != null && selectedTrend.contains("Increases Only")) {
                if (!"INCREASED".equalsIgnoreCase(ph.getChangeType())) continue;
            } else if (selectedTrend != null && selectedTrend.contains("New Fares Only")) {
                if (!"NEW_FLIGHT".equalsIgnoreCase(ph.getChangeType())) continue;
            }

            String trend = ph.getChangeType();
            if ("DROPPED".equalsIgnoreCase(trend)) {
                trend = "▼ DROPPED (" + String.format("%.1f%%", ph.getPercentChange()) + ")";
            } else if ("INCREASED".equalsIgnoreCase(trend)) {
                trend = "▲ INCREASED (+" + String.format("%.1f%%", ph.getPercentChange()) + ")";
            } else if ("NEW_FLIGHT".equalsIgnoreCase(trend)) {
                trend = "✨ NEW DEAL FARE";
            }

            costTableModel.addRow(new Object[]{
                    "Token #" + ph.getTokenBatch(),
                    ph.getRecordedAt() != null ? ph.getRecordedAt().toString().substring(0, 16) : "-",
                    ph.getFlightNumber(),
                    ph.getAirline(),
                    ph.getRoute(),
                    String.format("%.2f", ph.getOldPrice()),
                    String.format("%.2f", ph.getNewPrice()),
                    ph.getFormattedPriceChange(),
                    trend,
                    ph.getSource()
            });
        }
    }

    // ==========================================
    // 5. TAB 4: MYSQL WORKBENCH LIVE VIEWER
    // ==========================================
    private JPanel createDatabasePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 10, 10, 10));

        JPanel guideBanner = new JPanel(new BorderLayout());
        guideBanner.setBackground(new Color(245, 248, 255));
        guideBanner.setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 205, 240), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel guideText = new JLabel("<html><b>💻 How to view this data in MySQL Workbench:</b><br>" +
                "1. Open <b>MySQL Workbench</b> &rarr; Connect to <b>Local instance 3306</b> (User: <code>root</code>, Pass: <code>root</code>)<br>" +
                "2. Run queries: <code>USE flight_db; SELECT * FROM flights;</code> or <code>SELECT * FROM price_history;</code> or <code>SELECT * FROM flight_comparisons;</code><br>" +
                "3. Any additions, price changes, or comparisons made here are immediately committed and queryable in Workbench!</html>");
        guideText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        guideBanner.add(guideText, BorderLayout.CENTER);
        panel.add(guideBanner, BorderLayout.NORTH);

        // Controls
        JPanel queryBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        comboDbQuery = new JComboBox<>(new String[]{
                "SELECT * FROM flights ORDER BY price ASC;",
                "SELECT * FROM price_history ORDER BY history_id DESC;",
                "SELECT * FROM flight_comparisons ORDER BY comparison_id DESC;",
                "SELECT origin_code, destination_code, COUNT(*), MIN(price), AVG(price) FROM flights GROUP BY origin_code, destination_code;"
        });
        comboDbQuery.setPreferredSize(new Dimension(550, 28));

        ModernButton btnExecQuery = new ModernButton("▶ Execute Query", ButtonStyle.PRIMARY);
        btnExecQuery.addActionListener(e -> executeWorkbenchQuery());

        lblDbStatus = new JLabel("Status: Ready");
        lblDbStatus.setFont(new Font("Segoe UI", Font.ITALIC, 11));

        queryBar.add(new JLabel("Active SQL Query:"));
        queryBar.add(comboDbQuery);
        queryBar.add(btnExecQuery);
        queryBar.add(lblDbStatus);

        // Database Table Viewer
        dbTableModel = new DefaultTableModel();
        dbTable = new JTable(dbTableModel);
        dbTable.setRowHeight(24);
        dbTable.setFont(new Font("Consolas", Font.PLAIN, 12));
        dbTable.getTableHeader().setDefaultRenderer(new ModernUI.ModernHeaderRenderer(ModernUI.COLOR_DARK));

        JScrollPane scroll = new JScrollPane(dbTable);

        JPanel centerContainer = new JPanel(new BorderLayout(0, 8));
        centerContainer.setOpaque(false);
        centerContainer.add(queryBar, BorderLayout.NORTH);
        centerContainer.add(scroll, BorderLayout.CENTER);

        panel.add(centerContainer, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // 6. TAB 5: API KEYS & 30-CREDIT QUOTA
    // ==========================================
    private JPanel createApiPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel infoCard = new JPanel(new GridLayout(7, 1, 0, 8));
        infoCard.setBackground(ModernUI.COLOR_CARD_BG);
        infoCard.setBorder(new CompoundBorder(
                new LineBorder(ModernUI.COLOR_BORDER, 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel("🔒 API Security, Keys & 30-Credit Strict Quota Guard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(ModernUI.COLOR_PRIMARY);

        JLabel key1 = new JLabel("• FlightAPI.io Key: 6acf19dda1289f0a87706cac (Endpoint: api.flightapi.io/onewaytrip)");
        JLabel key2 = new JLabel("• AviationStack Key: a9e149b8333543b1ad80113f410264be (Endpoint: api.aviationstack.com/v1/flights)");
        lblApiUsageText = new JLabel("• Active Quota: " + creditManager.getUsedCredits() + " / " + ApiCreditManager.MAX_CREDITS + " credits used (" +
                creditManager.getRemainingCredits() + " remaining).");
        JLabel policy = new JLabel("• Strict Ceiling Enforcement: Once 30 credits are reached, all external requests are locked to prevent overages.");
        JLabel safetyNote = new JLabel("• Quota Conservation: Offline caching and MySQL allow unlimited searches, filters, and comparisons at zero credit cost.");

        // Progress bar for credit visualization
        progressApiCredits = new JProgressBar(0, ApiCreditManager.MAX_CREDITS);
        progressApiCredits.setValue(creditManager.getUsedCredits());
        progressApiCredits.setStringPainted(true);
        progressApiCredits.setFont(new Font("Segoe UI", Font.BOLD, 12));
        progressApiCredits.setForeground(ModernUI.COLOR_CYAN);
        progressApiCredits.setString(creditManager.getUsedCredits() + " / " + ApiCreditManager.MAX_CREDITS + " Credits Used (" + creditManager.getRemainingCredits() + " Left)");

        infoCard.add(title);
        infoCard.add(key1);
        infoCard.add(key2);
        infoCard.add(lblApiUsageText);
        infoCard.add(progressApiCredits);
        infoCard.add(policy);
        infoCard.add(safetyNote);

        panel.add(infoCard, BorderLayout.NORTH);

        // Actions & Cache Preview
        JPanel cachePreview = new JPanel(new BorderLayout(0, 8));
        cachePreview.setOpaque(false);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionRow.setOpaque(false);
        ModernButton btnTriggerApi = new ModernButton("🌐 Execute Live Flight API Request", ButtonStyle.CYAN);
        btnTriggerApi.addActionListener(e -> showLiveFetchDialog());
        actionRow.add(btnTriggerApi);

        cachePreview.add(actionRow, BorderLayout.NORTH);

        JTextArea txtCache = new JTextArea();
        txtCache.setFont(new Font("Consolas", Font.PLAIN, 11));
        txtCache.setEditable(false);
        try {
            File f = new File("aviationstack_cache.json");
            if (f.exists()) {
                String content = new String(Files.readAllBytes(f.toPath()));
                txtCache.setText(content.substring(0, Math.min(2500, content.length())) + "\n... [Remaining payload stored in database]");
            }
        } catch (Exception ignored) {}

        cachePreview.add(new JScrollPane(txtCache), BorderLayout.CENTER);
        panel.add(cachePreview, BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // ==========================================
    // LIVE API REFRESH (REFRESHES ALL PLATFORMS SIMULTANEOUSLY)
    // ==========================================
    private void showLiveFetchDialog() {
        if (!creditManager.canMakeApiCall()) {
            JOptionPane.showMessageDialog(this,
                    "API Quota Limit Reached!\n\n" +
                            "You have used all " + ApiCreditManager.MAX_CREDITS + " available credits.\n" +
                            "Further Live API calls are strictly blocked to protect your account from overage.",
                    "Quota Limit Reached", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog(this, "Refresh Live Flight Data", true);
        dlg.setSize(520, 260);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(12, 12));

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBorder(new EmptyBorder(18, 18, 18, 18));
        content.setBackground(Color.WHITE);

        // Banner
        JPanel banner = new JPanel(new GridLayout(3, 1, 0, 6));
        banner.setBackground(new Color(239, 246, 255));
        banner.setBorder(new CompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel bTitle = new JLabel("Live Flight Data Synchronization");
        bTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bTitle.setForeground(ModernUI.COLOR_PRIMARY);

        JLabel bSub = new JLabel("Synchronizes and updates live flight fares across all routes simultaneously from both FlightAPI.io and AviationStack.");
        bSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bSub.setForeground(ModernUI.COLOR_TEXT_MAIN);

        JLabel bCost = new JLabel("<html><b>Action:</b> Queries both live API platforms, updates MySQL across all routes, and logs all detected fare changes in the Cost Tracker.</html>");
        bCost.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bCost.setForeground(ModernUI.COLOR_TEXT_MUTED);

        banner.add(bTitle);
        banner.add(bSub);
        banner.add(bCost);
        content.add(banner, BorderLayout.CENTER);

        // Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        ModernButton btnCancel = new ModernButton("Cancel", ButtonStyle.OUTLINE);
        ModernButton btnExecute = new ModernButton("Refresh All Live Data", ButtonStyle.CYAN);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnExecute.addActionListener(e -> {
            dlg.dispose();
            executeLiveApiFetch();
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnExecute);
        content.add(btnPanel, BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    private void executeLiveApiFetch() {
        JDialog loadingDlg = new JDialog(this, "Refreshing Live Flight Data...", true);
        loadingDlg.setSize(420, 130);
        loadingDlg.setLocationRelativeTo(this);
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel lblMsg = new JLabel("Synchronizing all routes with FlightAPI.io and AviationStack engines...", SwingConstants.CENTER);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        p.add(lblMsg, BorderLayout.NORTH);
        p.add(bar, BorderLayout.CENTER);
        loadingDlg.setContentPane(p);

        SwingWorker<LiveFlightApiService.ApiFetchResult, Void> worker = new SwingWorker<>() {
            @Override
            protected LiveFlightApiService.ApiFetchResult doInBackground() throws Exception {
                return liveApiService.refreshAllPlatforms();
            }

            @Override
            protected void done() {
                loadingDlg.dispose();
                try {
                    LiveFlightApiService.ApiFetchResult res = get();
                    refreshAllData();

                    if (res.success) {
                        // Switch immediately to Cost Tracker tab to list the changes!
                        if (tabbedPane != null) {
                            tabbedPane.setSelectedIndex(2);
                        }
                        if (lblTokenBatchBanner != null) {
                            lblTokenBatchBanner.setText(String.format("Active Sync: Entire dataset refreshed from both live API platforms (%d price changes: %d drops, %d hikes).",
                                    res.flightsAddedOrUpdated, res.priceDrops, res.priceHikes));
                        }

                        // Auto-filter by this session batch so user sees only the new changes
                        if (comboTokenBatchFilter != null && comboTokenBatchFilter.getItemCount() > 1) {
                            comboTokenBatchFilter.setSelectedIndex(comboTokenBatchFilter.getItemCount() - 1);
                            applyCostTrackerFilter();
                        }

                        JOptionPane.showMessageDialog(FlightAppGUI.this,
                                "Flight fares successfully refreshed across all routes from both FlightAPI.io and AviationStack!\n\n" +
                                        "• Total Cost Changes: " + res.flightsAddedOrUpdated + "\n" +
                                        "  - Fare Drops (Savings): " + res.priceDrops + "\n" +
                                        "  - Fare Hikes: " + res.priceHikes + "\n\n" +
                                        "All detected changes are now listed below in the Cost Tracker section.",
                                "Live Market Refreshed", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(FlightAppGUI.this,
                                res.message,
                                "API Notice", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(FlightAppGUI.this,
                            "Error refreshing live data: " + ex.getMessage(),
                            "Network Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
        loadingDlg.setVisible(true);
    }

    // ==========================================
    // LOGIC & DATA BINDING
    // ==========================================
    public void refreshAllData() {
        loadAllFlightsToTable();
        populateFilterDropdowns();
        populateComparisonDropdowns();
        loadCostTrackerData();
        executeWorkbenchQuery();
        updateStats();
    }

    private void loadAllFlightsToTable() {
        List<Flight> flights = flightDAO.getAllFlights();
        flightTableModel.setRowCount(0);
        for (Flight f : flights) {
            flightTableModel.addRow(new Object[]{
                    f.getId(),
                    f.getFlightNumber(),
                    f.getAirline(),
                    f.getRoute(),
                    f.getOriginAirport() != null ? f.getOriginAirport() : f.getOriginCode(),
                    f.getDestinationAirport() != null ? f.getDestinationAirport() : f.getDestinationCode(),
                    f.getDepartureTime(),
                    f.getArrivalTime(),
                    f.getDuration(),
                    f.getStopsDescription(),
                    f.getCabinClass(),
                    String.format("%.2f", f.getPrice()),
                    f.getDataSource(),
                    f.getStatus()
            });
        }
        updateStats();
    }

    private void populateFilterDropdowns() {
        List<String> origins = flightDAO.getDistinctOrigins();
        List<String> dests = flightDAO.getDistinctDestinations();
        List<String> airlines = flightDAO.getDistinctAirlines();

        comboOrigin.removeAllItems();
        comboOrigin.addItem("ALL");
        for (String o : origins) comboOrigin.addItem(o);

        comboDest.removeAllItems();
        comboDest.addItem("ALL");
        for (String d : dests) comboDest.addItem(d);

        comboAirline.removeAllItems();
        comboAirline.addItem("ALL");
        for (String a : airlines) comboAirline.addItem(a);
    }

    private void populateComparisonDropdowns() {
        List<Flight> flights = flightDAO.getAllFlights();
        comboFlight1.removeAllItems();
        comboFlight2.removeAllItems();

        for (Flight f : flights) {
            FlightItem item = new FlightItem(f);
            comboFlight1.addItem(item);
            comboFlight2.addItem(item);
        }

        if (comboFlight2.getItemCount() > 1) {
            comboFlight2.setSelectedIndex(1);
        }
    }

    private void applyFilter() {
        String orig = (String) comboOrigin.getSelectedItem();
        String dest = (String) comboDest.getSelectedItem();
        String air = (String) comboAirline.getSelectedItem();
        String cabin = (String) comboCabin.getSelectedItem();
        Double maxP = null;
        try {
            if (!txtMaxPrice.getText().trim().isEmpty()) {
                maxP = Double.parseDouble(txtMaxPrice.getText().trim());
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric max price.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Flight> results = flightDAO.searchFlights(orig, dest, air, maxP, cabin);
        flightTableModel.setRowCount(0);
        for (Flight f : results) {
            flightTableModel.addRow(new Object[]{
                    f.getId(),
                    f.getFlightNumber(),
                    f.getAirline(),
                    f.getRoute(),
                    f.getOriginAirport() != null ? f.getOriginAirport() : f.getOriginCode(),
                    f.getDestinationAirport() != null ? f.getDestinationAirport() : f.getDestinationCode(),
                    f.getDepartureTime(),
                    f.getArrivalTime(),
                    f.getDuration(),
                    f.getStopsDescription(),
                    f.getCabinClass(),
                    String.format("%.2f", f.getPrice()),
                    f.getDataSource(),
                    f.getStatus()
            });
        }
        lblTotalFlightsBadge.setText("Filtered: " + results.size());
    }

    private void selectFlightForComparison(int flightId, boolean isFlight1) {
        JComboBox<FlightItem> targetCombo = isFlight1 ? comboFlight1 : comboFlight2;
        for (int i = 0; i < targetCombo.getItemCount(); i++) {
            FlightItem item = targetCombo.getItemAt(i);
            if (item.flight.getId() == flightId) {
                targetCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void runComparison() {
        FlightItem item1 = (FlightItem) comboFlight1.getSelectedItem();
        FlightItem item2 = (FlightItem) comboFlight2.getSelectedItem();

        if (item1 == null || item2 == null) {
            JOptionPane.showMessageDialog(this, "Please select two flights to compare.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Flight f1 = item1.flight;
        Flight f2 = item2.flight;

        renderFlightCard(panelCard1, f1, "Flight 1");
        renderFlightCard(panelCard2, f2, "Flight 2");

        lastCalculatedComparison = comparisonService.compare(f1, f2);

        lblPriceDiff.setText("<html><b>Price Analysis:</b><br>" + lastCalculatedComparison.getCheaperFlight() + "</html>");
        lblDurationDiff.setText("<html><b>Duration & Convenience:</b><br>" + lastCalculatedComparison.getDurationDifference() + "</html>");
        lblVerdict.setText("<html>" + lastCalculatedComparison.getVerdict() + "</html>");

        btnSaveComparison.setEnabled(true);
    }

    private void renderFlightCard(JPanel card, Flight f, String title) {
        card.removeAll();
        JLabel header = new JLabel(title + " — " + f.getFlightNumber() + " (" + f.getAirline() + ")", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setForeground(ModernUI.COLOR_PRIMARY);
        card.add(header, BorderLayout.NORTH);

        String info = String.format(
                "Airline: %s\n" +
                        "Route: %s (%s) ➡ %s (%s)\n" +
                        "Departure: %s\n" +
                        "Arrival: %s\n" +
                        "Duration: %s\n" +
                        "Stops: %s\n" +
                        "Cabin: %s\n" +
                        "Price: $%.2f %s\n" +
                        "Source: %s\n" +
                        "Status: %s",
                f.getAirline(), f.getOriginCode(), f.getOriginAirport(),
                f.getDestinationCode(), f.getDestinationAirport(),
                f.getDepartureTime(), f.getArrivalTime(), f.getDuration(),
                f.getStopsDescription(), f.getCabinClass(), f.getPrice(), f.getCurrency(),
                f.getDataSource(), f.getStatus()
        );

        JTextArea ta = new JTextArea(info);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ta.setEditable(false);
        ta.setOpaque(false);
        ta.setBorder(new EmptyBorder(8, 8, 8, 8));
        card.add(ta, BorderLayout.CENTER);

        card.revalidate();
        card.repaint();
    }

    private void saveCurrentComparison() {
        if (lastCalculatedComparison == null) return;
        boolean saved = flightDAO.saveComparison(lastCalculatedComparison);
        if (saved) {
            JOptionPane.showMessageDialog(this,
                    "Comparison successfully saved to MySQL database table 'flight_comparisons'!\n" +
                            "You can now query it in MySQL Workbench or in Tab 4.",
                    "Saved to MySQL", JOptionPane.INFORMATION_MESSAGE);
            btnSaveComparison.setEnabled(false);
            executeWorkbenchQuery();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save comparison to MySQL.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeWorkbenchQuery() {
        String query = (String) comboDbQuery.getSelectedItem();
        if (query == null) return;

        try (Connection conn = DbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            Vector<String> colNames = new Vector<>();
            for (int i = 1; i <= colCount; i++) {
                colNames.add(meta.getColumnLabel(i));
            }

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= colCount; i++) {
                    row.add(rs.getObject(i));
                }
                data.add(row);
            }

            dbTableModel.setDataVector(data, colNames);
            lblDbStatus.setText("Rows fetched: " + data.size() + " | DB: flight_db");
        } catch (Exception e) {
            lblDbStatus.setText("Error: " + e.getMessage());
        }
    }

    private void showAddTicketDialog() {
        JDialog dlg = new JDialog(this, "Add New Flight Ticket to MySQL", true);
        dlg.setSize(440, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(9, 2, 8, 8));
        form.setBorder(new EmptyBorder(15, 15, 15, 15));
        form.setBackground(Color.WHITE);

        JTextField fNum = new JTextField("EK 202");
        JTextField fAir = new JTextField("Emirates");
        JTextField fOrig = new JTextField("JFK");
        JTextField fDest = new JTextField("DXB");
        JTextField fDep = new JTextField("2026-11-20 23:00");
        JTextField fArr = new JTextField("2026-11-21 19:45");
        JTextField fDur = new JTextField("12h 45m");
        JTextField fPrice = new JTextField("780.00");
        JComboBox<String> fStops = new JComboBox<>(new String[]{"0 (Direct)", "1 Stop", "2 Stops"});

        form.add(new JLabel("Flight Number:")); form.add(fNum);
        form.add(new JLabel("Airline:")); form.add(fAir);
        form.add(new JLabel("Origin Code (IATA):")); form.add(fOrig);
        form.add(new JLabel("Destination Code:")); form.add(fDest);
        form.add(new JLabel("Departure Time:")); form.add(fDep);
        form.add(new JLabel("Arrival Time:")); form.add(fArr);
        form.add(new JLabel("Duration:")); form.add(fDur);
        form.add(new JLabel("Price ($):")); form.add(fPrice);
        form.add(new JLabel("Stops:")); form.add(fStops);

        dlg.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        ModernButton btnCancel = new ModernButton("Cancel", ButtonStyle.OUTLINE);
        ModernButton btnSubmit = new ModernButton("Save to MySQL", ButtonStyle.PRIMARY);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSubmit.addActionListener(e -> {
            try {
                Flight nf = new Flight(
                        fNum.getText().trim(), fAir.getText().trim(),
                        fOrig.getText().trim().toUpperCase(), fOrig.getText().trim() + " Airport",
                        fDest.getText().trim().toUpperCase(), fDest.getText().trim() + " Airport",
                        fDep.getText().trim(), fArr.getText().trim(), fDur.getText().trim(),
                        fStops.getSelectedIndex(), "Economy", Double.parseDouble(fPrice.getText().trim()),
                        "USD", "Custom Manual Entry", "Scheduled"
                );
                flightDAO.saveFlight(nf);
                // Also log benchmark entry in Price History
                PriceHistory ph = new PriceHistory(
                        1,
                        nf.getFlightNumber(), nf.getAirline(), nf.getRoute(),
                        nf.getPrice(), nf.getPrice(), "BENCHMARK", "Manual Ticket Entry"
                );
                flightDAO.recordPriceChange(ph);

                dlg.dispose();
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Flight Ticket successfully saved to MySQL and Price Tracker!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error saving: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);
        dlg.add(btnPanel, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void updateStats() {
        int count = flightDAO.getFlightCount();
        lblTotalFlightsBadge.setText("Total Flights: " + count);
    }

    // Helper item for ComboBox
    private static class FlightItem {
        final Flight flight;
        FlightItem(Flight f) { this.flight = f; }
        @Override
        public String toString() {
            return String.format("%s | %s | %s->%s | $%.2f",
                    flight.getFlightNumber(), flight.getAirline(),
                    flight.getOriginCode(), flight.getDestinationCode(),
                    flight.getPrice());
        }
    }
}
