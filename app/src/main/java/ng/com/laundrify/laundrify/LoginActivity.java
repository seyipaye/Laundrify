package ng.com.laundrify.laundrify;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private final String TAG = "EmailPassword";
    private EditText email, password;
    CircularProgressButton circularProgressButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //for changing status bar icon colors
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        changeStatusBarColor();
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);
        circularProgressButton = findViewById(R.id.cirLoginButton);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        changeToSignup(email);
    }


    public void loginClicked (View view) {
        Log.d(TAG, "signIn:" + email);

        if (validateForm()) {
            showProgressDialog((long) 60000);

            // [START sign_in_with_email]
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                hideProgressDialog();
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(),
                                        Toast.LENGTH_LONG).show();
                                updateUI(null);
                            }
                        }
                    });
            // [END sign_in_with_email]
        } else {
            hideProgressDialog();
            Toast.makeText(this, "Unable to validate form", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "loginClicked: Unable to validate form");
        }
    }

    public void forgotPassword (View view) {
        Toast.makeText(this, "Contact us", Toast.LENGTH_SHORT).show();
        contact("+2347012454239", "Hello, I can't remember my password, "
                + "can you help me get it back ?.\n My Firstname is... \n Lastname is... \n and My Email is...");
    }

    private void contact(String toNumber, String message) {
        try {
            toNumber = toNumber.replace("+", "").replace(" ", "");

            Intent sendIntent = new Intent("android.intent.action.MAIN");
            sendIntent.putExtra("jid", toNumber + "@s.whatsapp.net");
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setPackage("com.whatsapp");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(findViewById(android.R.id.content), "Please make sure you have whatsapp", Snackbar.LENGTH_LONG).show();
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        String emailT = email.getText().toString();
        if (!TextUtils.isEmpty(emailT)) {
            if (!Patterns.EMAIL_ADDRESS.matcher(emailT).matches()) {
                valid = false;
                email.setError("Please enter a valid email address");
            } else {
                email.setError(null);
            }
        } else {
            valid = false;
            email.setError("Required");
        }

        String passwordT = password.getText().toString();
        if (!TextUtils.isEmpty(passwordT)) {
            if (passwordT.matches("null") || passwordT.matches("nil")) {
                valid = false;
                password.setError("Please note that password must not contain programming syntax");
            } else {
                password.setError(null);
            }
        } else if (passwordT.length() < 6){
            valid = false;
            password.setError("You should have a minimum of 6 characters in your password");
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            startActivity(new Intent(getApplicationContext(), SplashActivity.class));
            String NOTIF_CHANNEL_ID = "LaundrifyOrder";
            createNotificationChannel(NOTIF_CHANNEL_ID, "Order Reminders",
                    "This let's you control weather or not you want to display notification for your laundry status");
            finish();
        } else {
            hideProgressDialog();
        }

    }


    Handler handler = new Handler();
    public void showProgressDialog(Long time) {

        circularProgressButton.startAnimation();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(circularProgressButton!=null && circularProgressButton.isAnimating()) {
                    circularProgressButton.stopAnimation();
                    Toast.makeText(LoginActivity.this, "Couldn't connect, please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        }, time);
    }

    public void hideProgressDialog() {
        if(circularProgressButton!=null && circularProgressButton.isAnimating()) {
            circularProgressButton.revertAnimation();
            handler.removeCallbacksAndMessages(null);
        }
    }

    public void changeToSignup (View view) {
        startActivity(new Intent(this,SignupActivity.class));
        overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
        hideProgressDialog();
        finish();
    }

    private void createNotificationChannel (String channelID, CharSequence channelName, String channelDesc) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelID, channelName, importance);
            channel.setDescription(channelDesc);
            channel.setShowBadge(false);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.login_bk_color));
        }
    }

    public void facebookLogin(View view) {
        Toast.makeText(this, "Temporarily not available", Toast.LENGTH_SHORT).show();
    }
}

