package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Base class for dialog fragments to reduce boilerplate code.
 */
public abstract class BaseDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(getLayoutResourceId());

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        setupView(dialog);
        return dialog;
    }

    /**
     * @return The layout resource ID for the dialog.
     */
    protected abstract int getLayoutResourceId();

    /**
     * Initialize views and listeners here.
     *
     * @param dialog The created dialog.
     */
    protected abstract void setupView(Dialog dialog);
}
