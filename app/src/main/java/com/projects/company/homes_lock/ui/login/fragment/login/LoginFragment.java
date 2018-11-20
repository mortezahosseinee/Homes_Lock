package com.projects.company.homes_lock.ui.login.fragment.login;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.projects.company.homes_lock.R;
import com.projects.company.homes_lock.models.datamodels.response.FailureModel;
import com.projects.company.homes_lock.models.viewmodels.LoginViewModel;
import com.projects.company.homes_lock.models.viewmodels.LoginViewModelFactory;
import com.projects.company.homes_lock.ui.device.activity.LockActivity;
import com.projects.company.homes_lock.ui.login.fragment.register.RegisterFragment;
import com.projects.company.homes_lock.utils.helper.ViewHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements ILoginFragment, View.OnClickListener {

    //region Declare Constants
    public static String TAG = LoginFragment.class.getName();
    //endregion Declare Constants

    //region Declare Views
    private TextInputEditText tietEmail;
    private TextInputEditText tietPassword;
    private Button btnLogin;
    private TextView txvDirectConnect;
    private TextView txvSignUp;
    private TextView txvForgetPassword;
    //endregion Declare Views

    //region Declare Variables
    //endregion Declare Variables

    //region Declare Objects
    private LoginViewModel mLoginViewModel;
    //endregion Declare Objects

    public LoginFragment() {
        // Required empty public constructor
    }

    //region Main CallBacks
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //region Initialize Variables
        //endregion Initialize Variables

        //region Initialize Objects
        //endregion Initialize Objects
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //region Initialize Views
        tietEmail = view.findViewById(R.id.tiet_email);
        tietPassword = view.findViewById(R.id.tiet_password);
        btnLogin = view.findViewById(R.id.btn_login);
        txvDirectConnect = view.findViewById(R.id.txv_direct_connect);
        txvSignUp = view.findViewById(R.id.txv_sign_up);
        txvForgetPassword = view.findViewById(R.id.txv_forget_password);
        //endregion Initialize Views

        //region Setup Views
        btnLogin.setOnClickListener(this);
        txvDirectConnect.setOnClickListener(this);
        txvSignUp.setOnClickListener(this);
        txvForgetPassword.setOnClickListener(this);
        //endregion Setup Views

        //region Initialize Objects
        this.mLoginViewModel = ViewModelProviders.of(
                this,
                new LoginViewModelFactory(getActivity().getApplication(), this))
                .get(LoginViewModel.class);
        //endregion Initialize Objects
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                mLoginViewModel.login(tietEmail.getText().toString(), tietPassword.getText().toString());
                break;
            case R.id.txv_direct_connect:
                startActivity(new Intent(getActivity(), LockActivity.class));
                break;
            case R.id.txv_sign_up:
                ViewHelper.setFragment((AppCompatActivity) getActivity(), R.id.frg_login_activity, new RegisterFragment());
                break;
            case R.id.txv_forget_password:
                ViewHelper.setFragment((AppCompatActivity) getActivity(), R.id.frg_login_activity, new ForgetPasswordFragment());
                break;
        }
    }
    //endregion Main CallBacks

    //region Login CallBacks
    @Override
    public void onLoginSuccessful(Object response) {
//        LoginViewModel.insert();
        startActivity(new Intent(getActivity(), LockActivity.class));
    }

    @Override
    public void onLoginFailed(Object response) {
        Log.i(this.getClass().getSimpleName(), ((FailureModel) response).getFailureMessage());
        Toast.makeText(getContext(), ((FailureModel) response).getFailureMessage(), Toast.LENGTH_LONG).show();
    }
    //endregion Login CallBacks

    //region Declare Methods
    //endregion Declare Methods
}
