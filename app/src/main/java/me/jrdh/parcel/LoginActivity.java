package me.jrdh.parcel;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.jrdh.parcel.api.AuthenticationService;
import me.jrdh.parcel.api.UserAgentInterceptor;
import me.jrdh.parcel.api.models.LoginRequest;
import me.jrdh.parcel.api.models.LoginResponse;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {
    public static final String PARCEL_WEB_URL = "https://web.parcelapp.net/";

    @BindView(R.id.email)
    TextInputEditText emailAutoTextView;

    @BindView(R.id.password)
    TextInputEditText passwordEditText;

    @BindView(R.id.login_progress)
    View progressView;

    @BindView(R.id.login_form)
    View loginFormView;

    @BindView(R.id.email_sign_in_button)
    Button signInButton;

    @BindView(R.id.login_sign_up)
    AppCompatTextView signUpTextView;

    AuthenticationService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        // Generate device UUID if first time running
        generateDeviceUUID();

        // Check if user is already logged in and get out of here ASAP
        if (isLoggedIn()) {
            startActivity(new Intent(this, ParcelActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            overridePendingTransition(0,0);
            finish();
            return;
        }

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new UserAgentInterceptor())
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://secure.parcelapp.net")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build();

        authService = retrofit.create(AuthenticationService.class);

        passwordEditText.setOnEditorActionListener(
                (view, id, keyEvent) -> {
                    if (id == R.id.login || id == EditorInfo.IME_NULL) {
                        attemptLogin();
                        return true;
                    }
                    return false;
            }
        );

        signInButton.setOnClickListener(v -> attemptLogin());

        signUpTextView.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PARCEL_WEB_URL)))
        );
    }

    private void generateDeviceUUID() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (prefs.getString(ParcelSettings.UUID, "").isEmpty()) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(ParcelSettings.UUID, java.util.UUID.randomUUID().toString());
            editor.apply();
        }

        Log.d("Parcel", "Device ID: " + prefs.getString(ParcelSettings.UUID, ""));
    }


    private void attemptLogin() {

        // Reset errors.
        emailAutoTextView.setError(null);
        passwordEditText.setError(null);

        // Store values at the time of the login attempt.
        String email = emailAutoTextView.getText().toString();
        String password = passwordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.error_invalid_password));
            focusView = passwordEditText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            emailAutoTextView.setError(getString(R.string.error_field_required));
            focusView = emailAutoTextView;
            cancel = true;
        }

        // There was an error; don't attempt login and focus the first
        // form field with an error.
        if (cancel) {
            focusView.requestFocus();
            return;
        }

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);

        LoginRequest loginRequest = new LoginRequest(email, PreferenceManager.getDefaultSharedPreferences(this).getString(ParcelSettings.UUID, ""), password, "ios", "");

        showProgress(true);

        authService.login(loginRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response -> authenticateLogin(response),
                    e -> Log.e("Parcel", "Login failure: " + e.getMessage())
                );

    }

    void authenticateLogin(LoginResponse response) {
        Log.w("Parcel", "Login success: id: " + response.id + " Hash: " + response.hash);

        showProgress(false);

        // if the response is invalid
        if (response.id == null || response.hash == null || !response.result.equalsIgnoreCase("success")) {
            passwordEditText.setError(getString(R.string.error_incorrect_password));
            passwordEditText.requestFocus();
            return;
        }

        // Store response.id and response.hash
        storeCredentials(response.id, response.hash);

        startActivity(new Intent(this, ParcelActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    void storeCredentials (String userId, String hash) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();

        prefs.putString(ParcelSettings.USER_ID, userId);
        prefs.putString(ParcelSettings.HASH, hash);
        prefs.apply();
    }

    boolean isLoggedIn() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return !prefs.getString(ParcelSettings.USER_ID, "").isEmpty();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            return;
        }

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        loginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}

