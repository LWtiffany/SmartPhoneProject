package hku.cs.hkutopia;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import androidx.core.content.res.ResourcesCompat;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hku.cs.hkutopia.model.CampusStory;
import hku.cs.hkutopia.utils.StoryManager;
import hku.cs.hkutopia.utils.UserSessionManager;
import android.content.SharedPreferences;
import android.app.Dialog;
import android.view.WindowManager;
import android.graphics.BitmapFactory;
import java.io.InputStream;
import android.os.Build;

public class PostActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_STORAGE_PERMISSION = 1002;

    private EditText etTitle;
    private EditText etContent;
    private CardView cardAddImage;
    private CardView cardImagePreview;
    private ImageView imgPreview;
    private TextView tvImagePreviewText;
    private Button btnPublish;
    private Button btnPreview;
    private SwitchCompat switchSaveToAlbum;

    private Uri selectedImageUri;
    private List<String> selectedTags = new ArrayList<>();
    private String selectedLocation = "";
    private boolean isPreviewMode = false;
    private CampusStory previewStory = null;

    // Tag views
    // Location views

    //private View locationTagsContainer;
    private List<String> availableTags = new ArrayList<>();
    private List<String> availableLocations = new ArrayList<>();
    private LinearLayout tagsContainer;
    private LinearLayout locationsContainer;

    private boolean needLoginCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add this at the beginning of onCreate() after setContentView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false);
            setTurnScreenOn(false);
        }
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );

        setContentView(R.layout.activity_post);

        // 检查用户是否已登录
        UserSessionManager sessionManager = UserSessionManager.getInstance(this);
        if (!sessionManager.isLoggedIn()) {
            // 用户未登录，显示提示并重定向到登录页面
            Toast.makeText(this, "请先登录后再发布", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            // 设置标志，在onResume中检查登录状态
            finish();
            return;
        }

        initializeViews();
        setupListeners();
        initializeTagsAndLocations();
    }

    private void initializeViews() {
        // Initialize EditTexts
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);

        // Initialize image related views
        cardAddImage = findViewById(R.id.cardAddImage);
        cardImagePreview = findViewById(R.id.cardImagePreview);
        imgPreview = findViewById(R.id.imgPreview);
        tvImagePreviewText = findViewById(R.id.tvImagePreviewText);

        // Initialize buttons
        btnPublish = findViewById(R.id.btnPublish);
        btnPreview = findViewById(R.id.btnPreview);

        //locationTagsContainer = findViewById(R.id.locationTagsContainer);
        tagsContainer = findViewById(R.id.tagsContainer);
        locationsContainer = findViewById(R.id.locationsContainer);

        // Set up toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // 修改这两行，确保支持中文输入
        int inputType = android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;
        etTitle.setInputType(inputType);
        etContent.setInputType(inputType);

        // 设置IME选项，优化输入体验
        etTitle.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        etContent.setImeOptions(android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        // Add this at the end of initializeViews() method
        // Optimize horizontal scrolling performance
        HorizontalScrollView tagsScrollView = (HorizontalScrollView) tagsContainer.getParent();
        tagsScrollView.setHorizontalScrollBarEnabled(false);
        tagsScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        HorizontalScrollView locationsScrollView = (HorizontalScrollView) locationsContainer.getParent();
        locationsScrollView.setHorizontalScrollBarEnabled(false);
        locationsScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    private void setupListeners() {
        // Set up image selection
        cardAddImage.setOnClickListener(v -> {
            // 检查存储权限
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // 请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            } else {
                // 已有权限，打开图片选择器
                openImagePicker();
            }
        });

        // Set up tag selection
        setupTagListeners();

        // Set up location selection
        setupLocationListeners();

        // Set up publish button
        btnPublish.setOnClickListener(v -> publishPost());

        // Set up preview button
        btnPreview.setOnClickListener(v -> {
            if (isPreviewMode) {
                // Exit preview mode
                exitPreviewMode();
            } else {
                // Enter preview mode
                enterPreviewMode();
            }
        });

        // Set up location section
        LinearLayout locationSection = findViewById(R.id.locationSection);
        locationSection.setOnClickListener(v -> {
            // Show location selection dialog or expand location options
            toggleLocationOptions();
        });

        // Set up privacy section
        LinearLayout privacySection = findViewById(R.id.privacySection);
        privacySection.setOnClickListener(v -> {
            // Show privacy options dialog
            showPrivacyOptionsDialog();
        });
    }

    private void initializeTagsAndLocations() {
        // 初始化可用标签
        availableTags.add("#开发者选项");
        availableTags.add("#软件设计与开发");
        availableTags.add("#开发");
        availableTags.add("#本科生毕设");
        availableTags.add("#港大生活");
        availableTags.add("#校园风光");
        availableTags.add("#学术研究");
        availableTags.add("#留学生活");
        availableTags.add("#校园活动");
        availableTags.add("#美食分享");
        availableTags.add("#学习心得");
        availableTags.add("#考试季");

        // 初始化可用地点
        availableLocations.add("香港大学");
        availableLocations.add("香港电车叮叮车");
        availableLocations.add("中西区海滨长廊");
        availableLocations.add("新兴食家");
        availableLocations.add("港大主楼");
        availableLocations.add("百周年校园");
        availableLocations.add("黄克竞楼");
        availableLocations.add("香港大学图书馆");
        availableLocations.add("港大学生会");
        availableLocations.add("港大医学院");

        // 生成标签和地点UI
        generateTagsUI();
        generateLocationsUI();
    }

    private void setupTagListeners() {
        // 现在使用动态生成的标签，不需要单独设置监听器
    }

    private void setupLocationListeners() {
        // 现在使用动态生成的地点，不需要单独设置监听器
    }

    // 添加生成标签UI的方法
    private void generateTagsUI() {
        // 清除现有标签
        tagsContainer.removeAllViews();

        // 为每个标签创建视图
        for (String tag : availableTags) {
            TextView tagView = createTagView(tag);
            tagView.setOnClickListener(v -> {
                if (selectedTags.contains(tag)) {
                    // 取消选择标签
                    selectedTags.remove(tag);
                    tagView.setBackgroundResource(R.drawable.tag_pill_bg);
                    tagView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                } else {
                    // 选择标签
                    selectedTags.add(tag);
                    tagView.setBackgroundResource(R.drawable.category_pill_selected_bg);
                    tagView.setTextColor(ContextCompat.getColor(this, R.color.white));
                }
            });
            tagsContainer.addView(tagView);
        }
    }

    // 添加生成地点UI的方法
    private void generateLocationsUI() {
        // 清除现有地点
        locationsContainer.removeAllViews();

        // 为每个地点创建视图
        for (String location : availableLocations) {
            TextView locationView = createTagView(location);
            locationView.setOnClickListener(v -> {
                // 重置所有地点视图
                for (int i = 0; i < locationsContainer.getChildCount(); i++) {
                    View child = locationsContainer.getChildAt(i);
                    if (child instanceof TextView) {
                        child.setBackgroundResource(R.drawable.tag_pill_bg);
                        ((TextView) child).setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                    }
                }

                // 选择当前地点
                selectedLocation = location;
                locationView.setBackgroundResource(R.drawable.category_pill_selected_bg);
                locationView.setTextColor(ContextCompat.getColor(this, R.color.white));

                Toast.makeText(this, "已选择位置: " + selectedLocation, Toast.LENGTH_SHORT).show();
            });
            locationsContainer.addView(locationView);
        }
    }

    // 添加创建标签视图的辅助方法
    private TextView createTagView(String text) {
        TextView tagView = new TextView(this);
        tagView.setText(text);
        tagView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        tagView.setTextSize(14);

        // 使用ResourcesCompat安全地加载字体
        try {
            tagView.setTypeface(ResourcesCompat.getFont(this, R.font.shoukesong));
        } catch (Exception e) {
            // 记录错误但继续使用默认字体
            android.util.Log.e("PostActivity", "Error loading font: " + e.getMessage());
        }

        tagView.setBackgroundResource(R.drawable.tag_pill_bg);
        tagView.setPadding(
                getResources().getDimensionPixelSize(R.dimen.tag_padding_horizontal),
                getResources().getDimensionPixelSize(R.dimen.tag_padding_vertical),
                getResources().getDimensionPixelSize(R.dimen.tag_padding_horizontal),
                getResources().getDimensionPixelSize(R.dimen.tag_padding_vertical)
        );

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0,
                getResources().getDimensionPixelSize(R.dimen.tag_margin_end), 0);
        tagView.setLayoutParams(params);

        return tagView;
    }

    private void toggleLocationOptions() {
        // 直接在方法内部查找 HorizontalScrollView
        HorizontalScrollView locationTagsContainer = (HorizontalScrollView) locationsContainer.getParent();

        // Toggle visibility with animation
        if (locationTagsContainer.getVisibility() == View.VISIBLE) {
            locationTagsContainer.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> locationTagsContainer.setVisibility(View.GONE))
                    .start();
        } else {
            locationTagsContainer.setAlpha(0f);
            locationTagsContainer.setVisibility(View.VISIBLE);
            locationTagsContainer.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
        }
    }

    private void showPrivacyOptionsDialog() {
        String[] options = {"公开可见", "仅关注者可见", "仅自己可见"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("选择可见范围")
                .setItems(options, (dialog, which) -> {
                    TextView privacyText = (TextView)((LinearLayout)findViewById(R.id.privacySection)).getChildAt(1);
                    privacyText.setText(options[which]);
                })
                .show();
    }

    private void enterPreviewMode() {
        // Save current input as preview story
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "请先填写标题和正文", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create preview story
        previewStory = createStoryFromInput();

        // 显示预览对话框
        showPreviewDialog(previewStory);

        isPreviewMode = true;
    }

    // 添加显示预览对话框的方法
    private void showPreviewDialog(CampusStory story) {
        // 创建自定义对话框
        Dialog previewDialog = new Dialog(this, R.style.FullScreenDialogStyle);
        previewDialog.setContentView(R.layout.dialog_story_preview);

        // 设置对话框宽度为屏幕宽度
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(previewDialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        previewDialog.getWindow().setAttributes(layoutParams);

        // 初始化对话框中的视图
        TextView tvTitle = previewDialog.findViewById(R.id.previewTitle);
        TextView tvContent = previewDialog.findViewById(R.id.previewContent);
        TextView tvAuthor = previewDialog.findViewById(R.id.previewAuthor);
        TextView tvDate = previewDialog.findViewById(R.id.previewDate);
        ImageView ivImage = previewDialog.findViewById(R.id.previewImage);
        Button btnClose = previewDialog.findViewById(R.id.btnClosePreview);

        // 设置内容
        tvTitle.setText(story.getTitle());
        tvContent.setText(story.getContent());
        tvAuthor.setText(story.getAuthor());
        tvDate.setText(story.getDate());

        // 设置图片
        if (selectedImageUri != null) {
            ivImage.setImageURI(selectedImageUri);
        } else {
            // 设置默认图片
            ivImage.setImageResource(R.drawable.news_image1);
        }

        // 设置标签
        LinearLayout tagsContainer = previewDialog.findViewById(R.id.previewTagsContainer);
        tagsContainer.removeAllViews();
        for (String tag : selectedTags) {
            TextView tagView = createTagView(tag);
            tagsContainer.addView(tagView);
        }

        // 设置关闭按钮点击事件
        btnClose.setOnClickListener(v -> {
            previewDialog.dismiss();
            exitPreviewMode();
        });

        // 显示对话框
        previewDialog.show();
    }

    // 修改exitPreviewMode()方法
    private void exitPreviewMode() {
        isPreviewMode = false;
    }

    private void publishPost() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("请输入标题");
            return;
        }

        if (TextUtils.isEmpty(content)) {
            etContent.setError("请输入正文");
            return;
        }

        // Disable UI to prevent multiple submissions
        btnPublish.setEnabled(false);

        // Create a new story from input
        CampusStory newStory = createStoryFromInput();

        // Save to StoryManager in background
        new Thread(() -> {
            StoryManager.getInstance(PostActivity.this).addStory(newStory);

            // Show success message on UI thread
            runOnUiThread(() -> {
                showPublishSuccessDialog();
            });
        }).start();
    }

    private void showPublishSuccessDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_publish_success, null);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setView(dialogView);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        // Auto dismiss after delay
        new android.os.Handler().postDelayed(() -> {
            dialog.dismiss();
            finish();
        }, 2000);
    }

    private CampusStory createStoryFromInput() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        // Create tags list
        List<String> tags = new ArrayList<>();
        for (String tag : selectedTags) {
            // Remove # prefix if present
            if (tag.startsWith("#")) {
                tags.add(tag.substring(1));
            } else {
                tags.add(tag);
            }
        }

        // Add location as a tag if selected
        if (!TextUtils.isEmpty(selectedLocation)) {
            tags.add(selectedLocation);
        }

        // Create a new story
        CampusStory newStory = new CampusStory();
        newStory.setId(generateUniqueId());
        newStory.setTitle(title);
        newStory.setContent(content);

        // Get current user or use default
        UserSessionManager sessionManager = UserSessionManager.getInstance(this);
        User user = sessionManager.getUserDetails();
        if (user != null) {
            newStory.setAuthor(user.getName());
        } else {
            newStory.setAuthor("匿名用户");
        }

        // Set date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        newStory.setDate(dateFormat.format(new Date()));

        // Set image URL based on selected image or default
        if (selectedImageUri != null) {
            // In a real app, we would upload the image and get a URL
            // For now, we'll use a placeholder
            newStory.setImageUrl("news_image5");
        } else {
            newStory.setImageUrl("news_image1");
        }

        // Set tags
        newStory.setTags(tags);

        // Set initial metrics
        newStory.setLikes(0);
        newStory.setComments(0);
        newStory.setLatest(true);

        return newStory;
    }

    private int generateUniqueId() {
        // Simple implementation - in a real app, this would be handled by a backend
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限获取成功，打开图片选择器
                openImagePicker();
            } else {
                Toast.makeText(this, "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        try {
            startActivityForResult(Intent.createChooser(intent, "选择图片"), REQUEST_IMAGE_PICK);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "请安装文件管理器", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Find the image loading code in onActivityResult and replace it with this optimized version:
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();

            // Use a background thread for image processing
            new Thread(() -> {
                try {
                    // Load the selected image with reduced size to avoid memory issues
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2; // Reduce image size to 1/4

                    Bitmap bitmap = null;
                    try (InputStream inputStream = getContentResolver().openInputStream(selectedImageUri)) {
                        bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                    }

                    final Bitmap finalBitmap = bitmap;

                    // Update UI on main thread
                    runOnUiThread(() -> {
                        if (finalBitmap != null) {
                            // Show the image preview
                            imgPreview.setImageBitmap(finalBitmap);
                            imgPreview.setVisibility(View.VISIBLE);
                            tvImagePreviewText.setVisibility(View.GONE);
                            cardImagePreview.setVisibility(View.VISIBLE);

                            // Show success toast
                            Toast.makeText(PostActivity.this, "图片上传成功", Toast.LENGTH_SHORT).show();
                        } else {
                            handleImageLoadError(new Exception("Failed to decode bitmap"));
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> handleImageLoadError(e));
                }
            }).start();
        }
    }

    // Add this helper method to handle image loading errors:
    private void handleImageLoadError(Exception e) {
        Toast.makeText(this, "无法加载图片: " + e.getMessage(), Toast.LENGTH_SHORT).show();

        // Show text preview instead
        imgPreview.setVisibility(View.GONE);
        tvImagePreviewText.setVisibility(View.VISIBLE);
        cardImagePreview.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Release any resources that might be causing issues
        if (imgPreview != null && imgPreview.getDrawable() != null) {
            imgPreview.setImageDrawable(null);
        }
    }
}