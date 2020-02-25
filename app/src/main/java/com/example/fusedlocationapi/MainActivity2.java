package com.example.fusedlocationapi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity2 extends AppCompatActivity {

    private static final String TAG = "";
    TextView txtLocationResult;

    TextView txtUpdatedOn;

    Button btnStartUpdates;

    Button btnStopUpdates;

    private String mLastUpdateTime;

    private TextView tv;
    private Geocoder geocoder;
    private List<Address> addresses;

    private boolean permissionGranted = true, isLocationSuccess = false;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 500;

    // fastest updates interval - 5 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 500;

    private static final int REQUEST_CHECK_SETTINGS = 100;


    // bunch of location related apis
    private FusedLocationProviderClient mFusedLocationClient;
    private FusedLocationProviderApi api;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    static int i=0;

    // boolean flag to toggle the ui
    private Boolean mRequestingLocationUpdates = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtLocationResult = findViewById(R.id.location_result);
        txtUpdatedOn = findViewById(R.id.updated_on);
        btnStartUpdates = findViewById(R.id.btn_start_location_updates);
        btnStopUpdates = findViewById(R.id.btn_stop_location_updates);
        tv = (TextView) findViewById(R.id.tv);

        initPermission();
    }


    private void initPermission() {
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(MainActivity2.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(MainActivity2.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            permissionGranted = true;
            init();
            startLocationUpdate();
        }
    }



    @Override
    public void onResume() {
        super.onResume();

        // Resuming location updates depending on button state and
        // allowed permissions
//        if (permissionGranted) {
//            startLocationUpdates();
//        }
//
//        updateLocationUI();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (permissionGranted) {
            // pausing location updates
            stopLocationUpdates();
        }

    }

    private void init() {
        if (permissionGranted) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mSettingsClient = LocationServices.getSettingsClient(this);

            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    // location is received
                    mCurrentLocation = locationResult.getLastLocation();
                    mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                    updateLocationUI();
                }
            };

            mRequestingLocationUpdates = false;
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            mLocationSettingsRequest = builder.build();
//        startLocationButtonClick();
        }
    }

    //    @OnClick(R.id.btn_start_location_updates)
    public void startLocationButtonClick(View view) {
        // Requesting ACCESS_FINE_LOCATION using Dexter library
//        Dexter.withActivity(this)
//                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//                .withListener(new PermissionListener() {
//                    @Override
//                    public void onPermissionGranted(PermissionGrantedResponse response) {
//                        mRequestingLocationUpdates = true;
//                        startLocationUpdates();
//                    }
//
//                    @Override
//                    public void onPermissionDenied(PermissionDeniedResponse response) {
//                        if (response.isPermanentlyDenied()) {
//                            // open device settings when the permission is
//                            // denied permanently
////                            openSettings();
//                        }
//                    }
//
//                    @Override
//                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
//                        token.continuePermissionRequest();
//                    }
//                }).check();

        isLocationSuccess = true;
        if (permissionGranted) {
            startLocationUpdate();
        } else {

            initPermission();

        }

    }

    //    @OnClick(R.id.btn_stop_location_updates)
    public void stopLocationButtonClick(View view) {
        if (permissionGranted) {
            mRequestingLocationUpdates = false;
            stopLocationUpdates();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        if (permissionGranted) {
                            startLocationUpdate();
                        } else {
                            initPermission();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e(TAG, "User choose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        break;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            int len = permissions.length;
            for (int i = 0; i < len; i++) {
                if (ActivityCompat.checkSelfPermission(MainActivity2.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = false;
                }
            }

            if (permissionGranted) {
//                permissionGranted = true;
                init();
//            if(isLocationSuccess)

                startLocationUpdate();

            } else {

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity2.this);
                dialog.setTitle("Location");
                dialog.setMessage("You did not give permission to access your Location. Do want to exit");
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestManualPermission(permissions, requestCode);
                        permissionGranted = true;
                    }
                });
                dialog.show();
            }
        }
    }

    private void requestManualPermission(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    //to check location is enabled or not
    private void startLocationUpdate() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        Toast.makeText(getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());
//                        LocationManager manager= (LocationManager) getSystemService(LOCATION_SERVICE);

                        updateLocationUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity2.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);

                                Toast.makeText(MainActivity2.this, errorMessage, Toast.LENGTH_LONG).show();
                                break;
                            case LocationSettingsStatusCodes.SUCCESS:
                                permissionGranted = true;
                                init();
                        }
                        updateLocationUI();
                    }
                });
    }

    public void stopLocationUpdates() {
        // Removing location updates
        if (mLocationCallback != null)
            mFusedLocationClient
                    .removeLocationUpdates(mLocationCallback)
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            Toast.makeText(getApplicationContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();
                            //                        toggleButtons();
                        }
                    });
    }

    //    @OnClick(R.id.btn_get_last_location)
    public void showLastKnownLocation(View view) {
        if (mCurrentLocation != null) {

            StringBuilder str = new StringBuilder();
            if (geocoder.isPresent()) {
//                    Toast.makeText(getApplicationContext(),
//                            "geocoder present", Toast.LENGTH_SHORT).show();
                Address returnAddress = addresses.get(0);
//                    String city = returnAddress.getCountryName();
//                    String region_code = returnAddress.getCountryCode();
//                    String zipcode = returnAddress.getPostalCode();
                String strett = returnAddress.getSubLocality();
                String stname = returnAddress.getAdminArea();
                String stname1 = returnAddress.getFeatureName();
                String localityString = returnAddress.getLocality();


                str.append(stname1 + ",");
                str.append(strett + ",");
                str.append(localityString + ",");
                str.append(stname + ",");
                tv.setText(str);
                //tv.setText("Latitude :" +mCurrentLocation.getLatitude()+ "Longitude : " +mCurrentLocation.getLongitude());
//                Toast.makeText(getApplicationContext(), "Lat: " + mCurrentLocation.getLatitude()
//                        + ", Lng: " + mCurrentLocation.getLongitude(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Last known location is not available!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            txtLocationResult.setText(
                    "Lat: " + mCurrentLocation.getLatitude() + ", " +
                            "Lng: " + mCurrentLocation.getLongitude()
            );

            try {

                new GeocodeAsyncTask().execute(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());


//                geocoder = new Geocoder(MainActivity2.this, Locale.ENGLISH);
//                addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
//                StringBuilder str = new StringBuilder();
//                if (geocoder.isPresent()) {
////                    Toast.makeText(getApplicationContext(),
////                            "geocoder present", Toast.LENGTH_SHORT).show();
//                    Address returnAddress = addresses.get(0);
////                    String city = returnAddress.getCountryName();
////                    String region_code = returnAddress.getCountryCode();
////                    String zipcode = returnAddress.getPostalCode();
//                    String strett = returnAddress.getSubLocality();
//                    String stname = returnAddress.getAdminArea();
//                    String stname1 = returnAddress.getFeatureName();
//                    String localityString = returnAddress.getLocality();
//                    //                    str.append(city + "" + region_code + "");
////                    str.append(zipcode + "");
//
//                    str.append(stname1 +",");
//                    str.append(strett +",");
//                    str.append(localityString + ",");
//                    str.append(stname +",");
//
//                    tv.setText(str);
////                    Toast.makeText(getApplicationContext(), str,
////                            Toast.LENGTH_SHORT).show();
//                } else {
//
//                    Toast.makeText(getApplicationContext(),
//                            "geocoder not present", Toast.LENGTH_SHORT).show();
//                }

// } else {
// Toast.makeText(getApplicationContext(),
// "address not available", Toast.LENGTH_SHORT).show();
// }
            } catch (Exception e) {
// TODO Auto-generated catch block

                e.printStackTrace();
                Log.e("tag", e.getMessage());
            }


            // giving a blink animation on TextView
            txtLocationResult.setAlpha(0);
            txtLocationResult.animate().alpha(1).setDuration(300);

            // location last updated time
            txtUpdatedOn.setText("Last updated on: " + mLastUpdateTime);
        }

//        toggleButtons();
    }

    private class GeocodeAsyncTask extends AsyncTask<Double, Void, Address> {

        String errorMessage = "";
        String address;


        @SuppressLint("LongLogTag")
        @Override
        protected Address doInBackground(Double... latlang) {
            Geocoder geocoder = new Geocoder(MainActivity2.this, Locale.getDefault());
            List<Address> addresses = null;
            if (geocoder.isPresent()) {
                try {
                    addresses = geocoder.getFromLocation(latlang[0], latlang[1], 1);
                    Log.d(TAG, "doInBackground: ************");
                } catch (IOException ioException) {
                    errorMessage = "Service Not Available";
                    Log.e(TAG, errorMessage, ioException);
                } catch (IllegalArgumentException illegalArgumentException) {
                    errorMessage = "Invalid Latitude or Longitude Used";
                    Log.e(TAG, errorMessage + ". " +
                            "Latitude = " + latlang[0] + ", Longitude = " +
                            latlang[1], illegalArgumentException);
                }

                if (addresses != null && addresses.size() > 0)
                    return addresses.get(0);
            }
//            else {
//                new GetGeoCodeAPIAsynchTask().execute(latlang[0], latlang[1]);
//            }

            return null;
        }

        @SuppressLint("LongLogTag")
        protected void onPostExecute(Address addresss) {

            if (addresss == null) {
                new GetGeoCodeAPIAsynchTask().execute(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                Log.d(TAG, "onPostExecute: *****");
            } else {
//                progressBar.setVisibility(View.GONE);
                address = addresss.getAddressLine(0);
//                City = addresss.getLocality();
//                Log.d(TAG, "onPostExecute: **************************" + City);
//                String city = addresss.getLocality();
//                String state = addresss.getAdminArea();
//                //create your custom title
//                String title = city + "-" + state;
                tv.setText(address);
//                Geocoder geocoder = new Geocoder(MainActivity2.this);
//                try {
//                    ArrayList<Address> addresses = (ArrayList<Address>) geocoder.getFromLocationName("karur", 50);
//                    for (Address address3 : addresses) {
//                        double lat = address3.getLatitude();
//                        double lon = address3.getLongitude();
////                        address2.setText(lat +
////                                "\n"
////                                + lon);
//
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//
//                }
                //create your custom title
//                String title = city + "-" + state;
//                Alertbox alertbox=new Alertbox(MainActivity.this);
//                alertbox.showAlertboxwithback("Your Current location is "+city);


            }
        }
    }

    public String getLatLongByURL(String requestURL) {
        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private class GetGeoCodeAPIAsynchTask extends AsyncTask<Double, Void, String[]> {
        String address = "", City = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(Double... latlang) {
            String response;
            final String APIKEY = "AIzaSyBRmYkOOy9QhrI53Fp3h_Tt8t7amWNa4Q0";
            try {
                String URL = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latlang[0] + "," + latlang[1] + "&key=" + APIKEY;
                Log.v("URL", URL);
                response = getLatLongByURL(URL);
                return new String[]{response};
            } catch (Exception e) {
                return new String[]{"error"};
            }
        }

        @SuppressLint("LongLogTag")
        @Override
        protected void onPostExecute(String... result) {
            try {
                JSONObject jsonObject = new JSONObject(result[0]);

                address = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                        .getJSONArray("address_components").getJSONObject(0).getString("long_name");

//                editor.putString("FromAddress", address);
//                editor.putString("ToAddress", address);
//                editor.commit();
//                editor.apply();

                City = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                        .getJSONArray("address_components").getJSONObject(2).getString("long_name");

                String state = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                        .getJSONArray("address_components").getJSONObject(4).getString("long_name");

                Log.d(TAG, "onPostExecute: " + City);
                tv.setText(City);

//                String title = city + "-" + state;

//                Alertbox alertbox=new Alertbox(MainActivity.this);
//                alertbox.showAlertbox("Your Current location is "+city);

//                if (fetchType == FROMADDRESS) {
//
//                    mPrimaryAddress.setText(address);
//                    mSecondaryAddress.setText(title);
//                    mDropText.requestFocus();
//                    fromaddress = address;
//                    mPickupText.setText(address);
//
//                    editor.putString("FromAddress", fromaddress);
//                    editor.apply();
//                    editor.commit();
//
//                    Log.i("FromAddress1", address);
//                    Log.i("FromAddress2", title);
//                } else {
//                    mPrimaryAddress.setText(address);
//                    mSecondaryAddress.setText(title);
//                    mDropText.setText(address);
//                    toaddress = address;
//                    editor.putString("ToAddress", toaddress).commit();
//
//                    Log.i("ToAddress1", address);
//                    Log.i("ToAddress2", title);
//
//                }


                Log.d("Address", "" + address);
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            progressBar.setVisibility(View.GONE);

        }
    }
}
