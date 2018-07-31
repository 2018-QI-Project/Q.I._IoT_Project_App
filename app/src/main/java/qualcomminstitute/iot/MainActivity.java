package qualcomminstitute.iot;

import android.app.Fragment;
import android.content.DialogInterface;
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
import android.util.Log;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static qualcomminstitute.iot.NetworkInterface.REST_API;
import static qualcomminstitute.iot.NetworkInterface.SERVER_ADDRESS;
import static qualcomminstitute.iot.NetworkInterface.TOAST_CLIENT_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_DEFAULT_FAILED;
import static qualcomminstitute.iot.NetworkInterface.TOAST_EXCEPTION;
import static qualcomminstitute.iot.NetworkInterface.TOAST_TOKEN_FAILED;

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
                    case R.id.menuSignOut :
                        Utility.showYesNoDialog(MainActivity.this, "Sign Out", "Do you want to Sign Out?",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                HttpURLConnection serverConnection = null;
                                                String serverURL = "http://" + SERVER_ADDRESS + REST_API.get("SIGN_OUT");
                                                try {
                                                    URL url = new URL(serverURL);

                                                    // URL을 통한 서버와의 연결 설정
                                                    serverConnection = (HttpURLConnection)url.openConnection();
                                                    serverConnection.setRequestMethod("POST");
                                                    serverConnection.setRequestProperty(NetworkInterface.SIGN_OUT_MESSAGE.get("CLIENT_KEY"), NetworkInterface.SIGN_OUT_MESSAGE.get("CLIENT_VALUE"));

                                                    // 요청 결과
                                                    InputStream is = serverConnection.getInputStream();
                                                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                                                    String readLine;
                                                    StringBuilder response = new StringBuilder();
                                                    while ((readLine = br.readLine()) != null) {
                                                        response.append(readLine);
                                                    }
                                                    br.close();

                                                    // 응답 메세지 JSON 파싱
                                                    JSONObject rootObject = new JSONObject(response.toString());

                                                    if(rootObject.has(NetworkInterface.SIGN_OUT_MESSAGE.get("SUCCESS"))) {
                                                        MainActivity.this.finish();
                                                    }
                                                    else {
                                                        switch(rootObject.getString(NetworkInterface.SIGN_OUT_MESSAGE.get("MESSAGE"))) {
                                                            case "invalid client type":
                                                                Utility.displayToastMessage(handler, MainActivity.this, TOAST_CLIENT_FAILED);
                                                                break;
                                                            case "invalid tokenApp":
                                                                Utility.displayToastMessage(handler, MainActivity.this, TOAST_TOKEN_FAILED);
                                                                break;
                                                            default:
                                                                Utility.displayToastMessage(handler, MainActivity.this, TOAST_DEFAULT_FAILED);
                                                                break;
                                                        }
                                                    }
                                                }
                                                catch(MalformedURLException e) {
                                                    Log.e(this.getClass().getName(), "URL ERROR!");
                                                    Utility.displayToastMessage(handler, MainActivity.this, TOAST_EXCEPTION);
                                                }
                                                catch(JSONException e) {
                                                    Log.e(this.getClass().getName(), "JSON ERROR!");
                                                    Utility.displayToastMessage(handler, MainActivity.this, TOAST_EXCEPTION);
                                                }
                                                catch(IOException e) {
                                                    Log.e(this.getClass().getName(), "IO ERROR!");
                                                    Utility.displayToastMessage(handler, MainActivity.this, TOAST_EXCEPTION);
                                                }
                                                finally {
                                                    if(serverConnection != null) {
                                                        serverConnection.disconnect();
                                                    }
                                                }
                                            }
                                        }.start();
                                    }
                                },
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                });
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
                fragment = new SensorInformationFragment();
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