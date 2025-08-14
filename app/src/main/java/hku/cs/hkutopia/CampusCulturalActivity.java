package hku.cs.hkutopia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hku.cs.hkutopia.adapter.CulturalProductAdapter;
import hku.cs.hkutopia.model.ProductItem;
import hku.cs.hkutopia.utils.ProductDataUtils;

public class CampusCulturalActivity extends AppCompatActivity {

    private static final String TAG = "CampusCulturalActivity";
    private RecyclerView productsRecyclerView;
    private CulturalProductAdapter adapter;
    private List<ProductItem> allProducts;
    private List<ProductItem> displayedProducts;
    private Button btnLoadMore;
    private boolean isAllProductsShown = false;
    private static final int INITIAL_LOAD_COUNT = 6; // Initially load 6 products

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_cultural);

        // Initialize views
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        btnLoadMore = findViewById(R.id.btnLoadMore);
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageView btnLocation = findViewById(R.id.btnLocation);
        CardView purchaseInfoCard = findViewById(R.id.purchaseInfoCard);

        // Apply animation to purchase info card
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        purchaseInfoCard.startAnimation(fadeIn);

        // Set back button
        btnBack.setOnClickListener(v -> finish());

        // Set location button click listener
        btnLocation.setOnClickListener(v -> {
            Toast.makeText(this, "导航至游客中心", Toast.LENGTH_SHORT).show();
            // Navigate to map with visitor center location
            try {
                Intent mapIntent = new Intent(this, MapNavigationActivity.class);
                mapIntent.putExtra("DESTINATION", "visitor_center");
                startActivity(mapIntent);
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to visitor center", e);
                Toast.makeText(this, "无法导航至游客中心", Toast.LENGTH_SHORT).show();
            }
        });

        // Load all product data
        allProducts = ProductDataUtils.loadProductsFromJson(this, R.raw.cultural_products);
        Log.d(TAG, "Total products loaded from JSON: " + allProducts.size());

        // Initially display only the first 6 products
        displayedProducts = new ArrayList<>();
        loadInitialProducts();

        // Set up RecyclerView layout
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2); // Grid layout with 2 columns
        productsRecyclerView.setLayoutManager(layoutManager);

        // Initialize adapter and set it
        adapter = new CulturalProductAdapter(this, displayedProducts);
        productsRecyclerView.setAdapter(adapter);

        // Set up load more button
        btnLoadMore.setVisibility(View.VISIBLE);
        btnLoadMore.setOnClickListener(v -> {
            loadAllProducts(); // Changed to load all products at once
            Toast.makeText(CampusCulturalActivity.this, "已加载全部文创产品", Toast.LENGTH_SHORT).show();
            btnLoadMore.setVisibility(View.GONE);
            isAllProductsShown = true;
        });
    }

    // Load initial products
    private void loadInitialProducts() {
        displayedProducts.clear();
        int count = Math.min(INITIAL_LOAD_COUNT, allProducts.size());
        for (int i = 0; i < count; i++) {
            displayedProducts.add(allProducts.get(i));
        }
        Log.d(TAG, "Initial products loaded: " + displayedProducts.size());
    }

    // Load all products - completely replaces the displayed products list
    private void loadAllProducts() {
        // Clear and add all products to ensure a fresh state
        displayedProducts.clear();
        displayedProducts.addAll(allProducts);

        Log.d(TAG, "All products loaded, total count: " + displayedProducts.size());

        // Notify adapter of the complete data set change
        adapter.notifyDataSetChanged();
    }
}
