package com.recyscan.utils;

import android.content.Context;
import com.recyscan.R;
import com.recyscan.models.Product;

import java.util.Locale;

/**
 * Utility to map OpenFoodFacts product data to German recycling categories.
 */
public class RecyclingMapper {

    public static String determineRecyclingType(String categories, String packaging) {
        if (categories == null) categories = "";
        if (packaging == null) packaging = "";
        
        String combined = (categories + " " + packaging).toLowerCase(Locale.ROOT);

        if (combined.contains("glass") || combined.contains("glas")) {
            return Constants.TYPE_ALTGLAS;
        } else if (combined.contains("paper") || combined.contains("pappe") || combined.contains("karton") || combined.contains("cardboard")) {
            return Constants.TYPE_ALTPAPIER;
        } else if (combined.contains("plastic") || combined.contains("plastik") || combined.contains("metal") || combined.contains("metall") || combined.contains("alu") || combined.contains("can") || combined.contains("dose")) {
            return Constants.TYPE_GELBER_SACK;
        } else if (combined.contains("bio") || combined.contains("organic") || combined.contains("fruit") || combined.contains("vegetable")) {
            return Constants.TYPE_BIOMUELL;
        }

        return Constants.TYPE_RESTMUELL;
    }

    /**
     * Map AI detection label to German recycling category.
     */
    public static String fromAiLabel(String label) {
        if (label == null) return Constants.TYPE_RESTMUELL;
        
        String cleanLabel = label.toLowerCase(Locale.ROOT).replace("_", " ");
        
        if (cleanLabel.contains("glass") || cleanLabel.contains("glas")) {
            return Constants.TYPE_ALTGLAS;
        } else if (cleanLabel.contains("cardboard") || cleanLabel.contains("pappe") || cleanLabel.contains("paper") || cleanLabel.contains("zeitung")) {
            // Note: Cardboard is Altpapier, but beverage cartons (Tetra Pak) often go to Gelber Sack.
            // In many datasets "carton" might refer to beverage cartons.
            return Constants.TYPE_ALTPAPIER;
        } else if (cleanLabel.contains("plastic") || cleanLabel.contains("kunststoff") || cleanLabel.contains("metal") || cleanLabel.contains("can") || cleanLabel.contains("dose") || cleanLabel.contains("cup") || cleanLabel.contains("carton")) {
            // 'carton' is often used for beverage cartons which belong to Gelber Sack in Germany
            return Constants.TYPE_GELBER_SACK;
        } else if (cleanLabel.contains("food") || cleanLabel.contains("waste") || cleanLabel.contains("organic") || cleanLabel.contains("bio") || cleanLabel.contains("essen")) {
            return Constants.TYPE_BIOMUELL;
        }

        return Constants.TYPE_RESTMUELL;
    }

    public static String getInstructions(Context context, String type) {
        switch (type) {
            case Constants.TYPE_ALTGLAS:
                return context.getString(R.string.instr_altglas);
            case Constants.TYPE_ALTPAPIER:
                return context.getString(R.string.instr_altpapier);
            case Constants.TYPE_GELBER_SACK:
                return context.getString(R.string.instr_gelber_sack);
            case Constants.TYPE_BIOMUELL:
                return context.getString(R.string.instr_biomuell);
            default:
                return context.getString(R.string.instr_restmuell);
        }
    }

    public static String getLocalizedBinName(Context context, String type) {
        switch (type) {
            case Constants.TYPE_ALTGLAS: return context.getString(R.string.type_altglas);
            case Constants.TYPE_ALTPAPIER: return context.getString(R.string.type_altpapier);
            case Constants.TYPE_GELBER_SACK: return context.getString(R.string.type_gelber_sack);
            case Constants.TYPE_BIOMUELL: return context.getString(R.string.type_biomuell);
            default: return context.getString(R.string.type_restmuell);
        }
    }

    public static String getCategoryIcon(String type) {
        switch (type) {
            case Constants.TYPE_ALTGLAS: return "ic_altglas";
            case Constants.TYPE_ALTPAPIER: return "ic_altpapier";
            case Constants.TYPE_GELBER_SACK: return "ic_gelber_sack";
            case Constants.TYPE_BIOMUELL: return "ic_biomuell";
            default: return "ic_restmuell";
        }
    }
}
