package com.recyscan.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.recyscan.R;
import com.recyscan.models.Product;
import com.recyscan.utils.Constants;
import com.recyscan.utils.ThemeHelper;
import com.recyscan.viewmodels.HomeViewModel;

/**
 * Displays detailed information about a scanned/searched product.
 * Shows recycling category, instructions, pfand info, material type, and tips.
 */
public class ProductResultActivity extends AppCompatActivity {

    private HomeViewModel viewModel;

    // UI Elements
    private TextView tvProductName, tvBarcode, tvRecyclingType, tvInstructions;
    private TextView tvMaterial, tvPfandValue, tvPfandLabel;
    private Chip chipRecyclable;
    private CardView cardRecyclingType, cardPfand;
    private ImageView ivCategoryIcon;
    private LinearLayout layoutTips;
    private MaterialButton btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_result);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Bind views
        bindViews();

        // Setup back button
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        // Get product data from intent
        int productId = getIntent().getIntExtra(Constants.EXTRA_PRODUCT_ID, -1);
        String barcode = getIntent().getStringExtra(Constants.EXTRA_BARCODE);

        if (productId != -1) {
            loadProductById(productId);
        } else if (barcode != null) {
            loadProductByBarcode(barcode);
        } else {
            finish(); // No data provided
        }

        // Done button
        btnDone.setOnClickListener(v -> finish());
    }

    private void bindViews() {
        tvProductName = findViewById(R.id.tv_product_name);
        tvBarcode = findViewById(R.id.tv_barcode);
        tvRecyclingType = findViewById(R.id.tv_recycling_type);
        tvInstructions = findViewById(R.id.tv_instructions);
        tvMaterial = findViewById(R.id.tv_material);
        tvPfandValue = findViewById(R.id.tv_pfand_value);
        tvPfandLabel = findViewById(R.id.tv_pfand_label);
        chipRecyclable = findViewById(R.id.chip_recyclable);
        cardRecyclingType = findViewById(R.id.card_recycling_type);
        cardPfand = findViewById(R.id.card_pfand);
        ivCategoryIcon = findViewById(R.id.iv_category_icon);
        layoutTips = findViewById(R.id.layout_tips);
        btnDone = findViewById(R.id.btn_done);
    }

    /**
     * Load product by ID and display its details.
     */
    private void loadProductById(int id) {
        viewModel.getProductById(id).observe(this, this::displayProduct);
    }

    /**
     * Load product by barcode and display its details.
     */
    private void loadProductByBarcode(String barcode) {
        viewModel.getProductByBarcode(barcode).observe(this, this::displayProduct);
    }

    /**
     * Populate the UI with product data.
     */
    private void displayProduct(Product product) {
        if (product == null) {
            tvProductName.setText("Produkt nicht gefunden");
            return;
        }

        // ========== Basic Info ==========
        tvProductName.setText(product.getProductName());

        String barcodeText = product.getBarcode();
        if (barcodeText != null && !barcodeText.isEmpty()) {
            tvBarcode.setText("Barcode: " + barcodeText);
            tvBarcode.setVisibility(View.VISIBLE);
        } else {
            tvBarcode.setVisibility(View.GONE);
        }

        // ========== Recyclable Status Chip ==========
        if (product.isRecyclable()) {
            chipRecyclable.setText("Recyclable");
            chipRecyclable.setChipBackgroundColorResource(R.color.green_light);
            chipRecyclable.setTextColor(ContextCompat.getColor(this, R.color.green_primary));
        } else {
            chipRecyclable.setText("Nicht recyclebar");
            chipRecyclable.setChipBackgroundColorResource(R.color.grey_light);
            chipRecyclable.setTextColor(ContextCompat.getColor(this, R.color.grey_dark));
        }

        // ========== Recycling Type Card ==========
        tvRecyclingType.setText(product.getRecyclingType());
        setRecyclingTypeStyle(product.getRecyclingType());

        // ========== Pfand Card ==========
        if (product.isPfand()) {
            cardPfand.setVisibility(View.VISIBLE);
            tvPfandValue.setText("Pfand: " + product.getFormattedPfandValue());
            tvPfandLabel.setText("Rückgabewert");
        } else {
            cardPfand.setVisibility(View.GONE);
        }

        // ========== Material Type ==========
        tvMaterial.setText(product.getMaterialType());

        // ========== Instructions ==========
        tvInstructions.setText(product.getInstructions());

        // ========== Tips Section ==========
        addRecyclingTips(product);
    }

    /**
     * Set the recycling type card style based on category.
     */
    private void setRecyclingTypeStyle(String type) {
        int bgColor, iconRes;

        switch (type) {
            case Constants.TYPE_GELBER_SACK:
                bgColor = R.color.yellow_light;
                iconRes = R.drawable.ic_gelber_sack;
                break;
            case Constants.TYPE_ALTPAPIER:
                bgColor = R.color.blue_light;
                iconRes = R.drawable.ic_altpapier;
                break;
            case Constants.TYPE_BIOMUELL:
                bgColor = R.color.brown_light;
                iconRes = R.drawable.ic_biomuell;
                break;
            case Constants.TYPE_ALTGLAS:
                bgColor = R.color.teal_light;
                iconRes = R.drawable.ic_altglas;
                break;
            case Constants.TYPE_RESTMUELL:
            default:
                bgColor = R.color.grey_light;
                iconRes = R.drawable.ic_restmuell;
                break;
        }

        cardRecyclingType.setCardBackgroundColor(ContextCompat.getColor(this, bgColor));
        ivCategoryIcon.setImageResource(iconRes);
    }

    /**
     * Add contextual recycling tips based on the product.
     */
    private void addRecyclingTips(Product product) {
        layoutTips.removeAllViews();

        // Add tips based on recycling type
        switch (product.getRecyclingType()) {
            case Constants.TYPE_GELBER_SACK:
                addTip("Verpackung leer machen, nicht ausspülen");
                addTip("Nicht stapeln - Sortieranlage braucht einzelne Teile");
                if (product.isPfand()) {
                    addTip("Etikett muss für die Pfandstation lesbar bleiben");
                }
                break;
            case Constants.TYPE_ALTPAPIER:
                addTip("Pappe flach zusammenfalten");
                addTip("Keine nassen oder verschmutzten Papiere");
                break;
            case Constants.TYPE_BIOMUELL:
                addTip("Kein Plastik in die Biotonne");
                addTip("Zeitungspapier als Einwicklung ist erlaubt");
                break;
            case Constants.TYPE_ALTGLAS:
                addTip("Nach Farben sortieren: Weiß, Grün, Braun");
                addTip("Deckel und Verschlüsse entfernen");
                break;
            default:
                addTip("Im Zweifel: Restmüll ist immer richtig");
                break;
        }
    }

    /**
     * Add a single tip item to the tips section.
     */
    private void addTip(String tipText) {
        View tipView = getLayoutInflater().inflate(R.layout.item_tip, layoutTips, false);
        TextView tvTip = tipView.findViewById(R.id.tv_tip_text);
        tvTip.setText(tipText);
        layoutTips.addView(tipView);
    }
}
