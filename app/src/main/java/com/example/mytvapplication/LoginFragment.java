
package com.example.mytvapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginFragment extends Fragment {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private RequestQueue requestQueue;
    private View loadingView;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestQueue = Volley.newRequestQueue(requireContext());
        checkLoginStatus();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

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

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
       loginButton = view.findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> loginUser());

        return view;
    }



    private void checkLoginStatus() {
        showLoadingPage();
        String url = "http://193.106.55.136:5432/auth/checkLogin"; // Replace with your server's URL

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        boolean isLoggedIn = response.getBoolean("isLoggedIn");
                        if (isLoggedIn) {
                            String usernametosend = response.getString("username");

                            // Navigate to HomeFragment
                            Bundle bundle = new Bundle();
                            bundle.putString("username", usernametosend);
                            // Pass username to HomeFragment
                            hideLoadingPage();
                            NavHostFragment.findNavController(LoginFragment.this)
                                    .navigate(R.id.action_loginFragment_to_homeFragment, bundle);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(requireContext(), "Not Logged In", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void loginUser() {
        showLoadingPage();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        String url = "http://193.106.55.136:5432/auth/login"; // Replace with your server URL

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            String username = response.getString("username");

                            // Save email and username to SharedPreferences
                            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("email", email);
                            editor.putString("username", username);
                            editor.apply();

                            Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show();

                            // Navigate to HomeFragment
                            Bundle bundle = new Bundle();
                            bundle.putString("username", username); // Pass username to HomeFragment
                            hideLoadingPage();
                            NavHostFragment.findNavController(LoginFragment.this)
                                    .navigate(R.id.action_loginFragment_to_homeFragment, bundle);
                        } else {
                            Toast.makeText(requireContext(), "Login failed. Invalid credentials.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(jsonObjectRequest);
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
