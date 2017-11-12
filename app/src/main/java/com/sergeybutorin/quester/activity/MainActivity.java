package com.sergeybutorin.quester.activity;

import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sergeybutorin.quester.Constants;
import com.sergeybutorin.quester.R;
import com.sergeybutorin.quester.fragment.AuthFragment;
import com.sergeybutorin.quester.fragment.QMapFragment;
import com.sergeybutorin.quester.network.AuthController;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    TextView nameTextView;
    TextView emailTextView;
    NavigationView navigationView;
    Menu menu;
    MenuItem loginItem;
    MenuItem logoutItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        nameTextView = headerView.findViewById(R.id.user_name);
        emailTextView = headerView.findViewById(R.id.user_email);
        menu = navigationView.getMenu();
        loginItem = menu.findItem(R.id.nav_login);
        logoutItem = menu.findItem(R.id.nav_logout);

        setUserInformation();

        getSupportFragmentManager().beginTransaction().replace(R.id.content, new QMapFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.nav_login:
                getSupportFragmentManager().beginTransaction().replace(R.id.content, new AuthFragment()).commit();
                break;
            case R.id.nav_map:
                getSupportFragmentManager().beginTransaction().replace(R.id.content, new QMapFragment()).commit();
                break;
            case R.id.nav_my_places:
                Toast.makeText(this, "My places", Toast.LENGTH_LONG).show();
                break;
            case R.id.nav_logout:
                AuthController.logout(getApplicationContext());
                setUserInformation();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setUserInformation() {
        if (AuthController.isAuthorized(getApplicationContext())) {
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String name = sp.getString(Constants.USER_KEYS.FIRSTNAME.getValue(), null) + " " +
                    sp.getString(Constants.USER_KEYS.LASTNAME.getValue(), null);
            nameTextView.setText(name);

            emailTextView.setVisibility(View.VISIBLE);
            emailTextView.setText(sp.getString(Constants.USER_KEYS.EMAIL.getValue(), null));

            loginItem.setVisible(false);
            logoutItem.setVisible(true);
        } else {
            nameTextView.setText(R.string.guest_name);

            emailTextView.setVisibility(View.GONE);

            loginItem.setVisible(true);
            logoutItem.setVisible(false);
        }
    }
}
