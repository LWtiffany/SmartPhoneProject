package hku.cs.hkutopia;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import hku.cs.hkutopia.utils.UserSessionManager;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final int RESEND_COOLDOWN_SECONDS = 30;

    private CardView cardUserInfo, cardLogin, cardQrCode, cardReservations;
    private TextView tvUserName, tvUserEmail;
    private TextInputEditText etName, etEmail, etVerificationCode;
    private TextInputLayout layoutVerificationCode;
    private View layoutVerificationCodeContainer;
    private Button btnAuthAction, btnLogout, btnViewReservations, btnResendCode;
    private ImageButton btnTogglePrivacy;
    private ImageView imgQrCode;
    private UserSessionManager sessionManager;
    private boolean isPrivacyEnabled = false;
    private boolean isCodeRequested = false; // Track whether verification code has been requested
    private CountDownTimer resendCooldownTimer; // Timer for resend button cooldown

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        // Initialize ApiClient with context
        ApiClient.getInstance().init(this);

        // Initialize session manager
        sessionManager = UserSessionManager.getInstance(this);

        initializeViews();
        setupBackButton();
        setupAuthAction();
        setupResendButton();
        setupPrivacyToggle();
        setupLogoutButton();
        setupReservationsButton();

        // Update UI based on login status
        updateUIBasedOnLoginStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel timer to prevent memory leaks
        if (resendCooldownTimer != null) {
            resendCooldownTimer.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh UI when activity resumes (in case user status changed)
        updateUIBasedOnLoginStatus();
    }

    private void initializeViews() {
        // Cards
        cardUserInfo = findViewById(R.id.cardUserInfo);
        cardLogin = findViewById(R.id.cardLogin);
        cardQrCode = findViewById(R.id.cardQrCode);
        cardReservations = findViewById(R.id.cardReservations);

        // Text views
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);

        // Edit texts
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etVerificationCode = findViewById(R.id.etVerificationCode);

        // Layouts
        layoutVerificationCode = findViewById(R.id.layoutVerificationCode);
        layoutVerificationCodeContainer = findViewById(R.id.layoutVerificationCodeContainer);

        // Buttons
        btnAuthAction = findViewById(R.id.btnAuthAction);
        btnResendCode = findViewById(R.id.btnResendCode);
        btnLogout = findViewById(R.id.btnLogout);
        btnViewReservations = findViewById(R.id.btnViewReservations);
        btnTogglePrivacy = findViewById(R.id.btnTogglePrivacy);

        // Images
        imgQrCode = findViewById(R.id.imgQrCode);
    }

    private void setupAuthAction() {
        btnAuthAction.setOnClickListener(v -> {
            if (!isCodeRequested) {
                // First phase: Request verification code
                requestVerificationCode();
            } else {
                // Second phase: Verify code and login/register
                verifyCodeAndLogin();
            }
        });
    }

    private void setupResendButton() {
        btnResendCode.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Please enter a valid email");
                return;
            }

            // Disable button while processing and start cooldown
            btnResendCode.setEnabled(false);
            startResendCooldown();

            // Request verification code from server
            ApiClient.getInstance().sendVerificationCode(email, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(final String response) {
                    // Run on UI thread
                    runOnUiThread(() -> {
                        // Show success message
                        showToast(getString(R.string.verification_code_sent));
                    });
                }

                @Override
                public void onFailure(final String error) {
                    // Run on UI thread
                    runOnUiThread(() -> {
                        // Show error
                        showToast(error);
                    });
                }
            });
        });
    }

    private void startResendCooldown() {
        // Cancel any existing timer
        if (resendCooldownTimer != null) {
            resendCooldownTimer.cancel();
        }

        // Start a new countdown timer
        resendCooldownTimer = new CountDownTimer(RESEND_COOLDOWN_SECONDS * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                btnResendCode.setText(getString(R.string.wait_before_resend, secondsRemaining));
            }

            @Override
            public void onFinish() {
                btnResendCode.setEnabled(true);
                btnResendCode.setText(R.string.btn_resend_code);
            }
        }.start();
    }

    private void requestVerificationCode() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (!validateInput(name, email)) {
            return;
        }

        // Disable button while processing
        btnAuthAction.setEnabled(false);
        btnAuthAction.setText(R.string.submitting);

        // Request verification code from server
        ApiClient.getInstance().sendVerificationCode(email, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(final String response) {
                // Run on UI thread
                runOnUiThread(() -> {
                    // Show verification code field
                    layoutVerificationCodeContainer.setVisibility(View.VISIBLE);

                    // Update button text
                    btnAuthAction.setText(R.string.btn_verify);
                    btnAuthAction.setEnabled(true);

                    // Mark code as requested
                    isCodeRequested = true;

                    // Start resend button cooldown
                    startResendCooldown();

                    // Show success message
                    showToast(getString(R.string.verification_code_sent));
                });
            }

            @Override
            public void onFailure(final String error) {
                // Run on UI thread
                runOnUiThread(() -> {
                    // Reset button
                    btnAuthAction.setText(R.string.btn_get_code);
                    btnAuthAction.setEnabled(true);

                    // Show error
                    showToast(error);
                });
            }
        });
    }

    private void verifyCodeAndLogin() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String code = etVerificationCode.getText().toString().trim();

        if (!validateInput(name, email)) {
            return;
        }

        if (code.isEmpty()) {
            etVerificationCode.setError("Verification code is required");
            return;
        }

        // Disable button while processing
        btnAuthAction.setEnabled(false);
        btnAuthAction.setText(R.string.submitting);

        // Verify code with server
        ApiClient.getInstance().verifyCode(email, code, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(final String response) {
                // Run on UI thread
                runOnUiThread(() -> {
                    // Login the user
                    sessionManager.createLoginSession(name, email);

                    // Update UI
                    updateUIBasedOnLoginStatus();

                    // Reset verification state
                    isCodeRequested = false;

                    // Show success message
                    showToast(getString(R.string.verification_success));
                });
            }

            @Override
            public void onFailure(final String error) {
                // Run on UI thread
                runOnUiThread(() -> {
                    // Reset button
                    btnAuthAction.setText(R.string.btn_verify);
                    btnAuthAction.setEnabled(true);

                    // Show error
                    showToast(error);
                });
            }
        });
    }

    private boolean validateInput(String name, String email) {
        if (name.isEmpty()) {
            etName.setError("Name is required");
            return false;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            return false;
        }

        return true;
    }

    private void setupPrivacyToggle() {
        btnTogglePrivacy.setOnClickListener(v -> {
            isPrivacyEnabled = !isPrivacyEnabled;
            updateUserInfoPrivacy();
        });
    }

    private void updateUserInfoPrivacy() {
        User user = sessionManager.getUserDetails();
        if (user == null) return;

        if (isPrivacyEnabled) {
            // Show masked email
            tvUserEmail.setText(user.getMaskedEmail());
            btnTogglePrivacy.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            // Show full email
            tvUserEmail.setText(user.getEmail());
            btnTogglePrivacy.setImageResource(android.R.drawable.ic_menu_view);
        }
    }

    private void setupLogoutButton() {
        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            updateUIBasedOnLoginStatus();
            showToast("Logged out successfully");
        });
    }

    private void setupReservationsButton() {
        btnViewReservations.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReservationListActivity.class);
            startActivity(intent);
        });
    }

    private void updateUIBasedOnLoginStatus() {
        boolean isLoggedIn = sessionManager.isLoggedIn();

        if (isLoggedIn) {
            // User is logged in - show user info, QR code, and reservations
            cardUserInfo.setVisibility(View.VISIBLE);
            cardQrCode.setVisibility(View.VISIBLE);
            cardReservations.setVisibility(View.VISIBLE);
            cardLogin.setVisibility(View.GONE);

            // Update user info
            User user = sessionManager.getUserDetails();
            if (user != null) {
                tvUserName.setText(user.getName());
                tvUserEmail.setText(user.getEmail());

                // Generate QR code with user email
                generateQRCode(user.getEmail());
            }
        } else {
            // User is not logged in - show login form
            cardUserInfo.setVisibility(View.GONE);
            cardQrCode.setVisibility(View.GONE);
            cardReservations.setVisibility(View.GONE);
            cardLogin.setVisibility(View.VISIBLE);

            // Reset form fields and state
            etName.setText("");
            etEmail.setText("");
            etVerificationCode.setText("");
            layoutVerificationCodeContainer.setVisibility(View.GONE);
            btnAuthAction.setText(R.string.btn_get_code);
            btnResendCode.setText(R.string.btn_resend_code);
            btnResendCode.setEnabled(true);
            isCodeRequested = false;

            // Cancel any existing timer
            if (resendCooldownTimer != null) {
                resendCooldownTimer.cancel();
            }
        }
    }

    private void generateQRCode(String content) {
        try {
            // Use the BarcodeEncoder from journeyapps library which simplifies QR code generation
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 512, 512);
            imgQrCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error generating QR code");
        }
    }

    private void setupBackButton() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}