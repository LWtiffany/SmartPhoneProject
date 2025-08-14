package hku.cs.hkutopia;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import hku.cs.hkutopia.model.CampusStory;
import hku.cs.hkutopia.model.Comment;
import hku.cs.hkutopia.adapter.StoryImageAdapter;
import androidx.viewpager2.widget.ViewPager2;
import java.util.ArrayList;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CampusStoryDetailActivity extends AppCompatActivity {

    private CampusStory story;
    private androidx.viewpager2.widget.ViewPager2 storyImagePager;
    private LinearLayout indicatorContainer;
    private List<ImageView> indicators = new ArrayList<>();
    private TextView storyTitle, storyContent, authorName, storyDate;
    private TextView likeCount, commentCount;
    private LinearLayout tagsContainer;
    private Button followButton;
    private LinearLayout commentsContainer;
    private EditText commentInput;
    private boolean isFollowed = false;
    private List<Comment> comments = new ArrayList<>();
    private boolean isLiked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_story_detail);

        // Get story from intent
        story = (CampusStory) getIntent().getSerializableExtra("story");
        if (story == null) {
            Toast.makeText(this, "Error loading story", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        initializeViews();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Populate UI with story data
        populateStoryData();

        // Set up interaction buttons
        setupInteractionButtons();

        // Set up comment input
        setupCommentInput();
    }

    private void initializeViews() {
        storyImagePager = findViewById(R.id.storyImagePager);
        indicatorContainer = findViewById(R.id.indicatorContainer);
        storyTitle = findViewById(R.id.storyTitle);
        storyContent = findViewById(R.id.storyContent);
        authorName = findViewById(R.id.authorName);
        storyDate = findViewById(R.id.storyDate);
        likeCount = findViewById(R.id.likeCount);
        commentCount = findViewById(R.id.commentCount);
        tagsContainer = findViewById(R.id.tagsContainer);
        followButton = findViewById(R.id.followButton);
        commentsContainer = findViewById(R.id.commentsContainer);
        commentInput = findViewById(R.id.commentInput);
    }

    private void populateStoryData() {
        // 设置图片 - 使用故事中的所有图片
        List<String> imageUrls = story.getImageUrls();
        if (imageUrls == null || imageUrls.isEmpty()) {
            // 如果没有图片列表，则使用主图片
            imageUrls = new ArrayList<>();
            String mainImageUrl = story.getImageUrl();
            if (mainImageUrl != null && !mainImageUrl.isEmpty()) {
                imageUrls.add(mainImageUrl);
            } else {
                // 如果连主图片也没有，使用默认图片
                imageUrls.add("news_image1");
            }
        }

        // 设置适配器
        StoryImageAdapter adapter = new StoryImageAdapter(this, imageUrls);
        storyImagePager.setAdapter(adapter);

        // 创建指示器
        createIndicators(imageUrls.size());

        // 添加页面变化监听器
        storyImagePager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
            }
        });

        // Set text fields
        storyTitle.setText(story.getTitle());
        storyContent.setText(story.getContent());
        authorName.setText(story.getAuthor());
        storyDate.setText(story.getDate());
        likeCount.setText(String.valueOf(story.getLikes()));
        commentCount.setText(String.valueOf(story.getComments()));

        // Set tags
        tagsContainer.removeAllViews();
        List<String> tags = story.getTags();
        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                TextView tagView = new TextView(this);
                tagView.setText("#" + tag);
                tagView.setTextColor(getResources().getColor(R.color.text_primary));
                tagView.setTextSize(14);
                tagView.setPadding(36, 18, 36, 18);
                tagView.setBackground(getResources().getDrawable(R.drawable.tag_background));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 24, 0);
                tagView.setLayoutParams(params);

                tagsContainer.addView(tagView);
            }
        }
    }

    private void setupInteractionButtons() {
        // Like button
        LinearLayout likeButton = findViewById(R.id.likeButton);
        ImageView likeIcon = likeButton.findViewById(R.id.likeIcon);
        likeButton.setOnClickListener(v -> {
            isLiked = !isLiked;
            if (isLiked) {
                story.setLikes(story.getLikes() + 1);
                likeIcon.setImageResource(R.drawable.ic_like_filled);
            } else {
                story.setLikes(story.getLikes() - 1);
                likeIcon.setImageResource(R.drawable.ic_like);
            }
            likeCount.setText(String.valueOf(story.getLikes()));
            Toast.makeText(this, isLiked ? "点赞成功" : "取消点赞", Toast.LENGTH_SHORT).show();
        });

        // Comment button
        findViewById(R.id.commentButton).setOnClickListener(v -> {
            commentInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(commentInput, InputMethodManager.SHOW_IMPLICIT);
        });

        // Share button
        findViewById(R.id.shareButton).setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, story.getTitle());
            shareIntent.putExtra(Intent.EXTRA_TEXT, story.getContent());
            startActivity(Intent.createChooser(shareIntent, "分享笔记"));
        });

        // Follow button
        followButton.setOnClickListener(v -> {
            isFollowed = !isFollowed;
            updateFollowButtonState();
            Toast.makeText(this, isFollowed ? "关注成功" : "已取消关注", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateFollowButtonState() {
        if (isFollowed) {
            followButton.setText("已关注");
            followButton.setBackgroundResource(R.drawable.followed_button_bg);
        } else {
            followButton.setText("关注");
            followButton.setBackgroundResource(R.drawable.follow_button_bg);
        }
    }

    private void setupCommentInput() {
        Button sendButton = findViewById(R.id.sendCommentButton);
        sendButton.setOnClickListener(v -> {
            String commentText = commentInput.getText().toString().trim();
            if (!TextUtils.isEmpty(commentText)) {
                addNewComment(commentText);
                commentInput.setText("");

                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(commentInput.getWindowToken(), 0);
            }
        });
    }

    private void addNewComment(String commentText) {
        // Create a new comment
        Comment newComment = new Comment();
        newComment.setAuthor("User");
        newComment.setContent(commentText);
        newComment.setDate(getCurrentDate());
        newComment.setLocation("香港");
        newComment.setLikes(0);

        // Add to comments list
        comments.add(newComment);

        // Update UI
        addCommentToUI(newComment);

        // Update comment count
        story.setComments(story.getComments() + 1);
        commentCount.setText(String.valueOf(story.getComments()));
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.CHINA);
        return sdf.format(new Date());
    }

    private void addCommentToUI(Comment comment) {
        // Inflate comment view
        View commentView = getLayoutInflater().inflate(R.layout.item_comment, null);

        // Set data
        ((TextView) commentView.findViewById(R.id.commentAuthor)).setText(comment.getAuthor());
        ((TextView) commentView.findViewById(R.id.commentDateLocation)).setText(
                comment.getDate() + " " + comment.getLocation() + " 回复");
        ((TextView) commentView.findViewById(R.id.commentContent)).setText(comment.getContent());

        // Add like button functionality
        ImageView likeButton = commentView.findViewById(R.id.commentLikeButton);
        likeButton.setOnClickListener(v -> {
            comment.setLikes(comment.getLikes() + 1);
            if (comment.getLikes() > 0) {
                // Change icon to filled heart
                likeButton.setImageResource(R.drawable.ic_like_filled);
            }
            Toast.makeText(this, "点赞成功", Toast.LENGTH_SHORT).show();
        });

        // Add to comments container
        commentsContainer.addView(commentView, 0); // Add at the top
    }

    private void createIndicators(int count) {
        indicatorContainer.removeAllViews();
        indicators.clear();

        int size = getResources().getDimensionPixelSize(R.dimen.indicator_size);
        int margin = getResources().getDimensionPixelSize(R.dimen.indicator_margin);

        for (int i = 0; i < count; i++) {
            ImageView indicator = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(margin, 0, margin, 0);
            indicator.setLayoutParams(params);

            indicator.setImageResource(R.drawable.indicator_inactive);
            indicatorContainer.addView(indicator);
            indicators.add(indicator);
        }

        // 默认选中第一个
        if (!indicators.isEmpty()) {
            indicators.get(0).setImageResource(R.drawable.indicator_active);
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < indicators.size(); i++) {
            indicators.get(i).setImageResource(
                    i == position ? R.drawable.indicator_active : R.drawable.indicator_inactive);
        }
    }
}