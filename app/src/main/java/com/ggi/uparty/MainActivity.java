package com.ggi.uparty;

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.ggi.uparty.create.CreateEventFragment;
import com.ggi.uparty.create.CreateEventTwoFragment;
import com.ggi.uparty.other.BirthdayParser;
import com.ggi.uparty.other.BlankFragment;
import com.ggi.uparty.other.EventDetailView;
import com.ggi.uparty.other.LocationItem;
import com.ggi.uparty.other.SimpleVoteItem;
import com.ggi.uparty.viewpagerFrags.ChatFragment;
import com.ggi.uparty.viewpagerFrags.ExploreFragment;
import com.ggi.uparty.viewpagerFrags.MapEventItem;
import com.ggi.uparty.viewpagerFrags.MapFragment;
import com.ggi.uparty.viewpagerFrags.SettingsFragment;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.imperiumlabs.geofirestore.GeoFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements ExploreFragment.OnFragmentInteractionListener, ChatFragment.OnFragmentInteractionListener, MapFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener, LocationEnabledFragment.OnFragmentInteractionListener, EventDetailView.OnFragmentInteractionListener, CreateEventFragment.OnFragmentInteractionListener, CreateEventTwoFragment.OnFragmentInteractionListener, BlankFragment.OnFragmentInteractionListener {

    public CustomViewPager pager;
    PageAdapter pageAdapter;
    final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    boolean mLocationPermissionGranted;
    public FusedLocationProviderClient mFusedLocationProviderClient;
    public Location mLastKnownLocation;
    GoogleApiClient googleApiClient;
    MainActivity mainActivity;
    PlaceDetectionClient mPlaceDetectionClient;
    public String userColor;
    public String username;
    public LocationItem locationItem;
    public LocationItem spectatorLocationItem;
    public Boolean isAdmin;

    private String pushToken;

    public boolean isSignedIn;
    public String uID;

    public boolean allowBackPress;

    //Old info check variables
    private String oldCity;
    private Double oldLat;
    private Double oldLong;
    private String oldPlace;
    private String gender;
    private String birthday;
    private int age;

    //--------VIEWING PLACE/CITY CHAT VARIABLES---------
    public String viewChatPlaceID;
    public String viewChatPlaceName;
    public String viewChatCityID;
    public String viewChatCityName;

    //--------List of visible events for reference by other fragments
    public ArrayList<MapEventItem> shownEvents;
    public ArrayList<MapEventItem> closeEvents;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationPermissionGranted = false;
        pager = findViewById(R.id.viewPager);
        ArrayList<Fragment> fragments = getFragments();
        pageAdapter = new PageAdapter(getSupportFragmentManager(), fragments);
        mainActivity = this;
        locationItem = null;
        userColor = "Orange";
        isAdmin = false;

        allowBackPress = false;

        spectatorLocationItem = new LocationItem();

        viewChatPlaceID = null;
        viewChatPlaceName = null;
        viewChatCityID = null;
        viewChatCityName = null;

        getLocationPermission();

        MobileAds.initialize(this, "ca-app-pub-7483920165975135~8044109421");

        mPlaceDetectionClient = Places.getPlaceDetectionClient(MainActivity.this, null);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mainActivity);
        isSignedIn = false;
        if (FirebaseAuth.getInstance().getUid() != null) {
            isSignedIn = true;
        }

        if (isSignedIn) {
            username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            String uid = FirebaseAuth.getInstance().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference users = db.collection("users");
            DocumentReference user = users.document(uid);
            this.uID = uid;
            user.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    userColor = documentSnapshot.getString("Color");
                    oldCity = documentSnapshot.getString("City");
                    oldLat = documentSnapshot.getDouble("Lat");
                    oldLong = documentSnapshot.getDouble("Long");
                    oldPlace = documentSnapshot.getString("Place");
                    gender = documentSnapshot.getString("Gender");
                    birthday = documentSnapshot.getString("Birthday");
                    isAdmin = documentSnapshot.getBoolean("Admin");
                    if(isAdmin == null || !isAdmin) isAdmin = false;
                    BirthdayParser birthdayParser = new BirthdayParser(birthday);
                    age = getAge(birthdayParser.birthYear, birthdayParser.birthMonth, birthdayParser.birthDay);
                    //Update users push token
                    FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this, new OnSuccessListener<InstanceIdResult>() {
                        @Override
                        public void onSuccess(InstanceIdResult instanceIdResult) {
                            String newToken = instanceIdResult.getToken();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            final String uID = FirebaseAuth.getInstance().getUid();
                            CollectionReference users = db.collection("users");
                            DocumentReference user = users.document(mainActivity.uID); //may cause crash
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("Push", newToken);
                            user.update(map);
                            pushToken = newToken;

                            try {
                                mFusedLocationProviderClient.getLastLocation()
                                        .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                                            @Override
                                            public void onSuccess(Location location) {
                                                // Got last known location. In some rare situations this can be null.
                                                if (location != null) {
                                                    // Logic to handle location object
                                                    mLastKnownLocation = location;
                                                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                                    final FirebaseUser user = mAuth.getCurrentUser();
                                                    String cityString = "";
                                                    Geocoder geocoder = new Geocoder(mainActivity, Locale.getDefault());
                                                    try {
                                                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                                        //String address = addresses.get(0).getSubLocality();
                                                        String cityName = addresses.get(0).getLocality();
                                                        String stateName = addresses.get(0).getAdminArea();
                                                        String countryName = addresses.get(0).getCountryName();

                                                        cityString = cityName + "@" + stateName + "@" + countryName;
                                                        if (locationItem == null) {
                                                            locationItem = new LocationItem();
                                                        }
                                                        locationItem.city = cityName;
                                                        locationItem.state = stateName;
                                                        locationItem.country = countryName;
                                                        //Location retrieved, tell the map fragment too update
                                                        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(makeFragmentName(pager.getId(), 2));
                                                        mapFragment.loadMapEvents();
                                                        ((TextView) findViewById(R.id.city_name)).setText(cityName);

                                                    } catch (Exception e) {

                                                    }
                                                    String lastUpdated = "";
                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                                                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                                                    Date date = new Date(System.currentTimeMillis());
                                                    lastUpdated = sdf.format(date);
                                                    lastUpdated = lastUpdated + " +0000";
                                                    Map<String, Object> map = new HashMap<>();
                                                    map.put("Lat", location.getLatitude());
                                                    map.put("Long", location.getLongitude());
                                                    map.put("City", cityString);
                                                    map.put("LastUpdated", lastUpdated);
                                                    db.collection("users").document(mainActivity.uID).update(map);

                                                    CollectionReference geoFireUsers = FirebaseFirestore.getInstance().collection("geo-users");
//                                                    GeoFirestore geoFirestore = new GeoFirestore(geoFireUsers);
//                                                    geoFirestore.setLocation(uID, new GeoPoint(location.getLatitude(), location.getLongitude()));

                                                    //Update the city after location information has been collected
                                                    updateCity();


//                                                    Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
//                                                    placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
//                                                        @Override
//                                                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
//                                                            if (task.isSuccessful()) {
//                                                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
//                                                                Place mostLikelyPlace = null;
//                                                                double likelihood = 0.0;
//                                                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
//                                                                    if (mostLikelyPlace == null) {
//                                                                        mostLikelyPlace = placeLikelihood.getPlace();
//                                                                        likelihood = placeLikelihood.getLikelihood();
//                                                                    } else {
//                                                                        if (placeLikelihood.getLikelihood() > likelihood) {
//                                                                            mostLikelyPlace = placeLikelihood.getPlace();
//                                                                            likelihood = placeLikelihood.getLikelihood();
//                                                                        }
//                                                                    }
//                                                                }
//
//                                                                String placeKey = mostLikelyPlace.getId();
//                                                                Map<String, Object> map = new HashMap<>();
//                                                                map.put("Place", placeKey);
//                                                                db.collection("users").document(mainActivity.uID).update(map);
//                                                                if (locationItem == null) {
//                                                                    locationItem = new LocationItem();
//                                                                }
//                                                                locationItem.placeKey = placeKey;
//                                                                locationItem.placeName = mostLikelyPlace.getName().toString();
//                                                                locationItem.placeLatLng = mostLikelyPlace.getLatLng();
//                                                                likelyPlaces.release();
//                                                                updatePlace();
//                                                                updateChat();
//                                                            }
//                                                        }
//                                                    });
                                                } else {
                                                    Bundle b = getIntent().getExtras();
                                                    String prompted = null;
                                                    if (b != null) {
                                                        prompted = b.getString("prompt");
                                                    }
                                                    if (prompted == null || !prompted.equals("true")) {
                                                        //They have not been prompted and their location is probably off
                                                        Fragment fragment = null;
                                                        Class fragmentClass = LocationEnabledFragment.class;
                                                        try {
                                                            fragment = (Fragment) fragmentClass.newInstance();
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        FragmentManager fragmentManager = getSupportFragmentManager();
                                                        findViewById(R.id.fullscreen_frame).setVisibility(View.VISIBLE);
                                                        fragmentManager.beginTransaction().replace(R.id.fullscreen_frame, fragment, "LocOffPrompt").commit();
                                                    }


                                                }
                                            }
                                        });
                            } catch (SecurityException securityException) {
                                getLocationPermission();
                            }

                        }
                    });
                }

            });
        } else {
            try {
                mFusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    // Logic to handle location object
                                    mLastKnownLocation = location;
                                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                    final FirebaseUser user = mAuth.getCurrentUser();
                                    String cityString = "";
                                    Geocoder geocoder = new Geocoder(mainActivity, Locale.getDefault());
                                    try {
                                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                        //String address = addresses.get(0).getSubLocality();
                                        String cityName = addresses.get(0).getLocality();
                                        String stateName = addresses.get(0).getAdminArea();
                                        String countryName = addresses.get(0).getCountryName();

                                        cityString = cityName + "@" + stateName + "@" + countryName;
                                        if (locationItem == null) {
                                            locationItem = new LocationItem();
                                        }
                                        locationItem.city = cityName;
                                        locationItem.state = stateName;
                                        locationItem.country = countryName;


                                    } catch (Exception e) {

                                    }


//                                    Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
//                                    placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
//                                            PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
//                                            Place mostLikelyPlace = null;
//                                            double likelihood = 0.0;
//                                            for (PlaceLikelihood placeLikelihood : likelyPlaces) {
//                                                if (mostLikelyPlace == null) {
//                                                    mostLikelyPlace = placeLikelihood.getPlace();
//                                                    likelihood = placeLikelihood.getLikelihood();
//                                                } else {
//                                                    if (placeLikelihood.getLikelihood() > likelihood) {
//                                                        mostLikelyPlace = placeLikelihood.getPlace();
//                                                        likelihood = placeLikelihood.getLikelihood();
//                                                    }
//                                                }
////                                            Log.i(TAG, String.format("Place '%s' has likelihood: %g",
////                                                    placeLikelihood.getPlace().getName(),
////                                                    placeLikelihood.getLikelihood()));
//                                            }
//
//                                            String placeKey = mostLikelyPlace.getId();
//                                            if (locationItem == null) {
//                                                locationItem = new LocationItem();
//                                            }
//                                            locationItem.placeKey = placeKey;
//                                            locationItem.placeName = mostLikelyPlace.getName().toString();
//
//                                            likelyPlaces.release();
                                    //updateChat();
                                }

                            }
                        });
            } catch (SecurityException securityException) {
                getLocationPermission();
            }
        }


        pager.setAdapter(pageAdapter);
        pager.setOffscreenPageLimit(4);
        pager.setCurrentItem(2);
        ((ImageView) findViewById(R.id.map_img)).setColorFilter(getResources().getColor(R.color.colorPrimary));
        setTabClicks();

        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
            }

            @Override
            public void onConnectionSuspended(int i) {
            }
        }).build();

        //Set the click for the create event button
        ImageView createEventButton = findViewById(R.id.create_event);
        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateEventFragment createEventFragment = null;
                Bundle bundle = new Bundle();
                bundle.putInt("startState", 1);
                try {
                    createEventFragment = CreateEventFragment.class.newInstance();
                    createEventFragment.setArguments(bundle);
                    //createEventTwoFragment.setArguments();
                } catch (Exception e) {

                }
                if (createEventFragment != null) {
                    FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                    mainActivity.findViewById(R.id.fullscreen_frame).setVisibility(View.VISIBLE);
                    fragmentManager.beginTransaction().replace(R.id.fullscreen_frame, createEventFragment, "CREATE_EVENT_TWO").commit();
                }
            }
        });

    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
//        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
//                android.Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            mLocationPermissionGranted = true;
//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    recreate();
                }
            }
        }

    }


    private void updateCity() {
        // Open app, load whatever is stored for the user to lastLat, lastlon, lastEtc...
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final CollectionReference cities = db.collection("cities");
        //final String uID = FirebaseAuth.getInstance().getUid();
        //If new is different and not null then remove old user from city, count population down by one
        String newCity = locationItem.toString();
        //Update to the new city, update users data, go to city collection and add them to users w/push token

        if (oldCity == null || !oldCity.equals(newCity)) {
            //New city needs to be updated
            final DocumentReference newCityRef = cities.document(newCity);
            newCityRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        //City exists, so add the user and increase the population count
                        Double population = documentSnapshot.getDouble("Population");
                        if (population == 0) {
                            CollectionReference cityUsers = newCityRef.collection("users");
                            DocumentReference userRef = cityUsers.document(uID);
                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("Push", pushToken);
                            userRef.set(userMap);
                        } else {
                            CollectionReference cityUsers = newCityRef.collection("users");
                            DocumentReference userRef = cityUsers.document(uID);
                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("Push", pushToken);
                            userRef.update(userMap);
                        }
//                        HashMap<String, Object> map = new HashMap<>();
//                        map.put("Population", population + 1);
//                        newCityRef.update(map);

                    } else {
                        //City doesn't exist, so create the city with all of the fields and set population to 1
//                        HashMap<String, Object> map = new HashMap<>();
//                        map.put("Population", 1);
//                        newCityRef.set(map);

                        CollectionReference cityUsers = newCityRef.collection("users");
                        DocumentReference userRef = cityUsers.document(uID);
                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("Push", pushToken);
                        userRef.set(userMap);
                    }
                }
            });
        }
        //Get population, add one, update popuation
        //If city doesnt exist then set population to 1

    }

    private void updatePlace() {
        //Does everything like city
        //Now with male count, female count, and average age
        //Check if user is male or female and update accordingly
        // methods addToAvg(currentAvg,currentPop, ageChange) and removeFromAvg(currentAvg,currentPop,ageChange) -- make these
        //If new place -- set up votes setupVotes() -- adds topics and sets vote parameters to 0
        //Geofire for places geofireStore

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final CollectionReference places = db.collection("places");
        //final String uID = FirebaseAuth.getInstance().getUid();
        final String newPlaceKey = locationItem.placeKey;
        final DocumentReference newPlaceRef = places.document(newPlaceKey);
        newPlaceRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if (documentSnapshot.exists()) {
                    //The place already exists
                    Double population = documentSnapshot.getDouble("Population");
                    Double maleCount = documentSnapshot.getDouble("Male");
                    Double femaleCount = documentSnapshot.getDouble("Female");
                    Double avgAge = documentSnapshot.getDouble("Age");
                    if (population != null) { //null safety
                        if (population == 0) {
                            CollectionReference placeUsers = newPlaceRef.collection("users");
                            DocumentReference user = placeUsers.document(uID);
                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("Push", pushToken);
                            user.set(userMap);
                        } else {
                            CollectionReference placeUsers = newPlaceRef.collection("users");
                            DocumentReference user = placeUsers.document(uID);
                            HashMap<String, Object> userMap = new HashMap<>();
                            userMap.put("Push", pushToken);
                            user.update(userMap);
                        }
                        final Double pop = population;
                        //Check if the user has already voted
                        CollectionReference voters = newPlaceRef.collection("voters");
                        voters.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot snapshots) {
                                boolean hasVoted = false;
                                for (QueryDocumentSnapshot snapshot : snapshots) {
                                    String voterID = snapshot.getId();
                                    //String uID = FirebaseAuth.getInstance().getUid();
                                    if (voterID.equals(uID)) {
                                        hasVoted = true;
                                    }
                                }
//                                if (!hasVoted) {
//                                    if (pop >= 3) {
//                                        doVotingPrompt();
//                                    }
//                                }
                            }
                        });

                    }
//
                } else {
                    //The place does not exist, create neccesary fields()
                    setTopicsTypes(newPlaceRef);

                    HashMap<String, Object> map = new HashMap<>();
                    double lat = mainActivity.locationItem.placeLatLng.latitude;
                    double lng = mainActivity.locationItem.placeLatLng.longitude;
                    map.put("Name", locationItem.placeName);
                    map.put("Lat", lat);
                    map.put("Long", lng);
                    newPlaceRef.set(map);

                    CollectionReference cityUsers = newPlaceRef.collection("users");
                    DocumentReference userRef = cityUsers.document(uID);
                    HashMap<String, Object> userMap = new HashMap<>();
                    userMap.put("Push", pushToken);
                    userRef.set(userMap);
                    CollectionReference geoFirestorePlaceRef = FirebaseFirestore.getInstance().collection("geo-places");
                    GeoFirestore geoFirestorePlaces = new GeoFirestore(geoFirestorePlaceRef);
                    geoFirestorePlaces.setLocation(newPlaceKey, new GeoPoint(lat, lng));
                }
            }
        });

    }

    private void doVotingPrompt() {
        final Dialog votePromptOne = new Dialog(mainActivity);
        votePromptOne.setContentView(R.layout.vote_one_dialog);
        ImageView yesButton = votePromptOne.findViewById(R.id.yes_button);
        ImageView noButton = votePromptOne.findViewById(R.id.no_button);
        TextView text = votePromptOne.findViewById(R.id.one_text);
        yesButton.setColorFilter(getResources().getColor(R.color.colorPrimary));
        noButton.setColorFilter(getResources().getColor(R.color.colorPrimary));
        text.setText("Are you currently attending an event at " + locationItem.placeName + "?");

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                final Dialog votePromptTwo = new Dialog(mainActivity);
//                votePromptTwo.setContentView(R.layout.vote_two_dialog);
//
//                RecyclerView typeRecycler = votePromptTwo.findViewById(R.id.type_list);
//                ArrayList<SimpleVoteItem> typeList = loadTypes(locationItem.placeKey);
//                VoteRecyclerAdapter typeAdapter = new VoteRecyclerAdapter(c, typeList, votePromptTwo, locationItem.placeKey);
//                LinearLayoutManager layoutManager = new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false);
//                typeRecycler.setLayoutManager(layoutManager);
//                typeRecycler.setAdapter(typeAdapter);
//
//                //Mark the person as having voted
//                final FirebaseFirestore db = FirebaseFirestore.getInstance();
//                final CollectionReference places = db.collection("places");
//                //final String uID = FirebaseAuth.getInstance().getUid();
//                final String newPlaceKey = locationItem.placeKey;
//                final DocumentReference place = places.document(newPlaceKey);
//                final CollectionReference voters = place.collection("voters");
//                final DocumentReference voter = voters.document(uID);
//                String time;
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
//                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//                Date date = new Date(System.currentTimeMillis());
//                time = sdf.format(date);
//                time = time + " +0000";
//                HashMap<String, Object> map = new HashMap<>();
//                map.put("Date", time);
//                voter.set(map);
//
//                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//                lp.copyFrom(votePromptTwo.getWindow().getAttributes());
//                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//                votePromptTwo.show();
//                votePromptTwo.getWindow().setAttributes(lp);
//                votePromptOne.dismiss();
            }
        });
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Mark the person as having voted
                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                final CollectionReference places = db.collection("places");
                //final String uID = FirebaseAuth.getInstance().getUid();
                final String newPlaceKey = locationItem.placeKey;
                final DocumentReference place = places.document(newPlaceKey);
                final CollectionReference voters = place.collection("voters");
                final DocumentReference voter = voters.document(uID);
                String time;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = new Date(System.currentTimeMillis());
                time = sdf.format(date);
                time = time + " +0000";
                HashMap<String, Object> map = new HashMap<>();
                map.put("Date", time);
                voter.set(map);
                votePromptOne.dismiss();
            }
        });
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(votePromptOne.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        votePromptOne.show();
        votePromptOne.getWindow().setAttributes(lp);
    }

    private ArrayList<SimpleVoteItem> loadTypes(String placeID) {
        ArrayList<SimpleVoteItem> typeList = new ArrayList<>();
        ArrayList<String> types = new ArrayList<>();
        types.add("Appearance or Signing");
        types.add("Attraction");
        types.add("Camp, Trip, or Retreat");
        types.add("Class, Training, or Workshop");
        types.add("Concert or Performance");
        types.add("Conference");
        types.add("Convention");
        types.add("Dinner or Gala");
        types.add("Festival or Fair");
        types.add("Game or Competition");
        types.add("Meeting or Networking Event");
        types.add("Other");
        types.add("Party or Social Gathering");
        types.add("Race or Endurance Event");
        types.add("Rally");
        types.add("Screening");
        types.add("Seminar or Talk");
        types.add("Tour");
        types.add("Tournament");
        types.add("Tradeshow, Consumer Show, or Expo");
        for (String type : types) {
            SimpleVoteItem simpleVoteItem = new SimpleVoteItem();
            simpleVoteItem.placeID = placeID;
            simpleVoteItem.mode = "type";
            simpleVoteItem.itemName = type;
            typeList.add(simpleVoteItem);
        }

        return typeList;
    }

    private void setTopicsTypes(DocumentReference newPlaceRef) {
        CollectionReference topicRef = newPlaceRef.collection("type");
        CollectionReference typeRef = newPlaceRef.collection("type");
        ArrayList<String> topics = new ArrayList<>();
        topics.add("Auto, Boat, & Air");
        topics.add("Business & Professional");
        topics.add("Charity & Causes");
        topics.add("Community & Culture");
        topics.add("Family & Education");
        topics.add("Fashion & Beauty");
        topics.add("Film, Media, & Entertainment");
        topics.add("Food & Drink");
        topics.add("Government & Politics");
        topics.add("Health & Wellness");
        topics.add("Hobbies & Special Interest");
        topics.add("Home & Lifestyle");
        topics.add("Music");
        topics.add("Other");
        topics.add("Performing & Visual Arts");
        topics.add("Religion & Spirituality");
        topics.add("School Activities");
        topics.add("Science & Technology");
        topics.add("Seasonal & Holiday");
        topics.add("Sports & Fitness");
        topics.add("Travel & Outdoor");

        for (String topic : topics) {
            DocumentReference doc = topicRef.document(topic);
            HashMap<String, Object> map = new HashMap<>();
            map.put("Votes", 0);
            doc.set(map);
        }

        ArrayList<String> types = new ArrayList<>();
        types.add("Appearance or Signing");
        types.add("Attraction");
        types.add("Camp, Trip, or Retreat");
        types.add("Class, Training, or Workshop");
        types.add("Concert or Performance");
        types.add("Conference");
        types.add("Convention");
        types.add("Dinner or Gala");
        types.add("Festival or Fair");
        types.add("Game or Competition");
        types.add("Meeting or Networking Event");
        types.add("Other");
        types.add("Party or Social Gathering");
        types.add("Race or Endurance Event");
        types.add("Rally");
        types.add("Screening");
        types.add("Seminar or Talk");
        types.add("Tour");
        types.add("Tournament");
        types.add("Tradeshow, Consumer Show, or Expo");

        for (String type : types) {
            DocumentReference doc = typeRef.document(type);
            HashMap<String, Object> map = new HashMap<>();
            map.put("Votes", 0);
            doc.set(map);
        }


    }


    private void setTabClicks() {
        //Explore
        findViewById(R.id.explore_tab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ImageView) findViewById(R.id.explore_img)).setColorFilter(getResources().getColor(R.color.colorPrimary));
                ((ImageView) findViewById(R.id.chat_img)).setColorFilter(Color.parseColor("#5B5B5B"));
                ((ImageView) findViewById(R.id.map_img)).setColorFilter(Color.parseColor("#5B5B5B"));
                ((ImageView) findViewById(R.id.settings_img)).setColorFilter(Color.parseColor("#5B5B5B"));
                findViewById(R.id.create_event).setVisibility(View.GONE);
                pager.setCurrentItem(0);
                viewChatPlaceID = null;
                viewChatPlaceName = null;
                viewChatCityID = null;
                viewChatCityName = null;
            }
        });

        //Chat
        findViewById(R.id.chat_tab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateChat();
                ((ImageView) findViewById(R.id.chat_img)).setColorFilter(getResources().getColor(R.color.colorPrimary));
                ((ImageView) findViewById(R.id.explore_img)).setColorFilter(Color.parseColor("#5B5B5B"));
                ((ImageView) findViewById(R.id.map_img)).setColorFilter(Color.parseColor("#5B5B5B"));
                ((ImageView) findViewById(R.id.settings_img)).setColorFilter(Color.parseColor("#5B5B5B"));
                findViewById(R.id.create_event).setVisibility(View.GONE);
                pager.setCurrentItem(1);

            }
        });

        //Map
        findViewById(R.id.map_tab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ImageView) findViewById(R.id.map_img)).setColorFilter(getResources().getColor(R.color.colorPrimary));
                ((ImageView) findViewById(R.id.chat_img)).setColorFilter(Color.parseColor("#5B5B5B"));
                ((ImageView) findViewById(R.id.explore_img)).setColorFilter(Color.parseColor("#5B5B5B"));
                ((ImageView) findViewById(R.id.settings_img)).setColorFilter(Color.parseColor("#5B5B5B"));
                findViewById(R.id.create_event).setVisibility(View.VISIBLE);
                pager.setCurrentItem(2);
                viewChatPlaceID = null;
                viewChatPlaceName = null;
                viewChatCityID = null;
                viewChatCityName = null;
            }
        });

        //Settings
        findViewById(R.id.settings_tab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ImageView) findViewById(R.id.settings_img)).setColorFilter(getResources().getColor(R.color.colorPrimary));
                ((ImageView) findViewById(R.id.chat_img)).setColorFilter(Color.parseColor("#5B5B5B"));
                ((ImageView) findViewById(R.id.explore_img)).setColorFilter(Color.parseColor("#5B5B5B"));
                ((ImageView) findViewById(R.id.map_img)).setColorFilter(Color.parseColor("#5B5B5B"));
                findViewById(R.id.create_event).setVisibility(View.GONE);
                pager.setCurrentItem(3);
                viewChatPlaceID = null;
                viewChatPlaceName = null;
                viewChatCityID = null;
                viewChatCityName = null;
            }
        });

    }

    public void openPrompt(String eventID, int startState){
        CreateEventFragment createEventFragment = null;
        Bundle b = new Bundle();
        b.putInt("startState",startState);
        b.putString("eventID",eventID);
        try {
            createEventFragment = CreateEventFragment.class.newInstance();
            createEventFragment.setArguments(b);
        } catch (Exception e) {

        }
        if (createEventFragment != null) {
            FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
            mainActivity.findViewById(R.id.fullscreen_frame).setVisibility(View.VISIBLE);
            fragmentManager.beginTransaction().replace(R.id.fullscreen_frame, createEventFragment, "EVENT_OPTIONS").commit();
        }
    }

    private int getAge(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

    public ArrayList<Fragment> getFragments() {
        ArrayList<Fragment> listOfFrags = new ArrayList<>();

        listOfFrags.add(ExploreFragment.newInstance());
        listOfFrags.add(ChatFragment.newInstance());
        listOfFrags.add(MapFragment.newInstance());
        listOfFrags.add(SettingsFragment.newInstance());

        return listOfFrags;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onBackPressed() {
        if (allowBackPress) {
            BlankFragment blankFragment = null;
            try {
                blankFragment = BlankFragment.class.newInstance();
                //createEventTwoFragment.setArguments();
            } catch (Exception e) {

            }
            if (blankFragment != null) {
                FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                mainActivity.findViewById(R.id.fullscreen_frame).setVisibility(View.VISIBLE);
                fragmentManager.beginTransaction().replace(R.id.fullscreen_frame, blankFragment, "BLANK").commit();
            }
            findViewById(R.id.fullscreen_frame).setVisibility(View.GONE);
            allowBackPress = false;
        }
    }

    public void updateChat() {
        ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentByTag(makeFragmentName(pager.getId(), 1));
        if (chatFragment != null && locationItem != null) {
            ((TextView) chatFragment.getView().findViewById(R.id.event_tab)).setText(locationItem.placeName);
            chatFragment.lastLocationItem = locationItem;
        }
    }

    private static String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }
}
