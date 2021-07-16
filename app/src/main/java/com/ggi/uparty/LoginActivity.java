package com.ggi.uparty;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.FragmentManager;
//import android.net.Uri;
//import android.support.v4.app.Fragment;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.ggi.uparty.loginFrags.AnonInfoFragment;
import com.ggi.uparty.loginFrags.ConfirmEmailFragment;
import com.ggi.uparty.loginFrags.LaunchFragment;
import com.ggi.uparty.loginFrags.LocationPermissionFragment;
import com.ggi.uparty.loginFrags.LoginFragment;
import com.ggi.uparty.loginFrags.NewAccountFragment;
import com.ggi.uparty.loginFrags.OnboardingActivity;
import com.ggi.uparty.loginFrags.RecoveryFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

public class LoginActivity extends AppCompatActivity implements LaunchFragment.OnFragmentInteractionListener, AnonInfoFragment.OnFragmentInteractionListener, ConfirmEmailFragment.OnFragmentInteractionListener, LoginFragment.OnFragmentInteractionListener, NewAccountFragment.OnFragmentInteractionListener, RecoveryFragment.OnFragmentInteractionListener, LocationPermissionFragment.OnFragmentInteractionListener {

    final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    public boolean canGoBack;
    boolean mLocationPermissionGranted;
    LoginActivity loginActivity;
    Bundle savedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.savedInstanceState = savedInstanceState;

//        mAuth = FirebaseAuth.getInstance();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
        //not logged in, go to launch fragment

        ;
        final float currentVersion = 44.0f;
        String email = "";
        String password = "";
        canGoBack = true;
        mLocationPermissionGranted = false;
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        loginActivity = this;

        //See the last version of info screens the user has seen, if any.
        //If they havent seen any, or there is a version higher than what is saved
        //Then launch the OnboardingActivity
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        float lastVersionSeen = sharedPreferences.getFloat("lastVersionSeen", -1);///sharedPreferences.getLong("lastVersionSeen",-1);
        final float lastVersionFinal = lastVersionSeen;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference introScreensCol = db.collection("intro-screens");
        introScreensCol.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                float locHighestVersion = lastVersionFinal;
                System.out.println("VERSION: " + locHighestVersion);
                boolean launchIntro = false;
                for (QueryDocumentSnapshot snap : queryDocumentSnapshots) {
                    Double version = snap.getDouble("Version");
                    System.out.println("SCREEN VERSION: " + version);
                    if(version != null && version > locHighestVersion && version <= currentVersion){
                        launchIntro = true;
                    }

                }
                if(launchIntro){
                    //Launch the OnboardingActivity
                    Intent intent = new Intent(getApplicationContext(), OnboardingActivity.class);
                    startActivity(intent);
                }

                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putFloat("lastVersionSeen", currentVersion);
                editor.commit();
            }
        });




        if(getIntent().getExtras() == null || getIntent().getExtras().getString("logout") == null || !getIntent().getExtras().getString("logout").equals("logout")) {

            email = sharedPreferences.getString("upartyEmail", "");
            password = sharedPreferences.getString("upartyPassword", "");

        }else{
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("upartyPassword", "");
            editor.putString("upartyEmail", "");
            editor.commit();

        }
        final String passFinal = password;

        if(email.equals("") || password.equals("")){
            //Go to launch screen
            Fragment fragment = null;
            Class fragmentClass = LaunchFragment.class;
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fullscreen_frame, fragment, "Launch").commit();
        }else{
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        //Check to see if theyre version is outdated, if so then give em' the BOOT
                        try {
                            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                            final int verCode = pInfo.versionCode;
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            CollectionReference versionInfo = db.collection("version-management");
                            DocumentReference androidInfo = versionInfo.document("android");
                            androidInfo.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                    boolean isOutdated = true;
                                    if(documentSnapshot != null) {
                                        Double minVersion = documentSnapshot.getDouble("MinVersion");
                                        if (minVersion != null && verCode >= minVersion) isOutdated = false;
                                        if(isOutdated){
                                            //Notify user that they're out of date
                                            Toast.makeText(loginActivity,"Please update uParty to the latest version",Toast.LENGTH_SHORT).show();
                                        }else{
                                            //Proceed my children
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            if(user.isEmailVerified()){
                                                //Continue to MainActivity
                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                startActivity(intent);
                                            }else{
                                                //Go to email verification
                                                Fragment fragment = null;
                                                try {
                                                    fragment = ConfirmEmailFragment.newInstance(passFinal);
                                                } catch (Exception ee) {
                                                    ee.printStackTrace();
                                                }
                                                FragmentManager fragmentManager = getSupportFragmentManager();
                                                fragmentManager.beginTransaction().replace(R.id.fullscreen_frame, fragment, "CONFIRMATION").commit();
                                            }

                                        }
                                    }
                                }
                            });
                        }catch(PackageManager.NameNotFoundException boot){
                            //give them the boot here too

                        }



                    }else{
                        //Go to launch screen
                        Fragment fragment = null;
                        Class fragmentClass = LaunchFragment.class;
                        try {
                            fragment = (Fragment) fragmentClass.newInstance();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.fullscreen_frame, fragment, "Launch").commit();
                    }
                }
            });
        }

        //See if shared preferences exists, if it does exist then check if tag "locpermission" is equal to "denied"
        boolean isDenied = false;
        String perm = sharedPreferences.getString("locpermission","");
        if(perm != null && perm.equals("denied"))isDenied = true;

        //Check if location permission has been granted, pop up permissions fragment if permission is not granted
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //set "locpermission in the shared preferences to "granted"
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("locpermission", "granted");
            editor.commit();
        } else {
            //permission has not been granted, if they haven't been asked before then show the permission screen
            if(!isDenied) {
                canGoBack = false;
                //Pop up fragment
                Fragment fragment = null;
                try {
                    fragment = LocationPermissionFragment.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fullscreen_frame, fragment, "PERMISSION").commit();
            }
        }






    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 3){
            onCreate(savedInstanceState);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            mAuth.signOut();
        }
    }

    @Override
    public void onBackPressed() {
        if(canGoBack) {
            Fragment fragment = null;
            Class fragmentClass = LaunchFragment.class;
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fullscreen_frame, fragment, "Launch").commit();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    canGoBack = true;
                    Intent i = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }else{
                    //The permission was denied so write to shared preferences
                    SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("locpermission", "denied");
                    editor.commit();
                    canGoBack = true;
                    Fragment fragment = null;
                    Class fragmentClass = LaunchFragment.class;
                    try {
                        fragment = (Fragment) fragmentClass.newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fullscreen_frame, fragment, "Launch").commit();
                }
            }

        }

    }
}
