package com.example.sagivproject.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.os.BundleCompat;

import com.example.sagivproject.R;
import com.example.sagivproject.bases.BaseDialog;
import com.example.sagivproject.models.TipOfTheDay;
import com.example.sagivproject.utils.CalendarUtil;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerativeBackend;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A dialog fragment for adding or editing a Tip of the Day.
 */
@AndroidEntryPoint
public class TipDialog extends BaseDialog {
    private static final String ARG_TIP = "arg_tip";

    @Inject
    CalendarUtil calendarUtil;

    private TipOfTheDay tip;
    private String selectedDateId = null;
    private List<String> existingDates = new ArrayList<>();
    private boolean isInspirationMode = false;
    private TipDialogListener listener;

    @Inject
    public TipDialog() {
    }

    /**
     * Sets the tip data and listener for this dialog.
     *
     * @param tip      The tip to edit, or null if creating a new tip.
     * @param listener Callback for submission.
     */
    public void setData(@Nullable TipOfTheDay tip, TipDialogListener listener) {
        setData(tip, new ArrayList<>(), listener);
    }

    /**
     * Sets the tip data, existing dates for validation, and listener.
     *
     * @param tip           The tip to edit, or null.
     * @param existingDates List of date IDs that already have a tip.
     * @param listener      Callback for submission.
     */
    public void setData(@Nullable TipOfTheDay tip, List<String> existingDates, TipDialogListener listener) {
        Bundle args = new Bundle();
        if (tip != null) {
            args.putSerializable(ARG_TIP, tip);
        }
        setArguments(args);
        this.existingDates = existingDates;
        this.listener = listener;
    }

    /**
     * Sets the dialog to Inspiration mode (no date required).
     */
    public void setInspirationMode(boolean inspirationMode) {
        this.isInspirationMode = inspirationMode;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tip = BundleCompat.getSerializable(getArguments(), ARG_TIP, TipOfTheDay.class);
        }
    }

    /**
     * Returns the layout resource ID for the dialog.
     *
     * @return The ID of the layout resource.
     */
    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_tip;
    }

    /**
     * Initializes the dialog's views, sets up button listeners, and populates data if editing.
     *
     * @param dialog The dialog instance.
     */
    @Override
    protected void setupViews(Dialog dialog) {
        TextView txtTitle = dialog.findViewById(R.id.txtTipDialogTitle);
        EditText inputDate = dialog.findViewById(R.id.inputTipDate);
        TextInputLayout layoutDate = (TextInputLayout) inputDate.getParent().getParent();
        EditText inputContent = dialog.findViewById(R.id.inputTipContent);
        TextInputLayout layoutContent = dialog.findViewById(R.id.layoutTipContent);
        Button btnSave = dialog.findViewById(R.id.btnTipSave);
        Button btnCancel = dialog.findViewById(R.id.btnTipCancel);

        if (tip != null) {
            txtTitle.setText(isInspirationMode ? "עריכת השראה" : "עריכת טיפ");
            inputContent.setText(tip.getTip());
            selectedDateId = tip.getId();
            if (!isInspirationMode) {
                inputDate.setText(calendarUtil.formatDbDateToDisplay(selectedDateId));
                inputDate.setEnabled(false);
                layoutDate.setEndIconVisible(false);
            }
        } else {
            txtTitle.setText(isInspirationMode ? R.string.add_new_inspiration : R.string.add_new_tip);
            if (!isInspirationMode) {
                inputDate.setOnClickListener(v -> calendarUtil.openDatePicker(requireContext(), selectedDateId, (millis, dbDate, dateStr) -> {
                    selectedDateId = dbDate;
                    inputDate.setText(dateStr);
                }, false, false, CalendarUtil.DEFAULT_DATE_FORMAT));
            }
        }

        if (isInspirationMode) {
            layoutDate.setVisibility(View.GONE);
            layoutContent.setHint("תוכן ההשראה");
        }

        GenerativeModel generativeModel = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-2.5-flash-lite");
        GenerativeModelFutures model = GenerativeModelFutures.from(generativeModel);

        layoutContent.setEndIconOnClickListener(v -> {
            inputContent.setText(isInspirationMode ? R.string.creating_inspiration : R.string.creating_tip);
            layoutContent.setEndIconVisible(false);

            String prompt = isInspirationMode ?
                    "תן משפט השראה, סיפור קצר או בדיחה קצרה בעברית. עד 6 משפטים. בלי אימוג'ים. בבקשה תשלח רק את התשובה בלי הקדמה מיותרת" :
                    "תן טיפ יומי קצר לחיים בעברית. עד 6 משפטים. בלי אימוג'ים. בבקשה תשלח רק את התשובה בלי הקדמה מיותרת";
            Content content = new Content.Builder().addText(prompt).build();

            ListenableFuture<GenerateContentResponse> responseFuture = model.generateContent(content);
            Executor mainExecutor = ContextCompat.getMainExecutor(requireContext());

            Futures.addCallback(responseFuture, new FutureCallback<>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String text = result.getText();
                    if (text == null) text = "לא התקבלה תשובה.";
                    inputContent.setText(text);
                    layoutContent.setEndIconVisible(true);
                }

                @Override
                public void onFailure(@NonNull Throwable t) {
                    inputContent.setText(String.format("שגיאה ביצירת טיפ: %s", t.getMessage()));
                    layoutContent.setEndIconVisible(true);
                }
            }, mainExecutor);
        });

        btnSave.setOnClickListener(v -> {
            String content = inputContent.getText().toString().trim();

            if (!isInspirationMode && (selectedDateId == null || content.isEmpty())) {
                Toast.makeText(requireContext(), "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
                return;
            } else if (isInspirationMode && content.isEmpty()) {
                Toast.makeText(requireContext(), "נא להזין תוכן להשראה", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isInspirationMode && tip == null && existingDates.contains(selectedDateId)) {
                Toast.makeText(requireContext(), "כבר קיים טיפ לתאריך זה. נא לבחור תאריך אחר או לערוך את הקיים.", Toast.LENGTH_LONG).show();
                return;
            }

            TipOfTheDay tipData = tip == null ? new TipOfTheDay() : tip;
            tipData.setTip(content);
            tipData.setId(selectedDateId);

            if (listener != null) {
                listener.onSubmit(tipData);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    /**
     * Interface definition for a callback to be invoked when the dialog's form is submitted.
     */
    public interface TipDialogListener {
        /**
         * Called when the user clicks the save button with valid data.
         *
         * @param tip The created or updated tip.
         */
        void onSubmit(TipOfTheDay tip);
    }
}