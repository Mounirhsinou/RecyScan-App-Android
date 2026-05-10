package com.recyscan.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.recyscan.models.Product;

import java.util.List;

/**
 * Data Access Object for Product entity.
 * Provides methods to query, insert, and manage products in the local database.
 */
@Dao
public interface ProductDao {

    // ========== Query Methods ==========

    /** Get all products ordered by name */
    @Query("SELECT * FROM products ORDER BY product_name ASC")
    LiveData<List<Product>> getAllProducts();

    /** Search products by barcode (exact match) */
    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    LiveData<Product> getProductByBarcode(String barcode);

    /** Search products by barcode (synchronous for scanner result) */
    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    Product getProductByBarcodeSync(String barcode);

    /** Search products by name (partial match, case-insensitive) */
    @Query("SELECT * FROM products WHERE LOWER(product_name) LIKE '%' || LOWER(:query) || '%' ORDER BY product_name ASC")
    LiveData<List<Product>> searchProducts(String query);

    /** Get products by recycling type */
    @Query("SELECT * FROM products WHERE recycling_type = :type ORDER BY product_name ASC")
    LiveData<List<Product>> getProductsByRecyclingType(String type);

    /** Get product count */
    @Query("SELECT COUNT(*) FROM products")
    int getProductCount();

    /** Get product by ID */
    @Query("SELECT * FROM products WHERE id = :id")
    LiveData<Product> getProductById(int id);

    // ========== Insert Methods ==========

    /** Insert a single product */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Product product);

    /** Insert multiple products at once */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Product> products);

    // ========== Update & Delete ==========

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);

    /** Delete all products (for database reset) */
    @Query("DELETE FROM products")
    void deleteAll();
}
