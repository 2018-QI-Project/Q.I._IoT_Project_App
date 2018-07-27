package qualcomminstitute.iot;

import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();

        drawerLayout = findViewById(R.id.layDrawerLayout);
        navigationView = findViewById(R.id.navView);
        toolbar = findViewById(R.id.barMainToolBar);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(FragmentName.REAL_DATA.getName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            displayView(FragmentName.REAL_DATA.ordinal());
        }

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.menuRealTimeData:
                        if(getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(FragmentName.REAL_DATA.getName());
                            displayView(FragmentName.REAL_DATA.ordinal());
                        }
                        break;
                    case R.id.menuSensorInformation:
                        if(getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(FragmentName.SENSOR_INFORMATION.getName());
                            displayView(FragmentName.SENSOR_INFORMATION.ordinal());
                        }
                        break;
                    case R.id.menuPreviousData:
                        if(getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(FragmentName.PREVIOUS_DATA.getName());
                            displayView(FragmentName.PREVIOUS_DATA.ordinal());
                        }
                        break;
                    case R.id.menuChangePassword:
                        if(getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(FragmentName.CHANGE_PASSWORD.getName());
                            displayView(FragmentName.CHANGE_PASSWORD.ordinal());
                        }
                        break;
                    case R.id.menuIDCancel :
                        if(getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(FragmentName.ID_CANCEL.getName());
                            displayView(FragmentName.ID_CANCEL.ordinal());
                        }
                        break;
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return false;
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void displayView(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new MapFragment();
                break;
            case 1:
                fragment = new BluetoothFragment();
                break;
            case 2:
                fragment = new PreviousDataFragment();
                break;
            case 3:
                fragment = new ChangePasswordFragment();
                break;
            case 4:
                fragment = new IDCancelFragment();
                break;
            default:
                break;
        }
        if (fragment != null) {
            handler.post(new CommitFragmentRunnable(fragment));
        }
    }

    private class CommitFragmentRunnable implements Runnable {
        private Fragment fragment;

        CommitFragmentRunnable(Fragment fragment) {
            this.fragment = fragment;
        }

        @ Override
        public void run() {
            getFragmentManager().beginTransaction().replace(R.id.layFrameLayout, fragment).commit();
        }
    }
}