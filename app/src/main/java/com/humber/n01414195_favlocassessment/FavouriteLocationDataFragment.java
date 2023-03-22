package com.humber.n01414195_favlocassessment;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class FavouriteLocationDataFragment extends Fragment {
    private final SupportMapFragment supportMapFragment;
    private final GoogleMap googleMap;
    private final SharedPreferences sharedPreferences;
    private final LatLng latLng;
    private final boolean isFavouriteLocationDataTapped;
    private CharSequence previousTitleCharSequence;

    public FavouriteLocationDataFragment(SupportMapFragment supportMapFragment, GoogleMap googleMap, SharedPreferences sharedPreferences, LatLng latLng, boolean isFavouriteLocationDataTapped) {
        this.supportMapFragment = supportMapFragment;
        this.googleMap = googleMap;
        this.sharedPreferences = sharedPreferences;
        this.latLng = latLng;
        this.isFavouriteLocationDataTapped = isFavouriteLocationDataTapped;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        previousTitleCharSequence = requireActivity().getTitle();
        requireActivity().setTitle("Favourite Location Data");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favourite_location_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText titleEditText = view.findViewById(R.id.et_title);
        EditText descriptionEditText = view.findViewById(R.id.et_description);
        RatingBar ratingBar = view.findViewById(R.id.rb_rating);
        if (isFavouriteLocationDataTapped) {
            titleEditText.setText(sharedPreferences.getString("title", null));
            descriptionEditText.setText(sharedPreferences.getString("description", null));
            ratingBar.setRating(sharedPreferences.getFloat("rating", 0));
        }
        view.findViewById(R.id.b_cancel).setOnClickListener(v -> removeCurrentFragment());
        view.findViewById(R.id.b_save).setOnClickListener(v -> {
            if (titleEditText.getText().toString().equals("") || descriptionEditText.getText().toString().equals("")) {
                Toast.makeText(requireActivity(), "Please fill out the form", Toast.LENGTH_SHORT).show();
            } else {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("title", titleEditText.getText().toString());
                editor.putString("description", descriptionEditText.getText().toString());
                editor.putFloat("rating", ratingBar.getRating());
                editor.putFloat("latitude", (float) latLng.latitude);
                editor.putFloat("longitude", (float) latLng.longitude);
                editor.apply();
                ((MainActivity) requireActivity()).addFavouriteLocationDataMarker(googleMap);
                removeCurrentFragment();
            }
        });
    }

    private void removeCurrentFragment() {
        requireActivity().setTitle(previousTitleCharSequence);
        getParentFragmentManager().beginTransaction().remove(this).show(supportMapFragment).commit();
    }
}