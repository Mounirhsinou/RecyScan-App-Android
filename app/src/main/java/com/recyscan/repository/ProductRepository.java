package com.recyscan.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.recyscan.database.AppDatabase;
import com.recyscan.database.ProductDao;
import com.recyscan.models.Product;

import java.util.List;

/**
 * Repository for Product data.
 * Acts as a single source of truth, mediating between the DAO and ViewModel.
 */
public class ProductRepository {

    private final ProductDao productDao;

    public ProductRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        productDao = db.productDao();
    }

    // ========== Query Methods ==========

    /** Get all products as LiveData */
    public LiveData<List<Product>> getAllProducts() {
        return productDao.getAllProducts();
    }

    /** Search product by barcode */
    public LiveData<Product> getProductByBarcode(String barcode) {
        return productDao.getProductByBarcode(barcode);
    }

    /** Synchronous barcode lookup (for scanner result) */
    public Product getProductByBarcodeSync(String barcode) {
        return productDao.getProductByBarcodeSync(barcode);
    }

    /** Search products by name */
    public LiveData<List<Product>> searchProducts(String query) {
        return productDao.searchProducts(query);
    }

    /** Get products by recycling type */
    public LiveData<List<Product>> getProductsByRecyclingType(String type) {
        return productDao.getProductsByRecyclingType(type);
    }

    /** Get product by ID */
    public LiveData<Product> getProductById(int id) {
        return productDao.getProductById(id);
    }

    // ========== Write Methods ==========

    /** Insert a product on a background thread */
    public void insert(Product product) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.insert(product));
    }

    /** Update a product on a background thread */
    public void update(Product product) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.update(product));
    }

    /** Delete a product on a background thread */
    public void delete(Product product) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.delete(product));
    }
}
