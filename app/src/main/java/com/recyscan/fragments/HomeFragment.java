package com.recyscan.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.recyscan.R;
import com.recyscan.activities.ProductResultActivity;
import com.recyscan.activities.ScannerActivity;
import com.recyscan.adapters.CategoryAdapter;
import com.recyscan.adapters.ProductAdapter;
import com.recyscan.models.Product;
import com.recyscan.models.RecyclingCategory;
import com.recyscan.utils.Constants;
import com.recyscan.viewmodels.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Home Fragment - Main screen of the app.
 * Features: Scan button, search bar, quick category cards, and search results.
 */
public class HomeFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private HomeViewModel viewModel;
    private EditText etSearch;
    private RecyclerView rvCategories, rvSearchResults;
    private MaterialCardView cardScan;
    private View layoutSearchResults, layoutMain;
    private TextView tvEmptySearch;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        bindViews(view);
        setupCategories();
        setupSearch();
        setupScanButton();
    }

    private void bindViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        rvCategories = view.findViewById(R.id.rv_categories);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        cardScan = view.findViewById(R.id.card_scan);
        layoutSearchResults = view.findViewById(R.id.layout_search_results);
        layoutMain = view.findViewById(R.id.layout_main_content);
        tvEmptySearch = view.findViewById(R.id.tv_empty_search);
    }

    /**
     * Setup the recycling category grid (Plastik, Papier, Glas, Biomüll, Restmüll).
     */
    private void setupCategories() {
        List<RecyclingCategory> categories = new ArrayList<>();

        categories.add(new RecyclingCategory(
                getString(R.string.cat_plastic), getString(R.string.cat_plastic_desc), "Plastikflaschen, Joghurtbecher, Folien",
                "Gelber Sack / Gelbe Tonne", R.drawable.ic_gelber_sack, R.color.yellow_light, "#FFF8E1"
        ));
        categories.add(new RecyclingCategory(
                getString(R.string.cat_paper), getString(R.string.cat_paper_desc), "Zeitungen, Kartons, Druckerpapier",
                "Altpapier / Blaue Tonne", R.drawable.ic_altpapier, R.color.blue_light, "#E3F2FD"
        ));
        categories.add(new RecyclingCategory(
                getString(R.string.cat_glass), getString(R.string.cat_glass_desc), "Weinflaschen, Marmeladengläser, Senfgläser",
                "Altglas-Container", R.drawable.ic_altglas, R.color.teal_light, "#E0F2F1"
        ));
        categories.add(new RecyclingCategory(
                getString(R.string.cat_bio), getString(R.string.cat_bio_desc), "Obstreste, Kaffeesatz, Eierschalen",
                "Biotonne / Braune Tonne", R.drawable.ic_biomuell, R.color.brown_light, "#EFEBE9"
        ));

        categoryAdapter = new CategoryAdapter(categories, category -> {
            // Use logical identification instead of localized name
            String dbType = Constants.TYPE_RESTMUELL;
            if (category.getName().equals(getString(R.string.cat_plastic))) dbType = Constants.TYPE_GELBER_SACK;
            else if (category.getName().equals(getString(R.string.cat_paper))) dbType = Constants.TYPE_ALTPAPIER;
            else if (category.getName().equals(getString(R.string.cat_glass))) dbType = Constants.TYPE_ALTGLAS;
            else if (category.getName().equals(getString(R.string.cat_bio))) dbType = Constants.TYPE_BIOMUELL;
            
            showCategoryProducts(dbType);
        });

        rvCategories.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvCategories.setAdapter(categoryAdapter);
        rvCategories.setNestedScrollingEnabled(false);
    }

    /**
     * Show products filtered by category.
     */
    private void showCategoryProducts(String type) {
        layoutMain.setVisibility(View.GONE);
        layoutSearchResults.setVisibility(View.VISIBLE);

        productAdapter = new ProductAdapter(this);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSearchResults.setAdapter(productAdapter);

        viewModel.getProductsByRecyclingType(type).observe(getViewLifecycleOwner(), products -> {
            if (products != null && !products.isEmpty()) {
                productAdapter.setProducts(products);
                tvEmptySearch.setVisibility(View.GONE);
                rvSearchResults.setVisibility(View.VISIBLE);
            } else {
                tvEmptySearch.setVisibility(View.VISIBLE);
                tvEmptySearch.setText(R.string.empty_category);
                rvSearchResults.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Setup the search functionality with text change listener.
     */
    private void setupSearch() {
        productAdapter = new ProductAdapter(this);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSearchResults.setAdapter(productAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    // Show search results
                    layoutMain.setVisibility(View.GONE);
                    layoutSearchResults.setVisibility(View.VISIBLE);
                    viewModel.setSearchQuery(query);
                } else {
                    // Show main content
                    layoutMain.setVisibility(View.VISIBLE);
                    layoutSearchResults.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Observe search results
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), products -> {
            if (products != null && !products.isEmpty()) {
                productAdapter.setProducts(products);
                tvEmptySearch.setVisibility(View.GONE);
                rvSearchResults.setVisibility(View.VISIBLE);
            } else if (etSearch.getText().toString().trim().length() >= 2) {
                tvEmptySearch.setVisibility(View.VISIBLE);
                tvEmptySearch.setText(R.string.no_results);
                rvSearchResults.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Setup the scan barcode button.
     */
    private void setupScanButton() {
        cardScan.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ScannerActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(requireActivity(), ProductResultActivity.class);
        intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.getId());
        startActivity(intent);
    }
}
