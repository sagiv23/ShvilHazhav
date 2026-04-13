package com.example.sagivproject.screens;

import android.content.Context;
import android.content.Intent;
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
import com.example.sagivproject.models.User;
import com.example.sagivproject.services.IAdapterService;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.services.IDialogService;
import com.example.sagivproject.ui.AdminMenuFragment;
import com.example.sagivproject.ui.LoggedInMenuFragment;
import com.example.sagivproject.ui.LoggedOutMenuFragment;
import com.example.sagivproject.ui.MenuNavigationListener;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
public abstract class BaseActivity extends AppCompatActivity implements MenuNavigationListener {
    private static final String TAG = "BaseActivity";
    /**
     * Utility for managing local user preferences and session.
     */
    @Inject
    protected SharedPreferencesUtil sharedPreferencesUtil;
    /**
     * The central database service façade.
     */
    @Inject
    protected IDatabaseService databaseService;
    /**
     * Activity-scoped service for providing RecyclerView adapters.
     */
    @Inject
    protected IAdapterService adapterService;
    /**
     * Activity-scoped service for showing UI dialogs.
     */
    @Inject
    protected IDialogService dialogService;
    /**
     * The root layout for the navigation drawer.
     */
    protected DrawerLayout drawerLayout;
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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
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

                Class<? extends Fragment> targetFragmentClass = (currentUser != null)
                        ? LoggedInMenuFragment.class
                        : LoggedOutMenuFragment.class;

                Fragment currentMenuFrag = getSupportFragmentManager().findFragmentById(R.id.drawer_menu_container);
                if (currentMenuFrag == null || !currentMenuFrag.getClass().equals(targetFragmentClass)) {
                    try {
                        Fragment menuFragment = targetFragmentClass.getDeclaredConstructor().newInstance();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.drawer_menu_container, menuFragment)
                                .commit();
                    } catch (Exception e) {
                        Log.e(TAG, "Error creating drawer menu fragment instance", e);
                    }
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
                        .replace(topMenuContainer.getId(), new AdminMenuFragment())
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
     * Navigates to a destination identified by a resource ID.
     *
     * @param resId The resource ID of the target destination (e.g., {@code R.id.mainActivity}).
     */
    @Override
    public void onNavigate(int resId) {
        onNavigate(resId, null);
    }

    /**
     * Centralized navigation logic to the appropriate home screen based on user role.
     *
     * @param user The authenticated user.
     */
    protected void navigateToUserHome(User user) {
        if (sharedPreferencesUtil.isUserNotLoggedIn()) {
            onNavigate(R.id.landingActivity);
            return;
        }
        Intent intent;
        if (user.isAdmin()) {
            intent = new Intent(this, AdminPageActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
     * Sets a personalized greeting message in a TextView.
     *
     * @param textViewId The ID of the TextView to update.
     * @param user       The user whose name will be displayed.
     */
    protected void setGreeting(int textViewId, User user) {
        if (user != null) {
            TextView textView = findViewById(textViewId);
            if (textView != null) {
                textView.setText(String.format("שלום %s", user.getFullName()));
            }
        }
    }

    /**
     * Navigates to a destination with optional arguments and a standard slide animation.
     *
     * @param resId The resource ID of the target destination.
     * @param args  Optional Bundle of arguments to pass to the target activity.
     */
    @Override
    public void onNavigate(int resId, Bundle args) {
        closeDrawer();
        Intent intent = null;

        if (resId == R.id.mainActivity) {
            intent = new Intent(this, MainActivity.class);
        } else if (resId == R.id.landingActivity) {
            intent = new Intent(this, LandingActivity.class);
        } else if (resId == R.id.contactActivity) {
            intent = new Intent(this, ContactActivity.class);
        } else if (resId == R.id.detailsAboutUserActivity) {
            intent = new Intent(this, DetailsAboutUserActivity.class);
        } else if (resId == R.id.settingsActivity) {
            intent = new Intent(this, SettingsActivity.class);
        } else if (resId == R.id.loginActivity) {
            intent = new Intent(this, LoginActivity.class);
        } else if (resId == R.id.registerActivity) {
            intent = new Intent(this, RegisterActivity.class);
        } else if (resId == R.id.adminPageActivity) {
            intent = new Intent(this, AdminPageActivity.class);
        } else if (resId == R.id.nav_logout) {
            sharedPreferencesUtil.signOutUser();
            intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }

        if (intent != null) {
            if (intent.getComponent() != null && this.getClass().getName().equals(intent.getComponent().getClassName())) {
                return;
            }
            if (args != null) intent.putExtras(args);

            ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(
                    this, R.anim.slide_in_right, R.anim.slide_out_left);

            startActivity(intent, options.toBundle());
        }
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
            if (ContextCompat.checkSelfPermission(this, p) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
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
}