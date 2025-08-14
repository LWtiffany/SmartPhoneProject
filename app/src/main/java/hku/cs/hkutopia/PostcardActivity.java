package hku.cs.hkutopia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class PostcardActivity extends AppCompatActivity {

    private ViewPager2 postcardViewPager;
    private List<Postcard> postcards;
    private FrameLayout categoryLotus, categoryFlower, categoryScenery, categoryGate, categoryArchitecture, categoryLibrary;
    private CardView categoryCover1, categoryCover2, categoryCover3, categoryCover4, categoryCover5, categoryCover6;
    private HorizontalScrollView categoriesScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postcard);

        // Initialize back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Initialize postcards data
        initPostcards();

        // Initialize ViewPager
        postcardViewPager = findViewById(R.id.postcardViewPager);
        PostcardAdapter adapter = new PostcardAdapter(postcards);
        postcardViewPager.setAdapter(adapter);

        // Initialize category views
        initCategoryViews();

        // Add page change listener for category highlighting
        postcardViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                highlightCategory(position);
            }
        });

        // Initialize with the first category highlighted
        postcardViewPager.post(() -> highlightCategory(0));

        // Add page transformer for 3D effect
        postcardViewPager.setPageTransformer(new DepthPageTransformer());

        // Initialize category buttons
        initCategories();
    }

    private void initCategoryViews() {
        // Initialize category layouts
        categoryLotus = findViewById(R.id.categoryLotus);
        categoryFlower = findViewById(R.id.categoryFlower);
        categoryScenery = findViewById(R.id.categoryScenery);
        categoryGate = findViewById(R.id.categoryGate);
        categoryArchitecture = findViewById(R.id.categoryArchitecture);
        categoryLibrary = findViewById(R.id.categoryLibrary);

        // Initialize category cover CardViews
        categoryCover1 = findViewById(R.id.categoryCover1);
        categoryCover2 = findViewById(R.id.categoryCover2);
        categoryCover3 = findViewById(R.id.categoryCover3);
        categoryCover4 = findViewById(R.id.categoryCover4);
        categoryCover5 = findViewById(R.id.categoryCover5);
        categoryCover6 = findViewById(R.id.categoryCover6);

        // Initialize scroll view
        categoriesScrollView = findViewById(R.id.postcardCategoriesScrollView);
    }

    private void initPostcards() {
        postcards = new ArrayList<>();

        // Add sample postcards
        postcards.add(new Postcard(
                R.drawable.postcard_lotus,
                "心中本无鱼",
                "心静自然凉",
                "香港大学阳光"
        ));

        postcards.add(new Postcard(
                R.drawable.postcard_library,
                "知识的殿堂",
                "学海无涯",
                "香港大学图书馆"
        ));

        postcards.add(new Postcard(
                R.drawable.postcard_main_building,
                "百年学府",
                "承载历史",
                "香港大学主楼"
        ));

        postcards.add(new Postcard(
                R.drawable.postcard_garden,
                "校园一隅",
                "静谧时光",
                "香港大学花园"
        ));

        postcards.add(new Postcard(
                R.drawable.postcard_sunset,
                "夕阳西下",
                "余晖满校园",
                "香港大学日落"
        ));

        postcards.add(new Postcard(
                R.drawable.postcard_night,
                "忙碌的校园",
                "车来车往",
                "香港大学校车"
        ));
    }

    private void highlightCategory(int position) {
        // Reset all categories to normal size
        resetAllCategories();

        // Highlight the selected category
        CardView cardView = null;
        View categoryView = null;

        switch (position) {
            case 0:
                cardView = categoryCover1;
                categoryView = categoryLotus;
                break;
            case 1:
                cardView = categoryCover2;
                categoryView = categoryFlower;
                break;
            case 2:
                cardView = categoryCover3;
                categoryView = categoryScenery;
                break;
            case 3:
                cardView = categoryCover4;
                categoryView = categoryGate;
                break;
            case 4:
                cardView = categoryCover5;
                categoryView = categoryArchitecture;
                break;
            case 5:
                cardView = categoryCover6;
                categoryView = categoryLibrary;
                break;
        }

        if (cardView != null) {
            // Scale up the selected category
            cardView.animate().scaleX(1.2f).scaleY(1.2f).setDuration(300).start();
        }

        if (categoryView != null) {
            // Scroll to center the selected category
            final View finalCategoryView = categoryView;
            categoriesScrollView.post(() -> {
                int scrollX = (finalCategoryView.getLeft() + finalCategoryView.getRight()) / 2 - categoriesScrollView.getWidth() / 2;
                categoriesScrollView.smoothScrollTo(scrollX, 0);
            });
        }
    }

    private void resetAllCategories() {
        // Reset all category card views to normal size
        if (categoryCover1 != null) categoryCover1.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
        if (categoryCover2 != null) categoryCover2.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
        if (categoryCover3 != null) categoryCover3.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
        if (categoryCover4 != null) categoryCover4.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
        if (categoryCover5 != null) categoryCover5.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
        if (categoryCover6 != null) categoryCover6.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
    }

    private void initCategories() {
        // Set click listeners
        View.OnClickListener categoryClickListener = v -> {
            int position = 0;
            if (v == categoryLotus) {
                position = 0;
            } else if (v == categoryFlower) {
                position = 1;
            } else if (v == categoryScenery) {
                position = 2;
            } else if (v == categoryGate) {
                position = 3;
            } else if (v == categoryArchitecture) {
                position = 4;
            } else if (v == categoryLibrary) {
                position = 5;
            }

            // Animate to selected postcard
            postcardViewPager.setCurrentItem(position, true);
            // The highlighting will be handled by the page change callback
        };

        categoryLotus.setOnClickListener(categoryClickListener);
        categoryFlower.setOnClickListener(categoryClickListener);
        categoryScenery.setOnClickListener(categoryClickListener);
        categoryGate.setOnClickListener(categoryClickListener);
        categoryArchitecture.setOnClickListener(categoryClickListener);
        categoryLibrary.setOnClickListener(categoryClickListener);
    }

    // Postcard data class
    private static class Postcard {
        private int imageResId;
        private String title;
        private String description;
        private String location;

        public Postcard(int imageResId, String title, String description, String location) {
            this.imageResId = imageResId;
            this.title = title;
            this.description = description;
            this.location = location;
        }

        public int getImageResId() {
            return imageResId;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getLocation() {
            return location;
        }
    }

    // Adapter for postcards
    private class PostcardAdapter extends RecyclerView.Adapter<PostcardAdapter.PostcardViewHolder> {
        private List<Postcard> postcards;

        public PostcardAdapter(List<Postcard> postcards) {
            this.postcards = postcards;
        }

        @NonNull
        @Override
        public PostcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_postcard, parent, false);
            return new PostcardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PostcardViewHolder holder, int position) {
            Postcard postcard = postcards.get(position);
            holder.bind(postcard);
        }

        @Override
        public int getItemCount() {
            return postcards.size();
        }

        class PostcardViewHolder extends RecyclerView.ViewHolder {
            private ImageView postcardImage;
            private TextView postcardTitle;
            private TextView postcardDescription;
            private TextView postcardLocation;

            public PostcardViewHolder(@NonNull View itemView) {
                super(itemView);
                postcardImage = itemView.findViewById(R.id.postcardImage);
                postcardTitle = itemView.findViewById(R.id.postcardTitle);
                postcardDescription = itemView.findViewById(R.id.postcardDescription);
                postcardLocation = itemView.findViewById(R.id.postcardLocation);
            }

            public void bind(Postcard postcard) {
                postcardImage.setImageResource(postcard.getImageResId());
                postcardTitle.setText(postcard.getTitle());
                postcardDescription.setText(postcard.getDescription());
                postcardLocation.setText(postcard.getLocation());
            }
        }
    }

    // Page transformer for 3D effect
    private class DepthPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        @Override
        public void transformPage(@NonNull View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);
            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1f);
                view.setTranslationX(0f);
                view.setTranslationZ(0f);
                view.setScaleX(1f);
                view.setScaleY(1f);
            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);
                // Move it behind the left page
                view.setTranslationZ(-1f);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
        }
    }
}