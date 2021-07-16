package com.ggi.uparty.other;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ggi.uparty.MainActivity;
import com.ggi.uparty.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EventDetailView.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EventDetailView#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventDetailView extends Fragment {




    private OnFragmentInteractionListener mListener;
    private MainActivity mainActivity;

    boolean isGoing;
    final int MIN_DISTANCE = 300;

    public EventDetailView() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static EventDetailView newInstance(Bundle bundle) {
        EventDetailView fragment = new EventDetailView();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        isGoing = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_detail_view, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainActivity.allowBackPress = true;
        byte[] byteArray = getArguments().getByteArray("image");
        Double ageAll = getArguments().getDouble("ageAll");
        Double ageThere = getArguments().getDouble("ageThere");
        int femaleAll = getArguments().getInt("femaleAll");
        int femaleThere = getArguments().getInt("femaleThere");
        int maleAll = getArguments().getInt("maleAll");
        int maleThere = getArguments().getInt("maleThere");
        int populationAll = getArguments().getInt("populationAll");
        int populationThere = getArguments().getInt("populationThere");
        final String eventID = getArguments().getString("eventID");
        final String cityID = getArguments().getString("cityID");
        String website = getArguments().getString("website");
        final double lat = getArguments().getDouble("lat");
        final double lon = getArguments().getDouble("lon");

        TextView ageAllText = view.findViewById(R.id.age_all);
        TextView ageThereText = view.findViewById(R.id.age_there);
        TextView maleAllText = view.findViewById(R.id.male_all);
        TextView maleThereText = view.findViewById(R.id.male_there);
        TextView femaleAllText = view.findViewById(R.id.female_all);
        TextView femaleThereText = view.findViewById(R.id.female_there);
        TextView populationAllText = view.findViewById(R.id.population_all);
        TextView populationThereText = view.findViewById(R.id.population_there);
        final ImageView goingImg = view.findViewById(R.id.going_image);
        ImageView shareButton = view.findViewById(R.id.share_holder);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareBody = "There is an event happening at " + getArguments().getString("name") + ". Check it out on uParty! https://www.upartyapp.com";
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(sharingIntent);
            }
        });

        final TextView goingText = view.findViewById(R.id.going_text);

        ageAllText.setText("(" + ageAll + ")");
        ageThereText.setText("" + ageThere);
        femaleAllText.setText("(" + femaleAll + ")");
        femaleThereText.setText("" + femaleThere);
        maleAllText.setText("(" + maleAll + ")");
        maleThereText.setText("" + maleThere);
        populationAllText.setText("(" + populationAll + ")");
        populationThereText.setText("" + populationThere);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String uID = FirebaseAuth.getInstance().getUid();

        final boolean isInRange = checkDistance(lat,lon);
        if(isInRange){
            goingText.setText("At Event");
            CollectionReference users = db.collection("users");
            DocumentReference user = users.document(uID);
            user.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String event = documentSnapshot.getString("Event");
                    if(event == null){
                        //They are not going anywhere, set image and text to grey
                        goingImg.setColorFilter(Color.parseColor("#808080"));
                        goingText.setTextColor(Color.parseColor("#808080"));
                    }else{
                        //They are already going, set image and text to blue
                        goingImg.setColorFilter(getResources().getColor(R.color.colorPrimary));
                        goingText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                }
            });
        }else{
            goingText.setText("Going");
            CollectionReference cities = db.collection("cities");
            DocumentReference city = cities.document(cityID);
            CollectionReference events = city.collection("events");
            DocumentReference event = events.document(eventID);
            CollectionReference all = event.collection("all");
            all.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    boolean isThere = false;
                    for(DocumentSnapshot doc : queryDocumentSnapshots){
                        String id = doc.getId();
                        if(uID.equals(id)){
                            isThere = true;
                        }
                    }
                    if(isThere){
                        goingImg.setColorFilter(getResources().getColor(R.color.colorPrimary));
                        goingText.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }else{
                        goingImg.setColorFilter(Color.parseColor("#808080"));
                        goingText.setTextColor(Color.parseColor("#808080"));
                    }
                }
            });


        }





        if(byteArray != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            ImageView eventImg = view.findViewById(R.id.event_img);
            eventImg.setImageBitmap(bmp);
        }
        TextView eventName = view.findViewById(R.id.event_name);
        String eventNameString = getArguments().getString("name");
        eventName.setText(eventNameString);
        View goingHolder = view.findViewById(R.id.going_holder);
        View shareHolder = view.findViewById(R.id.share_holder);
        View moreHolder = view.findViewById(R.id.more_holder);

        //final TextView goingText = view.findViewById(R.id.going_text);
        goingHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String uID = FirebaseAuth.getInstance().getUid();

                if(!isInRange){
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference cities = db.collection("cities");
                    DocumentReference city = cities.document(cityID);
                    CollectionReference events = city.collection("events");
                    DocumentReference event = events.document(eventID);
                    final CollectionReference all = event.collection("all");
                    final DocumentReference user = all.document(uID);
                    user.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.exists()){
                                //User is already "going", so delete them
                                user.delete();
                                goingImg.setColorFilter(Color.parseColor("#808080"));
                                goingText.setTextColor(Color.parseColor("#808080"));


                            }else{
                                //User is not already goign so add them
                                CollectionReference users = db.collection("users");
                                DocumentReference user = users.document(uID);
                                user.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                        String push = documentSnapshot.getString("Push");
                                        String birthday = documentSnapshot.getString("Birthday");
                                        String gender = documentSnapshot.getString("Gender");

                                        HashMap<String, Object> userMap = new HashMap<>();
                                        userMap.put("Push",push);
                                        userMap.put("Gender",gender);
                                        userMap.put("Birthday", birthday);
                                        all.document(uID).set(userMap);
                                        goingImg.setColorFilter(getResources().getColor(R.color.colorPrimary));
                                        goingText.setTextColor(getResources().getColor(R.color.colorPrimary));
                                    }
                                });

                            }
                        }
                    });



                }else{
                    String uID2 = FirebaseAuth.getInstance().getUid();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference users = db.collection("users");
                    final DocumentReference user = users.document(uID2);
                    user.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String event = documentSnapshot.getString("Event");
                            if(event == null){
                                //update "Event" to hold the placeID of the event
                                HashMap<String,Object> update = new HashMap<>();
                                update.put("Event",eventID);
                                user.update(update);
                                goingImg.setColorFilter(getResources().getColor(R.color.colorPrimary));
                                goingText.setTextColor(getResources().getColor(R.color.colorPrimary));
                            }else{
                                HashMap<String,Object> update = new HashMap<>();
                                update.put("Event", FieldValue.delete());
                                user.update(update);
                                goingImg.setColorFilter(Color.parseColor("#808080"));
                                goingText.setTextColor(Color.parseColor("#808080"));
                            }
                        }
                    });

                }

            }
        });
        shareHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        moreHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        final RecyclerView remarksRecycler = view.findViewById(R.id.remarks_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        remarksRecycler.setLayoutManager(linearLayoutManager);
        final ArrayList<String> remarks = new ArrayList<>();
        FirebaseFirestore db2 = FirebaseFirestore.getInstance();
        CollectionReference cities = db2.collection("cities");
        DocumentReference city;
        if(mainActivity.spectatorLocationItem != null && !mainActivity.spectatorLocationItem.toString().equals("")){
            city = cities.document(mainActivity.spectatorLocationItem.toString());
        }else{
            city = cities.document(mainActivity.locationItem.toString());
        }
        CollectionReference events = city.collection("events");
        DocumentReference event = events.document(eventID);
        CollectionReference remarksRef = event.collection("remarks");
        remarksRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                for(DocumentSnapshot snapshot : queryDocumentSnapshots){
                    String remark = snapshot.getString("Comment");
                    remarks.add(remark);
                }
                //done, do the thing
                EventRemarksAdapter eventRemarksAdapter = new EventRemarksAdapter(mainActivity,remarks);
                remarksRecycler.setAdapter(eventRemarksAdapter);
            }
        });


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
        if(context instanceof MainActivity) mainActivity = (MainActivity)context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
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
