package ng.com.laundrify.laundrify.RaveClasses;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import ng.com.laundrify.laundrify.R;

public class VerificationActivity extends AppCompatActivity {
    private static final String TAG = VerificationActivity.class.getName();
    public static final String ACTIVITY_MOTIVE = "activityMotive";
    public static final String INTENT_SENDER = "sender";
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_futher_verification);
        if(getIntent()!=null & getIntent().getIntExtra("theme",0)!=0){
            setTheme(getIntent().getIntExtra("theme",0));
        }
        if (findViewById(R.id.frame_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            if(getIntent()!=null & getIntent().getStringExtra(ACTIVITY_MOTIVE)!=null){
                switch (getIntent().getStringExtra(ACTIVITY_MOTIVE).toLowerCase()){
                    case "otp":
                        fragment = new OTPFragment();
                        fragment.setArguments(getIntent().getExtras());
                        break;
                    case "pin":
                        fragment = new PinFragment();
                        fragment.setArguments(getIntent().getExtras());
                        break;
                    case "web":
                        fragment = new WebFragment();
                        fragment.setArguments(getIntent().getExtras());
                        break;
                    case "avsvbv":
                        fragment = new AVSVBVFragment();
                        fragment.setArguments(getIntent().getExtras());
                        break;
                    default:
                        Log.e(TAG,"No extra value matching motives");
                }
            }

            if(fragment!=null){
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.frame_container, fragment).commit();
            } else{
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
        fragment.onActivityResult(requestCode, resultCode, data);
    }
}
