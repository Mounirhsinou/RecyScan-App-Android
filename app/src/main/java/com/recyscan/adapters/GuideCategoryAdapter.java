package com.recyscan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.recyscan.R;
import com.recyscan.models.RecyclingCategory;

import java.util.List;

/**
 * Adapter for the recycling guide categories list.
 * Shows expandable cards with category details.
 */
public class GuideCategoryAdapter extends RecyclerView.Adapter<GuideCategoryAdapter.GuideViewHolder> {

    private final List<RecyclingCategory> categories;
    private int expandedPosition = -1;

    public GuideCategoryAdapter(List<RecyclingCategory> categories) {
        this.categories = categories;
    }

    @NonNull
    @Override
    public GuideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_guide_category, parent, false);
        return new GuideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuideViewHolder holder, int position) {
        RecyclingCategory cat = categories.get(position);
        holder.tvName.setText(cat.getName());
        holder.tvDescription.setText(cat.getDescription());
        holder.tvAccepted.setText(cat.getAcceptedItems());
        holder.tvExamples.setText(cat.getExampleProducts());
        holder.ivIcon.setImageResource(cat.getIconResId());

        boolean isExpanded = position == expandedPosition;
        holder.layoutDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.ivExpand.setRotation(isExpanded ? 180f : 0f);

        holder.layoutHeader.setOnClickListener(v -> {
            int prev = expandedPosition;
            expandedPosition = isExpanded ? -1 : position;
            notifyItemChanged(prev);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() { return categories.size(); }

    static class GuideViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon, ivExpand;
        TextView tvName, tvDescription, tvAccepted, tvExamples;
        View layoutHeader, layoutDetails;

        GuideViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_guide_icon);
            ivExpand = itemView.findViewById(R.id.iv_expand);
            tvName = itemView.findViewById(R.id.tv_guide_name);
            tvDescription = itemView.findViewById(R.id.tv_guide_description);
            tvAccepted = itemView.findViewById(R.id.tv_accepted_items);
            tvExamples = itemView.findViewById(R.id.tv_example_products);
            layoutHeader = itemView.findViewById(R.id.layout_guide_header);
            layoutDetails = itemView.findViewById(R.id.layout_guide_details);
        }
    }
}
