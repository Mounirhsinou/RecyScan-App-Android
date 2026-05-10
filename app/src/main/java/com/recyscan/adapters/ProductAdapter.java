package com.recyscan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.recyscan.R;
import com.recyscan.models.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying products in search results.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products = new ArrayList<>();
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.tvName.setText(product.getProductName());
        holder.tvType.setText(product.getRecyclingType());
        holder.tvMaterial.setText(product.getMaterialType());

        // Set category color
        int colorRes;
        int iconRes;
        switch (product.getRecyclingType()) {
            case "Gelber Sack": colorRes = R.color.yellow_light; iconRes = R.drawable.ic_gelber_sack; break;
            case "Altpapier": colorRes = R.color.blue_light; iconRes = R.drawable.ic_altpapier; break;
            case "Biomüll": colorRes = R.color.brown_light; iconRes = R.drawable.ic_biomuell; break;
            case "Altglas": colorRes = R.color.teal_light; iconRes = R.drawable.ic_altglas; break;
            default: colorRes = R.color.grey_light; iconRes = R.drawable.ic_restmuell; break;
        }

        holder.ivIcon.setImageResource(iconRes);
        holder.ivIcon.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), colorRes));

        if (product.isPfand()) {
            holder.chipPfand.setVisibility(View.VISIBLE);
            holder.chipPfand.setText("Pfand " + product.getFormattedPfandValue());
        } else {
            holder.chipPfand.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onProductClick(product));
    }

    @Override
    public int getItemCount() { return products.size(); }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType, tvMaterial;
        ImageView ivIcon;
        Chip chipPfand;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvType = itemView.findViewById(R.id.tv_recycling_type);
            tvMaterial = itemView.findViewById(R.id.tv_material);
            ivIcon = itemView.findViewById(R.id.iv_category_icon);
            chipPfand = itemView.findViewById(R.id.chip_pfand);
        }
    }
}
