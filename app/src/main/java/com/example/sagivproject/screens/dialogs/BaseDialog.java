package com.example.sagivproject.screens.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.sagivproject.utils.SharedPreferencesUtil;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An abstract base class for all dialog fragments in the application.
 * <p>
 * This class provides common infrastructure for dialogs, including:
 * <ul>
 * <li>Hilt dependency injection for common services.</li>
 * <li>Standardized dialog initialization (content view and transparent background).</li>
 * </ul>
 * </p>
 */
@AndroidEntryPoint
public abstract class BaseDialog extends DialogFragment {

    /**
     * Utility for managing local user preferences and session.
     */
    @Inject
    protected SharedPreferencesUtil sharedPreferencesUtil;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = createDialogInstance(savedInstanceState);
        dialog.setContentView(getLayoutResId());

        Window window = dialog.getWindow();
        if (window != null && shouldSetTransparentBackground()) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        setupViews(dialog);
        return dialog;
    }

    /**
     * Creates the dialog instance. Subclasses can override this to return a custom dialog type
     * or use a specific theme. By default, it uses the DialogFragment's internal theme logic.
     *
     * @param savedInstanceState The last saved instance state of the Fragment, or null.
     * @return A new Dialog instance.
     */
    @NonNull
    protected Dialog createDialogInstance(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    /**
     * Determines whether the dialog window should have a transparent background.
     * Subclasses can override this to return false if they use a custom theme.
     *
     * @return true by default.
     */
    protected boolean shouldSetTransparentBackground() {
        return true;
    }

    /**
     * Returns the layout resource ID for the dialog's content view.
     *
     * @return The layout resource ID.
     */
    @LayoutRes
    protected abstract int getLayoutResId();

    /**
     * Performs view initialization and event listener setup.
     *
     * @param dialog The dialog instance.
     */
    protected abstract void setupViews(Dialog dialog);
}
