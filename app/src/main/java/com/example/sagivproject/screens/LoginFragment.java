package com.example.sagivproject.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseFragment;
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IAuthService;
import com.example.sagivproject.utils.Validator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A fragment for user login.
 */
@AndroidEntryPoint
public class LoginFragment extends BaseFragment {
    @Inject
    protected Validator validator;
    private EditText editTextEmail, editTextPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnLogin = view.findViewById(R.id.btnLogin);
        editTextEmail = view.findViewById(R.id.edt_login_email);
        editTextPassword = view.findViewById(R.id.edt_login_password);

        btnLogin.setOnClickListener(v -> tryLogin());

        // Using Safe Args to retrieve arguments with null check
        if (getArguments() != null) {
            LoginFragmentArgs args = LoginFragmentArgs.fromBundle(getArguments());
            String lastEmail = args.getUserEmail();
            if (lastEmail != null && !lastEmail.isEmpty()) {
                editTextEmail.setText(lastEmail);
            }
        }
    }

    private void tryLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!validateInput(email, password)) {
            return;
        }

        databaseService.getAuthService().login(email, password, new IAuthService.LoginCallback() {
            @Override
            public void onSuccess(User user) {
                sharedPreferencesUtil.saveUser(user);

                if (user.isAdmin()) {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "התחברת למשתמש מנהל בהצלחה!", Toast.LENGTH_SHORT).show();
                    navigateTo(R.id.action_loginFragment_to_adminPageFragment);
                } else {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "התחברת בהצלחה!", Toast.LENGTH_SHORT).show();
                    navigateTo(R.id.action_loginFragment_to_mainFragment);
                }
            }

            @Override
            public void onError(String message) {
                if (getContext() != null)
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            if (getContext() != null)
                Toast.makeText(getContext(), "נא למלא אימייל וסיסמה", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (validator.isEmailNotValid(email)) {
            editTextEmail.requestFocus();
            if (getContext() != null)
                Toast.makeText(getContext(), "כתובת האימייל אינה תקינה", Toast.LENGTH_LONG).show();
            return false;
        }

        if (validator.isPasswordNotValid(password)) {
            editTextPassword.requestFocus();
            if (getContext() != null)
                Toast.makeText(getContext(), "הסיסמה קצרה מדי", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
}
