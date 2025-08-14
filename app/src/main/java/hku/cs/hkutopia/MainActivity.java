package hku.cs.hkutopia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;

import hku.cs.hkutopia.model.CampusStory;
import hku.cs.hkutopia.utils.JsonUtils;
import hku.cs.hkutopia.utils.StoryManager;
import hku.cs.hkutopia.utils.UserSessionManager;

public class MainActivity extends AppCompatActivity {

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isMapNavigationStarting = false;
    private List<CampusStory> campusStories;
    private int currentStoryIndex = 0;
    private static final int STORIES_PER_PAGE = 4;
    private LinearLayout newsContainer;
    private List<CardView> displayedStoryCards = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set current date - using Chinese format
        TextView tvDate = findViewById(R.id.tvDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("M月d日", Locale.CHINESE);
        tvDate.setText(dateFormat.format(new Date()));

        // Set up campus tour card click
        CardView cardCampusTour = findViewById(R.id.cardCampusTour);
        cardCampusTour.setOnClickListener(v -> {
            android.util.Log.d("MainActivity", "Campus tour card clicked");
            startMapNavigation();
        });

        // Set up visit booking card click
        CardView cardVisitBooking = findViewById(R.id.cardVisitBooking);
        cardVisitBooking.setOnClickListener(v -> {
            android.util.Log.d("MainActivity", "Visit booking card clicked");
            startReservationActivity();
        });

        // Set up feature buttons
        setupFeatureButtons();

        // Set up bottom navigation
        setupBottomNavigation();

        // Initialize news container
        newsContainer = findViewById(R.id.newsContainer);

        // Load campus stories data
        loadCampusStories();

        // Set up campus stories
        setupCampusStories();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 重置标志，允许再次启动地图
        isMapNavigationStarting = false;

        // 确保底部导航栏在返回主页时选中"主页"选项
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
        }

        // Refresh stories when returning to the main activity
        refreshStories();
    }

    private void refreshStories() {
        // Reload stories
        loadCampusStories();

        // Clear current displayed stories
        newsContainer.removeAllViews();
        displayedStoryCards.clear();

        // Reset index
        currentStoryIndex = 0;

        // Load initial stories
        loadMoreStories();
    }

    private void loadCampusStories() {
        // 加载预定义的故事
        List<CampusStory> predefinedStories = JsonUtils.loadCampusStories(this);

        // 加载用户创建的故事
        List<CampusStory> userStories = StoryManager.getInstance(this).getUserStories();

        // 合并两个列表，用户故事在前
        campusStories = new ArrayList<>();
        campusStories.addAll(userStories);
        campusStories.addAll(predefinedStories);

        // 更新Latest标签逻辑 - 只有最近两天内发布的故事才显示Latest标签
        updateLatestTags();
    }

    // 添加新方法来更新Latest标签
    private void updateLatestTags() {
        if (campusStories == null || campusStories.isEmpty()) {
            return;
        }

        // 获取当前日期
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date currentDate = new Date();

        // 计算两天前的日期
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_YEAR, -2);
        Date twoDaysAgo = calendar.getTime();

        // 更新每个故事的Latest标签
        for (CampusStory story : campusStories) {
            try {
                // 解析故事日期
                Date storyDate = dateFormat.parse(story.getDate());

                // 如果故事日期在两天内，则设置为Latest
                if (storyDate != null && storyDate.after(twoDaysAgo)) {
                    story.setLatest(true);
                } else {
                    story.setLatest(false);
                }
            } catch (Exception e) {
                // 如果日期解析失败，默认不显示Latest
                story.setLatest(false);
            }
        }
    }

    private void setupFeatureButtons() {
        // Feature buttons
        View featureDaily = findViewById(R.id.featureDaily);
        if (featureDaily != null) {
            featureDaily.setOnClickListener(v -> {
                // Start the Campus Cultural Activity
                Intent intent = new Intent(MainActivity.this, CampusCulturalActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        findViewById(R.id.featureDigital).setOnClickListener(v -> {
            // Start the Postcard Activity instead of showing a toast
            Intent intent = new Intent(MainActivity.this, PostcardActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        findViewById(R.id.featureHistory).setOnClickListener(v -> {
            // 启动港大历史页面
            Intent intent = new Intent(MainActivity.this, CampusHistoryActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        findViewById(R.id.featureVirtual).setOnClickListener(v -> {
            // Start the Campus Community Activity
            Intent intent = new Intent(MainActivity.this, CampusCommunityActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_post) {
                // "发布" 按钮点击 - 启动发布页面
                startPostActivity();
                return true;
            } else if (itemId == R.id.navigation_home) {
                // Already on home
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // "我的" 按钮点击
                startProfileActivity();
                return true;
            }
            return false;
        });
        // 设置主页为默认选中
        bottomNavigation.setSelectedItemId(R.id.navigation_home);

    }

    private void startPostActivity() {
        android.util.Log.d("MainActivity", "Starting PostActivity");
        try {
            // 检查用户是否已登录
            UserSessionManager sessionManager = UserSessionManager.getInstance(this);
            if (!sessionManager.isLoggedIn()) {
                // 用户未登录，显示提示并重定向到登录页面
                Toast.makeText(this, "请先登录后再发布", Toast.LENGTH_SHORT).show();
                startProfileActivity();
                return;
            }

            // 用户已登录，正常启动发布页面
            Intent intent = new Intent(MainActivity.this, PostActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error starting PostActivity", e);
            Toast.makeText(this, "发布页面加载失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startReservationActivity() {
        android.util.Log.d("MainActivity", "Starting ReservationActivity");
        try {
            Intent intent = new Intent(MainActivity.this, ReservationActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error starting ReservationActivity", e);
            Toast.makeText(this, "预约页面加载失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupCampusStories() {
        // Set up View More button
        Button btnViewMore = findViewById(R.id.btnViewMore);
        if (btnViewMore != null) {
            btnViewMore.setOnClickListener(v -> {
                currentStoryIndex += STORIES_PER_PAGE;

                // If we've reached the end, loop back to the beginning
                if (currentStoryIndex >= campusStories.size()) {
                    currentStoryIndex = 0;
                    Toast.makeText(MainActivity.this, "已显示全部故事，返回开始", Toast.LENGTH_SHORT).show();
                    // Clear all displayed cards to start fresh from the beginning
                    newsContainer.removeAllViews();
                    displayedStoryCards.clear();
                }

                // Load and append the next batch of stories
                loadMoreStories();
            });
        }

        // Load initial stories
        loadMoreStories();
    }

    private void loadMoreStories() {
        if (campusStories == null || campusStories.isEmpty()) {
            return;
        }

        // Calculate how many stories to display (max 4 per page)
        int storiesCount = Math.min(STORIES_PER_PAGE, campusStories.size() - currentStoryIndex);

        if (storiesCount <= 0) {
            Toast.makeText(MainActivity.this, "没有更多故事了", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < storiesCount; i += 2) {
            // 创建一个水平布局容器来放置每对卡片
            LinearLayout rowContainer = new LinearLayout(this);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, 16); // 设置行之间的垂直间距为16dp
            rowContainer.setLayoutParams(rowParams);
            rowContainer.setOrientation(LinearLayout.HORIZONTAL);
            rowContainer.setPadding(8, 0, 8, 0); // 左右内边距，确保与父容器有间距

            // 添加第一个卡片
            final int storyIndex1 = currentStoryIndex + i;
            CardView storyCard1 = createStoryCard(storyIndex1);

            // 设置卡片的布局参数，确保等宽且有固定间距
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    0, // 宽度为0，权重为1，确保等宽
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            cardParams.weight = 1; // 等宽权重
            cardParams.setMargins(0, 0, 8, 0); // 右边距8dp
            storyCard1.setLayoutParams(cardParams);
            rowContainer.addView(storyCard1);
            displayedStoryCards.add(storyCard1);

            // 如果还有第二个卡片，也添加到这一行
            if (i + 1 < storiesCount) {
                final int storyIndex2 = currentStoryIndex + i + 1;
                CardView storyCard2 = createStoryCard(storyIndex2);

                LinearLayout.LayoutParams cardParams2 = new LinearLayout.LayoutParams(
                        0, // 宽度为0，权重为1，确保等宽
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                cardParams2.weight = 1; // 等宽权重
                cardParams2.setMargins(8, 0, 0, 0); // 左边距8dp
                storyCard2.setLayoutParams(cardParams2);
                rowContainer.addView(storyCard2);
                displayedStoryCards.add(storyCard2);
            } else {
                // 如果只有一个卡片，添加一个空的占位视图保持布局平衡
                View placeholderView = new View(this);
                LinearLayout.LayoutParams placeholderParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                placeholderParams.weight = 1;
                placeholderParams.setMargins(8, 0, 0, 0);
                placeholderView.setLayoutParams(placeholderParams);
                rowContainer.addView(placeholderView);
            }

            // 将行容器添加到主容器
            newsContainer.addView(rowContainer);
        }
    }

    private CardView createStoryCard(final int storyIndex) {
        CampusStory story = campusStories.get(storyIndex);

        // Inflate the card layout
        View cardView = getLayoutInflater().inflate(R.layout.item_campus_story, null);
        CardView storyCard = (CardView) cardView;

        // Find views within the card
        ImageView imageView = storyCard.findViewById(R.id.storyImage);
        TextView titleView = storyCard.findViewById(R.id.storyTitle);
        TextView authorView = storyCard.findViewById(R.id.storyAuthor);
        TextView latestTag = storyCard.findViewById(R.id.latestTag);

        // Set content
        titleView.setText(story.getTitle());
        authorView.setText(story.getAuthor());

        // Set image
        if (story.getImageUrls() != null && !story.getImageUrls().isEmpty()) {
            int imageResId = getResources().getIdentifier(
                    story.getImageUrls().get(0), "drawable", getPackageName());
            if (imageResId != 0) {
                imageView.setImageResource(imageResId);
            } else {
                imageView.setImageResource(R.drawable.news_image1); // Default image
            }
        } else {
            imageView.setImageResource(R.drawable.news_image1); // Default image
        }


        // Show/hide latest tag
        latestTag.setVisibility(story.isLatest() ? View.VISIBLE : View.GONE);

        // Set click listener
        storyCard.setOnClickListener(v -> openStoryDetail(storyIndex));

        return storyCard;
    }

    private void startProfileActivity() {
        android.util.Log.d("MainActivity", "Starting ProfileActivity");
        try {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error starting ProfileActivity", e);
            Toast.makeText(this, "个人信息页面加载失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openStoryDetail(int storyIndex) {
        if (campusStories != null && storyIndex < campusStories.size()) {
            Intent intent = new Intent(MainActivity.this, CampusStoryDetailActivity.class);
            intent.putExtra("story", campusStories.get(storyIndex));
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            Toast.makeText(this, "故事数据加载失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void startMapNavigation() {
        // 防止重复点击导致多次启动
        if (isMapNavigationStarting) {
            return;
        }

        isMapNavigationStarting = true;
        android.util.Log.d("MainActivity", "Starting MapNavigationActivity");

        // 使用延迟启动，让UI线程有时间处理当前事件
        mainHandler.postDelayed(() -> {
            try {
                // 直接启动地图活动
                Intent intent = new Intent(MainActivity.this, MapNavigationActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Error starting MapNavigationActivity", e);
                Toast.makeText(MainActivity.this, "地图加载失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                isMapNavigationStarting = false;
            }
        }, 100); // 短暂延迟，让UI线程有时间响应
    }
}