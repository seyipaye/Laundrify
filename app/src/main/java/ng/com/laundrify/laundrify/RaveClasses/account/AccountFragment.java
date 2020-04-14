package ng.com.laundrify.laundrify.RaveClasses.account;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import ng.com.laundrify.laundrify.R;
import ng.com.laundrify.laundrify.RaveClasses.OTPFragment;
import ng.com.laundrify.laundrify.RaveClasses.Payload;
import ng.com.laundrify.laundrify.RaveClasses.RaveConstants;
import ng.com.laundrify.laundrify.RaveClasses.RavePayActivity;
import ng.com.laundrify.laundrify.RaveClasses.RavePayInitializer;
import ng.com.laundrify.laundrify.RaveClasses.VerificationActivity;
import ng.com.laundrify.laundrify.RaveClasses.ViewObject;
import ng.com.laundrify.laundrify.RaveClasses.WebFragment;
import ng.com.laundrify.laundrify.RaveClasses.data.Bank;
import ng.com.laundrify.laundrify.RaveClasses.data.Callbacks;
import ng.com.laundrify.laundrify.RaveClasses.responses.RequeryResponse;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment implements AccountContract.View, DatePickerDialog.OnDateSetListener, View.OnClickListener {
    public static final int FOR_INTERNET_BANKING = 111;
    public static final int FOR_0TP = 222;
    TextInputEditText accountNumberEt;
    TextInputLayout accountNumberTil;
    EditText bankEt;
    Button payButton;
    AccountPresenter presenter;
    Bank selectedBank;
    TextView pcidss_tv;
    private ProgressDialog progessDialog;
    private BottomSheetDialog bottomSheetDialog;
    private TextInputEditText amountEt;
    private TextInputLayout amountTil;
    private TextInputEditText emailEt;
    private TextInputLayout emailTil,rave_bvnTil;
    private TextInputEditText phoneEt;
    private TextInputLayout phoneTil;
    private String flwRef;
    private RavePayInitializer ravePayInitializer;
    private EditText dateOfBirthEt,bvnEt;
    View v;
    Calendar calendar = Calendar.getInstance();

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new AccountPresenter(getActivity(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_account, container, false);

        initializeViews();

        setListeners();

        ravePayInitializer = ((RavePayActivity) getActivity()).getRavePayInitializer();

        presenter.init(ravePayInitializer);

        pcidss_tv.setMovementMethod(LinkMovementMethod.getInstance());

        return v;
    }

    private void setListeners() {
        bankEt.setOnClickListener(this);
        payButton.setOnClickListener(this);
        dateOfBirthEt.setOnClickListener(this);
    }

    private void initializeViews() {
        accountNumberTil =  v.findViewById(R.id.rave_accountNumberTil);
        accountNumberEt =  v.findViewById(R.id.rave_accountNumberEt);
        pcidss_tv =  v.findViewById(R.id.rave_pcidss_compliant_tv);
        dateOfBirthEt =  v.findViewById(R.id.rave_dobEditText);
        payButton =  v.findViewById(R.id.rave_payButton);
        bankEt =  v.findViewById(R.id.rave_bankEditText);
        amountTil =  v.findViewById(R.id.rave_amountTil);
        rave_bvnTil =  v.findViewById(R.id.rave_bvnTil);
        amountEt =  v.findViewById(R.id.rave_amountTV);
        emailTil =  v.findViewById(R.id.rave_emailTil);
        phoneTil =  v.findViewById(R.id.rave_phoneTil);
        phoneEt =  v.findViewById(R.id.rave_phoneEt);
        emailEt =  v.findViewById(R.id.rave_emailEt);
        bvnEt =  v.findViewById(R.id.rave_bvnEt);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.rave_payButton) {
            clearErrors();
            collectData();
        } else if (i == R.id.rave_bankEditText){
            presenter.getBanks();
        } else if (i == R.id.rave_dobEditText){
            new DatePickerDialog(getActivity(), AccountFragment.this, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        }
    }

    @Override
    public void showGTBankAmountIssue() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.payWithBankAmountLimitPrompt));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }

    @Override
    public void onEmailValidated(String emailToSet, int visibility) {
        emailTil.setVisibility(visibility);
        emailEt.setText(emailToSet);
    }

    @Override
    public void onAmountValidated(String amountToSet, int visibility) {
        amountTil.setVisibility(visibility);
        amountEt.setText(amountToSet);
    }


    private void clearErrors(){
        bankEt.setError(null);
        accountNumberTil.setError(null);
        accountNumberTil.setErrorEnabled(false);
        emailTil.setError(null);
        emailTil.setErrorEnabled(false);
        phoneTil.setError(null);
        phoneTil.setErrorEnabled(false);
        bankEt.setError(null);
        dateOfBirthEt.setError(null);
        rave_bvnTil.setError(null);
        rave_bvnTil.setErrorEnabled(false);
    }

    private void collectData() {

        HashMap<String, ViewObject> dataHashMap = new HashMap<>();

        dataHashMap.put(RaveConstants.fieldEmail, new ViewObject(emailTil.getId(), emailEt.getText().toString(), TextInputLayout.class));
        dataHashMap.put(RaveConstants.fieldPhone, new ViewObject(phoneTil.getId(), phoneEt.getText().toString(), TextInputLayout.class));
        dataHashMap.put(RaveConstants.fieldAmount, new ViewObject(amountTil.getId(), amountEt.getText().toString(), TextInputLayout.class));

        if (accountNumberTil.getVisibility() == View.VISIBLE) {
            if (accountNumberEt.getText().toString().length() != 10) {

                dataHashMap.put(RaveConstants.fieldAccount, new ViewObject(accountNumberTil.getId(), "", TextInputLayout.class));
            }
            else{

                dataHashMap.put(RaveConstants.fieldAccount, new ViewObject(accountNumberTil.getId(), accountNumberEt.getText().toString(), TextInputLayout.class));
            }
         }

        presenter.onDataCollected(dataHashMap);
    }


    @Override
    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showInternetBankingSelected(int whatToShow) {
        accountNumberTil.setVisibility(whatToShow);
    }

    @Override
    public void showDateOfBirth(int whatToShow) {
        dateOfBirthEt.setVisibility(whatToShow);
    }

    @Override
    public void showBVN(int whatToShow) {
        rave_bvnTil.setVisibility(whatToShow);
    }

    @Override
    public void showBanks(List<Bank> banks) {
        bottomSheetDialog = new BottomSheetDialog(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.add_exisiting_bank, null, false);
        RecyclerView recyclerView = v.findViewById(R.id.rave_recycler);

        BanksRecyclerAdapter adapter = new BanksRecyclerAdapter();
        adapter.set(banks);

        adapter.setBankSelectedListener(new Callbacks.BankSelectedListener() {
            @Override
            public void onBankSelected(Bank b) {
                bottomSheetDialog.dismiss();
                bankEt.setError(null);
                bankEt.setText(b.getBankname());
                selectedBank = b;

                presenter.onInternetBankingValidated(b);

            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        bottomSheetDialog.setContentView(v);
        bottomSheetDialog.show();
        showProgressIndicator(false);

    }

    @Override
    public void showProgressIndicator(boolean active) {

        try {
            if (getActivity().isFinishing()) {
                return;
            }

            if (progessDialog == null) {
                progessDialog = new ProgressDialog(getActivity());
                progessDialog.setCanceledOnTouchOutside(false);
                progessDialog.setMessage("Please wait...");
            }

            if (active && !progessDialog.isShowing()) {
                progessDialog.show();
            } else {
                progessDialog.dismiss();
            }
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGetBanksRequestFailed(String message) {
        showToast(message);
    }

    @Override
    public void validateAccountCharge(String pbfPubKey, String flwRef, String validateInstruction) {
        this.flwRef = flwRef;
        Intent intent = new Intent(getContext(),VerificationActivity.class);
        intent.putExtra(VerificationActivity.ACTIVITY_MOTIVE,"otp");
        if (validateInstruction != null) {
            intent.putExtra(OTPFragment.EXTRA_CHARGE_MESSAGE, validateInstruction);
        }
        intent.putExtra("theme",ravePayInitializer.getTheme());
        startActivityForResult(intent, FOR_0TP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RavePayActivity.RESULT_SUCCESS){
            if(requestCode==FOR_0TP){
                String otp = data.getStringExtra(OTPFragment.EXTRA_OTP);
                presenter.validateAccountCharge(flwRef, otp, ravePayInitializer.getPublicKey());
            }else if(requestCode==FOR_INTERNET_BANKING){
                presenter.requeryTx(flwRef, ravePayInitializer.getPublicKey());
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDisplayInternetBankingPage(String authurl, String flwRef) {
        this.flwRef = flwRef;
        Intent intent = new Intent(getContext(),VerificationActivity.class);
        intent.putExtra(WebFragment.EXTRA_AUTH_URL,authurl);
        intent.putExtra(VerificationActivity.ACTIVITY_MOTIVE,"web");
        intent.putExtra("theme",ravePayInitializer.getTheme());
        startActivityForResult(intent,FOR_INTERNET_BANKING);
    }

    @Override
    public void onChargeAccountFailed(String message, String responseAsJSONString) {
        showToast(message);
    }


    @Override
    public void onPaymentSuccessful(String status, String responseAsJSONString) {
        Intent intent = new Intent();
        intent.putExtra("response", responseAsJSONString);

        if (getActivity() != null) {
            getActivity().setResult(RavePayActivity.RESULT_SUCCESS, intent);
            getActivity().finish();
        }
    }

    @Override
    public void onPaymentFailed(String status, String responseAsJSONString) {
        Intent intent = new Intent();
        intent.putExtra("response", responseAsJSONString);
        if (getActivity() != null) {
            getActivity().setResult(RavePayActivity.RESULT_ERROR, intent);
            getActivity().finish();
        }
    }

    @Override
    public void onValidateSuccessful(String flwRef, String responseAsJsonString) {
        presenter.requeryTx(flwRef, ravePayInitializer.getPublicKey());
    }

    @Override
    public void onValidateError(String message, String responseAsJSonString) {
        showToast(message);
    }

    @Override
    public void onPaymentError(String message) {
        showToast(message);
    }

    @Override
    public void onRequerySuccessful(RequeryResponse response, String responseAsJSONString) {
        presenter.verifyRequeryResponseStatus(response, responseAsJSONString, ravePayInitializer);
    }


    @Override
    public void displayFee(String charge_amount, final Payload payload, final boolean internetbanking) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("You will be charged a total of " + charge_amount + ravePayInitializer.getCurrency() + ". Do you want to continue?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                presenter.chargeAccount(payload, ravePayInitializer.getEncryptionKey(), internetbanking);
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }

    @Override
    public void showFetchFeeFailed(String s) {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter == null) {
            presenter = new AccountPresenter(getActivity(), this);
        }
        assert presenter != null;
        presenter.onAttachView(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (presenter != null) {
            presenter.onDetachView();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String formattedDay;
        String formattedMonth;

        dateOfBirthEt.setError(null);

        if (String.valueOf(dayOfMonth).length() != 2) {
               formattedDay = "0" + dayOfMonth;
        }
        else {
            formattedDay = dayOfMonth + "";
        }

        if (String.valueOf(month + 1).length() != 2) {
               formattedMonth = "0" + (month + 1);
        }
        else {
            formattedMonth = (month + 1) + "";
        }


        dateOfBirthEt.setText(formattedDay + "/" + formattedMonth + "/" + year);
    }



    @Override
    public void onValidationSuccessful(HashMap<String, ViewObject> dataHashMap) {

        ravePayInitializer.setAmount(Double.parseDouble(dataHashMap.get(RaveConstants.fieldAmount).getData()));

        dataHashMap.put(RaveConstants.fieldBVN, new ViewObject(bvnEt.getId(), bvnEt.getText().toString(), TextInputLayout.class));
        dataHashMap.put(RaveConstants.fieldDOB, new ViewObject(dateOfBirthEt.getId(), dateOfBirthEt.getText().toString(), TextInputLayout.class));
        dataHashMap.put(RaveConstants.fieldBankCode, new ViewObject(bankEt.getId(), selectedBank.getBankcode(), TextInputLayout.class));
        dataHashMap.put(RaveConstants.isInternetBanking, new ViewObject(bankEt.getId(), selectedBank.isInternetbanking()+"", TextInputLayout.class));

        presenter.processTransaction(dataHashMap, ravePayInitializer);
    }

    @Override
    public void showFieldError(int viewID, String message, Class<?> viewType) {
        if (viewType == TextInputLayout.class){
            TextInputLayout view  =  v.findViewById(viewID);
            view.setError(message);
        }
        else if (viewType == EditText.class){
            EditText view  =  v.findViewById(viewID);
            view.setError(message);
        }
    }


}
