package hku.cs.hkutopia;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Client for interacting with the remote API
 */
public class ApiClient {
    private static final String TAG = "ApiClient";
    // Server configuration
    private static final String SERVER_IP = "xxxxxxxx";
    private static final String SERVER_PORT = "xxxx";
    private static final String BASE_URL = "http://" + SERVER_IP + ":" + SERVER_PORT;
    
    // API endpoints
    private static final String BOOKING_ENDPOINT = "/tourists/book/";
    private static final String SEND_CODE_ENDPOINT = "/tourists/send_code/";
    private static final String VERIFY_CODE_ENDPOINT = "/tourists/verify_code/";
    private static final String REMAINING_SLOTS_ENDPOINT = "/tourists/remaining_slots/";
    
    // Singleton instance
    private static ApiClient instance;
    private Context appContext;
    
    // OkHttp client
    private final OkHttpClient client;
    
    /**
     * Constructor with default configuration
     */
    private ApiClient() {
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }
    
    /**
     * Initialize with application context
     */
    public void init(Context context) {
        this.appContext = context.getApplicationContext();
    }
    
    /**
     * Uploads a reservation to the server
     * 
     * @param reservation The reservation to upload
     * @param user The user making the reservation
     * @param callback Callback to handle success/failure
     */
    public void uploadReservation(Reservation reservation, User user, final ApiCallback callback) {
        // Convert time slot to format expected by server
        String timeSlotCode = convertTimeSlotToCode(reservation.getTimeSlot());
        
        // Format date to string
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = dateFormat.format(reservation.getDate());
        
        // Create form body with required parameters
        RequestBody formBody = new FormBody.Builder()
                .add("email", user.getEmail())
                .add("name", user.getName())
                .add("date", dateStr)
                .add("time_slot", timeSlotCode)
                .build();
        
        // Create request
        Request request = new Request.Builder()
                .url(BASE_URL + BOOKING_ENDPOINT)
                .post(formBody)
                .build();
        
        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to upload reservation", e);
                if (callback != null) {
                    callback.onFailure(getLocalizedErrorMessage("network_error"));
                }
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = "";
                try {
                    responseBody = response.body().string();
                    
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String message = jsonResponse.optString("message", "");
                    
                    Log.d(TAG, "Server response: " + response.code() + " - " + message);
                    
                    if (response.isSuccessful()) {
                        if (callback != null) {
                            callback.onSuccess(message);
                        }
                    } else {
                        // Handle specific error types based on status code and message
                        if (response.code() == 400) {
                            if (message.contains("already booked")) {
                                callback.onFailure(getLocalizedErrorMessage("already_booked"));
                            } else {
                                callback.onFailure(getLocalizedErrorMessage("invalid_input"));
                            }
                        } else if (response.code() == 500) {
                            callback.onFailure(getLocalizedErrorMessage("server_error"));
                        } else {
                            callback.onFailure(getLocalizedErrorMessage("unknown_error"));
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse JSON response: " + responseBody, e);
                    callback.onFailure(getLocalizedErrorMessage("parse_error"));
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error processing response", e);
                    callback.onFailure(getLocalizedErrorMessage("unknown_error"));
                }
            }
        });
    }
    
    /**
     * Sends a request to the server to send a verification code to the provided email
     * 
     * @param email The email to send the verification code to
     * @param callback Callback to handle success/failure
     */
    public void sendVerificationCode(String email, final ApiCallback callback) {
        // Create form body with required parameters
        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .build();
        
        // Create request
        Request request = new Request.Builder()
                .url(BASE_URL + SEND_CODE_ENDPOINT)
                .post(formBody)
                .build();
        
        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to send verification code", e);
                if (callback != null) {
                    callback.onFailure(getLocalizedErrorMessage("network_error"));
                }
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = "";
                try {
                    responseBody = response.body().string();
                    
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String message = jsonResponse.optString("message", "");
                    
                    Log.d(TAG, "Send code response: " + response.code() + " - " + message);
                    
                    if (response.isSuccessful()) {
                        if (callback != null) {
                            callback.onSuccess(message);
                        }
                    } else {
                        // Handle specific error types
                        if (response.code() == 400) {
                            callback.onFailure(getLocalizedErrorMessage("invalid_email"));
                        } else if (response.code() == 500) {
                            callback.onFailure(getLocalizedErrorMessage("server_error"));
                        } else {
                            callback.onFailure(getLocalizedErrorMessage("unknown_error"));
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse JSON response: " + responseBody, e);
                    callback.onFailure(getLocalizedErrorMessage("parse_error"));
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error processing response", e);
                    callback.onFailure(getLocalizedErrorMessage("unknown_error"));
                }
            }
        });
    }
    
    /**
     * Verifies a verification code with the server
     * 
     * @param email The email associated with the verification code
     * @param code The verification code to verify
     * @param callback Callback to handle success/failure
     */
    public void verifyCode(String email, String code, final ApiCallback callback) {
        // Create form body with required parameters
        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("code", code)
                .build();
        
        // Create request
        Request request = new Request.Builder()
                .url(BASE_URL + VERIFY_CODE_ENDPOINT)
                .post(formBody)
                .build();
        
        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to verify code", e);
                if (callback != null) {
                    callback.onFailure(getLocalizedErrorMessage("network_error"));
                }
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = "";
                try {
                    responseBody = response.body().string();
                    
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String message = jsonResponse.optString("message", "");
                    
                    Log.d(TAG, "Verify code response: " + response.code() + " - " + message);
                    
                    if (response.isSuccessful()) {
                        if (callback != null) {
                            callback.onSuccess(message);
                        }
                    } else {
                        // Handle specific error types
                        if (response.code() == 400) {
                            if (message.contains("Invalid code")) {
                                callback.onFailure(getLocalizedErrorMessage("invalid_code"));
                            } else {
                                callback.onFailure(getLocalizedErrorMessage("invalid_input"));
                            }
                        } else if (response.code() == 500) {
                            callback.onFailure(getLocalizedErrorMessage("server_error"));
                        } else {
                            callback.onFailure(getLocalizedErrorMessage("unknown_error"));
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse JSON response: " + responseBody, e);
                    callback.onFailure(getLocalizedErrorMessage("parse_error"));
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error processing response", e);
                    callback.onFailure(getLocalizedErrorMessage("unknown_error"));
                }
            }
        });
    }
    
    /**
     * Gets the remaining slots for a specific date and time slot
     * 
     * @param date The date to check (format: yyyy-MM-dd)
     * @param timeSlot The time slot code (A, B, or C)
     * @param callback Callback to handle success/failure
     */
    public void getRemainingSlots(String date, String timeSlot, final ApiCallback callback) {
        // Build URL with query parameters
        HttpUrl url = HttpUrl.parse(BASE_URL + REMAINING_SLOTS_ENDPOINT)
                .newBuilder()
                .addQueryParameter("date", date)
                .addQueryParameter("time_slot", timeSlot)
                .build();
        
        // Create request
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to get remaining slots", e);
                if (callback != null) {
                    callback.onFailure(getLocalizedErrorMessage("network_error"));
                }
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = "";
                try {
                    responseBody = response.body().string();
                    
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    
                    Log.d(TAG, "Remaining slots response: " + responseBody);
                    
                    if (response.isSuccessful()) {
                        if (callback != null) {
                            callback.onSuccess(responseBody);
                        }
                    } else {
                        // Handle error
                        callback.onFailure(getLocalizedErrorMessage("server_error"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse JSON response: " + responseBody, e);
                    callback.onFailure(getLocalizedErrorMessage("parse_error"));
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error processing response", e);
                    callback.onFailure(getLocalizedErrorMessage("unknown_error"));
                }
            }
        });
    }
    
    /**
     * Get localized error message based on error code
     */
    private String getLocalizedErrorMessage(String errorCode) {
        if (appContext == null) {
            // Default English messages if context is not available
            switch (errorCode) {
                case "network_error":
                    return "Network error. Please check your connection.";
                case "already_booked":
                    return "This email has already booked a tour.";
                case "invalid_input":
                    return "Invalid input. Please check your information.";
                case "server_error":
                    return "Server error. Please try again later.";
                case "parse_error":
                    return "Error parsing server response.";
                default:
                    return "Unknown error occurred.";
            }
        }
        
        // Get resource ID dynamically based on error code
        int stringId = appContext.getResources().getIdentifier(
                "error_" + errorCode, "string", appContext.getPackageName());
        
        if (stringId != 0) {
            return appContext.getString(stringId);
        } else {
            // Fallback to default English message
            return "An error occurred: " + errorCode;
        }
    }
    
    /**
     * Converts app's time slot format to server's format
     */
    private String convertTimeSlotToCode(String timeSlot) {
        if (timeSlot.contains("9:30 - 12:30") || timeSlot.toLowerCase().contains("morning")) {
            return "A";
        } else if (timeSlot.contains("12:30 - 15:30") || timeSlot.toLowerCase().contains("afternoon")) {
            return "B";
        } else if (timeSlot.contains("15:30 - 18:30") || timeSlot.toLowerCase().contains("evening")) {
            return "C";
        }
        // Default to A if unable to determine
        return "A";
    }
    
    /**
     * Callback interface for API responses
     */
    public interface ApiCallback {
        void onSuccess(String response);
        void onFailure(String error);
    }
} 
