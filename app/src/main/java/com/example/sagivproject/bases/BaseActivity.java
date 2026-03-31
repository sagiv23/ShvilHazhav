package com.example.sagivproject.bases;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;
import com.example.sagivproject.screens.AdminPageActivity;
import com.example.sagivproject.screens.ContactActivity;
import com.example.sagivproject.screens.DetailsAboutUserActivity;
import com.example.sagivproject.screens.LandingActivity;
import com.example.sagivproject.screens.LoginActivity;
import com.example.sagivproject.screens.MainActivity;
import com.example.sagivproject.screens.RegisterActivity;
import com.example.sagivproject.screens.SettingsActivity;
import com.example.sagivproject.services.IAdapterService;
import com.example.sagivproject.services.IDatabaseService;
import com.example.sagivproject.services.IDialogService;
import com.example.sagivproject.ui.AdminMenuFragment;
import com.example.sagivproject.ui.LoggedInMenuFragment;
import com.example.sagivproject.ui.LoggedOutMenuFragment;
import com.example.sagivproject.ui.MenuNavigationListener;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * An abstract base class for all activities in the application.
 * <p>
 * This class provides common infrastructure for activities, including:
 * <ul>
 *     <li>Hilt dependency injection for common services (Database, Dialogs, Adapters).</li>
 *     <li>Standardized navigation drawer and top bar management.</li>
 *     <li>A unified transition animation system between activities.</li>
 *     <li>Centralized handling of runtime permissions.</li>
 * </ul>
 * </p>
 */
@AndroidEntryPoint
public abstract class BaseActivity extends AppCompatActivity implements MenuNavigationListener {
    private static final String TAG = "BaseActivity";

    /**
     * Standard launcher for requesting multiple runtime permissions.
     * Results are handled in {@link #onPermissionsResult(Map)}.
     */
    protected final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionsResult);

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                // Admins use a top bar menu instead of a drawer
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
     * Requests a set of runtime permissions from the user.
     *
     * @param permissions A variable list of permission strings (e.g., {@code Manifest.permission.CAMERA}).
     */
    protected void requestPermissions(String... permissions) {
        requestPermissionLauncher.launch(permissions);
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
