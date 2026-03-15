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
import com.example.sagivproject.utils.CalendarUtil;
import com.example.sagivproject.utils.Validator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A fragment that handles the user registration process.
 * <p>
 * This fragment provides a form for new users to enter their personal details,
 * including name, birthdate, email, and password. It validates the input
 * using the {@link Validator} and {@link CalendarUtil} utilities and
 * uses the {@link IAuthService} to create a new user account.
 * </p>
 */
@AndroidEntryPoint
public class RegisterFragment extends BaseFragment {
    /**
     * Utility for date picking and formatting.
     */
    @Inject
    protected CalendarUtil calendarUtil;

    /**
     * Utility for validating user input.
     */
    @Inject
    protected Validator validator;

    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword, editTextBirthDate;
    private long birthDateMillis = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnRegister = view.findViewById(R.id.btnRegister);
        editTextFirstName = view.findViewById(R.id.edt_register_first_name);
        editTextLastName = view.findViewById(R.id.edt_register_last_name);
        editTextBirthDate = view.findViewById(R.id.edt_register_birth_date);
        editTextEmail = view.findViewById(R.id.edt_register_email);
        editTextPassword = view.findViewById(R.id.edt_register_password);

        editTextBirthDate.setFocusable(false);
        editTextBirthDate.setClickable(true);
        editTextBirthDate.setOnClickListener(v -> openDatePicker());
        btnRegister.setOnClickListener(v -> tryRegister());
    }

    /**
     * Attempts to register a new user with the provided details.
     */
    private void tryRegister() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String birthDate = editTextBirthDate.getText().toString();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!validateInput(firstName, lastName, birthDate, email, password)) {
            return;
        }

        databaseService.getAuthService().register(firstName, lastName, birthDateMillis, email, password, new IAuthService.RegisterCallback() {
            @Override
            public void onSuccess(User user) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "ההרשמה בוצעה בהצלחה!", Toast.LENGTH_SHORT).show();
                sharedPreferencesUtil.saveUser(user);
                navigateTo(R.id.action_registerFragment_to_mainFragment);
            }

            @Override
            public void onError(String message) {
                if (getContext() != null)
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Validates the registration input fields.
     *
     * @param firstName The first name entered.
     * @param lastName  The last name entered.
     * @param birthDate The birthdate string.
     * @param email     The email address.
     * @param password  The password.
     * @return true if all inputs are valid, false otherwise.
     */
    private boolean validateInput(String firstName, String lastName, String birthDate, String email, String password) {
        if (firstName.isEmpty() || lastName.isEmpty() || birthDate.isEmpty() || email.isEmpty() || password.isEmpty()) {
            if (getContext() != null)
                Toast.makeText(getContext(), "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (validator.isNameNotValid(firstName)) {
            editTextFirstName.requestFocus();
            if (getContext() != null)
                Toast.makeText(getContext(), "שם פרטי קצר מדי", Toast.LENGTH_LONG).show();
            return false;
        }

        if (validator.isNameNotValid(lastName)) {
            editTextLastName.requestFocus();
            if (getContext() != null)
                Toast.makeText(getContext(), "שם משפחה קצר מדי", Toast.LENGTH_LONG).show();
            return false;
        }

        if (birthDateMillis <= 0) {
            editTextBirthDate.requestFocus();
            if (getContext() != null)
                Toast.makeText(getContext(), "נא לבחור תאריך לידה", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (validator.isAgeNotValid(birthDateMillis)) {
            editTextBirthDate.requestFocus();
            if (getContext() != null)
                Toast.makeText(getContext(), "הגיל המינימלי להרשמה הוא 12", Toast.LENGTH_LONG).show();
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

    /**
     * Opens a date picker dialog to select the user's birthdate.
     */
    private void openDatePicker() {
        if (getActivity() == null) return;
        calendarUtil.openDatePicker(getActivity(), birthDateMillis, (dateMillis, formattedDate) -> {
            this.birthDateMillis = dateMillis;
            editTextBirthDate.setText(formattedDate);
        }, false, true, CalendarUtil.DEFAULT_DATE_FORMAT);
    }
}
