package com.ggi.uparty.loginFrags;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.ggi.uparty.LoginActivity;
import com.ggi.uparty.MainActivity;
import com.ggi.uparty.R;


public class LocationPermissionFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private LoginActivity loginActivity;
    final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    boolean mLocationPermissionGranted;


    public LocationPermissionFragment() {
        // Required empty public constructor
    }

    public static LocationPermissionFragment newInstance() {
        LocationPermissionFragment fragment = new LocationPermissionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //Get arguments
        }
        mLocationPermissionGranted = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_permission, container, false);
        View locationPermissionBtn = view.findViewById(R.id.loc_permission_btn);
        locationPermissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocationPermission();
            }
        });

        ImageView locImg = view.findViewById(R.id.location_image);
        locImg.setColorFilter(getResources().getColor(R.color.colorPrimary));


        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(loginActivity.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        }
//        else if(ContextCompat.checkSelfPermission(loginActivity.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_DENIED){
//            //The permission is denied so just send them to the launch fragment
//            loginActivity.canGoBack = true;
//            Fragment fragment = null;
//            Class fragmentClass = LaunchFragment.class;
//            try {
//                fragment = (Fragment) fragmentClass.newInstance();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            FragmentManager fragmentManager = loginActivity.getSupportFragmentManager();
//            fragmentManager.beginTransaction().replace(R.id.fullscreen_frame, fragment, "Launch").commit();
//
//        }
        else {
            ActivityCompat.requestPermissions(loginActivity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        if(context instanceof LoginActivity){
            loginActivity = (LoginActivity)context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
