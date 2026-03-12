package com.example.sagivproject.screens;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.sagivproject.R;
import com.example.sagivproject.ui.MenuNavigationListener;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * The main activity that hosts all fragments in the application.
 */
@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements MenuNavigationListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Navigates to a specific destination in the navigation graph with custom animations.
     *
     * @param resId The resource ID of the destination or action.
     */
    @Override
    public void onNavigate(int resId) {
        onNavigate(resId, null);
    }

    /**
     * Navigates to a specific destination in the navigation graph with arguments and custom animations.
     *
     * @param resId The resource ID of the destination or action.
     * @param args  The arguments to pass.
     */
    @Override
    public void onNavigate(int resId, Bundle args) {
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
