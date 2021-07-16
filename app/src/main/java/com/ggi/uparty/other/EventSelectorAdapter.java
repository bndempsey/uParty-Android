package com.ggi.uparty.other;

//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ggi.uparty.MainActivity;
import com.ggi.uparty.R;
import com.ggi.uparty.create.CreateEventFragment;
import com.ggi.uparty.viewpagerFrags.MapEventItem;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class EventSelectorAdapter extends  RecyclerView.Adapter<EventSelectorAdapter.ViewHolder> {

    MainActivity mainActivity;
    CreateEventFragment createEventFragment;
    ArrayList<MapEventItem> events;

    public EventSelectorAdapter(MainActivity mainActivity, CreateEventFragment createEventFragment, ArrayList<MapEventItem> events){
        this.mainActivity = mainActivity;
        this.createEventFragment = createEventFragment;
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.simple_vote_item,viewGroup,false);
        return new EventSelectorAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final int ii = i;
        viewHolder.itemText.setText(events.get(ii).eventName);
        viewHolder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String eventID = events.get(ii).eventID;
                String cityID = mainActivity.locationItem.toString();
                final String uID = FirebaseAuth.getInstance().getUid();
                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                //Check to see if the user has already voted on the event that they just selected
                CollectionReference cities = db.collection("cities");
                DocumentReference city = cities.document(cityID);
                CollectionReference events = city.collection("events");
                final DocumentReference event = events.document(eventID);
                CollectionReference voted = event.collection("voted");
                voted.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        boolean hasVoted = false;
                        for(DocumentSnapshot snap : queryDocumentSnapshots){
                            String snapKey = snap.getId();
                            if(snapKey.equals(uID)){
                                hasVoted = true;
                            }
                        }
                        if(!hasVoted){
                            //The user has not voted so proceed to the type/topic voting
                            //set them as attending the event
                            CollectionReference users = db.collection("users");
                            DocumentReference user = users.document(uID);
                            HashMap<String, Object> eventUpdateMap = new HashMap<>();
                            eventUpdateMap.put("Event",eventID);
                            user.update(eventUpdateMap);
                            createEventFragment.setState(2);
//                            user.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                                @Override
//                                public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
//                                    String push = documentSnapshot.getString("Push");
//                                    String birthday = documentSnapshot.getString("Birthday");
//                                    String gender = documentSnapshot.getString("Gender");
//
//                                    HashMap<String, Object> userMap = new HashMap<>();
//                                    userMap.put("Push",push);
//                                    userMap.put("Gender",gender);
//                                    userMap.put("Birthday", birthday);
//                                    CollectionReference attending = event.collection("there");
//                                    DocumentReference attendee = attending.document(uID);
//                                    attendee.set(userMap);
//                                    createEventFragment.setState(2);
//
//                                }
//                            });
                        }else{
                            //The user has already voted so do onBackPressed()
                            mainActivity.onBackPressed();
                        }
                    }
                });
                createEventFragment.attendingEvent = eventID;



            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        public View item;
        public TextView itemText;

        public ViewHolder(@NonNull View view) {
            super(view);
            item = view;
            itemText = view.findViewById(R.id.item_text);
        }
    }
}
