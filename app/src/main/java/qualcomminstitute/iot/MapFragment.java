package qualcomminstitute.iot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView = null;
    private Spinner viewSpinner;
    private TextView viewDate;
    private GoogleMap mMap;
    private String strToken;
    private Location currentLocation;
    private CircleOptions myCircle;
    private final int LOCATION_PERMISSION = 0;

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case NetworkInterface.REQUEST_FAIL :
                    Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_EXCEPTION);
                    break;
                case NetworkInterface.REQUEST_SUCCESS :
                    try {
                        // 응답 메세지 JSON 파싱
                        final JSONObject returnObject = new JSONObject(message.getData().getString(NetworkInterface.RESPONSE_DATA));

                        switch(returnObject.getString(NetworkInterface.MESSAGE_TYPE)) {
                            case NetworkInterface.MESSAGE_SUCCESS :
                                final JSONArray dataArray = returnObject.getJSONArray(NetworkInterface.MESSAGE_AIR_DATA);
                                handler.post(new Thread(){
                                    @Override
                                    public void run() {
                                        mMap.clear();
                                        try {
                                            for(int i = 0; i < dataArray.length(); ++i) {
                                                JSONObject dataObject = dataArray.getJSONObject(i);
                                                String strData;

                                                LatLng location = new LatLng(dataObject.getDouble(NetworkInterface.MESSAGE_LAT), dataObject.getDouble(NetworkInterface.MESSAGE_LON));
                                                mMap.addCircle(myCircle);
                                                switch(viewSpinner.getSelectedItem().toString()){
                                                    case "CO":
                                                        strData = "Date : " + Utility.convertUnixTime(dataObject.getLong(NetworkInterface.MESSAGE_DATE)) + "\n" +
                                                                "CO : " + dataObject.getDouble(NetworkInterface.MESSAGE_CO);
                                                        mMap.addMarker(new MarkerOptions().position(location).title(strData).alpha(0f));
                                                        mMap.addCircle(new CircleOptions().center(location).radius(10).fillColor(getResources().getColor(R.color.good)));
                                                        break;
                                                    case "NO2":
                                                        strData = "Date : " + Utility.convertUnixTime(dataObject.getLong(NetworkInterface.MESSAGE_DATE)) + "\n" +
                                                                "NO2 : " + dataObject.getDouble(NetworkInterface.MESSAGE_NO2);
                                                        mMap.addMarker(new MarkerOptions().position(location).title(strData).alpha(0f));
                                                        mMap.addCircle(new CircleOptions().center(location).radius(10).fillColor(getResources().getColor(R.color.moderate)));
                                                        break;
                                                    case "SO2":
                                                        strData = "Date : " + Utility.convertUnixTime(dataObject.getLong(NetworkInterface.MESSAGE_DATE)) + "\n" +
                                                                "SO2 : " + dataObject.getDouble(NetworkInterface.MESSAGE_SO2);
                                                        mMap.addMarker(new MarkerOptions().position(location).title(strData).alpha(0f));
                                                        mMap.addCircle(new CircleOptions().center(location).radius(10).fillColor(getResources().getColor(R.color.unhealthy)));
                                                        break;
                                                    case "O3":
                                                        strData = "Date : " + Utility.convertUnixTime(dataObject.getLong(NetworkInterface.MESSAGE_DATE)) + "\n" +
                                                                "O3 : " + dataObject.getDouble(NetworkInterface.MESSAGE_O3);
                                                        mMap.addMarker(new MarkerOptions().position(location).title(strData).alpha(0f));
                                                        mMap.addCircle(new CircleOptions().center(location).radius(10).fillColor(getResources().getColor(R.color.unhealthy)));
                                                        break;
                                                    case "PM2.5":
                                                        strData = "Date : " + Utility.convertUnixTime(dataObject.getLong(NetworkInterface.MESSAGE_DATE)) + "\n" +
                                                                "PM2.5 : " + dataObject.getDouble(NetworkInterface.MESSAGE_PM25);
                                                        mMap.addMarker(new MarkerOptions().position(location).title(strData).alpha(0f));
                                                        mMap.addCircle(new CircleOptions().center(location).radius(10).fillColor(getResources().getColor(R.color.hazardous)));
                                                        break;
                                                    default:
                                                        break;
                                                }
                                            }
                                            viewDate.setText(Utility.convertUnixTime(returnObject.getLong(NetworkInterface.MESSAGE_TIME_STAMP)));
                                        }
                                        catch(JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                break;
                            case NetworkInterface.MESSAGE_FAIL :
                                switch (returnObject.getString(NetworkInterface.MESSAGE_VALUE)) {
                                    case "invalid client type":
                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_CLIENT_FAILED);
                                        break;
                                    case "not valid token":
                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_TOKEN_FAILED);
                                        SharedPreferences data = getActivity().getSharedPreferences(PreferenceName.preferenceName, Context.MODE_PRIVATE);
                                        SharedPreferences.Editor dataEditor = data.edit();
                                        dataEditor.remove(PreferenceName.preferenceToken);
                                        dataEditor.apply();
                                        getActivity().finish();
                                        break;
                                    case "not registered sensor":
                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_UNUSED_SENSOR);
                                        break;
                                    case "nothing data":
                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_NO_DATA);
                                        break;
                                    default:
                                        Log.d("TEST", returnObject.getString(NetworkInterface.MESSAGE_VALUE));
                                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_DEFAULT_FAILED);
                                        break;
                                }
                                break;
                        }
                    }
                    catch(JSONException e) {
                        e.printStackTrace();
                        Log.e(this.getClass().getName(), "JSON ERROR!");
                        Utility.displayToastMessage(handler, getActivity(), NetworkInterface.TOAST_EXCEPTION);
                    }
            }
        }
    };

    public MapFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        // Token 가져오기
        SharedPreferences preferences = getActivity().getSharedPreferences(PreferenceName.preferenceName, Context.MODE_PRIVATE);
        strToken = preferences.getString(PreferenceName.preferenceToken, null);

        viewDate = view.findViewById(R.id.txtMapDate);
        viewSpinner = view.findViewById(R.id.spiMapFilter);
        viewSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    JSONObject rootObject = new JSONObject();
                    rootObject.put(NetworkInterface.REQUEST_CLIENT_TYPE, NetworkInterface.REQUEST_CLIENT);
                    rootObject.put(NetworkInterface.REQUEST_TOKEN, strToken);
                    rootObject.put(NetworkInterface.REQUEST_USER_TYPE, NetworkInterface.REQUEST_ALL);

                    new RequestMessage(NetworkInterface.REST_AIR_QUALITY_REAL_TIME, "POST", rootObject, handler).start();
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mapView = view.findViewById(R.id.mapNearAir);
        mapView.getMapAsync(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(mapView != null)
        {
            mapView.onCreate(savedInstanceState);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        settingGPS();
        getMyLocation();
        if (currentLocation != null) {
            double lng = currentLocation.getLongitude();
            double lat = currentLocation.getLatitude();
            LatLng myPlace = new LatLng(lat, lng);
            // mMap.addMarker(new MarkerOptions().position(myPlace).title("Here is my position!"));
            myCircle = new CircleOptions().center(myPlace).radius(10).fillColor(getResources().getColor(R.color.my_location));
            mMap.addCircle(myCircle);
            // 2.0f is Most Zoom Out
            // 21.0f is Most Zoom In
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 19.0f));
        }
    }

    private LocationManager locationManager;
    private LocationListener locationListener;

    private void getMyLocation() {
        // Register the listener with the Location Manager to receive location updates
        if(getActivity() != null) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // 사용자 권한 요청
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                // 수동으로 위치 구하기
                String locationProvider = LocationManager.GPS_PROVIDER;
                currentLocation = locationManager.getLastKnownLocation(locationProvider);
            }
        }

    }

    private void settingGPS() {
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                // TODO 위도, 경도로 하고 싶은 것
                LatLng myPlace = new LatLng(latitude, longitude);
                myCircle.center(myPlace);
                // 2.0f is Most Zoom Out
                // 21.0f is Most Zoom In
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myPlace));
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onLowMemory();
    }
}