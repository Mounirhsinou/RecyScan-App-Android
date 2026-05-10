package com.recyscan.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Product entity representing a recyclable item in the database.
 * Each product has a barcode, recycling type, and disposal instructions.
 */
@Entity(tableName = "products")
public class Product {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "product_name")
    @NonNull
    private String productName;

    @ColumnInfo(name = "barcode")
    private String barcode;

    @ColumnInfo(name = "recycling_type")
    @NonNull
    private String recyclingType;  // e.g., "Gelber Sack", "Altpapier", "Biomüll", "Altglas", "Restmüll"

    @ColumnInfo(name = "recyclable")
    private boolean recyclable;

    @ColumnInfo(name = "pfand")
    private boolean pfand;  // Whether the item has a deposit refund (Pfand)

    @ColumnInfo(name = "pfand_value")
    private double pfandValue;  // Pfand amount in euros (e.g., 0.25)

    @ColumnInfo(name = "instructions")
    private String instructions;  // Disposal/recycling instructions in German

    @ColumnInfo(name = "material_type")
    private String materialType;  // e.g., "PET", "HDPE", "Glas", "Pappe", "Aluminium"

    @ColumnInfo(name = "category_icon")
    private String categoryIcon;  // Icon resource name for the category

    // ========== Constructor ==========

    public Product(@NonNull String productName, String barcode, @NonNull String recyclingType,
                   boolean recyclable, boolean pfand, double pfandValue,
                   String instructions, String materialType, String categoryIcon) {
        this.productName = productName;
        this.barcode = barcode;
        this.recyclingType = recyclingType;
        this.recyclable = recyclable;
        this.pfand = pfand;
        this.pfandValue = pfandValue;
        this.instructions = instructions;
        this.materialType = materialType;
        this.categoryIcon = categoryIcon;
    }

    // ========== Getters & Setters ==========

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getProductName() { return productName; }
    public void setProductName(@NonNull String productName) { this.productName = productName; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    @NonNull
    public String getRecyclingType() { return recyclingType; }
    public void setRecyclingType(@NonNull String recyclingType) { this.recyclingType = recyclingType; }

    public boolean isRecyclable() { return recyclable; }
    public void setRecyclable(boolean recyclable) { this.recyclable = recyclable; }

    public boolean isPfand() { return pfand; }
    public void setPfand(boolean pfand) { this.pfand = pfand; }

    public double getPfandValue() { return pfandValue; }
    public void setPfandValue(double pfandValue) { this.pfandValue = pfandValue; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }

    public String getCategoryIcon() { return categoryIcon; }
    public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }

    /**
     * Returns formatted Pfand value as a string (e.g., "0,25€")
     */
    public String getFormattedPfandValue() {
        if (!pfand) return "Kein Pfand";
        return String.format("%.2f€", pfandValue).replace(".", ",");
    }
}
