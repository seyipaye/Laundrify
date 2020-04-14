package ng.com.laundrify.laundrify;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ArrayList<String> slideurls;
    private ArrayList<String> slideActions;
    private ImageView imageView;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference splashRef;

    SharedPreferences sharedPreferences;
    private String TAG = "Test";
    private DatabaseReference customerDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        setContentView(R.layout.activity_splash);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        splashRef = database.getReference();

        imageView = findViewById(R.id.imageView);
        Glide.with(this)
                .load(R.drawable.signandlogo)
                .into(imageView);

        sharedPreferences = this.getSharedPreferences("ng.com.laundrify.laundrify", Context.MODE_PRIVATE);

        slideurls = new ArrayList<>();
        checkUpdate();
    }

    //Update Checker
    private void checkUpdate() {
        netTime((long) 60000);
        splashRef.child("AppsVersion").child("Android").child("Laundrify")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        timeRemove();
                        if (compareVersions(dataSnapshot.getValue().toString())) {
                            askToUpdate();
                        } else {
                            getUrls();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(SplashActivity.this, "FAILED: Please check internet connection", Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void askToUpdate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this, R.style.AlertDialogCustom);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle("Out dated app");
        builder.setMessage("Our developers are working to ensure you get the best of our services, and we've got a new update all in place for you" +
                " we're sorry but it's necessary to update before continuation")
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        update(SplashActivity.this);
                    }})
                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }});

        final AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void update(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.

        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        } finally {
            uri = null;
            goToMarket = null;
        }
    }
    private boolean compareVersions(String freshVersion){
        try {
            String curVersion = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            //Log.i("Stuff", getPackageName() + freshVersion + curVersion);
            return value(curVersion) < value(freshVersion);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
    private long value(String string) {
        string = string.trim();
        if( string.contains( "." )){
            final int index = string.lastIndexOf( "." );
            return value( string.substring( 0, index ))* 100 + value( string.substring( index + 1 ));
        }
        else {
            return Long.valueOf( string );
        }
    }

    public void getUrls () {
        netTime((long) 60000);
        database.getReference().child("Ads").child("AdLinks").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                slideurls = (ArrayList<String>) dataSnapshot.getValue();
                database.getReference().child("Ads").child("AdStrings").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        timeRemove();
                        slideActions = (ArrayList<String>) dataSnapshot.getValue();
                        checkLogin();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void checkLogin() {
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchUserData();
        } else {
            // todo Intent intent = new Intent(getApplicationContext(), OnBoardActivity.class);
            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void fetchUserData() {
        netTime((long) 60000);
        customerDB = splashRef.child("Users").child("Customers").child(currentUser.getUid()).child("Info");

        customerDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    timeRemove();
                    launchActivity((Map) dataSnapshot.getValue());
                } else {
                    Toast.makeText(SplashActivity.this, "Register again. Please", Toast.LENGTH_SHORT).show();
                    // todo Intent intent = new Intent(getApplicationContext(), OnBoardActivity.class);
                    Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void launchActivity(final Map userMap) {
        Log.i("test", "Transporting map...");

        // Check if Notification key is still valid
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        String lastToken = String.valueOf(userMap.get("NotificationToken"));

                        if (lastToken == null || !token.matches(lastToken)) {
                            customerDB.child("NotificationToken").setValue(token);
                            userMap.put("NotificationToken", token);
                        }
                    }
                });

        // Pass Components
        try {
            sharedPreferences.edit().clear();
            sharedPreferences.edit().putString("slideurls", ObjectSerializer.serialize(slideurls)).apply();
            sharedPreferences.edit().putString("slideActions", ObjectSerializer.serialize(slideActions)).apply();
            MainActivity.assignMap(userMap);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
        Log.i("test", "Tried resuming");
    }

    Handler handler = new Handler();
    private  void netTime (Long time) {
        handler.postDelayed(new Runnable() {
            public void run() {
                    if (slideurls.isEmpty()) {
                    Toast.makeText(SplashActivity.this, "Couldn't connect, please try again later.", Toast.LENGTH_SHORT).show();
                    finish();
                    }
            }
        }, time);}

        private void timeRemove () {
            handler.removeCallbacksAndMessages(null);
        }
}
