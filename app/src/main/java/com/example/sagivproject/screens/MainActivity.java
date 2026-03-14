package com.example.sagivproject.screens;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.sagivproject.R;
import com.example.sagivproject.models.User;
import com.example.sagivproject.ui.AdminMenuFragment;
import com.example.sagivproject.ui.LoggedInMenuFragment;
import com.example.sagivproject.ui.LoggedOutMenuFragment;
import com.example.sagivproject.ui.MenuNavigationListener;
import com.example.sagivproject.utils.SharedPreferencesUtil;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * The main activity that hosts all fragments in the application.
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements MenuNavigationListener {
    private static final String TAG = "MainActivity";
    @Inject
    protected SharedPreferencesUtil sharedPreferencesUtil;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawer_layout);
        setupMenu();
    }

    /**
     * Updates the UI state (Drawer and Top Bar) based on user state.
     */
    public void setupMenu() {
        setupMenu(null);
    }

    /**
     * Updates the UI state (Drawer and Top Bar) based on user state.
     *
     * @param fragment The current fragment to configure its top bar if it has one.
     */
    public void setupMenu(Fragment fragment) {
        if (drawerLayout == null) return;

        User currentUser = sharedPreferencesUtil.getUser();
        boolean isAdmin = currentUser != null && currentUser.isAdmin();

        // 1. Drawer Management (Global Activity Level)
        if (isAdmin) {
            // Admins don't use the side drawer
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            Fragment currentMenuFrag = getSupportFragmentManager().findFragmentById(R.id.drawer_menu_container);
            if (currentMenuFrag != null) {
                getSupportFragmentManager().beginTransaction().remove(currentMenuFrag).commit();
            }
        } else {
            // Non-admins (logged in or out) use the side drawer
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

        // 2. Top Bar Management (Per-Fragment Level)
        if (fragment != null && fragment.getView() != null) {
            View view = fragment.getView();
            View btnOpenDrawer = view.findViewById(R.id.btn_open_drawer);
            View topBarTitle = view.findViewById(R.id.topBarTitle);
            ViewGroup topMenuContainer = view.findViewById(R.id.topMenuContainer);

            if (isAdmin) {
                // Hide standard top bar elements and show AdminMenuFragment
                if (btnOpenDrawer != null) btnOpenDrawer.setVisibility(View.GONE);
                if (topBarTitle != null) topBarTitle.setVisibility(View.GONE);
                if (topMenuContainer != null) {
                    topMenuContainer.setVisibility(View.VISIBLE);
                    Fragment existing = fragment.getChildFragmentManager().findFragmentById(topMenuContainer.getId());
                    if (!(existing instanceof AdminMenuFragment)) {
                        fragment.getChildFragmentManager().beginTransaction()
                                .replace(topMenuContainer.getId(), new AdminMenuFragment())
                                .commit();
                    }
                }
            } else {
                // Show standard top bar elements and hide the admin menu container
                if (btnOpenDrawer != null) {
                    btnOpenDrawer.setVisibility(View.VISIBLE);
                    btnOpenDrawer.setOnClickListener(v -> openDrawer());
                }
                if (topBarTitle != null) topBarTitle.setVisibility(View.VISIBLE);
                if (topMenuContainer != null) {
                    topMenuContainer.setVisibility(View.GONE);
                }
            }
        }
    }

    public void openDrawer() {
        if (drawerLayout != null && drawerLayout.getDrawerLockMode(GravityCompat.END) == DrawerLayout.LOCK_MODE_UNLOCKED) {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    public void closeDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.END);
        }
    }

    @Override
    public void onNavigate(int resId) {
        onNavigate(resId, null);
    }

    @Override
    public void onNavigate(int resId, Bundle args) {
        closeDrawer();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != resId) {
            NavOptions navOptions = new NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_up)
                    .setExitAnim(R.anim.slide_out_left)
                    .setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_down)
                    .build();
            navController.navigate(resId, args, navOptions);
        }
    }
}
