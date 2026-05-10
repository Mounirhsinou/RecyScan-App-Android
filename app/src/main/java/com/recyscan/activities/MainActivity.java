package com.recyscan.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.recyscan.R;
import com.recyscan.fragments.GuideFragment;
import com.recyscan.fragments.HomeFragment;
import com.recyscan.fragments.ReminderFragment;
import com.recyscan.fragments.SettingsFragment;
import com.recyscan.utils.LocaleHelper;
import com.recyscan.utils.ThemeHelper;

import android.content.Context;

/**
 * Main Activity hosting the bottom navigation and fragment container.
 * Manages navigation between Home, Guide, Reminder, and Settings screens.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup bottom navigation
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navItemSelectedListener);

        // Load Home fragment by default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    /**
     * Bottom navigation item selection listener.
     * Switches between fragments based on selected tab.
     */
    private final NavigationBarView.OnItemSelectedListener navItemSelectedListener =
            new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment fragment = null;
                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_home) {
                        fragment = new HomeFragment();
                    } else if (itemId == R.id.nav_guide) {
                        fragment = new GuideFragment();
                    } else if (itemId == R.id.nav_scanner) {
                        // Launch scanner activity instead of fragment
                        launchScanner();
                        return false; // Don't select this tab
                    } else if (itemId == R.id.nav_reminders) {
                        fragment = new ReminderFragment();
                    } else if (itemId == R.id.nav_settings) {
                        fragment = new SettingsFragment();
                    }

                    if (fragment != null) {
                        loadFragment(fragment);
                        return true;
                    }
                    return false;
                }
            };

    /**
     * Replace the current fragment in the container.
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out
                )
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /**
     * Launch the barcode scanner activity.
     */
    private void launchScanner() {
        android.content.Intent intent = new android.content.Intent(this, ScannerActivity.class);
        startActivity(intent);
    }

    /**
     * Select the Home tab programmatically.
     */
    public void selectHomeTab() {
        bottomNav.setSelectedItemId(R.id.nav_home);
    }
}
