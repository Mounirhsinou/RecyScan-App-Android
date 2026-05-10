package com.recyscan.models;

/**
 * Data class representing a recycling category for the Guide screen.
 * Not stored in Room - used as a UI model for displaying category information.
 */
public class RecyclingCategory {

    private String name;           // Category name (e.g., "Gelber Sack")
    private String description;    // What goes in this category
    private String acceptedItems;  // Comma-separated list of accepted items
    private String exampleProducts; // Example products
    private int iconResId;         // Drawable resource ID for the icon
    private int colorResId;        // Color resource ID for the category
    private String colorHex;       // Hex color string

    // ========== Constructor ==========

    public RecyclingCategory(String name, String description, String acceptedItems,
                             String exampleProducts, int iconResId, int colorResId, String colorHex) {
        this.name = name;
        this.description = description;
        this.acceptedItems = acceptedItems;
        this.exampleProducts = exampleProducts;
        this.iconResId = iconResId;
        this.colorResId = colorResId;
        this.colorHex = colorHex;
    }

    // ========== Getters ==========

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getAcceptedItems() { return acceptedItems; }
    public String getExampleProducts() { return exampleProducts; }
    public int getIconResId() { return iconResId; }
    public int getColorResId() { return colorResId; }
    public String getColorHex() { return colorHex; }

    // ========== Setters ==========

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setAcceptedItems(String acceptedItems) { this.acceptedItems = acceptedItems; }
    public void setExampleProducts(String exampleProducts) { this.exampleProducts = exampleProducts; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
    public void setColorResId(int colorResId) { this.colorResId = colorResId; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
}
