package com.recyscan.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.recyscan.models.Product;
import com.recyscan.repository.ProductRepository;

import java.util.List;

/**
 * ViewModel for the Home screen.
 * Manages product search and category filtering.
 */
public class HomeViewModel extends AndroidViewModel {

    private final ProductRepository repository;
    private final LiveData<List<Product>> allProducts;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private final MutableLiveData<String> categoryFilter = new MutableLiveData<>();

    // Search results that react to query changes
    private final LiveData<List<Product>> searchResults;

    // Category-filtered results
    private final LiveData<List<Product>> categoryProducts;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
        allProducts = repository.getAllProducts();

        // Transform search query into search results using switchMap
        searchResults = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return allProducts;
            }
            return repository.searchProducts(query.trim());
        });

        // Transform category filter into filtered results
        categoryProducts = Transformations.switchMap(categoryFilter, type -> {
            if (type == null || type.isEmpty()) {
                return allProducts;
            }
            return repository.getProductsByRecyclingType(type);
        });
    }

    // ========== Getters ==========

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    public LiveData<List<Product>> getSearchResults() {
        return searchResults;
    }

    public LiveData<List<Product>> getCategoryProducts() {
        return categoryProducts;
    }

    // ========== Actions ==========

    /** Update search query - triggers automatic LiveData update */
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    /** Set category filter */
    public void setCategoryFilter(String type) {
        categoryFilter.setValue(type);
    }

    /** Get product by barcode */
    public LiveData<Product> getProductByBarcode(String barcode) {
        return repository.getProductByBarcode(barcode);
    }

    /** Get product by ID */
    public LiveData<Product> getProductById(int id) {
        return repository.getProductById(id);
    }

    /** Get products by recycling type */
    public LiveData<List<Product>> getProductsByRecyclingType(String type) {
        return repository.getProductsByRecyclingType(type);
    }
}
