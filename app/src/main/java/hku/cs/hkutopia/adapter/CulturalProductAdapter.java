package hku.cs.hkutopia.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.util.List;

import hku.cs.hkutopia.ProductImageAdapter;
import hku.cs.hkutopia.R;
import hku.cs.hkutopia.model.ProductItem;

public class CulturalProductAdapter extends RecyclerView.Adapter<CulturalProductAdapter.ViewHolder> {

    private static final String TAG = "CulturalProductAdapter";
    private Context context;
    private List<ProductItem> productList;

    public CulturalProductAdapter(Context context, List<ProductItem> productList) {
        this.context = context;
        this.productList = productList;
        Log.d(TAG, "Adapter initialized with " + productList.size() + " products");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cultural_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position >= productList.size()) {
            Log.e(TAG, "Position out of bounds: " + position + ", list size: " + productList.size());
            return;
        }

        ProductItem product = productList.get(position);
        Log.d(TAG, "Binding product at position " + position + ": " + product.getName());

        holder.productNameTextView.setText(product.getName());
        holder.productPriceTextView.setText(String.format("HKD %.2f", product.getPrice()));

        // Setup horizontal gallery RecyclerView
        ProductImageAdapter imageAdapter = new ProductImageAdapter(context, product.getImages(), imagePosition -> {
            // Handle image click if needed
        });
        holder.imagesRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.imagesRecyclerView.setAdapter(imageAdapter);

        // Add snap helper for smooth horizontal scrolling
        SnapHelper snapHelper = new PagerSnapHelper();
        if (holder.imagesRecyclerView.getOnFlingListener() == null) {
            snapHelper.attachToRecyclerView(holder.imagesRecyclerView);
        }
    }

    @Override
    public int getItemCount() {
        int size = productList.size();
        Log.d(TAG, "getItemCount called, returning: " + size);
        return size;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView productNameTextView;
        TextView productPriceTextView;
        RecyclerView imagesRecyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.productName);
            productPriceTextView = itemView.findViewById(R.id.productPrice);
            imagesRecyclerView = itemView.findViewById(R.id.imagesRecyclerView);
        }
    }
}
