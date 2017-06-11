package com.raye7task;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, OnSuccessListener<Location>, GoogleMap.OnMapLongClickListener {
    private static final int LOCATION_PERM_REQ_CODE = 100;
    private static final int REQUEST_CODE_AUTOCOMPLETE_FROM = 1;
    private static final int REQUEST_CODE_AUTOCOMPLETE_TO = 2;
    private static final String TAG = MapsActivity.class.getName();
    private static final String ApiKey = "AIzaSyCYVyDE2Cj3LHGV6j5Jd8ivc9X_Aiy2tlg";
    public ProgressDialog progressDialog;
    private GoogleMap mapObj;
    private EditText[] edArr;
    private FusedLocationProviderClient mFusedLocationClient;
    private Marker fromMarker;
    private Marker toMarker;
    private Polyline line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_id);
        setSupportActionBar(toolbar);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        edArr = new EditText[2];
        edArr[0] = (EditText) findViewById(R.id.fromEditText);

        edArr[1] = (EditText) findViewById(R.id.toEditText);
        edArr[0].setFocusable(false);
        edArr[0].setClickable(true);
        edArr[1].setFocusable(false);
        edArr[1].setClickable(true);
        edArr[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAutoCompleteForFrom();
            }
        });
        edArr[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAutoCompleteForTo();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void startAutoCompleteForTo() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(this);
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE_TO);
        } catch (GooglePlayServicesRepairableException e) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void startAutoCompleteForFrom() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(this);
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE_FROM);
        } catch (GooglePlayServicesRepairableException e) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapObj = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mapObj.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
            Log.e("map", " permession denied");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERM_REQ_CODE);
        }
        mapObj.setOnMyLocationButtonClickListener(this);
        mapObj.setOnMapLongClickListener(this);
        mapObj.setTrafficEnabled(true);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERM_REQ_CODE:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mapObj.setMyLocationEnabled(true);
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_AUTOCOMPLETE_FROM:
                if (resultCode == RESULT_OK) {
                    if (fromMarker != null)
                        fromMarker.remove();
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    edArr[0].setText(place.getName());
                    fromMarker = mapObj.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName().toString()));
                    mapObj.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16));
                    Log.i("OnActivityResult ", "Place: " + place.getName());
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    Log.e("OnActivityResult e1", status.getStatusMessage());
                } else {
                    Log.i("OnActivityResult e2", "The user canceled the operation.");
                }
                break;
            case REQUEST_CODE_AUTOCOMPLETE_TO:
                if (resultCode == RESULT_OK) {
                    if (toMarker != null)
                        toMarker.remove();
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    edArr[1].setText(place.getName());
                    toMarker = mapObj.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName().toString()));
                    toMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    mapObj.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16));

                    Log.i("OnActivityResult ", "Place: " + place.getName());
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    Log.e("OnActivityResult e1", status.getStatusMessage());

                } else {
                    Log.i("OnActivityResult e2", "The user canceled the operation.");
                }
                break;
        }
        showRoute();
    }

    private void showRoute() {
        if (fromMarker != null && toMarker != null) {
            if (line != null)
                line.remove();
            getDirectionData(makeUrl(fromMarker.getPosition(), toMarker.getPosition()));


        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this);
        } else {
            Log.e("map", " permession denied");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERM_REQ_CODE);
        }
        return true;
    }

    @Override
    public void onSuccess(Location location) {
        if (location != null) {
            List<Address> addresses = getAddressFromLatLan(location.getLatitude(), location.getLongitude());
            String City = addresses.get(0).getAddressLine(1);

            if (fromMarker != null)
                fromMarker.remove();
            if (City != null) {
                fromMarker = mapObj.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title(City));
                Log.d("test", City);
                mapObj.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
                edArr[0].setText(City);
                showRoute();
            } else
                Toast.makeText(this, "can't find city", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        List<Address> addresses = getAddressFromLatLan(latLng.latitude, latLng.longitude);
        String addressLine = addresses.get(0).getAddressLine(0);
        String City = addresses.get(0).getAddressLine(1);

        if (fromMarker != null)
            fromMarker.remove();
        if (addressLine != null) {
            fromMarker = mapObj.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).title(addressLine));
            Log.d("test", addressLine);
            mapObj.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 16));
            edArr[1].setText(addressLine);
        } else {
            //set city instead of address
            fromMarker = mapObj.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).title(City));
            Log.d("test", City);
            mapObj.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 16));
            edArr[1].setText(City);
        }
        showRoute();
    }

    public List<Address> getAddressFromLatLan(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addressList;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addressList = geocoder.getFromLocation(latitude, longitude, 1);
            if (addressList.size() > 0) {
                return addressList;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String makeUrl(LatLng source, LatLng destination) {
        return "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" +// from
                Double.toString(source.latitude) +
                "," +
                Double.toString(source.longitude) +
                "&destination=" +// to
                Double.toString(destination.latitude) +
                "," +
                Double.toString(destination.longitude) +
                "&sensor=false&mode=driving&alternatives=true" +
                "&key=" + ApiKey + "";
    }

    public void getDirectionData(String url) {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Json Req Res", response);
                        drawRoute(response);
                        progressDialog.dismiss();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "something wrong happened ", Toast.LENGTH_LONG).show();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        progressDialog = new ProgressDialog(MapsActivity.this);
        progressDialog.setMessage("Getting route , please wait ....");
        progressDialog.setIndeterminate(true);
        progressDialog.show();

    }

    private void drawRoute(String response) {
        try {
            JSONObject json = new JSONObject(response);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            line = mapObj.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(10)
                    .color(Color.parseColor("#FFA500"))
                    .geodesic(true)
            );
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private List<LatLng> decodePoly(String encodedString) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encodedString.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encodedString.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encodedString.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

}


