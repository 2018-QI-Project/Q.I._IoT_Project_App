package qualcomminstitute.iot;

import android.content.res.Configuration;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLayout();
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

    private void initLayout() {
        toolbar = (Toolbar) findViewById(R.id.barMainToolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_menu_white_24dp);

        drawerLayout = (DrawerLayout) findViewById(R.id.layDrawerLayout);
        navigationView = (NavigationView) findViewById(R.id.navView);
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
                    case R.id.menuMain:

                        break;
                    case R.id.menuSensor:

                        break;
                    case R.id.menuPrevious:

                        break;
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
    /*
    private DrawerLayout drawerLayout;
    private ListView listView;
    private ActionBarDrawerToggle drawerToggle;
    private List<String> slidingMenu = new ArrayList<> ();

    public MainActivity() {
        Collections.addAll(slidingMenu, new String[]{
        });
    }

    private CharSequence mActionBarTitle;
    private CharSequence mMenuTitle;
    private Handler handler = new Handler();

    @ Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        listView = (ListView)findViewById(R.id.listView);

        setActionBar();
        setSlidingMenu();
        setDrawer();

        if (savedInstanceState == null) {
            displayView(0);
            getActionBar().setTitle(slidingMenu.get(0));
        }
    }

    private void displayView(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new BluetoothFragment();
                break;
            case 1:
                fragment = new MapFragment();
                break;
            case 2:
                break;
            default:
                break;
        }
        if (fragment != null) {
            // 프래그먼트 전환의 부드러운 처리를 위해 스레드를 사용한다.
            handler.post(new CommitFragmentRunnable(fragment));
            listView.setItemChecked(position, true);
            listView.setSelection(position);
            drawerLayout.closeDrawer(listView);
        } else {
            Log.e(getClass().getSimpleName(), "error in displayView(int position)");
        }
    }

    private void setActionBar() {
        mMenuTitle = mActionBarTitle = getTitle();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    private void setSlidingMenu() {
        ArrayAdapter<String> adapter = new ArrayAdapter<> (this, android.R.layout.simple_list_item_activated_1);
        adapter.addAll(slidingMenu);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @ Override
            public void onItemClick(AdapterView <  ?  > parent, View view, int position, long id) {
                mMenuTitle = slidingMenu.get(position);
                getActionBar().setTitle(mMenuTitle);
                displayView(position);
            }
        });
    }

    private void setDrawer() {
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name) {
            @ Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mActionBarTitle);
                invalidateOptionsMenu();
            }

            @ Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActionBar().setTitle(mMenuTitle);
                invalidateOptionsMenu();
            }
        };

        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(drawerToggle);
        //drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    }

    @ Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(listView)) {
            drawerLayout.closeDrawer(listView);
        } else {
            super.onBackPressed();
        }
    }

    @ Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @ Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 토글이 이벤트를 소비했다면 이벤트를 전파시키지 않고 종결한다.
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();
        switch (id) {
            case R.id.menuMain:
                alert(item.getTitle().toString());
                return true;
            case R.id.menuSensor:
                alert(item.getTitle().toString());
                return true;
            case R.id.menuPrevious:
                alert(item.getTitle().toString());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // invalidateOptionsMenu() 메소드를 호출하면 다음 메소드가 기동한다.
    @ Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isSlidingMenuOpened = drawerLayout.isDrawerOpen(listView);
        menu.findItem(R.id.menuSensor).setVisible(!isSlidingMenuOpened);
        menu.findItem(R.id.menuPrevious).setVisible(!isSlidingMenuOpened);
        return super.onPrepareOptionsMenu(menu);
    }

    protected void alert(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    // 액션바 토글 상태를 동기화하기 위해서 다음 두 개의 메서드를 오버라이드 한다.
    @SuppressLint("NewApi")
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        drawerToggle.syncState();
    }

    @ Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private class CommitFragmentRunnable implements Runnable {
        private Fragment fragment;

        CommitFragmentRunnable(Fragment fragment) {
            this.fragment = fragment;
        }

        @ Override
        public void run() {
            getFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment).commit();
        }
    }
}
    */