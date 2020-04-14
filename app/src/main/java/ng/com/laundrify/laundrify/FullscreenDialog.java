package ng.com.laundrify.laundrify;

import android.app.TimePickerDialog;
import androidx.fragment.app.DialogFragment;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class FullscreenDialog extends DialogFragment implements View.OnClickListener {
    private Callback callback;
    private TextView setTime;
    private RadioButton rb7, rb1, rb2, rb3, rb4, rb5, rb6, new_rb, noneRB;
    private RadioGroup rg;
    String ampm, day;
    int hour, minute, cHour, cMin, cDay;
    static int pDay, pHo, pMi;
    private View viewDup;
    static Calendar pickupCal;

    static FullscreenDialog newInstance(int pD, int pH, int pM) {
        pickupCal = Calendar.getInstance();
            pDay = pD;
            pHo = pH;
            pMi = pM;
        pickupCal.set(Calendar.HOUR_OF_DAY, pH);
        pickupCal.set(Calendar.MINUTE, pM);
        return new FullscreenDialog();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.weekly_dialog, container, false);
        viewDup = view;
        ImageButton close = view.findViewById(R.id.fullscreen_dialog_close);
        TextView action = view.findViewById(R.id.fullscreen_dialog_action);
        setTime = view.findViewById(R.id.editText);
        rg = view.findViewById(R.id.rg);
        noneRB = view.findViewById(R.id.noneRB);
        rb1 = view.findViewById(R.id.rb);
        rb2 = view.findViewById(R.id.rb1);
        rb3 = view.findViewById(R.id.rb2);
        rb4 = view.findViewById(R.id.rb3);
        rb5 = view.findViewById(R.id.rb4);
        rb6 = view.findViewById(R.id.rb5);
        rb7 = view.findViewById(R.id.rb6);
        cHour = 0;

        close.setOnClickListener(this);
        setTime.setOnClickListener(this);
        action.setOnClickListener(this);

        setPSettings();
        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.fullscreen_dialog_close:
                dismiss();
                break;

            case R.id.fullscreen_dialog_action:
                saveSelected();
                break;

            case R.id.editText:
                showTimer();
                break;
        }
    }

    public interface Callback {
        void onActionClick(int day, int hour, int min, Calendar pickupCal);
    }

    //Set Previous settings
    public void setPSettings () {
        switch (pDay) {
            case 0:
                Toast.makeText(getContext(), "Grabing your 10% bonus ? " + getString(R.string.winkingFace), Toast.LENGTH_LONG).show();
                setTime.setText("00:00..");
                break;
            case 1:
                rb1.setChecked(true);
                break;
            case 2:
                rb2.setChecked(true);
                break;
            case 3:
                rb3.setChecked(true);
                break;
            case 4:
                rb4.setChecked(true);
                break;
            case 5:
                rb5.setChecked(true);
                break;
            case 6:
                rb6.setChecked(true);
                break;
            case 7:
                rb7.setChecked(true);
                break;
        }

        //Set time text if there's day
        if (pDay != 0) {
            cHour = pHo;
            cMin = pMi;
            if (pHo > 12) {
                ampm = "PM";
                hour = pHo - 12;
            } else if (pHo == 12) {
                ampm = "PM";
                hour = pHo;
            } else {
                ampm = "AM";
                hour = pHo;
            }
            noneRB.setText("Cancel");
            setTime.setText(String.format("%02d:%02d", hour, pMi) + ampm);
        } else {
            noneRB.setText("none");
        }
    }

    //Show the freaking timer
    public void showTimer (){
        TimePickerDialog timePickerDialog;
        final Calendar c = Calendar.getInstance();

        if (pDay == 0) {
            minute = c.get(Calendar.MINUTE);
            hour = c.get(Calendar.HOUR_OF_DAY);
        } else {
            hour = pHo;
            minute = pMi;
        }


        timePickerDialog = new TimePickerDialog(new ContextThemeWrapper(getActivity(), R.style.FullscreenDialogTheme), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                if (hourOfDay > 12) {
                    ampm = "PM";
                    hour = hourOfDay - 12;
                } else if (hourOfDay == 12) {
                    ampm = "PM";
                    hour = hourOfDay;
                } else {
                    ampm = "AM";
                    hour = hourOfDay;
                }

                //Do something
                if (hourOfDay > 8 && hourOfDay < 17 ) {
                    setTime.setText(String.format("%02d:%02d", hour, minute) + ampm);
                    cHour = hourOfDay;
                    cMin = minute;
                    Log.e("test", "You choose " + hourOfDay + ":" + minute);

                    pickupCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    pickupCal.set(Calendar.MINUTE, minute);

                } else {
                    showTimer();
                    Toast.makeText(getContext(), "We only operate 9AM - 4PM, check the time again", Toast.LENGTH_LONG).show();
                }
            }
        }, hour,minute,false);
        timePickerDialog.show();
    }

    //Pass selected items back
    public void saveSelected() {

        //Get selected button
        int rbid = rg.getCheckedRadioButtonId();
        new_rb = viewDup.findViewById(rbid);
        day = (String) new_rb.getText();
        if (day.equals("Sunday")) {
            cDay = 1;
        } else if (day.equals("Monday")) {
            cDay = 2;
        } else if (day.equals("Tuesday")) {
            cDay = 3;
        } else if (day.equals("Wednesday")) {
            cDay = 4;
        } else if (day.equals("Thursday")) {
            cDay = 5;
        } else if (day.equals("Friday")) {
            cDay = 6;
        } else if (day.equals("Saturday")) {
            cDay = 7;
        }

        Log.e("test", String.valueOf(cDay));

        if (inputCorrect()) {
            pickupCal.set(Calendar.DAY_OF_WEEK, cDay);
            callback.onActionClick(cDay, cHour, cMin, pickupCal);
            dismiss();
        }
    }

    //Check if input is correct
    public boolean inputCorrect (){
        if (cDay == 0) {
            cMin = 0;
            cHour = 0;
            return true;
        } else {
            if (cHour != 0) {
                return true;
            } else {
                Toast.makeText(getContext(), "Unsaved, please check the time", Toast.LENGTH_LONG).show();
                showTimer();
                return false;
            }
        }
    }


}


