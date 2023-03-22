package com.humber.n01414195_favlocassessment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> locationPermissionRequest;
    private GoogleMap googleMap;
//    private Marker currentLocationMarker;
    private Marker favouriteLocationDataMarker;
    private FragmentManager fragmentManager;
    private SupportMapFragment supportMapFragment;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fragmentManager = getSupportFragmentManager();
        supportMapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.fragment_container_view);
        assert supportMapFragment != null;
        supportMapFragment.getMapAsync(this);
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        locationPermissionRequest = registerForActivityResult(new ActivityResultContracts
                        .RequestMultiplePermissions(), result -> {
                    Boolean fineLocationGranted = result.getOrDefault(
                            android.Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseLocationGranted = result.getOrDefault(
                            android.Manifest.permission.ACCESS_COARSE_LOCATION, false);
                    if (fineLocationGranted != null && fineLocationGranted) {
                        Toast.makeText(this, "Precise location access granted.", Toast.LENGTH_SHORT).show();
                        getLastLocation();
                    } else if (coarseLocationGranted != null && coarseLocationGranted) {
                        Toast.makeText(this, "Only approximate location access granted.", Toast.LENGTH_SHORT).show();
                        getLastLocation();
                    } else {
                        Toast.makeText(this, "No location access granted.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            });
            return;
        }
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(this, location -> {
            if (location != null) {
                System.out.println(location);
                if (googleMap != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
//                    Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_launcher_foreground);
//                    assert drawable != null;
//                    drawable.setTint(Color.RED);
//                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
//                    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//                    drawable.draw(new Canvas(bitmap));
//                    if (currentLocationMarker == null) {
//                        currentLocationMarker = googleMap.addMarker(new MarkerOptions().position(latLng).title("Current Location").icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
//                    } else {
//                        currentLocationMarker.setPosition(latLng);
//                    }
                }
            } else {
                Toast.makeText(MainActivity.this, "Location is null", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnCameraIdleListener(() -> {
            googleMap.clear();
            favouriteLocationDataMarker = null;
            addFavouriteLocationDataMarker(googleMap);
        });
        googleMap.setOnMapClickListener(latLng -> {
            triggerFavouriteLocationDataFragment(googleMap, latLng, false);
        });
        googleMap.setOnMarkerClickListener(marker -> {
            triggerFavouriteLocationDataFragment(googleMap, marker.getPosition(), true);
            return false;
        });
        getLastLocation();
    }

    private void triggerFavouriteLocationDataFragment(@NonNull GoogleMap googleMap, LatLng latLng, boolean isFavouriteLocationDataTapped) {
        FavouriteLocationDataFragment favouriteLocationDataFragment = new FavouriteLocationDataFragment(supportMapFragment, googleMap, sharedPreferences, latLng, isFavouriteLocationDataTapped);
        fragmentManager.beginTransaction().hide(supportMapFragment).add(R.id.fragment_container_view, favouriteLocationDataFragment).show(favouriteLocationDataFragment).commit();
    }

    public void addFavouriteLocationDataMarker(@NonNull GoogleMap googleMap) {
        if (sharedPreferences.contains("title")) {
            LatLng latLng = new LatLng(sharedPreferences.getFloat("latitude", 0), sharedPreferences.getFloat("longitude", 0));
            if (googleMap.getProjection().getVisibleRegion().latLngBounds.contains(latLng)) {
                if (favouriteLocationDataMarker == null) {
                    favouriteLocationDataMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(sharedPreferences.getFloat("latitude", 0), sharedPreferences.getFloat("longitude", 0))).title(sharedPreferences.getString("title", null)));
                } else {
                    favouriteLocationDataMarker.setPosition(new LatLng(sharedPreferences.getFloat("latitude", 0), sharedPreferences.getFloat("longitude", 0)));
                }
            }
        }
    }

    public void getCurrentLocation(View view) {
        getLastLocation();
    }
}