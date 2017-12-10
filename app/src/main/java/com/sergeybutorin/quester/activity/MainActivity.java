package com.sergeybutorin.quester.activity;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.sergeybutorin.quester.R;
import com.sergeybutorin.quester.fragment.AuthFragment;
import com.sergeybutorin.quester.fragment.QMapFragment;
import com.sergeybutorin.quester.fragment.QuestAddFragment;
import com.sergeybutorin.quester.model.Quest;
import com.sergeybutorin.quester.model.UserProfile;
import com.sergeybutorin.quester.utils.SPHelper;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        QMapFragment.QuestAddListener,
        QuestAddFragment.QuestSavedListener {

    private TextView nameTextView;
    private TextView emailTextView;
    private NavigationView navigationView;
    private Menu menu;
    private MenuItem loginItem;
    private MenuItem logoutItem;
    private SPHelper spHelper;

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

        spHelper = SPHelper.getInstance(getApplicationContext());

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
            case R.id.nav_logout:
                spHelper.removeUserData();
                setUserInformation();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setUserInformation() {
        UserProfile user = spHelper.getCurrentUser();
        if (user != null) {
            String name = user.getFirstName() + " " + user.getLastName();
            nameTextView.setText(name);

            emailTextView.setVisibility(View.VISIBLE);
            emailTextView.setText(user.getEmail());

            loginItem.setVisible(false);
            logoutItem.setVisible(true);
        } else {
            nameTextView.setText(R.string.guest_name);

            emailTextView.setVisibility(View.GONE);

            loginItem.setVisible(true);
            logoutItem.setVisible(false);
        }
    }

    @Override
    public void onPointsAdded(Quest quest) {
        QuestAddFragment questAddFragment = new QuestAddFragment();
        Bundle args = new Bundle();
        args.putSerializable(QuestAddFragment.QUEST_ARG, quest);
        questAddFragment.setArguments(args);
        getSupportFragmentManager()
                .beginTransaction().replace(R.id.content, questAddFragment).commit();
    }

    @Override
    public void onQuestSaved(Quest quest) {
        QMapFragment qMapFragment = new QMapFragment();
        Bundle args = new Bundle();
        args.putSerializable(QMapFragment.QUEST_ARG, quest);
        qMapFragment.setArguments(args);
        getSupportFragmentManager()
                .beginTransaction().replace(R.id.content, qMapFragment).commit();
    }
}
