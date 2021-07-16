package com.ggi.uparty.create;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ggi.uparty.MainActivity;
import com.ggi.uparty.R;
import com.ggi.uparty.other.EventSelectorAdapter;
import com.ggi.uparty.other.VoteRecyclerAdapter;
import com.ggi.uparty.viewpagerFrags.MapEventItem;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateEventFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateEventFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateEventFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private MainActivity mainActivity;
    AutocompleteSupportFragment placeAutoComplete;
    TextView typeTopicText;
    View autocompleteHolder;
    View typeTopicHolder;
    RecyclerView typeTopicRecycler;

    ArrayList<String> typeList;
    ArrayList<String> topicList;


    public Place selectedPlace;
    public String selectedType;
    public String selectedTopic;
    public String attendingEvent;
    public String placeCityID;

    PlacesClient placesClient;


    public int startState;

    public CreateEventFragment() {
        // Required empty public constructor
    }


    public static CreateEventFragment newInstance(Bundle b) {
        CreateEventFragment fragment = new CreateEventFragment();
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            startState = getArguments().getInt("startState");
        }
        typeList = new ArrayList<>();
        topicList = new ArrayList<>();
        loadTypeArray();
        loadTopicList();
        placesClient = Places.createClient(mainActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity.allowBackPress = true;

        typeTopicText = view.findViewById(R.id.type_topic_text);
        typeTopicHolder = view.findViewById(R.id.type_topic_holder);
        typeTopicRecycler = view.findViewById(R.id.type_topic_picker);
        autocompleteHolder = view.findViewById(R.id.autocomplete_holder);

        if(getArguments().getInt("startState") == 1) { //Creating a new event

            placeAutoComplete = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
            placeAutoComplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.PHOTO_METADATAS));
            placeAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    //Set place that was selected
                    selectedPlace = place;

                    LatLng latLng = place.getLatLng();
                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(getContext(), Locale.getDefault());
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
                        String cityID = city + "@" + state + "@" + country;
                        placeCityID = cityID;
                        createEvent(place,cityID);
                    } catch (IOException ignore) {

                    }






                    //Go to the type state
                    setState(2);
                }

                @Override
                public void onError(@NonNull Status status) {

                }
            });
        }else if(getArguments().getInt("startState") == 2){ //Asking user what event they are at
            setState(5);
        }else if(getArguments().getInt("startState") == 3){ //User opted to attend an event and hasnt voted
            attendingEvent = getArguments().getString("eventID");
            setState(2);
        }
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
        if(context instanceof MainActivity){
            mainActivity = (MainActivity)context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public void setState(int state){
        switch(state){
            case 1: //place selector
                autocompleteHolder.setVisibility(View.VISIBLE);
                typeTopicHolder.setVisibility(View.GONE);
                mainActivity.allowBackPress = true;
                break;
            case 2: //type selector

                autocompleteHolder.setVisibility(View.GONE);
                typeTopicHolder.setVisibility(View.VISIBLE);
                String typeText = "What type of event is this?";
                typeTopicText.setText(typeText);
                VoteRecyclerAdapter voteRecyclerAdapter = new VoteRecyclerAdapter(this,typeList,1,"");
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainActivity,RecyclerView.VERTICAL,false);
                typeTopicRecycler.setLayoutManager(linearLayoutManager);
                typeTopicRecycler.setAdapter(voteRecyclerAdapter);
                break;
            case 3: // topic selector
                //Do firebase call to set type vote
                if(selectedPlace != null) castVote(placeCityID,selectedPlace.getId(),"type",selectedType);
                else castVote(placeCityID,attendingEvent,"type",selectedType);
                //Since they have voted on a type, put their uID and timestamp under the voted section of that event
                if(attendingEvent != null){
                    String uID = FirebaseAuth.getInstance().getUid();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference cities = db.collection("cities");
                    DocumentReference city = cities.document(placeCityID);
                    CollectionReference events = city.collection("events");
                    DocumentReference event = events.document(attendingEvent);
                    CollectionReference voted = event.collection("voted");
                    DocumentReference userVoted = voted.document(uID);
                    HashMap<String,Object> userVoteMap = new HashMap<>();
                    String time;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = new Date(System.currentTimeMillis());
                    time = sdf.format(date);
                    time = time + " +0000";
                    userVoteMap.put("Time", time);
                    userVoted.set(userVoteMap);
                }

                autocompleteHolder.setVisibility(View.GONE);
                typeTopicHolder.setVisibility(View.VISIBLE);
                String topicText = "What is the topic of this event?";
                typeTopicText.setText(topicText);
                VoteRecyclerAdapter voteTopicRecyclerAdapter = new VoteRecyclerAdapter(this,topicList,2,"");
                LinearLayoutManager linearTopicLayoutManager = new LinearLayoutManager(mainActivity,RecyclerView.VERTICAL,false);
                typeTopicRecycler.setLayoutManager(linearTopicLayoutManager);
                typeTopicRecycler.setAdapter(voteTopicRecyclerAdapter);
                break;
            case 4:
                //Do firebase call to set topic vote
                if(selectedPlace != null) castVote(placeCityID,selectedPlace.getId(),"topic",selectedTopic);
                else castVote(placeCityID,attendingEvent,"topic",selectedTopic);
                Bundle bundle = new Bundle();
                String placeID = null;
                if(selectedPlace != null) {
                    placeID = selectedPlace.getId();
                }else{
                    placeID = attendingEvent;
                }
                bundle.putString("eventID", placeID);
                bundle.putString("cityID",placeCityID);

                //Go to the next fragment
                CreateEventTwoFragment createEventTwoFragment = null;
                try {
                    createEventTwoFragment = CreateEventTwoFragment.class.newInstance();
                    createEventTwoFragment.setArguments(bundle);
                    //createEventTwoFragment.setArguments();
                }catch(Exception e){

                }
                if(createEventTwoFragment != null) {
                    FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                    mainActivity.findViewById(R.id.fullscreen_frame).setVisibility(View.VISIBLE);
                    fragmentManager.beginTransaction().replace(R.id.fullscreen_frame, createEventTwoFragment,"CREATE_EVENT_TWO").commit();
                }
                break;
            case 5:
                //Ask the user if they are attending any of the listed events
                //Then ask type/topic/remarks
                autocompleteHolder.setVisibility(View.GONE);
                typeTopicHolder.setVisibility(View.VISIBLE);
                String attendingText = "Are you at one of these events?";
                typeTopicText.setText(attendingText);
                ArrayList<MapEventItem> possibleAttEvents = mainActivity.closeEvents;
                //Using the latlng of the MapEventItem, get the placeCityID and set it
                MapEventItem sampleEvent = possibleAttEvents.get(0);
                double lat = sampleEvent.lat;
                double lon = sampleEvent.lon;
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(getContext(), Locale.getDefault());
                String city = "";
                String state2 = "";
                String country = "";

                try {
                    addresses = geocoder.getFromLocation(lat, lon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                    //String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    city = addresses.get(0).getLocality();
                    state2 = addresses.get(0).getAdminArea();
                    country = addresses.get(0).getCountryName();
                    //String postalCode = addresses.get(0).getPostalCode();
                    //String knownName = addresses.get(0).getFeatureName();
                    String cityID = city + "@" + state2 + "@" + country;
                    placeCityID = cityID;
                } catch (IOException ignore) {

                }

                EventSelectorAdapter attendingRecyclerAdapter = new EventSelectorAdapter(mainActivity,this,possibleAttEvents);
                LinearLayoutManager attendingLayoutManager = new LinearLayoutManager(mainActivity,RecyclerView.VERTICAL,false);
                typeTopicRecycler.setLayoutManager(attendingLayoutManager);
                typeTopicRecycler.setAdapter(attendingRecyclerAdapter);
                break;
        }
    }

    private void castVote(String cityID, String placeID, final String typeTopic, final String vote){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference cities = db.collection("cities");
        DocumentReference city = cities.document(cityID);
        CollectionReference events = city.collection("events");
        DocumentReference event = events.document(placeID);
        final CollectionReference typeTopicRef = event.collection(typeTopic);
        typeTopicRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                boolean alreadyExists = false;
                DocumentSnapshot existingDoc = null;
                for(DocumentSnapshot snapshot : queryDocumentSnapshots){
                    String title = snapshot.getString(typeTopic);
                    if(title != null) {
                        if (title.equals(vote)) {
                            alreadyExists = true;
                            existingDoc = snapshot;
                        }
                    }
                }
                if(alreadyExists){
                    //Someone has already cast the same vote, so update the number of votes with +1
                    if(existingDoc.exists()) {
                        Double votes = existingDoc.getDouble("Votes");
                        if(votes != null){
                            double votesNew = votes + 1;
                            HashMap<String, Object> voteMap = new HashMap<>();
                            voteMap.put("Votes",votesNew);
                            DocumentReference voteRef = typeTopicRef.document(vote);
                            voteRef.update(voteMap);

                        }
                    }


                }else{
                    //Nobody has voted similarly on whatever, so add the vote name to the collection with a Votes value of 1
                    DocumentReference voteRef = typeTopicRef.document(vote);
                    HashMap<String, Object> voteMap = new HashMap<>();
                    voteMap.put("Votes",1.0);
                    voteRef.set(voteMap);

                }
            }
        });
    }

    private void createEvent(Place place, String cityID){
        HashMap<String,Object> eventMap= new HashMap<>();
        eventMap.put("MaleAll", 0);
        eventMap.put("FemaleAll", 0);
        eventMap.put("PopulationAll", 0);
        eventMap.put("AgeAll", 0);
        eventMap.put("MaleThere", 0);
        eventMap.put("FemaleThere", 0);
        eventMap.put("PopulationThere", 0);
        eventMap.put("AgeThere", 0);
        eventMap.put("Lat", place.getLatLng().latitude);
        eventMap.put("Long",place.getLatLng().longitude);
        eventMap.put("Name", place.getName());
        eventMap.put("Website", "");
        eventMap.put("Type", "Other");
        eventMap.put("Topic", "Other");
        String urlImg = "gs://uparty-22cc0.appspot.com/events/" + place.getId() + ".png";
        eventMap.put("ImageURL", urlImg);
        if(place.getWebsiteUri() != null){
            String url = place.getWebsiteUri().toString();
            eventMap.put("Website",url);
        }else{
            eventMap.put("Website", "");
        }
        Long millis = System.currentTimeMillis();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String text = formatter.format(new Date(millis));
        text += " +0000";
        eventMap.put("TimeCreated",text);
        eventMap.put("PlaceID",place.getId());



        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference cities = db.collection("cities");
        DocumentReference city = cities.document(cityID);
        CollectionReference events = city.collection("events");
        DocumentReference event = events.document(place.getId());
        event.set(eventMap);
        final String placeID = place.getId();

        List<PhotoMetadata> photos = place.getPhotoMetadatas();
        if(photos != null && photos.size() > 0) {
            PhotoMetadata photo = photos.get(0);
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photo).build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener(new OnSuccessListener<FetchPhotoResponse>() {
                @Override
                public void onSuccess(FetchPhotoResponse fetchPhotoResponse) {
                    Bitmap bitmap = fetchPhotoResponse.getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    String imgPath = "events/" + placeID + ".png";
                    StorageReference storageRef = storage.getReference(imgPath);
                    storageRef.putBytes(byteArray);
                }
            });


        }

    }

    private void loadTypeArray(){
        typeList.add("Appearance or Signing");
        typeList.add("Attraction");
        typeList.add("Camp, Trip, or Retreat");
        typeList.add("Class, Training, or Workshop");
        typeList.add("Concert or Performance");
        typeList.add("Conference");
        typeList.add("Convention");
        typeList.add("Dinner or Gala");
        typeList.add("Festival or Fair");
        typeList.add("Game or Competition");
        typeList.add("Meeting or Networking Event");
        typeList.add("Other");
        typeList.add("Party or Social Gathering");
        typeList.add("Race or Endurance Event");
        typeList.add("Rally");
        typeList.add("Screening");
        typeList.add("Seminar or Talk");
        typeList.add("Tour");
        typeList.add("Tournament");
        typeList.add("Tradeshow, Consumer Show, or Expo");
    }
    private void loadTopicList(){
        topicList.add("Auto, Boat, & Air");
        topicList.add("Business & Professional");
        topicList.add("Charity & Causes");
        topicList.add("Community & Culture");
        topicList.add("Family & Education");
        topicList.add("Fashion & Beauty");
        topicList.add("Film, Media, & Entertainment");
        topicList.add("Food & Drink");
        topicList.add("Government & Politics");
        topicList.add("Health & Wellness");
        topicList.add("Hobbies & Special Interest");
        topicList.add("Home & Lifestyle");
        topicList.add("Music");
        topicList.add("Other");
        topicList.add("Performing & Visual Arts");
        topicList.add("Religion & Spirituality");
        topicList.add("School Activities");
        topicList.add("Science & Technology");
        topicList.add("Seasonal & Holiday");
        topicList.add("Sports & Fitness");
        topicList.add("Travel & Outdoor");
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
