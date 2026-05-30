package com.example.sagivproject.bases;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.sagivproject.R;
import com.example.sagivproject.dialogs.LoadingDialog;
import com.example.sagivproject.models.User;
import com.example.sagivproject.screens.AdminPageActivity;
import com.example.sagivproject.screens.LandingActivity;
import com.example.sagivproject.screens.LoginActivity;
import com.example.sagivproject.screens.MainActivity;
import com.example.sagivproject.ui.AppMenuFragment;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An abstract base class for all activities in the application.
 * <p>
 * This class provides common infrastructure for activities, including:
 * <ul>
 * <li>Hilt dependency injection for common services (Database, Dialogs, Adapters).</li>
 * <li>Standardized navigation drawer and top bar management.</li>
 * <li>A unified transition animation system between activities.</li>
 * <li>Centralized handling of runtime permissions.</li>
 * </ul>
 * </p>
 */
@AndroidEntryPoint
public abstract class BaseActivity extends AppCompatActivity implements AppMenuFragment.OnNavigationListener {
    /**
     * Utility for managing local user preferences and session.
     */
    @Inject
    protected SharedPreferencesUtil sharedPreferencesUtil;
    /**
     * The root layout for the navigation drawer.
     */
    protected DrawerLayout drawerLayout;

    @Inject
    protected Provider<LoadingDialog> loadingDialogProvider;

    private LoadingDialog currentLoadingDialog;

    /**
     * Dialog for showing loading animation.
     */
    private int loadingCount = 0;

    /**
     * Callback to execute once a pending permission request is successfully granted.
     */
    private Runnable onPermissionGrantedCallback;

    /**
     * Standard launcher for requesting multiple runtime permissions.
     * Results are handled in {@link #onPermissionsResult(Map)}.
     */
    protected final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                onPermissionsResult(result);
                if (onPermissionGrantedCallback != null) {
                    boolean allGranted = true;
                    for (boolean granted : result.values()) {
                        if (!granted) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (allGranted) {
                        onPermissionGrantedCallback.run();
                    }
                    onPermissionGrantedCallback = null;
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Lifecycle", "onCreate: " + getClass().getSimpleName());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Lifecycle", "onStart: " + getClass().getSimpleName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Lifecycle", "onResume: " + getClass().getSimpleName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Lifecycle", "onPause: " + getClass().getSimpleName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Lifecycle", "onStop: " + getClass().getSimpleName());
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("Lifecycle", "onRestart: " + getClass().getSimpleName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Lifecycle", "onDestroy: " + getClass().getSimpleName());
    }

    /**
     * Initializes the activity with Edge-to-Edge support and window insets handling.
     *
     * @param layoutResID The layout resource to inflate.
     * @param rootId      The ID of the root view to apply insets to.
     */
    protected void setContent(@LayoutRes int layoutResID, @IdRes int rootId) {
        EdgeToEdge.enable(this);
        setContentView(layoutResID);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(rootId), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
            return insets;
        });
    }

    /**
     * Sets up the common menu UI components, including the Navigation Drawer and Top Bar.
     * <p>
     * This method dynamically injects the appropriate menu fragment (Admin, LoggedIn, or LoggedOut)
     * based on the current user's state. It also configures the visibility of top bar elements.
     * This should be called in {@code onCreate} after {@code setContentView}.
     * </p>
     */
    protected void setupMenu() {
        drawerLayout = findViewById(R.id.drawer_layout);
        User currentUser = sharedPreferencesUtil.getUser();
        boolean isAdmin = currentUser != null && currentUser.isAdmin();

        if (drawerLayout != null) {
            if (isAdmin) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

                Fragment currentMenuFrag = getSupportFragmentManager().findFragmentById(R.id.drawer_menu_container);
                if (!(currentMenuFrag instanceof AppMenuFragment) || ((AppMenuFragment) currentMenuFrag).getMenuType() == AppMenuFragment.MenuType.ADMIN) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.drawer_menu_container, AppMenuFragment.newInstance(AppMenuFragment.MenuType.LOGGED_IN))
                            .commit();
                }
            }
        }

        View btnOpenDrawer = findViewById(R.id.btn_open_drawer);
        View topBarTitle = findViewById(R.id.topBarTitle);
        ViewGroup topMenuContainer = findViewById(R.id.topMenuContainer);

        if (isAdmin) {
            if (btnOpenDrawer != null) btnOpenDrawer.setVisibility(View.GONE);
            if (topBarTitle != null) topBarTitle.setVisibility(View.GONE);
            if (topMenuContainer != null) {
                topMenuContainer.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction()
                        .replace(topMenuContainer.getId(), AppMenuFragment.newInstance(AppMenuFragment.MenuType.ADMIN))
                        .commit();
            }
        } else {
            if (btnOpenDrawer != null) {
                btnOpenDrawer.setVisibility(View.VISIBLE);
                btnOpenDrawer.setOnClickListener(v -> openDrawer());
            }
            if (topBarTitle != null) topBarTitle.setVisibility(View.VISIBLE);
            if (topMenuContainer != null) topMenuContainer.setVisibility(View.GONE);
        }
    }

    /**
     * Opens the navigation drawer if it exists in the current layout.
     */
    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    /**
     * Closes the navigation drawer if it is currently open.
     */
    public void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    /**
     * Centralized navigation logic to the appropriate home screen based on user role.
     *
     * @param user The authenticated user.
     */
    protected void navigateToUserHome(User user) {
        if (sharedPreferencesUtil.isUserNotLoggedIn()) {
            onNavigate(new Intent(this, LandingActivity.class));
            return;
        }

        Class<? extends AppCompatActivity> homeClass = user.isAdmin() ? AdminPageActivity.class : MainActivity.class;
        onNavigate(new Intent(this, homeClass)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    /**
     * Helper to create a styled adapter for spinners used in search/filter UI.
     *
     * @param options Array of strings to display in the spinner.
     * @return A styled ArrayAdapter.
     */
    @NonNull
    protected ArrayAdapter<String> createStyledSearchAdapter(String[] options) {
        return createStyledSearchAdapter(Arrays.asList(options));
    }

    /**
     * Helper to create a styled adapter for spinners used in search/filter UI.
     *
     * @param options List of strings to display in the spinner.
     * @return A styled ArrayAdapter.
     */
    @NonNull
    protected ArrayAdapter<String> createStyledSearchAdapter(List<String> options) {
        return new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTypeface(ResourcesCompat.getFont(BaseActivity.this, R.font.text_hebrew));
                tv.setTextSize(22);
                tv.setTextColor(ContextCompat.getColor(BaseActivity.this, R.color.text_color));
                tv.setPadding(24, 24, 24, 24);
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTypeface(ResourcesCompat.getFont(BaseActivity.this, R.font.text_hebrew));
                tv.setTextSize(22);
                tv.setTextColor(ContextCompat.getColor(BaseActivity.this, R.color.text_color));
                tv.setBackgroundColor(ContextCompat.getColor(BaseActivity.this, R.color.background_color_buttons));
                tv.setPadding(24, 24, 24, 24);
                return tv;
            }
        };
    }

    /**
     * Sets a personalized greeting message in a TextView with a professional fade-in.
     *
     * @param textViewId The ID of the TextView to update.
     */
    protected void setGreeting(int textViewId) {
        User user = sharedPreferencesUtil.getUser();
        if (user != null) {
            TextView textView = findViewById(textViewId);
            if (textView != null) {
                textView.setAlpha(0f);
                textView.setText(String.format("שלום %s", user.getFullName()));
                textView.animate()
                        .alpha(1f)
                        .setDuration(1000)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();
            }
        }
    }

    /**
     * Navigates to a destination using a pre-configured Intent.
     * Handles common logic like closing the drawer and applying professional transitions.
     *
     * @param intent The intent to launch.
     */
    @Override
    public void onNavigate(Intent intent) {
        if (intent == null) return;

        closeDrawer();

        // Prevent navigating to the same activity
        boolean clearStack = (intent.getFlags() & Intent.FLAG_ACTIVITY_CLEAR_TASK) != 0;
        if (intent.getComponent() != null &&
                this.getClass().getName().equals(intent.getComponent().getClassName()) &&
                !clearStack) {
            return;
        }

        // Professional Crossfade/Slide transition
        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(
                this, android.R.anim.fade_in, android.R.anim.fade_out);

        startActivity(intent, options.toBundle());
    }

    /**
     * Logs out the user, clears the session, and redirects to the login screen.
     */
    @Override
    public void onLogout() {
        User user = sharedPreferencesUtil.getUser();
        String email = (user != null) ? user.getEmail() : "";

        sharedPreferencesUtil.signOutUser();

        onNavigate(new Intent(this, LoginActivity.class)
                .putExtra("userEmail", email)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    /**
     * Triggers a short vibration.
     */
    protected void vibrateDevice() {
        Vibrator vibrator;
        VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        vibrator = vibratorManager.getDefaultVibrator();

        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    /**
     * Helper to check and request permissions, then execute a callback if all granted.
     */
    protected void runWithPermissions(Runnable action, String... permissions) {
        boolean allGranted = true;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            action.run();
        } else {
            this.onPermissionGrantedCallback = action;
            requestPermissions(permissions);
        }
    }

    protected void runWithPermission(String permission, Runnable action) {
        runWithPermissions(action, permission);
    }

    /**
     * Requests a set of runtime permissions from the user.
     * Internal use only via runWithPermissions.
     *
     * @param permissions A variable list of permission strings.
     */
    private void requestPermissions(String... permissions) {
        requestPermissionLauncher.launch(permissions);
    }

    /**
     * Opens the system settings screen for the current application.
     */
    protected void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    /**
     * Callback method invoked when the results of a permission request are available.
     * Subclasses should override this to handle specific permission outcomes.
     *
     * @param isGranted A map where the keys are permissions and values are their granted status.
     */
    protected void onPermissionsResult(Map<String, Boolean> isGranted) {
    }

    /**
     * Shows a non-cancelable loading dialog.
     * Uses a counter to handle multiple concurrent loading processes.
     */
    protected void showLoading() {
        if (isFinishing() || isDestroyed()) return;

        loadingCount++;
        if (loadingCount == 1) {
            if (currentLoadingDialog == null || !currentLoadingDialog.isAdded()) {
                currentLoadingDialog = loadingDialogProvider.get();
                // Ensure we don't show after state is saved to avoid crashes during config changes
                if (!getSupportFragmentManager().isStateSaved()) {
                    currentLoadingDialog.show(getSupportFragmentManager(), "LoadingDialog");
                }
            }
        }
    }

    /**
     * Hides the loading dialog if it is currently visible and no other processes are loading.
     */
    protected void hideLoading() {
        loadingCount--;
        if (loadingCount <= 0) {
            loadingCount = 0;
            if (currentLoadingDialog != null && currentLoadingDialog.isAdded()) {
                // Use dismissAllowingStateLoss to prevent crashes during theme switches or background returns
                currentLoadingDialog.dismissAllowingStateLoss();
                currentLoadingDialog = null;
            }
        }
    }
}