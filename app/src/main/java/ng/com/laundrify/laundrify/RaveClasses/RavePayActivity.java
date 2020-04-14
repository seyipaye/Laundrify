package ng.com.laundrify.laundrify.RaveClasses;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import ng.com.laundrify.laundrify.R;
import ng.com.laundrify.laundrify.RaveClasses.account.AccountFragment;
import ng.com.laundrify.laundrify.RaveClasses.ach.AchFragment;
import ng.com.laundrify.laundrify.RaveClasses.card.CardFragment;
import ng.com.laundrify.laundrify.RaveClasses.ghmobilemoney.GhMobileMoneyFragment;
import ng.com.laundrify.laundrify.RaveClasses.mpesa.MpesaFragment;
import ng.com.laundrify.laundrify.RaveClasses.ugmobilemoney.UgMobileMoneyFragment;

import static android.view.View.GONE;
import static ng.com.laundrify.laundrify.RaveClasses.RaveConstants.PERMISSIONS_REQUEST_READ_PHONE_STATE;
import static ng.com.laundrify.laundrify.RaveClasses.RaveConstants.RAVEPAY;
import static ng.com.laundrify.laundrify.RaveClasses.RaveConstants.RAVE_PARAMS;

public class RavePayActivity extends AppCompatActivity {

    ViewPager pager;
    TabLayout tabLayout;
    RelativeLayout permissionsRequiredLayout;
    RelativeLayout tokenModeCover;
    View mainContent;
    Button requestPermsBtn;
    int theme;
    RavePayInitializer ravePayInitializer;
    MainPagerAdapter mainPagerAdapter;
    public static String BASE_URL;
    public static int RESULT_SUCCESS = 111;
    public static int RESULT_ERROR = 222;
    public static int RESULT_CANCELLED = 333;
    public static boolean WITH_TOKEN = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get Parameters
        try {
            ravePayInitializer = Parcels.unwrap(getIntent().getParcelableExtra(RAVE_PARAMS));
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(RAVEPAY, "Error retrieving initializer");
        }

        //Set theme
        theme = ravePayInitializer.getTheme();
        if (theme != 0) {
            try {
                setTheme(theme);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        setContentView(R.layout.activity_rave_pay);

        //Set Staging indicator
        if (ravePayInitializer.isStaging()) {
            BASE_URL = RaveConstants.STAGING_URL;
            if(ravePayInitializer.getShowStagingLabel()){
                findViewById(R.id.stagingModeBannerLay).setVisibility(View.VISIBLE);
            }
        } else {
            BASE_URL = RaveConstants.LIVE_URL;
        }

        tabLayout = findViewById(R.id.sliding_tabs);
        pager = findViewById(R.id.pager);
        permissionsRequiredLayout = findViewById(R.id.rave_permission_required_layout);
        mainContent = findViewById(R.id.main_content);
        requestPermsBtn = findViewById(R.id.requestPermsBtn);
        tokenModeCover = findViewById(R.id.tokenModeCover);

        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        List<RaveFragment> raveFragments = new ArrayList<>();

        if (ravePayInitializer.isWithCard()) {
            raveFragments.add(new RaveFragment(new CardFragment(), "Card"));
        }

        if (ravePayInitializer.isWithAccount()) {
            if (ravePayInitializer.getCountry().equalsIgnoreCase("us") && ravePayInitializer.getCurrency().equalsIgnoreCase("usd")) {
                raveFragments.add(new RaveFragment(new AchFragment(), "ACH"));
            } else if (ravePayInitializer.getCountry().equalsIgnoreCase("ng") && ravePayInitializer.getCurrency().equalsIgnoreCase("ngn")){
                raveFragments.add(new RaveFragment(new AccountFragment(), "Account"));
            }
        }

        if (ravePayInitializer.isWithMpesa()) {
            raveFragments.add(new RaveFragment(new MpesaFragment(), "Mpesa"));
        }

        if (ravePayInitializer.isWithGHMobileMoney()) {
            raveFragments.add(new RaveFragment(new GhMobileMoneyFragment(), "GHANA MOBILE MONEY"));
        }

        if (ravePayInitializer.isWithUgMobileMoney()) {
            raveFragments.add(new RaveFragment(new UgMobileMoneyFragment(), "UGANDA MOBILE MONEY"));
        }

        mainPagerAdapter.setFragments(raveFragments);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkForRequiredPermissions();
        }

        requestPermsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},
                            PERMISSIONS_REQUEST_READ_PHONE_STATE);
                }
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                mainContent.setVisibility(View.VISIBLE);
                permissionsRequiredLayout.setVisibility(GONE);
                permissionVerified();

            } else {
                permissionsRequiredLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void permissionVerified() {
        if (ravePayInitializer.isWithToken()) {
            WITH_TOKEN = true;
            tokenModeCover.setVisibility(View.VISIBLE);
        } else {
            WITH_TOKEN = false;
            tokenModeCover.setVisibility(GONE);
        }
        pager.setAdapter(mainPagerAdapter);
        tabLayout.setupWithViewPager(pager);
    }

    public RavePayInitializer getRavePayInitializer() {
        return ravePayInitializer;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void checkForRequiredPermissions() {
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsRequiredLayout.setVisibility(View.VISIBLE);
        } else {
            permissionsRequiredLayout.setVisibility(GONE);
            permissionVerified();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RavePayActivity.RESULT_CANCELLED, new Intent());
        super.onBackPressed();
    }


}

