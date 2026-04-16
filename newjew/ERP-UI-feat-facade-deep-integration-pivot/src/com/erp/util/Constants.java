package com.erp.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;

/**
 * Application-wide constants. Utility class — cannot be instantiated.
 *
 * Palette is Tata Motors — navy/blue/gold. Legacy semantic aliases
 * (PRIMARY_COLOR, BG_DARK, etc.) are repointed to the Tata palette
 * so existing call sites shift theme without edits.
 */
public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    // ==================== APPLICATION INFO ====================
    public static final String APP_NAME = "Tata Motors ERP";
    public static final String APP_TAGLINE = "Enterprise Resource Planning";
    public static final String APP_SLOGAN = "The Trust of India";
    public static final String APP_VERSION = "2.0";
    public static final String FISCAL_YEAR = "FY 2025-26";
    public static final String PLANT_NAME = "Tata Motors Pune Plant";

    // ==================== WINDOW DIMENSIONS ====================
    public static final int LOGIN_WIDTH = 880;
    public static final int LOGIN_HEIGHT = 560;
    public static final int MAIN_WIDTH = 1280;
    public static final int MAIN_HEIGHT = 720;
    public static final Dimension LOGIN_SIZE = new Dimension(LOGIN_WIDTH, LOGIN_HEIGHT);
    public static final Dimension MAIN_SIZE = new Dimension(MAIN_WIDTH, MAIN_HEIGHT);

    // ==================== TATA PALETTE ====================
    public static final Color TATA_NAVY   = new Color(0x00, 0x1B, 0x52);
    public static final Color TATA_BLUE   = new Color(0x00, 0x3F, 0x8A);
    public static final Color TATA_GOLD   = new Color(0xD4, 0xAF, 0x37);
    public static final Color TATA_BG     = new Color(0xF4, 0xF6, 0xFB);
    public static final Color TATA_WHITE  = new Color(0xFF, 0xFF, 0xFF);
    public static final Color TATA_BORDER = new Color(0xD6, 0xDC, 0xE8);
    public static final Color TATA_TEXT   = new Color(0x1A, 0x20, 0x2C);
    public static final Color TATA_MUTED  = new Color(0x6B, 0x72, 0x85);
    public static final Color TATA_ROW_ALT = new Color(0xF8, 0xFA, 0xFF);

    // ==================== COLOR SCHEME (semantic aliases) ====================
    public static final Color PRIMARY_COLOR = TATA_BLUE;
    public static final Color PRIMARY_DARK  = TATA_NAVY;
    public static final Color PRIMARY_LIGHT = new Color(0xB8, 0xCB, 0xE6);

    public static final Color SECONDARY_COLOR = TATA_NAVY;
    public static final Color ACCENT_COLOR  = TATA_GOLD;
    public static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    public static final Color WARNING_COLOR = new Color(241, 196, 15);
    public static final Color DANGER_COLOR  = new Color(231, 76, 60);

    public static final Color BG_DARK  = TATA_NAVY;
    public static final Color BG_LIGHT = TATA_BG;
    public static final Color BG_WHITE = TATA_WHITE;

    public static final Color TEXT_PRIMARY   = TATA_TEXT;
    public static final Color TEXT_SECONDARY = TATA_MUTED;
    public static final Color TEXT_LIGHT     = TATA_WHITE;

    // ==================== FONTS ====================
    public static final String FONT_FAMILY = "Segoe UI";
    public static final Font FONT_TITLE    = new Font(FONT_FAMILY, Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font FONT_HEADING  = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_REGULAR  = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_SMALL    = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_BUTTON   = new Font(FONT_FAMILY, Font.BOLD, 13);

    // ==================== SPACING ====================
    public static final int PADDING_SMALL  = 5;
    public static final int PADDING_MEDIUM = 10;
    public static final int PADDING_LARGE  = 20;
    public static final int PADDING_XLARGE = 30;

    // ==================== SIDEBAR ====================
    public static final int SIDEBAR_WIDTH = 250;

    // ==================== MODULE NAMES ====================
    public static final String MODULE_DASHBOARD     = "Dashboard";
    public static final String MODULE_CRM           = "CRM";
    public static final String MODULE_PROJECT       = "Project Management";
    public static final String MODULE_HR            = "HR Management";
    public static final String MODULE_FINANCE       = "Financial Management";
    public static final String MODULE_SALES         = "Sales Management";
    public static final String MODULE_INVENTORY     = "Supply Chain";
    public static final String MODULE_MANUFACTURING = "Manufacturing";
    public static final String MODULE_ACCOUNTING    = "Accounting";
    public static final String MODULE_REPORTING     = "Reporting";
    public static final String MODULE_ANALYTICS     = "Data Analytics";
    public static final String MODULE_MARKETING     = "Marketing";
    public static final String MODULE_ORDER         = "Order Processing";
    public static final String MODULE_AUTOMATION    = "Automation";
    public static final String MODULE_BI            = "Business Intelligence";
}
