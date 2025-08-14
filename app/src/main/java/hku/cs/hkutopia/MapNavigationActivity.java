package hku.cs.hkutopia;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.widget.HorizontalScrollView;

public class MapNavigationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private CustomMapView customMapView;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private List<CampusLocation> campusLocations = new ArrayList<>();
    private Map<String, Object> markersMap = new HashMap<>();
    private String currentCategory = "全部";
    private String currentSubcategory = "";
    private CardView locationDetailPanel;
    private Animation slideUpAnimation;
    private Animation slideDownAnimation;
    private boolean isDetailPanelVisible = false;
    private CampusLocation selectedLocation;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "HkuCampusTourPrefs";
    private static final String PREF_CHECKINS = "checkins";

    // 创建一个线程池用于后台任务
    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // HKU campus bounds (approximate)
    private static final LatLng HKU_SOUTHWEST = new LatLng(22.281, 114.135);
    private static final LatLng HKU_NORTHEAST = new LatLng(22.285, 114.140);
    private static final LatLngBounds HKU_BOUNDS = new LatLngBounds(HKU_SOUTHWEST, HKU_NORTHEAST);
    private static final LatLng HKU_CENTER = new LatLng(
            (HKU_SOUTHWEST.latitude + HKU_NORTHEAST.latitude) / 2,
            (HKU_SOUTHWEST.longitude + HKU_NORTHEAST.longitude) / 2);

    // Category buttons
    private TextView categoryAll;
    private TextView categoryHistorical;
    private TextView categoryAcademic;
    private TextView categoryFacilities;
    private TextView categoryRestaurants;
    private TextView categoryGates;

    // Map coordinates for our custom map
    private static final Map<String, float[]> LOCATION_COORDINATES = new HashMap<>();

    static {
        // Initialize map coordinates for each location based on the provided map image
        // These are approximate positions on the custom map (x, y values from 0-1000)
        LOCATION_COORDINATES.put("庄月明楼", new float[]{200, 350});
        LOCATION_COORDINATES.put("校园访客中心", new float[]{850, 850});
        LOCATION_COORDINATES.put("百周年校庆校园", new float[]{850, 600});
        LOCATION_COORDINATES.put("地质博物馆", new float[]{480, 450});
        LOCATION_COORDINATES.put("美术博物馆", new float[]{150, 800});
        LOCATION_COORDINATES.put("本部大楼", new float[]{400, 850});
        LOCATION_COORDINATES.put("学生会餐厅", new float[]{550, 500});
        LOCATION_COORDINATES.put("美心餐厅", new float[]{200, 400});
        LOCATION_COORDINATES.put("太古堂学生餐厅", new float[]{100, 700});
        LOCATION_COORDINATES.put("港大新校门", new float[]{250, 900});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("MapNavigationActivity", "onCreate called");

        // 设置内容视图
        setContentView(R.layout.activity_map_navigation);

        // 在 initializeBasicUI(); 之前添加
        // 设置窗口标志以优化图形处理
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        // 初始化共享偏好设置
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // 初始化基本UI组件
        initializeBasicUI();

        // 初始化校园位置数据
        initializeCampusLocations();

        // 初始化自定义地图视图
        initializeCustomMapView();

        // 初始化Google地图
        initializeGoogleMap();

        // 设置类别过滤器
        setupCategoryFilters();
        // 添加所有标记 - 确保在初始化时显示所有标记
        addAllMarkers();
    }

    private void initializeBasicUI() {
        try {
            // 初始化动画
            slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            slideUpAnimation.setDuration(250);
            slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
            slideDownAnimation.setDuration(250);

            // 初始化面板
            locationDetailPanel = findViewById(R.id.locationDetailPanel);

            // 设置返回按钮
            ImageButton btnBack = findViewById(R.id.btnBack);
            btnBack.setOnClickListener(v -> {
                finish(); // 直接结束当前活动，返回主页
            });

            // 设置详情面板按钮
            setupDetailPanelButtons();

            // 添加重置视图按钮
            FloatingActionButton resetViewButton = findViewById(R.id.resetViewButton);
            resetViewButton.setOnClickListener(v -> {
                if (customMapView != null) {
                    // 添加按钮动画效果
                    ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(resetViewButton, "scaleX", 1f, 0.8f, 1f);
                    ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(resetViewButton, "scaleY", 1f, 0.8f, 1f);
                    AnimatorSet scaleDown = new AnimatorSet();
                    scaleDown.play(scaleDownX).with(scaleDownY);
                    scaleDown.setDuration(200);
                    scaleDown.start();

                    // 重置视图
                    customMapView.resetView();

                    // 添加轻微的振动效果
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        resetViewButton.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK);
                    }
                }
            });
        } catch (Exception e) {
            android.util.Log.e("MapNavigationActivity", "Error in initializeBasicUI", e);
        }
    }

    private void initializeCustomMapView() {
        try {
            customMapView = findViewById(R.id.customMapView);
            if (customMapView != null) {
                // 设置标记点击监听器
                customMapView.setOnMarkerClickListener(marker -> {
                    // 使用延迟处理以减轻主线程负担
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (marker != null && marker.getTag() instanceof CampusLocation) {
                            selectedLocation = (CampusLocation) marker.getTag();
                            showLocationDetailPanel(selectedLocation);
                        }
                    }, 50);
                });

                // 添加地图点击监听器以隐藏详情面板
                customMapView.setOnMapClickListener(() -> {
                    if (isDetailPanelVisible) {
                        hideLocationDetailPanel();
                    }
                });
                // 添加初始动画效果
                new Handler().postDelayed(() -> {
                    if (customMapView != null) {
                        // 应用轻微的透视效果，然后恢复
                        customMapView.applyPerspectiveEffect(10f, 5f);
                        customMapView.invalidate();

                        new Handler().postDelayed(() -> {
                            if (customMapView != null) {
                                customMapView.applyPerspectiveEffect(5f, 0f);
                                customMapView.invalidate();
                            }
                        }, 800);
                    }
                }, 300);
            }
        } catch (Exception e) {
            android.util.Log.e("MapNavigationActivity", "Error in initializeCustomMapView", e);
        }
    }

    private void initializeGoogleMap() {
        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                android.util.Log.d("MapNavigationActivity", "Getting map async");
                mapFragment.getMapAsync(this);
            }
        } catch (Exception e) {
            android.util.Log.e("MapNavigationActivity", "Error in initializeGoogleMap", e);
        }
    }

    private void setupDetailPanelButtons() {
        try {
            LinearLayout btnNavigate = findViewById(R.id.btnNavigate);
            btnNavigate.setOnClickListener(v -> {
                if (selectedLocation != null) {
                    startNavigation(selectedLocation);
                }
            });

            LinearLayout btnCheckIn = findViewById(R.id.btnCheckIn);
            btnCheckIn.setOnClickListener(v -> {
                if (selectedLocation != null) {
                    performCheckIn(selectedLocation);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("MapNavigationActivity", "Error in setupDetailPanelButtons", e);
        }
    }

    private void performCheckIn(CampusLocation location) {
        try {
            // 获取当前签到
            String checkIns = sharedPreferences.getString(PREF_CHECKINS, "");

            // 检查是否已经签到
            if (checkIns.contains(location.getName())) {
                Toast.makeText(this, "您已经在" + location.getName() + "打卡过了", Toast.LENGTH_SHORT).show();
                return;
            }

            // 添加新签到
            if (!checkIns.isEmpty()) {
                checkIns += ",";
            }
            checkIns += location.getName();

            // 保存到偏好设置
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PREF_CHECKINS, checkIns);
            editor.apply();

            // 更新UI以显示签到状态
            location.setCheckedIn(true);
            updateCheckInButton(true);

            // 添加成功动画
            LinearLayout btnCheckIn = findViewById(R.id.btnCheckIn);
            if (btnCheckIn != null) {
                AnimatorSet animatorSet = new AnimatorSet();
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(btnCheckIn, "scaleX", 1f, 1.2f, 1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(btnCheckIn, "scaleY", 1f, 1.2f, 1f);
                animatorSet.playTogether(scaleX, scaleY);
                animatorSet.setDuration(300);
                animatorSet.start();
            }

            Toast.makeText(this, "成功在" + location.getName() + "打卡！", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            android.util.Log.e("MapNavigationActivity", "Error in performCheckIn", e);
            Toast.makeText(this, "打卡失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCheckInButton(boolean isCheckedIn) {
        try {
            ImageView imgCheckIn = findViewById(R.id.imgCheckIn);
            TextView txtCheckIn = findViewById(R.id.txtCheckIn);
            LinearLayout btnCheckIn = findViewById(R.id.btnCheckIn);

            if (isCheckedIn) {
                imgCheckIn.setImageResource(R.drawable.ic_check_in_done);
                imgCheckIn.setColorFilter(ContextCompat.getColor(this, R.color.white));
                txtCheckIn.setText("已打卡！");
                txtCheckIn.setTextColor(ContextCompat.getColor(this, R.color.white));
                btnCheckIn.setBackgroundColor(ContextCompat.getColor(this, R.color.hku_red));
            } else {
                imgCheckIn.setImageResource(R.drawable.ic_check_in);
                imgCheckIn.setColorFilter(ContextCompat.getColor(this, R.color.black));
                txtCheckIn.setText("打卡");
                txtCheckIn.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                btnCheckIn.setBackgroundColor(Color.TRANSPARENT);
            }
        } catch (Exception e) {
            android.util.Log.e("MapNavigationActivity", "Error in updateCheckInButton", e);
        }
    }

    private void startNavigation(CampusLocation location) {
        try {
            // 创建一个Intent来启动Google Maps
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" +
                    location.getPosition().latitude + "," +
                    location.getPosition().longitude + "&mode=w");

            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // 检查是否安装了Google Maps
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                // 使用startActivityForResult而不是startActivity
                startActivityForResult(mapIntent, 1001);
            } else {
                // 如果未安装Google Maps，则在浏览器中打开
                Uri browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" +
                        location.getPosition().latitude + "," +
                        location.getPosition().longitude);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                startActivityForResult(browserIntent, 1001);
            }
        } catch (Exception e) {
            android.util.Log.e("MapNavigationActivity", "Error in startNavigation", e);
            Toast.makeText(this, "导航失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 添加onActivityResult方法来处理返回
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            // 从Google Maps返回
            Toast.makeText(this, "已返回校园地图", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupCategoryFilters() {
        try {
            categoryAll = findViewById(R.id.categoryAll);
            categoryHistorical = findViewById(R.id.categoryHistorical);
            categoryAcademic = findViewById(R.id.categoryAcademic);
            categoryFacilities = findViewById(R.id.categoryFacilities);
            categoryRestaurants = findViewById(R.id.categoryRestaurants);
            categoryGates = findViewById(R.id.categoryGates);

            View.OnClickListener categoryClickListener = v -> {
                // 重置所有类别
                categoryAll.setBackground(ContextCompat.getDrawable(this, R.drawable.futuristic_category_bg));
                categoryAll.setTextColor(ContextCompat.getColor(this, R.color.white));

                categoryHistorical.setBackground(ContextCompat.getDrawable(this, R.drawable.futuristic_category_bg));
                categoryHistorical.setTextColor(ContextCompat.getColor(this, R.color.white));

                categoryAcademic.setBackground(ContextCompat.getDrawable(this, R.drawable.futuristic_category_bg));
                categoryAcademic.setTextColor(ContextCompat.getColor(this, R.color.white));

                categoryFacilities.setBackground(ContextCompat.getDrawable(this, R.drawable.futuristic_category_bg));
                categoryFacilities.setTextColor(ContextCompat.getColor(this, R.color.white));

                categoryRestaurants.setBackground(ContextCompat.getDrawable(this, R.drawable.futuristic_category_bg));
                categoryRestaurants.setTextColor(ContextCompat.getColor(this, R.color.white));

                categoryGates.setBackground(ContextCompat.getDrawable(this, R.drawable.futuristic_category_bg));
                categoryGates.setTextColor(ContextCompat.getColor(this, R.color.white));

                // 设置选定的类别
                TextView selectedCategory = (TextView) v;
                selectedCategory.setBackground(ContextCompat.getDrawable(this, R.drawable.futuristic_category_selected_bg));
                selectedCategory.setTextColor(ContextCompat.getColor(this, R.color.white));

                // 添加选择动画
                AnimatorSet animatorSet = new AnimatorSet();
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(selectedCategory, "scaleX", 1f, 1.1f, 1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(selectedCategory, "scaleY", 1f, 1.1f, 1f);
                animatorSet.playTogether(scaleX, scaleY);
                animatorSet.setDuration(200);
                animatorSet.start();

                // 更新当前类别并过滤标记
                String selectedText = selectedCategory.getText().toString();
                if (selectedText.equals("全部")) {
                    currentCategory = "全部";
                    currentSubcategory = "";
                } else if (selectedText.equals("历史建筑")) {
                    currentCategory = "Historical";
                    currentSubcategory = "";
                } else if (selectedText.equals("教学建筑")) {
                    currentCategory = "Academic";
                    currentSubcategory = "";
                } else if (selectedText.equals("校园设施")) {
                    currentCategory = "Facilities";
                    currentSubcategory = "";
                } else if (selectedText.equals("餐厅")) {
                    currentCategory = "Facilities";
                    currentSubcategory = "Restaurant";
                } else if (selectedText.equals("校门")) {
                    currentCategory = "Gates";
                    currentSubcategory = "";
                }

                // 在后台线程中执行过滤操作
                backgroundExecutor.execute(() -> {
                    // 在主线程中更新UI
                    mainHandler.post(() -> filterMarkersByCategory());
                });
            };

            categoryAll.setOnClickListener(categoryClickListener);
            categoryHistorical.setOnClickListener(categoryClickListener);
            categoryAcademic.setOnClickListener(categoryClickListener);
            categoryFacilities.setOnClickListener(categoryClickListener);
            categoryRestaurants.setOnClickListener(categoryClickListener);
            categoryGates.setOnClickListener(categoryClickListener);
            // 默认选中"全部"类别
            categoryAll.setBackground(ContextCompat.getDrawable(this, R.drawable.futuristic_category_selected_bg));
        } catch (Exception e) {
            android.util.Log.e("MapNavigationActivity", "Error in setupCategoryFilters", e);
        }
    }

    // 添加新方法，确保在初始化时显示所有标记
    private void addAllMarkers() {
        try {
            // 清除现有标记
            if (customMapView != null) {
                customMapView.clearMarkers();
            }

            if (mMap != null) {
                mMap.clear();
            }

            markersMap.clear();

            // 添加所有位置的标记
            for (CampusLocation location : campusLocations) {
                addMarkerForLocation(location);
            }
        } catch (Exception e) {
            android.util.Log.e("MapNavigationActivity", "Error in addAllMarkers", e);
        }
    }


    private void filterMarkersByCategory() {
        try {
            // 清除自定义地图中的所有标记
            if (customMapView != null) {
                customMapView.clearMarkers();
            }

            // 如果可用，清除Google地图标记
            if (mMap != null) {
                mMap.clear();
            }

            markersMap.clear();

            // 根据选定的类别添加标记
            for (CampusLocation location : campusLocations) {
                boolean shouldShow = false;

                if (currentCategory.equals("全部")) {
                    shouldShow = true;
                } else if (currentSubcategory.isEmpty()) {
                    shouldShow = location.getCategory().equals(currentCategory);
                } else {
                    shouldShow = location.getCategory().equals(currentCategory) &&
                            location.getSubcategory().equals(currentSubcategory);
                }

                if (shouldShow) {
                    addMarkerForLocation(location);
                }
            }

            // 如果可见，隐藏详情面板
            if (isDetailPanelVisible) {
                hideLocationDetailPanel();
            }
        } catch (Exception e) {
            android.util.Log.e("MapNavigationActivity", "Error in filterMarkersByCategory", e);
        }
    }

    private void initializeCampusLocations() {
        try {
            // 更新基于需求的位置
            campusLocations.add(new CampusLocation(
                    "庄月明楼",
                    "庄月明楼",
                    "Academic",
                    "",
                    new LatLng(22.279979, 114.144419),
                    "庄月明楼是香港大学的主要教学楼之一，位于校园中心地带，设有多个讲堂和教室。",
                    R.drawable.ic_guide,
                    R.color.hku_red,
                    R.drawable.img_main_building,
                    "350米"
            ));

            campusLocations.add(new CampusLocation(
                    "校园访客中心",
                    "校园访客中心",
                    "Facilities",
                    "",
                    new LatLng(22.28116, 114.139329),
                    "校园访客中心为来访者提供校园导览和信息咨询服务，是了解香港大学历史和校园布局的理想起点。",
                    R.drawable.ic_guide,
                    R.color.hku_red,
                    R.drawable.img_main_building,
                    "400米"
            ));

            campusLocations.add(new CampusLocation(
                    "百周年校庆校园",
                    "百周年校庆校园",
                    "Historical",
                    "",
                    new LatLng(22.280656, 114.139341),
                    "百周年校庆校园是为纪念香港大学建校100周年而建造的现代化综合设施，包括学习共享空间和多功能厅。",
                    R.drawable.ic_guide,
                    R.color.hku_red,
                    R.drawable.img_main_building,
                    "300米"
            ));

            campusLocations.add(new CampusLocation(
                    "地质博物馆",
                    "地质博物馆",
                    "Facilities",
                    "Museum",
                    new LatLng(22.279821, 114.142519),
                    "香港大学地质博物馆收藏了丰富的岩石、矿物和化石标本，是了解地科学的重要教育资源。",
                    R.drawable.ic_guide,
                    R.color.hku_red,
                    R.drawable.img_main_building,
                    "450米"
            ));

            campusLocations.add(new CampusLocation(
                    "美术博物馆",
                    "美术博物馆",
                    "Facilities",
                    "Museum",
                    new LatLng(22.281301, 114.144402),
                    "香港大学美术馆定期举办各类艺术展览，展示中西方艺术作品，促进校园文化艺术交流。",
                    R.drawable.ic_guide,
                    R.color.hku_red,
                    R.drawable.img_main_building,
                    "400米"
            ));

            campusLocations.add(new CampusLocation(
                    "本部大楼",
                    "本部大楼",
                    "Historical",
                    "",
                    new LatLng(22.283721, 114.137403),
                    "港大主楼是校园内最古老的建筑，建于1912年。它采用爱德华巴洛克式建筑风格，是香港的法定古迹。",
                    R.drawable.ic_guide,
                    R.color.hku_red,
                    R.drawable.img_main_building,
                    "500米"
            ));

            campusLocations.add(new CampusLocation(
                    "学生会餐厅",
                    "学生会餐厅",
                    "Facilities",
                    "Restaurant",
                    new LatLng(22.280295, 114.141697),
                    "学生会餐厅提供多种平价美食，是学生们用餐和社交的热门场所。",
                    R.drawable.ic_dining,
                    R.color.marker_blue,
                    R.drawable.img_main_building,
                    "350米"
            ));

            campusLocations.add(new CampusLocation(
                    "美心餐厅",
                    "美心餐厅",
                    "Facilities",
                    "Restaurant",
                    new LatLng(22.280072, 114.144494),
                    "美心餐厅提供多样化的餐饮选择，包括中式、西式和国际美食，满足不同口味需求。",
                    R.drawable.ic_dining,
                    R.color.marker_blue,
                    R.drawable.img_main_building,
                    "300米"
            ));

            campusLocations.add(new CampusLocation(
                    "太古堂学生餐厅",
                    "太古堂学生餐厅",
                    "Facilities",
                    "Restaurant",
                    new LatLng(22.281038, 114.144595),
                    "太古食堂提供多样化的餐饮选择，环境舒适，是学生和教职员工用餐的理想场所。",
                    R.drawable.ic_dining,
                    R.color.marker_blue,
                    R.drawable.img_main_building,
                    "320米"
            ));

            campusLocations.add(new CampusLocation(
                    "港大新校门",
                    "港大新校门",
                    "Gates",
                    "",
                    new LatLng(22.281483, 114.144611),
                    "香港大学正门是校园的标志性入口，连接校园与薄扶林道，体现了大学的历史传统。",
                    R.drawable.ic_guide,
                    R.color.hku_red,
                    R.drawable.img_main_building,
                    "600米"
            ));

            // 加载签到状态
            String checkIns = sharedPreferences.getString(PREF_CHECKINS, "");
            for (CampusLocation location : campusLocations) {
                if (checkIns.contains(location.getName())) {
                    location.setCheckedIn(true);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MapNavigationActivity", "Error in initializeCampusLocations", e);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            mMap = googleMap;

            // 自定义地图样式
            try {
                boolean success = mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
                if (!success) {
                    android.util.Log.e("MapNavigationActivity", "Style parsing failed");
                }
            } catch (Exception e) {
                android.util.Log.e("MapNavigationActivity", "Error setting map style", e);
            }

            // 设置地图UI设置
            mMap.getUiSettings().setZoomControlsEnabled(false);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.getUiSettings().setRotateGesturesEnabled(true);
            mMap.getUiSettings().setTiltGesturesEnabled(true);

            // 将香港大学校园设置为初始位置并将相机限制在香港大学范围内
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HKU_CENTER, 17));
            mMap.setLatLngBoundsForCameraTarget(HKU_BOUNDS);
            mMap.setMinZoomPreference(16);
            mMap.setMaxZoomPreference(19);

            // 设置标记点击监听器
            mMap.setOnMarkerClickListener(marker -> {
                if (marker.getTag() instanceof CampusLocation) {
                    selectedLocation = (CampusLocation) marker.getTag();
                    showLocationDetailPanel(selectedLocation);
                }
                return true;
            });

            // 设置地图点击监听器以隐藏详情面板
            mMap.setOnMapClickListener(latLng -> {
                if (isDetailPanelVisible) {
                    hideLocationDetailPanel();
                }
            });

            // 如果已授予权限，则启用我的位置
            enableMyLocation();

            // 确保在地图准备好后添加所有标记
            addAllMarkers();
        } catch (Exception e) {
            android.util.Log.e("MapNavigationActivity", "Error in onMapReady", e);
            Toast.makeText(this, "地图初始化错误: " + e.getMessage(), Toast.LENGTH_LONG).show();
            handleMapInitializationFailure();
        }
    }

    private void addMarkerForLocation(CampusLocation location) {
        try {
            // 创建自定义标记图标 - 移除黑色背景
            View markerView = LayoutInflater.from(this).inflate(R.layout.custom_marker, null);
            ImageView markerIcon = markerView.findViewById(R.id.markerIcon);
            TextView markerTitle = markerView.findViewById(R.id.markerTitle);

            // 设置标记属性
            markerIcon.setImageResource(location.getIconRes());
            markerIcon.setColorFilter(ContextCompat.getColor(this, location.getMarkerBgColorRes()));
            markerTitle.setText(location.getChineseName());

            // 在创建位图之前添加以下代码
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            markerView.measure(widthMeasureSpec, heightMeasureSpec);
            markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());

            // 修改位图创建代码
            Bitmap markerBitmap = Bitmap.createBitmap(
                    markerView.getMeasuredWidth(),
                    markerView.getMeasuredHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(markerBitmap);
            markerView.draw(canvas);

            // 将标记添加到自定义地图视图，使用更精确的坐标计算
            if (customMapView != null) {
                float[] coordinates = LOCATION_COORDINATES.get(location.getName());
                if (coordinates != null) {
                    // 确保坐标准确性
                    MapMarker mapMarker = new MapMarker(coordinates[0], coordinates[1], markerBitmap, location);
                    customMapView.addMarker(mapMarker);
                    markersMap.put(location.getName(), mapMarker);
                }
            }

            // 将标记添加到Google地图以保持兼容性
            if (mMap != null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(location.getPosition())
                        .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                        .anchor(0.5f, 1.0f);  // 调整锚点以确保位置准确

                Marker marker = mMap.addMarker(markerOptions);
                if (marker != null) {
                    marker.setTag(location);
                    markersMap.put(location.getName() + "_google", marker);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MapNavigationActivity", "Error in addMarkerForLocation", e);
        }
    }

    private void showLocationDetailPanel(CampusLocation location) {
        try {
            // 更新详情面板内容
            TextView tvLocationName = findViewById(R.id.tvLocationName);
            TextView tvLocationDistance = findViewById(R.id.tvLocationDistance);
            TextView tvLocationDescription = findViewById(R.id.tvLocationDescription);

            tvLocationName.setText(location.getName());
            tvLocationDistance.setText("距离: " + location.getDistance());
            tvLocationDescription.setText(location.getDescription());

            // 更新签到按钮状态
            updateCheckInButton(location.isCheckedIn());

            // 如果尚未可见，则使用动画显示面板
            if (!isDetailPanelVisible) {
                locationDetailPanel.setVisibility(View.VISIBLE);
                locationDetailPanel.startAnimation(slideUpAnimation);
                isDetailPanelVisible = true;
            }
        } catch (Exception e) {
            android.util.Log.e("MapNavigationActivity", "Error in showLocationDetailPanel", e);
        }
    }

    private void hideLocationDetailPanel() {
        try {
            locationDetailPanel.startAnimation(slideDownAnimation);
            locationDetailPanel.setVisibility(View.GONE);
            isDetailPanelVisible = false;
        } catch (Exception e) {
            android.util.Log.e("MapNavigationActivity", "Error in hideLocationDetailPanel", e);
        }
    }

    private void enableMyLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } catch (Exception e) {
            android.util.Log.e("MapNavigationActivity", "Error in enableMyLocation", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "位置权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish(); // 直接返回主页，不再处理详情面板
    }

    private void handleMapInitializationFailure() {
        try {
            // 向用户显示消息
            Toast.makeText(this, "地图初始化失败，请稍后再试。", Toast.LENGTH_LONG).show();

            // 提供没有地图的基本UI
            findViewById(R.id.mapContainer).setVisibility(View.GONE);

            // 添加重试按钮
            FloatingActionButton fabRetry = findViewById(R.id.resetViewButton);
            fabRetry.setImageResource(android.R.drawable.ic_menu_rotate);
            fabRetry.setOnClickListener(v -> {
                // 重新启动活动
                recreate();
            });
        } catch (Exception e) {
            android.util.Log.e("MapNavigationActivity", "Error in handleMapInitializationFailure", e);
            finish(); // 如果处理失败，关闭活动
        }
    }

    @Override
    protected void onDestroy() {
        // 释放资源
        if (customMapView != null) {
            customMapView.clearMarkers();
        }
        markersMap.clear();
        campusLocations.clear();

        // 解除引用
        mMap = null;
        customMapView = null;

        super.onDestroy();
    }
}