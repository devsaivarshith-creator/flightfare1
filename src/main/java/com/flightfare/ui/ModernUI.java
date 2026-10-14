package com.flightfare.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern design system, vibrant color palette, and custom UI components.
 * Guarantees 100% visible, high-contrast button text and lively modern aesthetics.
 */
public class ModernUI {

    // Harmonious, Lively Color Palette
    public static final Color COLOR_PRIMARY = new Color(37, 99, 235);     // Electric Blue
    public static final Color COLOR_PRIMARY_HOVER = new Color(29, 78, 216);
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);    // Vibrant Emerald
    public static final Color COLOR_SUCCESS_HOVER = new Color(5, 150, 105);
    public static final Color COLOR_WARNING = new Color(245, 158, 11);    // Bright Amber
    public static final Color COLOR_WARNING_HOVER = new Color(217, 119, 6);
    public static final Color COLOR_DANGER = new Color(239, 68, 68);      // Vivid Crimson
    public static final Color COLOR_DANGER_HOVER = new Color(220, 38, 38);
    public static final Color COLOR_PURPLE = new Color(139, 92, 246);     // Vivid Violet
    public static final Color COLOR_PURPLE_HOVER = new Color(124, 58, 237);
    public static final Color COLOR_CYAN = new Color(14, 165, 233);       // Sky Blue
    public static final Color COLOR_CYAN_HOVER = new Color(2, 132, 199);
    public static final Color COLOR_DARK = new Color(30, 41, 59);         // Deep Slate
    public static final Color COLOR_DARK_HOVER = new Color(15, 23, 42);

    public static final Color COLOR_CANVAS = new Color(248, 250, 252);    // Clean Soft Slate
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_BORDER = new Color(226, 232, 240);    // Subtle border
    public static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);

    public enum ButtonStyle {
        PRIMARY, SUCCESS, WARNING, DANGER, PURPLE, CYAN, DARK, OUTLINE
    }

    /**
     * Modern rounded button with custom anti-aliased rendering and smooth hover effect.
     * Completely eliminates OS Look-and-Feel button text invisibility issues.
     */
    public static class ModernButton extends JButton {
        private final ButtonStyle style;
        private Color normalBg;
        private Color hoverBg;
        private Color normalFg;
        private boolean isHovered = false;
        private boolean isPressed = false;
        private int cornerRadius = 10;

        public ModernButton(String text, ButtonStyle style) {
            super(text);
            this.style = style;
            initStyle();
            initListeners();
        }

        private void initStyle() {
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 16, 8, 16));

            switch (style) {
                case PRIMARY:
                    normalBg = COLOR_PRIMARY;
                    hoverBg = COLOR_PRIMARY_HOVER;
                    normalFg = Color.WHITE;
                    break;
                case SUCCESS:
                    normalBg = COLOR_SUCCESS;
                    hoverBg = COLOR_SUCCESS_HOVER;
                    normalFg = Color.WHITE;
                    break;
                case WARNING:
                    normalBg = COLOR_WARNING;
                    hoverBg = COLOR_WARNING_HOVER;
                    normalFg = Color.WHITE;
                    break;
                case DANGER:
                    normalBg = COLOR_DANGER;
                    hoverBg = COLOR_DANGER_HOVER;
                    normalFg = Color.WHITE;
                    break;
                case PURPLE:
                    normalBg = COLOR_PURPLE;
                    hoverBg = COLOR_PURPLE_HOVER;
                    normalFg = Color.WHITE;
                    break;
                case CYAN:
                    normalBg = COLOR_CYAN;
                    hoverBg = COLOR_CYAN_HOVER;
                    normalFg = Color.WHITE;
                    break;
                case DARK:
                    normalBg = COLOR_DARK;
                    hoverBg = COLOR_DARK_HOVER;
                    normalFg = Color.WHITE;
                    break;
                case OUTLINE:
                default:
                    normalBg = Color.WHITE;
                    hoverBg = new Color(241, 245, 249);
                    normalFg = COLOR_PRIMARY;
                    break;
            }
            setForeground(normalFg);
        }

        private void initListeners() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        isHovered = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled()) {
                        isPressed = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int width = getWidth();
            int height = getHeight();

            Color currentBg;
            Color currentFg;

            if (!isEnabled()) {
                currentBg = new Color(226, 232, 240);
                currentFg = new Color(148, 163, 184);
            } else if (isPressed) {
                currentBg = hoverBg.darker();
                currentFg = normalFg;
            } else if (isHovered) {
                currentBg = hoverBg;
                currentFg = normalFg;
            } else {
                currentBg = normalBg;
                currentFg = normalFg;
            }

            // Fill background
            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius);

            // Draw outline if needed
            if (style == ButtonStyle.OUTLINE && isEnabled()) {
                g2.setColor(new Color(203, 213, 225));
                g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
            }

            // Draw Text
            g2.setColor(currentFg);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int textX = (width - fm.stringWidth(getText())) / 2;
            int textY = (height - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), textX, textY);

            g2.dispose();
        }
    }

    /**
     * Creates a lively rounded pill badge.
     */
    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        lbl.setOpaque(false);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setBorder(new EmptyBorder(5, 12, 5, 12));
        return lbl;
    }

    /**
     * Creates a modern KPI metric card with icon and vibrant styling.
     */
    public static JPanel createMetricCard(String icon, String title, String value, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        // Top Row: Icon + Title
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        top.setOpaque(false);
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(COLOR_TEXT_MUTED);
        top.add(lblIcon);
        top.add(lblTitle);
        card.add(top, BorderLayout.NORTH);

        // Center: Value
        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblVal.setForeground(accentColor);
        card.add(lblVal, BorderLayout.CENTER);

        // Bottom: Subtitle
        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel lblSub = new JLabel(subtitle);
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblSub.setForeground(COLOR_TEXT_MUTED);
            card.add(lblSub, BorderLayout.SOUTH);
        }

        return card;
    }

    /**
     * Beautiful Table Header Renderer.
     */
    public static class ModernHeaderRenderer extends DefaultTableCellRenderer {
        private final Color bgColor;
        public ModernHeaderRenderer(Color bg) {
            this.bgColor = bg;
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setBackground(bgColor);
            setBorder(new EmptyBorder(6, 6, 6, 6));
            return this;
        }
    }

    /**
     * Table Cell Renderer for Currency/Price with vibrant green text.
     */
    public static class PriceCellRenderer extends DefaultTableCellRenderer {
        public PriceCellRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (!isSelected) {
                setForeground(new Color(5, 150, 105)); // Forest green
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            }
            setBorder(new EmptyBorder(0, 4, 0, 8));
            return this;
        }
    }

    /**
     * Table Cell Renderer for Price Changes (Green for drop, Red for hike).
     */
    public static class PriceChangeCellRenderer extends DefaultTableCellRenderer {
        public PriceChangeCellRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            String str = value != null ? value.toString() : "";
            if (!isSelected) {
                if (str.startsWith("-") || str.contains("DROPPED") || str.contains("SAVINGS") || str.contains("▼")) {
                    setForeground(new Color(16, 185, 129)); // Lively Green
                    setBackground(new Color(236, 253, 245)); // Soft green tint
                } else if (str.startsWith("+") || str.contains("INCREASED") || str.contains("▲")) {
                    setForeground(new Color(239, 68, 68)); // Lively Red
                    setBackground(new Color(254, 242, 242)); // Soft red tint
                } else {
                    setForeground(COLOR_PRIMARY);
                    setBackground(new Color(239, 246, 255));
                }
            }
            setBorder(new EmptyBorder(2, 6, 2, 6));
            return this;
        }
    }
}
