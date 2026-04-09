package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sagivproject.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A generic dialog for taking a single string input.
 */
@AndroidEntryPoint
public class SingleInputDialog extends BaseDialog {
    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_HINT = "arg_hint";
    private static final String ARG_INITIAL_VALUE = "arg_initial_value";

    private OnInputSubmitListener listener;

    @Inject
    public SingleInputDialog() {
    }

    public void setData(String title, String hint, String initialValue, OnInputSubmitListener listener) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_HINT, hint);
        args.putString(ARG_INITIAL_VALUE, initialValue);
        setArguments(args);
        this.listener = listener;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.dialog_single_input;
    }

    @Override
    protected void setupView(Dialog dialog) {
        Bundle args = getArguments();
        if (args == null) return;

        TextView txtTitle = dialog.findViewById(R.id.txt_singleInput_title);
        TextInputLayout layoutInput = dialog.findViewById(R.id.layout_singleInput_input);
        TextInputEditText edtValue = dialog.findViewById(R.id.edt_singleInput_value);
        Button btnSave = dialog.findViewById(R.id.btn_singleInput_save);
        Button btnCancel = dialog.findViewById(R.id.btn_singleInput_cancel);

        txtTitle.setText(args.getString(ARG_TITLE));
        layoutInput.setHint(args.getString(ARG_HINT));
        edtValue.setText(args.getString(ARG_INITIAL_VALUE));

        btnSave.setOnClickListener(v -> {
            String input = Objects.requireNonNull(edtValue.getText()).toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(requireContext(), "השדה לא יכול להיות ריק", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onSubmit(input);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    public interface OnInputSubmitListener {
        void onSubmit(String input);
    }
}
