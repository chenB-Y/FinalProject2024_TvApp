package com.example.mytvapplication;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executor;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.FutureCallback;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HomeFragment extends Fragment {
    private TextView messageTextView;
    private TextView cityTextView;
    private TextView weatherTextView;
    private ImageView boardImageView;
    private TextView dateTimeTextView;
    private View box5;
    private View box6;
    private Boolean box8Flage=false;
    private View box8;
    private TextView stockDataTextView;
    private View loadingView;


    private RelativeLayout editableArea;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inflate the loading view
        loadingView = getLayoutInflater().inflate(R.layout.loading_page, null);

        // Set layout parameters to match the parent view
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        loadingView.setLayoutParams(layoutParams);

        // Add the loading view to the root view
        ((ViewGroup) view).addView(loadingView);

        // Bring the loading view to the front and hide it initially
        loadingView.bringToFront();

        loadingView.setElevation(1000f);
        loadingView.setVisibility(View.GONE);

        messageTextView = view.findViewById(R.id.messageEditText);
        cityTextView = view.findViewById(R.id.cityTextView);
        weatherTextView = view.findViewById(R.id.weatherTextView);
        boardImageView = view.findViewById(R.id.box3);
        dateTimeTextView = view.findViewById(R.id.box4);


        box5 = view.findViewById(R.id.box5);
        box6 = view.findViewById(R.id.box6);
        box8 = view.findViewById(R.id.box8);
        editableArea = view.findViewById(R.id.mainLayout);
        stockDataTextView = view.findViewById(R.id.stockDataTextView);

        //create a handler that will call checkForUpdates every 5 seconds
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkForUpdates(requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("email", ""));
                handler.postDelayed(this, 5000);
            }
        }, 5000);

        //handler for the news to call fetchAndDisplayNewsTicker(box6); every half hour 1800000 milliseconds
        Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchAndDisplayNewsTicker(box6);
                handler2.postDelayed(this, 1800000);
            }
        }, 0);

        Handler handler3 = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                displayCurrentTimeForCityOrCountry();
                handler3.postDelayed(this, 1000);
            }
        }, 0);
        startLogoutCheck();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");
        String username = sharedPreferences.getString("username", "");

        // Set color for boxes 5 and 6
        box6.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_bright));
        // Optionally, fetch and display box positions here if needed
        checkAndLoadTemplate(username);

    }
    private void startLogoutCheck() {
        Handler handler = new Handler();
        Runnable logoutCheckRunnable = new Runnable() {
            @Override
            public void run() {
                checkLogoutStatus();
                handler.postDelayed(this, 5000); // Repeat every 5 seconds
            }
        };
        handler.post(logoutCheckRunnable);
    }
    private void checkLogoutStatus() {
        // Get email from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");

        // Create JSON object to send in the request body
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
            return; // Exit if there is an error creating the JSON object
        }

        // Create OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

        // Create RequestBody with the JSON object
        RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json; charset=utf-8"));

        // Build the request
        Request request = new Request.Builder()
                .url("http://193.106.55.136:5432/user/IslogedoutFromTv")
                .post(requestBody)
                .build();

        // Send the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("LogoutCheck", "Request failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        boolean isLoggedOut = jsonResponse.getBoolean("logOutTV");

                        if (isLoggedOut) {
                            // Clear SharedPreferences and navigate to login fragment
                            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear(); // Remove all shared preferences
                            editor.apply();

                            // Navigate to login fragment
                            requireActivity().runOnUiThread(() -> {
                                Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_loginFragment);
                            });
                        }
                    } catch (JSONException e) {
                        Log.e("LogoutCheck", "Error parsing JSON response", e);
                    }
                } else {
                    Log.e("LogoutCheck", "Request failed with response code: " + response.code());
                }
            }
        });
    }
    private void checkAndRunApiFunctions() {
        String url;
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        try {
            url = "http://193.106.55.136:5432/user/getApi?username=" + URLEncoder.encode(username, "UTF-8");
            Log.d("API_REQUEST_URL", "Encoded URL: " + url); // Log the encoded URL
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getActivity(), "Error encoding URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
            return; // Exit the method if URL encoding fails
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("API_REQUEST_FAILED", "Failed to fetch API list", e); // Log the error
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Failed to fetch API list: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    Log.e("API_REQUEST_ERROR", "Error fetching API list: " + response.code() + " " + errorBody); // Log the error
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "Error fetching API list: " + response.code() + " " + errorBody, Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d("API_LIST_RESPONSE", "Response body: " + responseBody); // Log the response body

                try {
                    JSONObject jsonObject = new JSONObject(responseBody);
                    boolean success = jsonObject.getBoolean("success");
                    if (success) {
                        JSONArray jsonArray = jsonObject.getJSONArray("api");
                        final boolean[] hasStockExchangeApi = {false};
                        final boolean[] hasDadJokesApi = {false};
                        final boolean[] hasQuotesApi = {false};
                        final boolean[] hasCryptoPriceApi = {false};


                        if(jsonArray.length()==1){
                            box5.setVisibility(View.GONE);
                        }
                        if(jsonArray.length()==0){
                            box5.setVisibility(View.GONE);
                            requireView().findViewById(R.id.box7).setVisibility(View.GONE);
                        }


                        for (int i = 0; i < jsonArray.length(); i++) {
                            String apiName = jsonArray.getString(i);
                            Log.d("API_NAME", "API name: " + apiName); // Log each API name

                            if ("Stock Exchange API".equals(apiName)) {
                                hasStockExchangeApi[0] = true;
                            }
                            if ("Dad Jokes API".equals(apiName)) {
                                hasDadJokesApi[0] = true;
                            }
                            if ("Quotes API".equals(apiName)) {
                                hasQuotesApi[0] = true;
                            }
                            if ("Crypto Price API".equals(apiName)) {
                                hasCryptoPriceApi[0] = true;
                            }

                        }

                        // Run the functions based on API names
                        getActivity().runOnUiThread(() -> {
                            if (hasStockExchangeApi[0]) {
                                fetchExchangeRate();
                                Log.d("API_RESPONSE", "Stock Exchange API called");
                            }
                            if (hasDadJokesApi[0]) {
                                fetchAndDisplayDadJoke();
                                Log.d("API_RESPONSE", "Dad Jokes API called");
                            }
                            if (hasQuotesApi[0]) {
                                fetchAndDisplayQuote();
                                Log.d("API_RESPONSE", "Quotes API called");
                            }
                            if (hasCryptoPriceApi[0]) {
                                fetchCryptoPrice();
                                Log.d("API_RESPONSE", "Crypto Price API called");
                            }
                        });
                    } else {
                        String message = jsonObject.optString("message", "Unknown error");
                        Log.e("API_LIST_ERROR", "Failed to retrieve APIs: " + message); // Log the error message
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getActivity(), "Failed to retrieve APIs: " + message, Toast.LENGTH_LONG).show();
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("API_JSON_PARSE_ERROR", "Error parsing API list", e); // Log the parsing error
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "Error parsing API list: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
    private void fetchAndDisplayQuote() {


        new Thread(() -> {

            HttpURLConnection connection = null;
            try {
                Thread.sleep(1000);
                // Set up the request
                URL url = new URL("https://api.api-ninjas.com/v1/quotes");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("accept", "application/json");
                connection.setRequestProperty("X-Api-Key", "yfIKTF2vJ3a2niVZTH8Jdg==eAvp2ibBR9rsxFRX"); // Replace with your API key

                // Check the response code
                int responseCode = connection.getResponseCode();
                InputStream responseStream;

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // If the request is successful
                    responseStream = connection.getInputStream();
                } else {
                    // If the request fails, read the error stream
                    responseStream = connection.getErrorStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    Log.e("FetchQuoteTask", "Error response: " + errorResponse.toString());
                    throw new Exception("API request failed with response code: " + responseCode);
                }

                // Parse the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                String responseBody = responseBuilder.toString();
                Log.d("FetchQuoteTask", "Response body: " + responseBody); // Log the response body

                JSONArray jsonArray = new JSONArray(responseBody);
                if (jsonArray.length() > 0) {
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String quote = jsonObject.optString("quote", "No quote found");
                    if(!isBox8Used()){
                        // Display the quote in the UI
                        getActivity().runOnUiThread(() -> displayJokeInBox8(quote));
                    }
                    else{
                        getActivity().runOnUiThread(() -> displayInStockDataTextView(quote));
                    }

                } else {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "No quotes found.", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e("FetchQuoteTask", "Failed to fetch quote", e);
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Failed to fetch quote: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
    private void fetchAndDisplayDadJoke() {
        new Thread(() -> {
            HttpURLConnection connection = null;
            InputStream responseStream = null;
            BufferedReader reader = null;

            try {
                // Set up the request
                URL url = new URL("https://api.api-ninjas.com/v1/dadjokes");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("accept", "application/json");
                connection.setRequestProperty("X-Api-Key", "BOGTWWvanlVNMf4uO063eA==fXBEbwJmM6G9CZHp");

                // Check the response code
                int responseCode = connection.getResponseCode();
                Log.d("FetchDadJokeTask", "Response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // If the request is successful
                    responseStream = connection.getInputStream();
                } else {
                    // If the request fails, read the error stream
                    responseStream = connection.getErrorStream();
                    reader = new BufferedReader(new InputStreamReader(responseStream));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    Log.e("FetchDadJokeTask", "Error response: " + errorResponse.toString());
                    throw new Exception("API request failed with response code: " + responseCode);
                }

                // Parse the response
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(responseStream);
                String joke = root.get(0).get("joke").asText();

                // Update UI based on box8 usage
                getActivity().runOnUiThread(() -> {
                    displayInStockDataTextView(joke);
                });

            } catch (Exception e) {
                Log.e("FetchDadJokeTask", "Failed to fetch dad joke", e);
                getActivity().runOnUiThread(() -> {

                    Toast.makeText(getContext(), "Failed to fetch dad joke: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } finally {
                // Close resources and disconnect
                try {
                    if (reader != null) {
                        reader.close();
                    }
                    if (responseStream != null) {
                        responseStream.close();
                    }
                } catch (IOException e) {
                    Log.e("FetchDadJokeTask", "Error closing resources", e);
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
    private void fetchCryptoPrice() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://api.api-ninjas.com/v1/cryptoprice?symbol=LTCBTC";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Api-Key", "yfIKTF2vJ3a2niVZTH8Jdg==eAvp2ibBR9rsxFRX") // Replace with your actual API key
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                // Update UI on failure
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Failed to fetch crypto price: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    try {
                        // Parse the response JSON
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        double price = jsonResponse.getDouble("price"); // Adjust according to the actual response structure
                        String cryptoString = "LTC/BTC Price: " + price;

                        if(!isBox8Used()){
                            // Display the quote in the UI
                            getActivity().runOnUiThread(() -> displayJokeInBox8(cryptoString));
                        }
                        else{
                            getActivity().runOnUiThread(() -> displayInStockDataTextView(cryptoString));
                        }
                        // Update UI with the cryptocurrency price
                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getActivity(), "Error parsing crypto price data", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "Request failed with code: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
    private void fetchExchangeRate() {
        OkHttpClient client = createUnsafeOkHttpClient();  // Use the method below to create a more flexible client
        String url = "https://v6.exchangerate-api.com/v6/217e96a9479732cd27c59379/latest/USD";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FetchExchangeRate", "Request failed", e);
                // Update UI on failure
                getActivity().runOnUiThread(() -> {
                    String message = "Request failed: " + e.getMessage();
                    displayJokeInBox8(message);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d("FetchExchangeRate", "Response body: " + responseBody);

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        Log.d("FetchExchangeRate", "Parsed JSON: " + jsonResponse.toString());

                        JSONObject conversionRates = jsonResponse.getJSONObject("conversion_rates");
                        double rate = conversionRates.getDouble("ILS");

                        getActivity().runOnUiThread(() -> {
                            String message = "1 USD = " + rate + " ILS";
                            displayJokeInBox8(message);
                        });
                    } catch (JSONException e) {
                        Log.e("FetchExchangeRate", "Error parsing JSON data", e);
                        getActivity().runOnUiThread(() -> {
                            String message = "Error parsing data: " + e.getMessage();
                            displayJokeInBox8(message);
                        });
                    }
                } else {
                    Log.e("FetchExchangeRate", "Request failed with response code: " + response.code());
                    getActivity().runOnUiThread(() -> {
                        String message = "Request failed with code: " + response.code();
                        displayJokeInBox8(message);
                    });
                }
            }
        });
    }
    // Method to create OkHttpClient that ignores SSL errors (for development only)
    private OkHttpClient createUnsafeOkHttpClient() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void displayJokeInBox8(final String joke) {
        // Find the custom JokeBoxView
        JokeBoxView box8 = requireView().findViewById(R.id.box8);
        // Set the joke to be displayed
        box8.setJoke(joke);
        box8Flage=true;
        stockDataTextView.setTag("used");
    }
    private void displayInStockDataTextView(String text) {
        TextView stockDataTextView = requireView().findViewById(R.id.stockDataTextView);

        if (stockDataTextView != null) {
            // Set the text
            stockDataTextView.setText(text);

            // Get the height of the TextView and text
            stockDataTextView.post(() -> {
                int viewHeight = stockDataTextView.getHeight();
                int textHeight = stockDataTextView.getLayout().getHeight();

                if (textHeight > viewHeight) {
                    // If text height exceeds the view height, start scrolling animation
                    startScrollingAnimation(stockDataTextView, text, viewHeight);
                }
            });
        }
    }
    private void startScrollingAnimation(TextView textView, String text, int viewHeight) {
        textView.post(() -> {
            // Ensure the layout has been measured and rendered
            textView.postDelayed(() -> {
                // Set the text to the TextView
                textView.setText(text + "\n\n" + "\n\n"+  text);
                textView.post(() -> {
                    final int textHeight = textView.getLayout().getHeight();

                    // Check if text height exceeds the view height
                    if (textHeight > viewHeight) {
                        // Create a ValueAnimator to animate the scrolling effect
                        ValueAnimator animator = ValueAnimator.ofInt(0, textHeight / 2);

                        // Duration for one full scroll cycle (e.g., 35 seconds)
                        long duration = 35000; // Adjust this for the speed of scrolling
                        animator.setDuration(duration);
                        animator.setInterpolator(new LinearInterpolator());
                        animator.addUpdateListener(animation -> {
                            int offset = (int) animation.getAnimatedValue();
                            textView.scrollTo(0, offset);

                            // Reset scroll position for continuous scrolling
                            if (offset >= textHeight / 2) {
                                textView.scrollTo(0, 0);
                            }
                        });

                        animator.setRepeatCount(ValueAnimator.INFINITE);
                        animator.start();
                    } else {
                        // If text fits within the view, no animation needed
                        textView.setScrollY(0);
                    }
                });
            }, 100); // Delay to ensure layout measurement
        });
    }
    private boolean isBox8Used() {

        if(box8Flage){
            Log.d("FetchQuoteTask", "isBox8Used: " + true);
            return true;
        }
        Log.d("FetchQuoteTask", "isBox8Used: " + false);
        return false;

    }
    private void fetchAndSetBackgroundColor(String email) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://193.106.55.136:5432/user/getUserMainBackgroundColor?email=" + email;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Failed to fetch background color: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Parse the response
                    String responseBody = response.body().string();
                    JSONObject jsonResponse;
                    try {
                        jsonResponse = new JSONObject(responseBody);
                        String colorString = jsonResponse.getString("backgroundColor");
                        int colorInt = Integer.parseInt(colorString);

                        // Update the background color on the main thread
                        requireActivity().runOnUiThread(() -> {
                            // Set the background color of your view
                            editableArea.setBackgroundColor(colorInt);
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Failed to parse background color: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Failed to fetch background color: " + response.message(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void restartApp() {
        // Finish the current activity and remove it from the recent tasks
        if (getActivity() != null) {
            Intent intent = getActivity().getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
            if (intent != null) {
                // Add flags to clear the task and create a new task
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // Finish the current activity and remove it from the recent tasks
                getActivity().finish();
                getActivity().overridePendingTransition(0, 0);

                // Reopen the app's launch activity
                startActivity(intent);

                // Optionally, close the app completely
                Runtime.getRuntime().exit(0);
            }
        }
    }
    private void checkForUpdates(String email) {
        String checkUpdatesUrl = "http://193.106.55.136:5432/user/checkForUpdates?email=" + email;

        Request request = new Request.Builder()
                .url(checkUpdatesUrl)
                .get()
                .build();

        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> {
                    Log.e("CheckForUpdates", "Request failed", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        if (jsonObject.getBoolean("updateAvailable")) {
                            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            String username = sharedPreferences.getString("username", "");
                            restartApp();
                            checkAndLoadTemplate(username);// Fetch and update box position
                            Log.e("CheckForUpdates", "sucesssssssss");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("CheckForUpdates", "Error parsing JSON response", e);
                    }
                } else {
                    getActivity().runOnUiThread(() -> {
                        Log.e("CheckForUpdates", "Request failed with response code: " + response.code());
                    });
                }
            }
        });
    }
    private void fetchAndPopulateBoxPositions(String email) {



        String fetchBoxesUrl = "http://193.106.55.136:5432/user/getUserBoxes?email=" + email;

        Request request = new Request.Builder()
                .url(fetchBoxesUrl)
                .get()
                .build();

        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to fetch box positions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    Log.d("FetchBoxesResponse", jsonResponse);

                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        if (jsonObject.getBoolean("success")) {
                            JSONArray boxesArray = jsonObject.getJSONArray("boxes");

                            getActivity().runOnUiThread(() -> {
                                RelativeLayout mainLayout = requireView().findViewById(R.id.mainLayout);
                                if (mainLayout == null) {
                                    Toast.makeText(getContext(), "Main Layout not found", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                    @Override
                                    public void onGlobalLayout() {
                                        mainLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                                        int containerWidth = mainLayout.getWidth();
                                        int containerHeight = mainLayout.getHeight()-150;

                                        List<Rect> boxPositions = new ArrayList<>();


                                        for (int i = 0; i < boxesArray.length(); i++) {
                                            try {

                                                JSONObject box = boxesArray.getJSONObject(i);
                                                double xPercentage = box.getDouble("x");
                                                double yPercentage = box.getDouble("y");
                                                double widthPercentage = box.getDouble("width");
                                                double heightPercentage = box.getDouble("height");
                                                int x = (int) Math.round((xPercentage / 100) * containerWidth);
                                                int y = (int) Math.round((yPercentage / 100) * containerHeight);
                                                int width = (int) Math.round((widthPercentage / 100) * containerWidth);
                                                int height = (int) Math.round((heightPercentage / 100) * containerHeight);
                                                // Ensure a minimum and maximum width/height to avoid stretching
                                                width = Math.max(100, Math.min(width, containerWidth / 2));
                                                height = Math.max(100, Math.min(height, containerHeight / 2));

                                                int scaledX = Math.max(0, x);
                                                int scaledY = Math.max(0, y);

                                                // Ensure the final position is within screen boundaries
                                                scaledX = Math.min(scaledX, containerWidth - width);
                                                scaledY = Math.min(scaledY, containerHeight - height);

                                                // Check for overlaps and adjust if necessary
                                                Rect currentRect = new Rect(scaledX, scaledY, scaledX + width, scaledY + height);
                                                if (isOverlapping(currentRect, boxPositions)) {
                                                    currentRect = adjustPosition(currentRect, boxPositions, containerWidth, containerHeight);
                                                }
                                                if(i+1==6){
                                                    Log.d("FetchBoxesResponse", "Box " + (i + 1) + " position: " + currentRect.toString());
                                                    updateBox(i + 1, currentRect.left, currentRect.top, currentRect.width(), currentRect.height());
                                                }else {
                                                    boxPositions.add(currentRect);
                                                    Log.d("FetchBoxesResponse", "Box " + (i + 1) + " position: " + currentRect.toString());
                                                    updateBox(i + 1, currentRect.left, currentRect.top, currentRect.width(), currentRect.height());
                                                }

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                Log.e("FetchBoxesResponse", "Error parsing box data", e);
                                            }
                                        }
                                    }
                                });
                            });

                            fetchAndDisplayImage(email);
                            fetchAndDisplayMessages(email);
                            fetchWeather("ראשון לציון");
                            fetchAndSetBackgroundColor(email);
                            checkAndRunApiFunctions();
                            hideLoadingPage();

                        } else {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Failed to fetch box positions: User not found", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error parsing JSON response for box positions", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to fetch box positions: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }

            }
        });

    }
    private void updateBox(int boxNumber, int x, int y, int width, int height) {
        View boxView = getViewForBox(boxNumber);

        if (boxView != null) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
            layoutParams.leftMargin = x;
            layoutParams.topMargin = y;
            boxView.setLayoutParams(layoutParams);
            boxView.setVisibility(View.VISIBLE);  // Ensure visibility
        } else {
            Log.e("UpdateBox", "Box " + boxNumber + " view not found.");
        }
    }
    private View getViewForBox(int boxNumber) {
        switch (boxNumber) {
            case 1:
                return requireView().findViewById(R.id.box1);
            case 2:
                return requireView().findViewById(R.id.box2);
            case 3:
                return requireView().findViewById(R.id.box3);
            case 4:
                return requireView().findViewById(R.id.box4);
            case 5:
                return requireView().findViewById(R.id.box5);
            case 6:
                return requireView().findViewById(R.id.box6);
            case 7:
                return requireView().findViewById(R.id.box7);
            case 8:
                return requireView().findViewById(R.id.box8);
            default:
                return null;
        }
    }
    private Rect adjustPosition(Rect rect, List<Rect> boxPositions, int containerWidth, int containerHeight) {
        int offset = 10; // Start with a small offset
        boolean positionAdjusted = false;
        Rect adjustedRect = new Rect(rect);
        while (!positionAdjusted) {
            adjustedRect.offset(offset, 0);
            if (adjustedRect.right > containerWidth) {
                adjustedRect.offset(-2 * offset, 0); // Move left if out of bounds
            }
            // Check if within bounds and not overlapping
            if (adjustedRect.left >= 0 && adjustedRect.right <= containerWidth &&
                    adjustedRect.top >= 0 && adjustedRect.bottom <= containerHeight &&
                    !isOverlapping(adjustedRect, boxPositions)) {
                positionAdjusted = true;
            }
            offset += 10; // Increase the offset for the next iteration
        }
        return adjustedRect;
    }
    private boolean isOverlapping(Rect rect, List<Rect> boxPositions) {
        for (Rect existingRect : boxPositions) {
            if (Rect.intersects(existingRect, rect)) {
                return true;
            }
        }
        return false;
    }
    private void fetchAndDisplayMessages(String email) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://193.106.55.136:5432/user/getUserMessages").newBuilder();
        urlBuilder.addQueryParameter("email", email);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to fetch messages: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONArray messagesArray = jsonObject.getJSONArray("messages");
                        List<String> messeges = new ArrayList<>();
                        int[] colors = new int[messagesArray.length()];
                        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();

                        for (int i = 0; i < messagesArray.length(); i++) {
                            JSONObject messageObject = messagesArray.getJSONObject(i);
                            String message = messageObject.getString("text");
                            messeges.add(message);
                            if(message.equals("")){
                                continue;
                            }
                            String color = messageObject.getString("color");

                            // Log color for debugging
                            Log.d("ColorDebug", "Color fetched: " + color);

                            SpannableString spannableString = new SpannableString(message + "\n\n");

                            int parsedColor;
                            try {
                                parsedColor = Color.parseColor(color);
                            } catch (IllegalArgumentException e) {
                                // Log error and use a fallback color
                                Log.e("ColorDebug", "Invalid color format: " + color, e);
                                parsedColor = Color.BLACK; // Fallback color
                            }
                            colors[i] = parsedColor;

                            // Apply color span
                            spannableString.setSpan(new ForegroundColorSpan(parsedColor), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            // Apply size span
                            spannableString.setSpan(new AbsoluteSizeSpan(11, true), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            // Apply style span
                            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            spannableBuilder.append(spannableString);
                        }

                        getActivity().runOnUiThread(() -> {
                            TextView messageTextView = requireView().findViewById(R.id.messageEditText);
                            messageTextView.setText(spannableBuilder);

                            // Debugging: Reset text color to verify it is being overridden correctly
                            //

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                messageTextView.setAutoSizeTextTypeUniformWithConfiguration(
                                        11, 30, 1, TypedValue.COMPLEX_UNIT_SP);
                            }

                            startScrollingAnimation(messageTextView,colors,messeges);
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error parsing JSON response for messages", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to fetch messages: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private void fetchWeather(String city) {
        OkHttpClient client = new OkHttpClient();

        String apiKey = "af0bade2d470c93f334b0068390afd98";
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric&appid=" + apiKey;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to fetch weather: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONObject mainObject = jsonObject.getJSONObject("main");
                        String temperature = mainObject.getString("temp") + "°C";

                        JSONObject weatherObject = jsonObject.getJSONArray("weather").getJSONObject(0);
                        String description = weatherObject.getString("description");

                        getActivity().runOnUiThread(() -> {
                            cityTextView.setText(city);
                            weatherTextView.setText(temperature + " - " + description);
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error parsing JSON response for weather", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to fetch weather: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private void fetchAndDisplayImage(String email) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://193.106.55.136:5432/user/getBoardImage").newBuilder();
        urlBuilder.addQueryParameter("email", email);
        String url = urlBuilder.build().toString();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email); // Send email, not username
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error preparing request", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to fetch image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        String imageUrl = jsonObject.getString("imageUrl");
                        Log.d("myBalls", imageUrl);
                        String finalImageUrl = imageUrl.replace("public/BoardImages/", "http://193.106.55.136:5432/");
                        Log.d("myBalls:", finalImageUrl);

                        // Ensure the view is on the main thread
                        getActivity().runOnUiThread(() -> {
                            View box3 = requireView().findViewById(R.id.box3);
                            int targetWidth = box3.getWidth();
                            int targetHeight = box3.getHeight();

                            // Load the image into box3
                            Glide.with(requireContext())
                                    .asBitmap()
                                    .load(finalImageUrl)
                                    .override(targetWidth, targetHeight)
                                    .fitCenter() // Use fitCenter to scale the image within the view, maintaining aspect ratio
                                    .into(new SimpleTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                            box3.setBackground(new BitmapDrawable(getResources(), resource));
                                        }

                                        @Override
                                        public void onLoadCleared(@Nullable Drawable placeholder) {
                                            // Handle placeholder if needed
                                        }
                                    });
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error parsing JSON response for image", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to fetch image: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private void displayCurrentTimeForCityOrCountry() {
        // Example for New York City, adjust as needed
        String timeZoneId = "Asia/Jerusalem";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone(timeZoneId));
        String currentTime = sdf.format(new Date());

        dateTimeTextView.setText(currentTime);
    }
    private void checkAndLoadTemplate(String username) {
        showLoadingPage();
        // Construct the URL to check the template
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");
        String templateUrl = "http://193.106.55.136:5432/user/getTemplate?username=" + username;

        // Create the GET request
        Request request = new Request.Builder()
                .url(templateUrl)
                .get()
                .build();

        // Initialize the OkHttpClient
        OkHttpClient client = new OkHttpClient();

        // Enqueue the request to be executed asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle the failure case
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Failed to check template: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Handle the success case
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        String template = jsonObject.getString("template");

                        getActivity().runOnUiThread(() -> {
                            if (template.equals("NULL")) {
                                // Fetch and populate box positions
                                fetchAndPopulateBoxPositions(email);

                            } else {
                                // Load the specified template
                                loadTemplate(template);

                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Error parsing JSON response for template", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to check template: " + response.message(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
    private TextView newsTicker;
    private void loadTemplate(String templateName) {
        int layoutId = getResources().getIdentifier(templateName, "layout", requireContext().getPackageName());
        if (layoutId != 0) {
            // Inflate the template layout
            LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View templateView = inflater.inflate(layoutId, null);
            JokeBoxView box7 = templateView.findViewById(R.id.box7);
            box7.setJoke("Commercial");
            // Replace the current layout with the template layout
            ViewGroup container = getView().findViewById(R.id.mainLayout);
            container.removeAllViews();
            container.addView(templateView);
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String email = sharedPreferences.getString("email", "");
            fetchAndDisplayImageForTemplate(email, templateView);
            // Update date and time immediately
            updateDateTime(templateView);

            // Update date and time every minute
            Handler handler = new Handler();
            Runnable updateTimeRunnable = new Runnable() {
                @Override
                public void run() {
                    updateDateTime(templateView);
                    // Schedule the next update
                    handler.postDelayed(this, 60000); // 60,000 milliseconds = 1 minute
                }
            };
            handler.post(updateTimeRunnable);
            fetchAndDisplayMessagesInBox1(email,templateView);
            fetchAndDisplayWeatherTemp("ראשון לציון", templateView);
            View box6 = templateView.findViewById(R.id.box6);
            checkAndRunApiFunctions();

            fetchAndDisplayNewsTicker(box6);
            Handler handler2 = new Handler();
            Runnable updateTimeRunnable2 = new Runnable() {
                @Override
                public void run() {
                    fetchAndDisplayNewsTicker(box6);
                    // Schedule the next update
                    handler2.postDelayed(this, 1800000); // 60,000 milliseconds = 1 minute
                }
            };
            handler2.post(updateTimeRunnable2);
            hideLoadingPage();
        } else {
            Toast.makeText(getContext(), "Template not found: " + templateName, Toast.LENGTH_SHORT).show();
        }
    }
    private void fetchAndDisplayNewsTicker(View box6) {
        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://193.106.55.136:5432/tv/getNews").newBuilder();
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();

                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONArray titles = jsonObject.getJSONArray("titles");

                        StringBuilder tickerText = new StringBuilder();
                        for (int i = 0; i < titles.length(); i++) {
                            tickerText.append(titles.getString(i)).append("    |    ");
                        }

                        requireActivity().runOnUiThread(() -> {
                            ViewGroup parent = (ViewGroup) box6.getParent();
                            box6.setBackgroundColor(Color.TRANSPARENT);
                            if (parent != null) {
                                // Remove any previous TextView with the tag "newsTickerTextView"
                                View oldTicker = parent.findViewWithTag("newsTickerTextView");
                                if (oldTicker != null) {
                                    parent.removeView(oldTicker);
                                }

                                // Create and configure the new TextView for the news ticker
                                TextView newsTickerText = new TextView(requireContext());
                                newsTickerText.setText(tickerText.toString());
                                newsTickerText.setSingleLine(true);
                                newsTickerText.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                                newsTickerText.setMarqueeRepeatLimit(-1); // Repeat indefinitely
                                newsTickerText.setSelected(true);
                                newsTickerText.setTextColor(Color.BLACK);
                                newsTickerText.setTextSize(16); // Adjust size as needed
                                newsTickerText.setPadding(8, 0, 8, 0);
                                newsTickerText.setTag("newsTickerTextView"); // Set a tag to identify the TextView

                                if (parent instanceof ConstraintLayout) {
                                    ConstraintLayout.LayoutParams tickerParams = new ConstraintLayout.LayoutParams(
                                            ConstraintLayout.LayoutParams.MATCH_PARENT,
                                            ConstraintLayout.LayoutParams.WRAP_CONTENT
                                    );
                                    tickerParams.bottomToBottom = box6.getId();
                                    tickerParams.startToStart = box6.getId();
                                    tickerParams.endToEnd = box6.getId();

                                    // Add the new TextView to the ConstraintLayout
                                    parent.addView(newsTickerText, tickerParams);
                                } else if (parent instanceof RelativeLayout) {
                                    RelativeLayout.LayoutParams tickerParams = new RelativeLayout.LayoutParams(
                                            RelativeLayout.LayoutParams.MATCH_PARENT,
                                            RelativeLayout.LayoutParams.WRAP_CONTENT
                                    );
                                    tickerParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                    tickerParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                                    tickerParams.addRule(RelativeLayout.ALIGN_PARENT_END);

                                    // Add the new TextView to the RelativeLayout
                                    parent.addView(newsTickerText, tickerParams);

                                    // Ensure that the TextView is positioned in front of `box6`
                                    newsTickerText.bringToFront();
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    throw new IOException("Unexpected code " + response);
                }
            }
        });
    }
    private void updateDateTime(View templateView) {
        // Use a valid time zone ID
        String timeZoneId = "Asia/Jerusalem";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));
        timeFormat.setTimeZone(TimeZone.getTimeZone(timeZoneId));

        // Get current date and time
        String currentDate = dateFormat.format(new Date());
        String currentTime = timeFormat.format(new Date());

        // Find and update the TextViews in box4
        LinearLayout box4 = templateView.findViewById(R.id.box4);
        TextView dateTextView = box4.findViewById(R.id.dateTextView);
        TextView timeTextView = box4.findViewById(R.id.timeTextView);

        // Set the date and time on the TextViews
        dateTextView.setText(currentDate);
        timeTextView.setText(currentTime);
    }
    private void fetchAndDisplayWeatherTemp(String city, View templateView) {
        OkHttpClient client = new OkHttpClient();
        String apiKey = "af0bade2d470c93f334b0068390afd98";
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric&appid=" + apiKey;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to fetch weather: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONObject mainObject = jsonObject.getJSONObject("main");
                        String temperature = mainObject.getString("temp") + "°C";

                        JSONObject weatherObject = jsonObject.getJSONArray("weather").getJSONObject(0);
                        String description = weatherObject.getString("description");

                        getActivity().runOnUiThread(() -> {
                            // Find the box2 LinearLayout and TextViews within it
                            LinearLayout box2 = templateView.findViewById(R.id.box2);
                            TextView cityTextView = box2.findViewById(R.id.cityTextView);
                            TextView weatherTextView = box2.findViewById(R.id.weatherTextView);

                            // Set the city and weather information
                            cityTextView.setText(city);
                            weatherTextView.setText(temperature + " - " + description);

                            // Adjust text sizes to fit the box
                            adjustTextSizeToFit(box2, cityTextView, weatherTextView);
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error parsing JSON response for weather", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to fetch weather: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private void adjustTextSizeToFit(LinearLayout box2, TextView cityTextView, TextView weatherTextView) {
        // Ensure views are laid out before measuring
        box2.post(() -> {
            // Get the dimensions of the LinearLayout
            int boxWidth = box2.getWidth() - box2.getPaddingLeft() - box2.getPaddingRight();
            int boxHeight = box2.getHeight() - box2.getPaddingTop() - box2.getPaddingBottom();

            // Set initial text sizes
            float cityTextSize = 20; // Initial size for cityTextView
            float weatherTextSize = 18; // Initial size for weatherTextView

            // Measure and adjust text sizes
            Paint paint = new Paint();
            paint.setTextSize(cityTextSize);
            float cityTextWidth = paint.measureText(cityTextView.getText().toString());
            float cityTextHeight = getTextHeight(cityTextSize, paint);

            paint.setTextSize(weatherTextSize);
            float weatherTextWidth = paint.measureText(weatherTextView.getText().toString());
            float weatherTextHeight = getTextHeight(weatherTextSize, paint);

            // Reduce text size if it exceeds the layout dimensions
            while ((cityTextWidth > boxWidth || cityTextHeight > boxHeight) ||
                    (weatherTextWidth > boxWidth || weatherTextHeight > boxHeight)) {
                cityTextSize--;
                weatherTextSize--;
                if (cityTextSize <= 12 || weatherTextSize <= 12) break; // Minimum text size

                cityTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, cityTextSize);
                weatherTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, weatherTextSize);

                paint.setTextSize(cityTextSize);
                cityTextWidth = paint.measureText(cityTextView.getText().toString());
                cityTextHeight = getTextHeight(cityTextSize, paint);

                paint.setTextSize(weatherTextSize);
                weatherTextWidth = paint.measureText(weatherTextView.getText().toString());
                weatherTextHeight = getTextHeight(weatherTextSize, paint);
            }
        });
    }
    // Helper method to calculate text height
    private float getTextHeight(float textSize, Paint paint) {
        paint.setTextSize(textSize);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return fontMetrics.bottom - fontMetrics.top;
    }
    private void fetchAndDisplayMessagesInBox1(String email, View templateView) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://193.106.55.136:5432/user/getUserMessages").newBuilder();
        urlBuilder.addQueryParameter("email", email);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to fetch messages: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        JSONArray messagesArray = jsonObject.getJSONArray("messages");
                        List<String> messeges = new ArrayList<>();
                        int[] colors = new int[messagesArray.length()];
                        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();

                        for (int i = 0; i < messagesArray.length(); i++) {
                            JSONObject messageObject = messagesArray.getJSONObject(i);
                            String message = messageObject.getString("text");
                            messeges.add(message);
                            String color = messageObject.getString("color");

                            SpannableString spannableString = new SpannableString(message + "\n\n");
                            int parsedColor = android.graphics.Color.parseColor(color);
                            colors[i] = parsedColor;
                            spannableString.setSpan(new ForegroundColorSpan(parsedColor), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            spannableString.setSpan(new AbsoluteSizeSpan(24, true), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            spannableBuilder.append(spannableString);
                        }

                        getActivity().runOnUiThread(() -> {
                            LinearLayout box1 = templateView.findViewById(R.id.box1);
                            TextView messageTextView = box1.findViewById(R.id.messageEditText);

                            messageTextView.setText(spannableBuilder);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                messageTextView.setAutoSizeTextTypeUniformWithConfiguration(
                                        24, 36, 1, TypedValue.COMPLEX_UNIT_SP);
                            }

                            startScrollingAnimation(messageTextView,colors,messeges);
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error parsing JSON response for messages", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to fetch messages: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private void startScrollingAnimation(final TextView textView, final int[] colors, final List<String> messages) {
        textView.post(() -> {
            // Ensure the layout has been measured and rendered
            textView.postDelayed(() -> {
                // Get the height of the view
                final int viewHeight = textView.getHeight();

                // Create a SpannableStringBuilder to hold the entire text with colors
                SpannableStringBuilder spannableBuilder = new SpannableStringBuilder();

                // Append each message with its color
                int currentIndex = 0;
                for (int i = 0; i < messages.size(); i++) {
                    String message = messages.get(i);
                    int color = colors[i % colors.length]; // Use modulo to handle colors array length mismatch

                    SpannableString spannableString = new SpannableString(message + "\n\n");
                    spannableString.setSpan(new ForegroundColorSpan(color), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new AbsoluteSizeSpan(24, true), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    spannableBuilder.append(spannableString);
                    currentIndex += spannableString.length();
                }

                // Duplicate the styled text
                SpannableStringBuilder duplicatedText = new SpannableStringBuilder(spannableBuilder.toString() + "\n\n" + spannableBuilder.toString());

                // Apply colors to the duplicated text
                int originalTextLength = spannableBuilder.length();
                int offset = 0;
                for (int i = 0; i < messages.size(); i++) {
                    String message = messages.get(i);
                    int color = colors[i % colors.length];
                    int messageLength = message.length() + 2; // +2 for the line breaks

                    // Apply color to both original and duplicated parts
                    duplicatedText.setSpan(new ForegroundColorSpan(color), offset, offset + messageLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    duplicatedText.setSpan(new ForegroundColorSpan(color), offset + originalTextLength + 2, offset + originalTextLength + 2 + messageLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    offset += messageLength;
                }

                // Set the duplicated text to the TextView
                textView.setText(duplicatedText);

                // Recalculate the height with the duplicated text
                textView.post(() -> {
                    final int newTextHeight = textView.getLayout().getHeight();

                    // If textHeight is less than or equal to viewHeight, no need to scroll
                    if (newTextHeight <= viewHeight) return;

                    // Duration for one full scroll cycle (e.g., 35 seconds)
                    final long duration = 35000;
                    // ValueAnimator to scroll text
                    ValueAnimator animator = ValueAnimator.ofInt(0, newTextHeight - viewHeight);
                    animator.setDuration(duration);
                    animator.setInterpolator(new LinearInterpolator());
                    animator.addUpdateListener(animation -> {
                        int scrollY = (int) animation.getAnimatedValue();
                        textView.scrollTo(0, scrollY);
                    });

                    animator.setRepeatCount(ValueAnimator.INFINITE);
                    animator.setRepeatMode(ValueAnimator.RESTART);

                    animator.start();
                });
            }, 100); // Delay to ensure layout measurement
        });
    }
    private void fetchAndDisplayImageForTemplate(String email, View templateView) {
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email); // Send email, not username
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error preparing request", Toast.LENGTH_SHORT).show();
            return;
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody.toString());
        Request request = new Request.Builder()
                .url("http://193.106.55.136:5432/user/getBoardImage")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to fetch image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        String imageUrl = jsonObject.getString("imageUrl");
                        Log.d("myBalls", "Image URL: " + imageUrl);
                        String finalImageUrl = imageUrl.replace("public/BoardImages/", "http://193.106.55.136:5432/");
                        Log.d("myBalls", "Final Image URL: " + finalImageUrl);

                        final View boxImageView = templateView.findViewById(R.id.box3); // Replace with the actual ID of your view

                        getActivity().runOnUiThread(() -> {
                            Picasso.get()
                                    .load(finalImageUrl)
                                    .into(new Target() {
                                        @Override
                                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                            boxImageView.setBackground(new BitmapDrawable(getResources(), bitmap));
                                            Log.d("myBalls", "Success: Image loaded");
                                        }

                                        @Override
                                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                            Log.d("myBalls", "Failed to load image: " + e.getMessage());
                                        }

                                        @Override
                                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                                            // Optionally handle placeholder
                                        }
                                    });
                        });


                    } catch (JSONException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error parsing JSON response for image", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to fetch image: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    private void showLoadingPage() {
        if (loadingView != null) {
            requireActivity().runOnUiThread(() -> {
                loadingView.setVisibility(View.VISIBLE);
            });
            Log.d("LoadingScreen", "Loading screen shown");
        }
    }

    private void hideLoadingPage() {
        if (loadingView != null) {
            requireActivity().runOnUiThread(() -> {
                loadingView.setVisibility(View.GONE);
            });
            Log.d("LoadingScreen", "Loading screen hidden");
        }
    }

}
