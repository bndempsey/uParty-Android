package com.ggi.uparty.viewpagerFrags;

import android.content.Context;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.LinearSnapHelper;
//import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.ggi.uparty.MainActivity;
import com.ggi.uparty.R;
import com.ggi.uparty.other.EventItem;
import com.ggi.uparty.other.LocationItem;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    AutocompleteSupportFragment placeAutoComplete;
    private OnFragmentInteractionListener mListener;
    private MainActivity mainActivity;
    private MapFragment mapFragment;
    private GoogleMap mMap;
    private MapView mapView;
    private ArrayList<WeightedLatLng> heatList;
    private HashMap<String, EventItem> eventMap;
    private TileOverlay currentOverlay;
    private AdView mapAdView;
    private ArrayList<MapEventItem> shownEvents;
    private RecyclerView eventRecyclerView;
    private MapEventRecyclcer mapEventRecyclcer;
    private LinearLayoutManager layoutManager;
    private LinearSnapHelper linearSnapHelper;

    private final int MIN_DISTANCE = 300; //meters

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        //args
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //Something went wrong
        }
        mapFragment = this;
        heatList = new ArrayList<>();
        shownEvents = new ArrayList<>();
        currentOverlay = null;
        if (!Places.isInitialized()) {
            Places.initialize(mainActivity.getApplicationContext(), "AIzaSyANGLElQNQk9QBI1zzJFAUscZKHukxOTnY");
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        eventRecyclerView = view.findViewById(R.id.event_recycler);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        Display display = mainActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int recyclerPadding = width / 4;
        eventRecyclerView.setPadding(recyclerPadding, 0, recyclerPadding, 0);

        linearSnapHelper = new LinearSnapHelper();
        linearSnapHelper.attachToRecyclerView(eventRecyclerView);
        eventRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View snapView = linearSnapHelper.findSnapView(layoutManager);
                    if (snapView != null) {
                        int position = layoutManager.getPosition(snapView);
                        setMapToEventPos(position);
                    }
                }
            }
        });

//        LinearSnapHelper snapHelper = new LinearSnapHelper();
//        snapHelper.attachToRecyclerView(eventRecyclerView);
        //eventRecyclerView.addItemDecoration(new OffsetItemDecoration(mainActivity));


        placeAutoComplete = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        placeAutoComplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                EditText etPlace = (EditText) placeAutoComplete.getView().findViewById(R.id.places_autocomplete_search_input);
                etPlace.setText("");

                //Set the map to the selected city
                LatLng latLng = place.getLatLng();
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(mapFragment.getContext(), Locale.getDefault());
                String city = "";
                String state = "";
                String country = "";

                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                    //String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    city = addresses.get(0).getLocality();
                    state = addresses.get(0).getAdminArea();
                    country = addresses.get(0).getCountryName();
                    //String postalCode = addresses.get(0).getPostalCode();
                    //String knownName = addresses.get(0).getFeatureName();
                } catch (IOException ignore) {

                }
                String placeString = city + "@" + state + "@" + country;
                LocationItem spectate = new LocationItem();
                spectate.city = city;
                spectate.state = state;
                spectate.country = country;
                mainActivity.spectatorLocationItem = spectate;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
                //city zoom 13, event is 15
                //check if there are any events and change zoom/focus if there are events
                shownEvents = new ArrayList<>();
                mapEventRecyclcer = new MapEventRecyclcer(mainActivity, shownEvents, eventRecyclerView);
                eventRecyclerView.setLayoutManager(layoutManager);
                eventRecyclerView.setAdapter(mapEventRecyclcer);
                loadMapEvents();
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });


    }

    private void setMapToEventPos(int pos) {
        mMap.clear();
        mMap.getUiSettings().setAllGesturesEnabled(false);
        MapEventItem selectedEvent = shownEvents.get(pos);
        double lat = selectedEvent.lat;
        double lon = selectedEvent.lon;

        LatLng topRight = mMap.getProjection().getVisibleRegion().latLngBounds.northeast;
        LatLng bottomLeft = mMap.getProjection().getVisibleRegion().latLngBounds.southwest;
//        float[] distanceArray = new float[3];
//        Location.distanceBetween(topRight.latitude, topRight.longitude, bottomLeft.latitude, bottomLeft.longitude, distanceArray);
        MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(210)).position(new LatLng(lat, lon));
        mMap.addMarker(markerOptions);

        double estimatedOffset = (topRight.latitude - bottomLeft.latitude)/4;
        lat = lat - estimatedOffset;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 15));


    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mainActivity, R.raw.style));
        try {
            mMap.setMyLocationEnabled(false);
            if (mainActivity.mLastKnownLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mainActivity.mLastKnownLocation.getLatitude(), mainActivity.mLastKnownLocation.getLongitude()), 15));
                mMap.setMaxZoomPreference(20);
                mMap.setMinZoomPreference(10);
            } else {
                try {
                    mainActivity.mFusedLocationProviderClient.getLastLocation()
                            .addOnSuccessListener(mainActivity, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        // Logic to handle location object
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                                        mMap.setMaxZoomPreference(20);
                                        mMap.setMinZoomPreference(10);
                                        mainActivity.mLastKnownLocation = location;
                                        String cityString = "";
                                        Geocoder geocoder = new Geocoder(mainActivity, Locale.getDefault());
                                        try {
                                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                            //String address = addresses.get(0).getSubLocality();
                                            String cityName = addresses.get(0).getLocality();
                                            String stateName = addresses.get(0).getAdminArea();
                                            String countryName = addresses.get(0).getCountryName();

                                            cityString = cityName + "@" + stateName + "@" + countryName;
                                            if (mainActivity.locationItem == null) {
                                                mainActivity.locationItem = new LocationItem();
                                            }
                                            mainActivity.locationItem.city = cityName;
                                            mainActivity.locationItem.state = stateName;
                                            mainActivity.locationItem.country = countryName;

                                        } catch (Exception e) {

                                        }

                                        //we now have the users location
                                        final FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        CollectionReference users = db.collection("users");
                                        String uID = FirebaseAuth.getInstance().getUid();
                                        final DocumentReference user = users.document(uID);
                                        user.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                String eventID = documentSnapshot.getString("Event");
                                                if(eventID != null && !eventID.equals("")){
                                                    //They are attending an event so double check they they are still at the event and clear it if they are not
                                                    String cityID = mainActivity.locationItem.toString();
                                                    CollectionReference cities = db.collection("cities");
                                                    DocumentReference city = cities.document(cityID);
                                                    CollectionReference events = city.collection("events");
                                                    DocumentReference event = events.document(eventID);
                                                    event.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                            Double lat = documentSnapshot.getDouble("Lat");
                                                            Double lon = documentSnapshot.getDouble("Long");
                                                            if(lat != null && lon != null){
                                                                Location userLoc = mainActivity.mLastKnownLocation;
                                                                Location eventLoc = new Location("");
                                                                eventLoc.setLatitude(lat);
                                                                eventLoc.setLongitude(lon);
                                                                double distance = userLoc.distanceTo(eventLoc);
                                                                if(distance > 300){
                                                                    HashMap<String, Object> clearMap = new HashMap<>();
                                                                    clearMap.put("Event", FieldValue.delete());
                                                                    user.update(clearMap);
                                                                }
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                } catch (SecurityException securityException) {

                }
            }
        } catch (SecurityException securityException) {

        }
//        GoogleMap.OnCameraIdleListener onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
//            @Override
//            public void onCameraIdle() {
//                final GoogleMap map = mMap;
//
//                LatLng topRight = map.getProjection().getVisibleRegion().latLngBounds.northeast;
//                LatLng bottomLeft = map.getProjection().getVisibleRegion().latLngBounds.southwest;
//                float[] distanceArray = new float[3];
//                Location.distanceBetween(topRight.latitude, topRight.longitude, bottomLeft.latitude, bottomLeft.longitude, distanceArray);
//                float radius = distanceArray[0] / 2;
//
//                double centerLat = (topRight.latitude + bottomLeft.latitude) / 2;
//                double centerLong = (topRight.longitude + bottomLeft.longitude) / 2;
//                TextView currentLocation = mainActivity.findViewById(R.id.city_name);
//
//                LocationItem locationItem = getLocationName(centerLat, centerLong);
//                String locationName = locationItem.city;
//                currentLocation.setText(locationName);
//                mainActivity.spectatorLocationItem = locationItem;
//
//                //Get the user reference and sort out the visible ones
//                final FirebaseFirestore db = FirebaseFirestore.getInstance();
//                CollectionReference geoUserRef = db.collection("geo-users");
//                GeoFirestore geoFirestoreUser = new GeoFirestore(geoUserRef);
//                heatList = new ArrayList<>();
//                eventMap = new HashMap<>();
//                GeoQuery geoUserQuery = geoFirestoreUser.queryAtLocation(new GeoPoint(centerLat, centerLong), radius);
//                geoUserQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
//                    @Override
//                    public void onKeyEntered(String placeID, GeoPoint userLoc) {
//                        double lat = userLoc.getLatitude();
//                        double lon = userLoc.getLongitude();
//                        WeightedLatLng latLng = new WeightedLatLng(new LatLng(lat, lon), 1);
//                        heatList.add(latLng);
//                        // Create the gradient.
//                        int[] colors = {
//                                Color.argb(0, 0, 0, 0), //clear
//                                Color.parseColor("#F79621")    // Orange
//                        };
//
//                        float[] startPoints = {0, 0.5f};
//                        Gradient gradient = new Gradient(colors, startPoints);
//                        if (!heatList.isEmpty()) {
//                            HeatmapTileProvider heatmapTileProvider = new HeatmapTileProvider.Builder().weightedData(heatList).gradient(gradient).radius(50).build();
//                            if (currentOverlay == null) {
//                                currentOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));
//                            } else {
//                                currentOverlay.remove();
//                                currentOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onKeyExited(String s) {
//
//                    }
//
//                    @Override
//                    public void onKeyMoved(String s, GeoPoint geoPoint) {
//
//                    }
//
//                    @Override
//                    public void onGeoQueryReady() {
//
//
//                    }
//
//                    @Override
//                    public void onGeoQueryError(Exception e) {
//
//                    }
//                });
//                CollectionReference geoPlaceRef = db.collection("geo-places");
//                GeoFirestore geoFirestorePlace = new GeoFirestore(geoPlaceRef);
//                GeoQuery geoPlaceQuery = geoFirestorePlace.queryAtLocation(new GeoPoint(centerLat, centerLong), radius);
//                geoPlaceQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
//                    @Override
//                    public void onKeyEntered(final String placeID, GeoPoint placeLoc) {
//                        final String placeIDFin = placeID;
//                        CollectionReference places = db.collection("places");
//                        final DocumentReference place = places.document(placeID);
//                        place.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                            @Override
//                            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                Double population = documentSnapshot.getDouble("Population");
//                                Double lat = documentSnapshot.getDouble("Lat");
//                                Double lon = documentSnapshot.getDouble("Long");
//                                Double avgAge = documentSnapshot.getDouble("Age");
//                                Double numMales = documentSnapshot.getDouble("Male");
//                                Double numFemales = documentSnapshot.getDouble("Female");
//                                String placeName = documentSnapshot.getString("Name");
//                                if (population != null && population >= 3) {
//                                    //The place has enough people to be an event so make an event item and add it to the ArrayList
//                                    final EventItem event = new EventItem();
//                                    event.placeID = placeIDFin;
//                                    event.lat = lat;
//                                    event.lon = lon;
//                                    event.numMales = numMales.intValue();
//                                    event.numFemales = numFemales.intValue();
//                                    event.avgAge = avgAge.intValue();
//                                    event.population = population.intValue();
//                                    event.placeName = placeName;
//                                    //Figure out what type
//                                    CollectionReference types = place.collection("type");
//                                    types.orderBy("Votes", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                        @Override
//                                        public void onSuccess(QuerySnapshot snapshots) {
//                                            QueryDocumentSnapshot top = null;
//                                            for (QueryDocumentSnapshot topType : snapshots) {
//                                                top = topType;
//                                            }
//                                            Double votes = 0.0;
//                                            if (top != null) votes = top.getDouble("Votes");
//                                            if (votes != null && votes > 0) {
//                                                event.type = top.getId();
//                                            } else event.type = "Other";
//                                            String type = event.type;
//                                            int drawable;
//                                            switch (type) {
//                                                case "Appearance or Signing":
//                                                    drawable = R.drawable.appearance;
//                                                    break;
//                                                case ("Attraction"):
//                                                    drawable = R.drawable.attraction;
//                                                    break;
//                                                case ("Camp, Trip, or Retreat"):
//                                                    drawable = R.drawable.camp_trip;
//                                                    break;
//                                                case ("Class, Training, or Workshop"):
//                                                    drawable = R.drawable.class_training;
//                                                    break;
//                                                case ("Concert or Performance"):
//                                                    drawable = R.drawable.concert;
//                                                    break;
//                                                case ("Conference"):
//                                                    drawable = R.drawable.conference;
//                                                    break;
//                                                case ("Convention"):
//                                                    drawable = R.drawable.convention;
//                                                    break;
//                                                case ("Dinner or Gala"):
//                                                    drawable = R.drawable.dinner;
//                                                    break;
//                                                case ("Festival or Fair"):
//                                                    drawable = R.drawable.festival;
//                                                    break;
//                                                case ("Game or Competition"):
//                                                    drawable = R.drawable.game;
//                                                    break;
//                                                case ("Meeting or Networking Event"):
//                                                    drawable = R.drawable.meeting;
//                                                    break;
//                                                case ("Other"):
//                                                    drawable = R.drawable.other;
//                                                    break;
//                                                case ("Party or Social Gathering"):
//                                                    drawable = R.drawable.party;
//                                                    break;
//                                                case ("Race or Endurance Event"):
//                                                    drawable = R.drawable.race;
//                                                    break;
//                                                case ("Rally"):
//                                                    drawable = R.drawable.rally;
//                                                    break;
//                                                case ("Screening"):
//                                                    drawable = R.drawable.screening;
//                                                    break;
//                                                case ("Seminar or Talk"):
//                                                    drawable = R.drawable.seminar;
//                                                    break;
//                                                case ("Tour"):
//                                                    drawable = R.drawable.tour;
//                                                    break;
//                                                case ("Tournament"):
//                                                    drawable = R.drawable.tournament;
//                                                    break;
//                                                case ("Tradeshow, Consumer Show, or Expo"):
//                                                    drawable = R.drawable.trade_shower;
//                                                    break;
//
//                                                default:
//                                                    drawable = R.drawable.other;
//                                                    break;
//                                            }
//                                            LatLng location = new LatLng(event.lat, event.lon);
//                                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawable);
//                                            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 70, 70, true);
//                                            String snippet = event.placeID;
//                                            MarkerOptions eventMarkerOptions = new MarkerOptions().position(location).snippet(snippet).icon(BitmapDescriptorFactory.fromBitmap(scaled));
//                                            eventMap.put(placeID, event);
//                                            map.addMarker(eventMarkerOptions);
//                                        }
//                                    });
//
//
//                                }
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onKeyExited(String s) {
//
//                    }
//
//                    @Override
//                    public void onKeyMoved(String s, GeoPoint geoPoint) {
//
//                    }
//
//                    @Override
//                    public void onGeoQueryReady() {
//
//                    }
//
//                    @Override
//                    public void onGeoQueryError(Exception e) {
//
//                    }
//                });
//
//            }
//        };

        //mMap.setOnCameraIdleListener(onCameraIdleListener);

//        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                //Get basic event info
//                String placeID = marker.getSnippet();
//                final EventItem event = eventMap.get(placeID);
//                if (event != null) {
//                    FirebaseFirestore db = FirebaseFirestore.getInstance();
//                    CollectionReference places = db.collection("places");
//                    final DocumentReference place = places.document(placeID);
//                    CollectionReference topics = place.collection("type");
//                    topics.orderBy("Votes", Query.Direction.DESCENDING).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                        @Override
//                        public void onSuccess(QuerySnapshot snapshots) {
//                            QueryDocumentSnapshot top = null;
//                            for (QueryDocumentSnapshot topTopic : snapshots) {
//                                top = topTopic;
//                            }
//                            Double votes = 0.0;
//                            if (top != null) votes = top.getDouble("Votes");
//                            if (votes == null || votes == 0) {
//                                event.type = "Unknown";
//                            } else event.type = top.getId();
//                            CollectionReference remarksRef = place.collection("remarks");
//                            remarksRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                @Override
//                                public void onSuccess(QuerySnapshot snapshots) {
//                                    //Load all of the remarks and put them into the ArrayList
//                                    ArrayList<String> remarks = new ArrayList<>();
//                                    for (QueryDocumentSnapshot remark : snapshots) {
//                                        String comment = remark.getString("Comment");
//                                        remarks.add(comment);
//                                    }
//                                    //Everything is loaded now, pop up the dialog
//
//                                    final Dialog eventView = new Dialog(mainActivity);
//                                    eventView.setContentView(R.layout.old_event_detail_view);
//                                    TextView popCount = eventView.findViewById(R.id.population_count);
//                                    TextView maleCount = eventView.findViewById(R.id.male_count);
//                                    TextView femaleCount = eventView.findViewById(R.id.female_count);
//                                    TextView avgAge = eventView.findViewById(R.id.avg_age);
//                                    TextView type = eventView.findViewById(R.id.type);
//                                    TextView type = eventView.findViewById(R.id.type);
//                                    TextView name = eventView.findViewById(R.id.name);
//                                    View viewChatButton = eventView.findViewById(R.id.view_chat_btn);
//                                    popCount.setText(String.valueOf(event.population));
//                                    maleCount.setText(String.valueOf(event.numMales));
//                                    femaleCount.setText(String.valueOf(event.numFemales));
//                                    avgAge.setText(String.valueOf(event.avgAge));
//                                    type.setText(event.type);
//                                    type.setText(event.type);
//                                    name.setText(event.placeName);
//                                    RecyclerView remarksRecycler = eventView.findViewById(R.id.remarks_recycler);
//                                    EventRemarksAdapter eventRemarksAdapter = new EventRemarksAdapter(mainActivity, remarks);
//                                    LinearLayoutManager layoutManager = new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false);
//                                    remarksRecycler.setLayoutManager(layoutManager);
//                                    remarksRecycler.setAdapter(eventRemarksAdapter);
//                                    viewChatButton.setOnClickListener(new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View view) {
//                                            mainActivity.viewChatPlaceID = event.placeID;
//                                            mainActivity.viewChatPlaceName = event.placeName;
//                                            TextView currentLocation = mainActivity.findViewById(R.id.city_name);
//                                            currentLocation.setText(event.placeName);
//                                            mainActivity.updateChat();
//                                            ((ImageView) mainActivity.findViewById(R.id.chat_img)).setColorFilter(Color.parseColor("#F79621"));
//                                            ((ImageView) mainActivity.findViewById(R.id.explore_img)).setColorFilter(Color.parseColor("#5B5B5B"));
//                                            ((ImageView) mainActivity.findViewById(R.id.map_img)).setColorFilter(Color.parseColor("#5B5B5B"));
//                                            ((ImageView) mainActivity.findViewById(R.id.settings_img)).setColorFilter(Color.parseColor("#5B5B5B"));
//                                            mainActivity.pager.setCurrentItem(1);
//                                            eventView.dismiss();
//                                        }
//                                    });
//                                    eventView.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                                        @Override
//                                        public void onDismiss(DialogInterface dialogInterface) {
//                                            if (mainActivity.spectatorLocationItem != null && !mainActivity.spectatorLocationItem.toString().equals("")
//                                                    && !mainActivity.locationItem.toString().equals(mainActivity.spectatorLocationItem.toString())) {
//                                                TextView currentLocation = mainActivity.findViewById(R.id.city_name);
//                                                currentLocation.setText(mainActivity.spectatorLocationItem.city);
//                                            } else {
//                                                TextView currentLocation = mainActivity.findViewById(R.id.city_name);
//                                                currentLocation.setText(mainActivity.locationItem.city);
//                                            }
//                                        }
//                                    });
//
//
//                                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//                                    lp.copyFrom(eventView.getWindow().getAttributes());
//                                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//                                    lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//                                    //eventView.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//                                    eventView.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                                    eventView.show();
//                                    eventView.getWindow().setAttributes(lp);
//
//                                }
//                            });
//                        }
//                    });
//                }
//
//
//                return false;
//            }
//        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mainActivity != null && mainActivity.locationItem != null && mainActivity.locationItem.city != null) {
                if (mainActivity.spectatorLocationItem != null && !mainActivity.spectatorLocationItem.toString().equals("")
                        && !mainActivity.locationItem.toString().equals(mainActivity.spectatorLocationItem.toString())) {
                    TextView currentLocation = mainActivity.findViewById(R.id.city_name);
                    currentLocation.setText(mainActivity.spectatorLocationItem.city);
                } else {
                    TextView currentLocation = mainActivity.findViewById(R.id.city_name);
                    currentLocation.setText(mainActivity.locationItem.city);
                }
            }
        }
    }

    public void loadMapEvents() {
        //Get the events and update the adapter to make them show up
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference cities = db.collection("cities");
        DocumentReference city;
        String cityID = "";
        if (mainActivity.spectatorLocationItem == null || mainActivity.spectatorLocationItem.toString().equals("")) {

            cityID = mainActivity.locationItem.toString();
            city = cities.document(mainActivity.locationItem.toString());
        } else {
            cityID = mainActivity.spectatorLocationItem.toString();
            city = cities.document(mainActivity.spectatorLocationItem.toString());
        }
        final String finalCityID = cityID;

        CollectionReference events = city.collection("events");
        events.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                shownEvents = new ArrayList<>();
                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                    String eventID = snapshot.getId();
                    String eventName = snapshot.getString("Name");
                    String type = snapshot.getString("Type");
                    Double numThere = snapshot.getDouble("PopulationThere");
                    Double numGoing = snapshot.getDouble("PopulationAll");
                    Double lat = snapshot.getDouble("Lat");
                    Double lon = snapshot.getDouble("Long");

                    Double ageAll = snapshot.getDouble("AgeAll");
                    Double ageThere = snapshot.getDouble("AgeThere");
                    Double femaleAll = snapshot.getDouble("FemaleAll");
                    Double femaleThere = snapshot.getDouble("FemaleThere");
                    Double maleAll = snapshot.getDouble("MaleAll");
                    Double maleThere = snapshot.getDouble("MaleThere");
                    String website = snapshot.getString("Website");
                    String imagePath = snapshot.getString("ImageURL");


                    final MapEventItem mapEventItem = new MapEventItem();
                    mapEventItem.eventID = eventID;
                    mapEventItem.cityID = finalCityID;

                    if(numThere != null) mapEventItem.populationThere = numThere.intValue();
                    else mapEventItem.populationThere = 0;

                    if(numGoing != null) mapEventItem.populationAll = numGoing.intValue();
                    else mapEventItem.populationAll = 0;

                    if(ageAll != null) mapEventItem.ageAll = ageAll;
                    else mapEventItem.ageAll = 0.0;

                    if(ageThere != null) mapEventItem.ageThere = ageThere;
                    else mapEventItem.ageThere = 0.0;

                    if(femaleAll != null) mapEventItem.femaleAll = femaleAll.intValue();
                    else mapEventItem.femaleAll = 0;

                    if(femaleThere != null) mapEventItem.femaleThere = femaleThere.intValue();
                    else mapEventItem.femaleThere = 0;

                    if(maleAll != null) mapEventItem.maleAll = maleAll.intValue();
                    else mapEventItem.maleAll = 0;

                    if(maleThere != null) mapEventItem.maleThere = maleThere.intValue();
                    else mapEventItem.maleThere = 0;

                    mapEventItem.type = type;
                    mapEventItem.eventName = eventName;
                    mapEventItem.website = website;
                    mapEventItem.imagePath = imagePath;

                    if (lat != null && lon != null) {
                        mapEventItem.lat = lat;
                        mapEventItem.lon = lon;
                    }
                    boolean userClose = checkDistance(lat, lon);
                    if(userClose){
                        mapEventItem.userInDistance = true;
                    }else{
                        mapEventItem.userInDistance = false;
                    }

                    //Bitmap bitmap = fetchPhotoResponse.getBitmap();
                    //mapEventItem.image = bitmap;

                    shownEvents.add(mapEventItem);
                }
                mapEventRecyclcer = new MapEventRecyclcer(mainActivity, shownEvents, eventRecyclerView);
                eventRecyclerView.setLayoutManager(layoutManager);
                eventRecyclerView.setAdapter(mapEventRecyclcer);
                mainActivity.shownEvents = shownEvents;
                ArrayList<MapEventItem> closeEvents = new ArrayList<>();
                for (MapEventItem event : shownEvents){
                    if(event.userInDistance){
                        //User is within the minimum distance to possibly be attending the event
                        closeEvents.add(event);
                    }
                }
                if(!closeEvents.isEmpty()){
                    mainActivity.closeEvents = closeEvents;
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference users = db.collection("users");
                    String uID = FirebaseAuth.getInstance().getUid();
                    DocumentReference user = users.document(uID);
                    user.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String userEvent = documentSnapshot.getString("Event");
                            if(userEvent == null || userEvent.equals("")){
                                //User is not attending any events
                                //launch asking prompt
                                mainActivity.openPrompt(null,2);
                            }
                        }
                    });



                }else mainActivity.closeEvents = null;
            }
        });
    }

    private boolean checkDistance(double lat, double lon){
        Location userLoc = mainActivity.mLastKnownLocation;
        Location eventLoc = new Location("");
        eventLoc.setLatitude(lat);
        eventLoc.setLongitude(lon);
        if(userLoc != null){
            double distance = -1;
            distance = userLoc.distanceTo(eventLoc);
            if (Math.abs(distance)<= MIN_DISTANCE){
                return true;
            }else return false;
        }else return false;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
