package ng.com.laundrify.laundrify;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private final String TAG = "EmailPassword";
    private EditText email, password, firstName, lastName, phoneNumber;
    private DatabaseReference mCustomerDB;
    CircularProgressButton circularProgressButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        changeStatusBarColor();

        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.emailEditText1);
        password = findViewById(R.id.passwordEditText1);
        firstName = findViewById(R.id.firstNameEditText);
        lastName = findViewById(R.id.lastNameEditText);
        phoneNumber = findViewById(R.id.phoneNumberEditText);
        circularProgressButton = findViewById(R.id.cirRegisterButton);
    }

    public void signupClicked (View view) {

        if (validateForm()) {
            Log.d(TAG, "signIn:" + email);
            showProgressDialog((long) 60000);

            // [START create_user_with_email]
            mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                hideProgressDialog();
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(SignupActivity.this, task.getException().getLocalizedMessage(),
                                        Toast.LENGTH_LONG).show();
                                updateUI(null);
                            }

                            // [START_EXCLUDE]

                            // [END_EXCLUDE]
                        }
                    });
            // [END create_user_with_email]
        } else {
            hideProgressDialog();
            Toast.makeText(this, "Couldn't validate form. Check inputs", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "signupClicked: Couldn't validate form. Check inputs");
        }
    }

    private boolean validateForm() {
        boolean valid = true;
        Log.d(TAG, "validating...:");

        String firstNameT = firstName.getText().toString();
        if (!TextUtils.isEmpty(firstNameT)) {
            if (firstNameT.matches("null")) {
                valid = false;
                firstName.setError("Please enter a valid name");
            } else {
                firstName.setError(null);
            }
        } else {
            valid = false;
            firstName.setError("Required");
        }

        String lastNameT= lastName.getText().toString();
        if (!TextUtils.isEmpty(lastNameT)) {
            if (firstNameT.matches("null")) {
                valid = false;
                lastName.setError("Please enter a valid name");
            } else {
                lastName.setError(null);
            }
        } else {
            valid = false;
            lastName.setError("Required");
        }

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

        String phoneNumberT = phoneNumber.getText().toString();
        if (!TextUtils.isEmpty(emailT)) {
            if (!Patterns.PHONE.matcher(phoneNumberT).matches()) {
                valid = false;
                phoneNumber.setError("Please enter a valid phone no.");
            } else {
                phoneNumber.setError(null);
            }
        } else {
            valid = false;
            phoneNumber.setError("Required");
        }

        String passwordT = password.getText().toString();
        if (!TextUtils.isEmpty(passwordT)) {
            if (passwordT.matches("null") || passwordT.matches("nil")) {
                valid = false;
                password.setError("Please note that password must not contain programming syntax");
                password.requestFocus();
            } else {
                password.setError(null);
            }
        } else if (passwordT.length() < 6){
            valid = false;
            password.setError("You must have a minimum of 6 characters in your password");
        }
        return valid;
    }

    private void updateUI(final FirebaseUser user) {

        if (user != null) {
            //Update User info
            mCustomerDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user.getUid());

            Map userInfo = new HashMap();
            userInfo.put("FirstName", firstName.getText().toString());
            userInfo.put("LastName", lastName.getText().toString());
            userInfo.put("PhoneNumber", phoneNumber.getText().toString());
            userInfo.put("EmailAddress", email.getText().toString());
            userInfo.put("Password", password.getText().toString());

            mCustomerDB.child("Info").setValue(userInfo, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    // todo create channel here!
                    startActivity(new Intent(getApplicationContext(), SplashActivity.class));
                    hideProgressDialog();
                    finish();
                }
            });

        }
    }



    Handler handler = new Handler();

    public void showProgressDialog(Long time) {

        circularProgressButton.startAnimation();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(circularProgressButton!=null && circularProgressButton.isAnimating()) {
                    circularProgressButton.stopAnimation();
                    Toast.makeText(SignupActivity.this, "Couldn't connect, please try again later.", Toast.LENGTH_SHORT).show();
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

    public void changeToLogin (View view) {
        startActivity(new Intent(this,LoginActivity.class));
        overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);
        hideProgressDialog();
        finish();
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }

    public void facebookLogin(View view) {
        Toast.makeText(this, "Temporarily not available", Toast.LENGTH_SHORT).show();
    }
}

