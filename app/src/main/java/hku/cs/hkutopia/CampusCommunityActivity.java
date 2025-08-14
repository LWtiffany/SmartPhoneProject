package hku.cs.hkutopia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import hku.cs.hkutopia.adapter.MessageAdapter;
import hku.cs.hkutopia.model.Message;
import hku.cs.hkutopia.utils.UserSessionManager;

public class CampusCommunityActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private LinearLayout typingIndicator;
    private View typingDot1, typingDot2, typingDot3;
    private TextView onlineCount, messageCount, communityRules;

    private List<Message> messages = new ArrayList<>();
    private MessageAdapter messageAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();
    private boolean isTypingIndicatorVisible = false;

    // Admin responses for common questions
    private final String[] ADMIN_RESPONSES = {
            "欢迎来到港大社区！有什么我可以帮助你的吗？",
            "港大图书馆开放时间是周一至周五 8:00-22:00，周末 10:00-18:00。",
            "校园巴士每15分钟一班，从早上7点到晚上10点运行。",
            "你可以在学生中心二楼找到学生事务办公室。",
            "港大有多个食堂，主要的有SU餐厅、美心餐厅和太古食堂。",
            "如果你有任何问题，可以随时在社区里提问，我们会尽快回复！",
            "港大的校训是'明德格物'，意为通过学习和研究，培养高尚品德和追求真理的精神。",
            "你可以通过学校官网或到访客中心获取更多校园信息。",
            "感谢你的提问！我们会将这个问题转交给相关部门。"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campus_community);

        initializeViews();
        setupRecyclerView();
        setupListeners();
        loadInitialMessages();
        startRandomOnlineCountUpdates();
    }

    private void initializeViews() {
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        typingIndicator = findViewById(R.id.typingIndicator);
        typingDot1 = findViewById(R.id.typingDot1);
        typingDot2 = findViewById(R.id.typingDot2);
        typingDot3 = findViewById(R.id.typingDot3);
        onlineCount = findViewById(R.id.onlineCount);
        messageCount = findViewById(R.id.messageCount);
        communityRules = findViewById(R.id.communityRules);

        // Set up back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Set up info button
        ImageButton btnInfo = findViewById(R.id.btnInfo);
        btnInfo.setOnClickListener(v -> showInfoDialog());

        // Set up community rules
        communityRules.setOnClickListener(v -> showRulesDialog());
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);

        messageAdapter = new MessageAdapter(this, messages);
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> sendMessage());

        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Enable/disable send button based on input
                sendButton.setEnabled(s.toString().trim().length() > 0);
                sendButton.setAlpha(s.toString().trim().length() > 0 ? 1.0f : 0.5f);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Initially disable send button
        sendButton.setEnabled(false);
        sendButton.setAlpha(0.5f);
    }

    private void loadInitialMessages() {
        // Add some sample messages
        Message adminMessage = new Message(
                "大家好！我是社区管理员，有任何关于港大的问题都可以在这里提问，我们会尽快回复。",
                "港大社区管理员",
                Message.TYPE_ADMIN
        );
        messages.add(adminMessage);

        Message userMessage1 = new Message(
                "请问港大图书馆的开放时间是什么时候？",
                "访客",
                Message.TYPE_USER
        );
        messages.add(userMessage1);

        // Show typing indicator
        showTypingIndicator();

        // Simulate admin response after delay
        handler.postDelayed(() -> {
            hideTypingIndicator();
            Message adminResponse = new Message(
                    "港大图书馆开放时间是周一至周五 8:00-22:00，周末 10:00-18:00。",
                    "港大社区管理员",
                    Message.TYPE_ADMIN
            );
            messageAdapter.addMessage(adminResponse);
            scrollToBottom();

            // Update message count
            updateMessageCount(messages.size());
        }, 2000);

        // Set adapter with initial messages
        messageAdapter.notifyDataSetChanged();
        scrollToBottom();

        // Update message count
        updateMessageCount(messages.size());
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }

        // Get user name
        String userName = "访客";
        UserSessionManager sessionManager = UserSessionManager.getInstance(this);
        if (sessionManager.isLoggedIn()) {
            User user = sessionManager.getUserDetails();
            if (user != null) {
                userName = user.getName();
            }
        }

        // Create and add user message
        Message userMessage = new Message(content, userName, Message.TYPE_USER);
        messageAdapter.addMessage(userMessage);
        scrollToBottom();

        // Update message count
        updateMessageCount(messages.size());

        // Clear input
        messageInput.setText("");

        // Show typing indicator after a short delay
        handler.postDelayed(this::showTypingIndicator, 500);

        // Simulate admin response after random delay
        int responseDelay = 1500 + random.nextInt(2000);
        handler.postDelayed(() -> {
            hideTypingIndicator();

            // Get random admin response
            String adminResponse = ADMIN_RESPONSES[random.nextInt(ADMIN_RESPONSES.length)];

            Message adminMessage = new Message(
                    adminResponse,
                    "港大社区管理员",
                    Message.TYPE_ADMIN
            );
            messageAdapter.addMessage(adminMessage);
            scrollToBottom();

            // Update message count
            updateMessageCount(messages.size());
        }, responseDelay);
    }

    private void showTypingIndicator() {
        if (isTypingIndicatorVisible) {
            return;
        }

        isTypingIndicatorVisible = true;
        typingIndicator.setVisibility(View.VISIBLE);

        // Animate typing dots
        animateTypingDots();
    }

    private void hideTypingIndicator() {
        if (!isTypingIndicatorVisible) {
            return;
        }

        isTypingIndicatorVisible = false;

        // Fade out animation
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(300);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                typingIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        typingIndicator.startAnimation(fadeOut);
    }

    private void animateTypingDots() {
        // Create animations for each dot
        ObjectAnimator dot1Anim = createDotAnimation(typingDot1, 0);
        ObjectAnimator dot2Anim = createDotAnimation(typingDot2, 150);
        ObjectAnimator dot3Anim = createDotAnimation(typingDot3, 300);

        // Create animator set
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(dot1Anim, dot2Anim, dot3Anim);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (isTypingIndicatorVisible) {
                    // Repeat animation if still typing
                    animateTypingDots();
                }
            }
        });

        animatorSet.start();
    }

    private ObjectAnimator createDotAnimation(View dot, long delay) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(dot, "translationY", 0f, -5f, 0f);
        anim.setDuration(900);
        anim.setStartDelay(delay);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        return anim;
    }

    private void scrollToBottom() {
        if (messageAdapter.getItemCount() > 0) {
            messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    private void startRandomOnlineCountUpdates() {
        // Initial values
        int initialOnlineCount = 120 + random.nextInt(30);
        updateOnlineCount(initialOnlineCount);

        // Update online count periodically
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int currentCount = Integer.parseInt(onlineCount.getText().toString().replace("当前在线: ", ""));
                int delta = random.nextInt(5) - 2; // Random change between -2 and +2
                int newCount = Math.max(100, currentCount + delta);

                updateOnlineCount(newCount);

                // Schedule next update
                handler.postDelayed(this, 10000 + random.nextInt(10000));
            }
        }, 10000);
    }

    private void updateOnlineCount(int count) {
        // Animate the count change
        ValueAnimator animator = ValueAnimator.ofInt(
                Integer.parseInt(onlineCount.getText().toString().replace("当前在线: ", "")),
                count);

        animator.setDuration(1000);
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            onlineCount.setText("当前在线: " + animatedValue);
        });

        animator.start();
    }

    private void updateMessageCount(int count) {
        messageCount.setText("今日消息: " + count);
    }

    private void showInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("关于港大社区")
                .setMessage("港大社区是香港大学师生和访客交流的平台，这里可以提问关于校园的各种问题，分享校园生活，结交新朋友。\n\n" +
                        "社区管理员会定期回复大家的问题，也欢迎热心的同学们互相帮助解答。")
                .setPositiveButton("了解", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showRulesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("社区规则")
                .setMessage("1. 尊重他人，文明发言\n" +
                        "2. 不发布与校园无关的广告\n" +
                        "3. 不发布虚假信息\n" +
                        "4. 不发布侵犯他人隐私的内容\n" +
                        "5. 遵守香港大学的相关规定\n\n" +
                        "违反规则的用户将被禁言或封禁账号。")
                .setPositiveButton("我知道了", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove callbacks to prevent memory leaks
        handler.removeCallbacksAndMessages(null);
    }
}