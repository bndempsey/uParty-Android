package com.ggi.uparty.viewpagerFrags;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.Fragment;
//import android.support.v4.content.ContextCompat;
//import android.support.v4.content.FileProvider;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ggi.uparty.MainActivity;
import com.ggi.uparty.R;
import com.ggi.uparty.other.LocationItem;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.Nullable;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    MainActivity mainActivity;
    boolean isAnonymous;
    boolean isAnonymousImg;
    ChatRecyclerAdapter chatRecyclerAdapter;
    public LocationItem lastLocationItem;
    public ArrayList<MessageItem> messageItems;
    ListenerRegistration listenerRegistration;
    PlaceDetectionClient mPlaceDetectionClient;
    boolean isCityChat;
    boolean isSpectating;

    //---------MESSAGE BAR VIEWS---------
    TextView sendButton;
    EditText messageEdit;
    ImageView anonymousButton;
    TextView chatOffBar;
    View messageBar;
    ImageView pictureButton;
    //--------OTHER VIEWS--------
    RecyclerView chatRecycler;
    TextView placeTab;
    TextView cityTab;
    TextView hotTab;
    LinearLayout tabs;
    private AdView chatAdView;


    //--------CONSTANTS--------
    final int CITY_CHAT = 0;
    final int PLACE_CHAT = 1;
    final int HOT_CHAT = 2;
    final String NOT_SIGNED_IN = "You must be signed in to participate in chat";
    final String CHAT_SPECTATOR = "You cannot participate in this chat";

    //--------------IMAGE VARIABLES----------------
    Uri imgUri;
    int WRITE_EXTERNAL_PERMISSION = 1;


    public ChatFragment() {
        // Required empty public constructor
    }


    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
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
        isAnonymous = false;
        isAnonymousImg = false;
        lastLocationItem = mainActivity.locationItem;
        messageItems = new ArrayList<>();
        isCityChat = true;
        isSpectating = false;

        //Check if the user is at an event, make the event tab invisible if not
        String uID = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");
        if(uID != null) {
            DocumentReference user = users.document(uID);
            user.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null) {
                        String attendingEventID = documentSnapshot.getString("Event");
                        if (attendingEventID == null || attendingEventID.equals("")) {
                            //They are not attending an event so get rid of the event tab
                            placeTab.setVisibility(View.GONE);
                            tabs.setWeightSum(2);
                        } else {
                            placeTab.setVisibility(View.VISIBLE);
                            tabs.setWeightSum(3);
                        }
                    }
                }
            });
        }else{
            //onCreate(savedInstanceState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        anonymousButton = view.findViewById(R.id.anonymous_btn);
        pictureButton = view.findViewById(R.id.picture_button);
        chatRecycler = view.findViewById(R.id.chat_recycler);
        sendButton = view.findViewById(R.id.send_btn);
        messageEdit = view.findViewById(R.id.message_input);
        chatOffBar = view.findViewById(R.id.chat_off_bar);
        messageBar = view.findViewById(R.id.chat_entry);

        chatRecyclerAdapter = new ChatRecyclerAdapter(mainActivity, messageItems);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        chatRecycler.setLayoutManager(layoutManager);
        chatRecycler.setAdapter(chatRecyclerAdapter);
        placeTab = view.findViewById(R.id.event_tab);
        cityTab = view.findViewById(R.id.city_tab);
        hotTab = view.findViewById(R.id.hot_tab);
        tabs = view.findViewById(R.id.tabs);
        mPlaceDetectionClient = Places.getPlaceDetectionClient(mainActivity, null);
        //chatAdView = view.findViewById(R.id.ad_chat);
        //AdRequest adRequest = new AdRequest.Builder().build();
        //chatAdView.loadAd(adRequest);



        pictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if (ContextCompat.checkSelfPermission(mainActivity.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Dialog dialog = new Dialog(mainActivity);
                    dialog.setContentView(R.layout.take_or_select_img_dialog);
                    dialog.findViewById(R.id.take_picture).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            File outputFile = null;
                            try{
                                outputFile = createImageFile();
                            }catch (IOException e){

                            }
                            imgUri = FileProvider.getUriForFile(
                                    mainActivity,
                                    "com.ggi.uparty.provider", //(use your app signature + ".provider" )
                                    outputFile);
                            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            takePicture.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
                            startActivityForResult(takePicture, 0);
                        }
                    });
                    dialog.findViewById(R.id.choose_img).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, 1);
                        }
                    });
                    dialog.show();


                }else{
                    ActivityCompat.requestPermissions(mainActivity,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            WRITE_EXTERNAL_PERMISSION);
                }




            }
        });

        hotTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cityTab.setTextColor(Color.parseColor("#5B5B5B"));
                placeTab.setTextColor(Color.parseColor("#5B5B5B"));
                hotTab.setTextColor(ContextCompat.getColor(mainActivity, R.color.colorPrimary));
                if(mainActivity.spectatorLocationItem != null && !mainActivity.spectatorLocationItem.toString().equals("")){
                    String cityID = mainActivity.spectatorLocationItem.toString();
                    updateChat(HOT_CHAT, cityID);
                }else{
                    String cityID = mainActivity.locationItem.toString();
                    updateChat(HOT_CHAT, cityID);
                }
            }
        });

        cityTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cityTab.setTextColor(ContextCompat.getColor(mainActivity, R.color.colorPrimary));
                placeTab.setTextColor(Color.parseColor("#5B5B5B"));
                hotTab.setTextColor(Color.parseColor("#5B5B5B"));

                if(mainActivity.viewChatCityID != null){
                    //User is coming from the explore tab and is viewing the chat in another city
                    String cityID = mainActivity.viewChatCityID;
                    updateChat(CITY_CHAT, cityID);
                }else {
                    //User is not trying to view another cities chat but they may still be spectating from the map
                    if (!isSpectating) {
                        //The user is not looking at another city on the map
                        //TODO: Make the call to pull the chat messages
                        if (mainActivity.locationItem != null) {
                            String cityID = mainActivity.locationItem.toString();
                            updateChat(CITY_CHAT, cityID);
                        }
                    } else {
                        //The user is spectating another city on the map
                        if (mainActivity.spectatorLocationItem != null) {
                            String cityID = mainActivity.spectatorLocationItem.toString();
                            updateChat(CITY_CHAT, cityID);
                        }
                    }
                }

            }
        });
        placeTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (placeTab.getText() != null && placeTab.getText().length() > 0) {
                    cityTab.setTextColor(Color.parseColor("#5B5B5B"));
                    hotTab.setTextColor(Color.parseColor("#5B5B5B"));
                    placeTab.setTextColor(ContextCompat.getColor(mainActivity, R.color.colorPrimary));
                    if(mainActivity.viewChatPlaceID != null){
                        //The user is trying to view a chat from the detail view of a place so load the chat for that place
                        updateChat(PLACE_CHAT,mainActivity.viewChatPlaceID);

                    }else {
                        //The user is not viewing a place chat from the detail view so use their current place
                        if (mainActivity.locationItem != null) {
                            String placeID = mainActivity.locationItem.placeKey;
                            //TODO: Make the call to pull the chat messages
                            updateChat(PLACE_CHAT, placeID);
                        }
                    }
                }
            }
        });


        anonymousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAnonymous) {
                    isAnonymous = false;
                    anonymousButton.setColorFilter(Color.parseColor("#5B5B5B"));
                } else {
                    isAnonymous = true;
                    anonymousButton.setColorFilter(getResources().getColor(R.color.colorPrimary));
                }
            }
        });


        return view;
    }

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

    public void resetUpdateChat() {

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (lastLocationItem != null) {
                if (mainActivity.spectatorLocationItem != null) {
                    if (mainActivity.spectatorLocationItem.toString().equals("")) {
                        isSpectating = false;
                    } else {
                        if (mainActivity.spectatorLocationItem.toString().equals(mainActivity.locationItem.toString())) {
                            isSpectating = false;
                        } else {
                            isSpectating = true;
                        }
                    }
                }


                lastLocationItem = mainActivity.locationItem;
                if (!isSpectating) {
                    String cityName = lastLocationItem.city;
                    //cityTab.setText(cityName);
                } else {
                    String cityName = mainActivity.spectatorLocationItem.city;
                    //cityTab.setText(cityName);
                }
//                String placeName = lastLocationItem.placeName;
//                placeTab.setText(placeName);

                TextView currentLocation = mainActivity.findViewById(R.id.city_name);
                currentLocation.setText("Chat");
                //isCityChat = true;
                cityTab.setTextColor(ContextCompat.getColor(mainActivity, R.color.colorPrimary));
                placeTab.setTextColor(Color.parseColor("#5B5B5B"));
                //TODO:Make call to set up the chat
                if(!isSpectating)
                updateChat(CITY_CHAT,lastLocationItem.toString());
                else updateChat(CITY_CHAT,mainActivity.spectatorLocationItem.toString());


            }
            //--------HANDLE THE CASES WHERE A PLACE OR CITY IS BEING SPECTATED
            if (mainActivity.viewChatPlaceID != null && mainActivity.viewChatPlaceName != null) {
                if (placeTab.getText() != null && placeTab.getText().length() > 0) {
                    cityTab.setTextColor(Color.parseColor("#5B5B5B"));
                    placeTab.setTextColor(ContextCompat.getColor(mainActivity, R.color.colorPrimary));
                    //placeTab.setText(mainActivity.viewChatPlaceName);
                    //TODO: Make call to set up place chat with the place the user is trying to view
                    updateChat(PLACE_CHAT,mainActivity.viewChatPlaceID);
                }
            }
            if(mainActivity.viewChatCityID != null && mainActivity.viewChatCityName != null){
                cityTab.setTextColor(ContextCompat.getColor(mainActivity, R.color.colorPrimary));
                placeTab.setTextColor(Color.parseColor("#5B5B5B"));
                //cityTab.setText(mainActivity.viewChatCityName);
                updateChat(CITY_CHAT,mainActivity.viewChatCityID);
            }


        } else {
            if (messageItems != null) {
                messageItems.clear();
            }
            if (listenerRegistration != null) {
                listenerRegistration.remove();
            }
        }
    }
    public void updateChat(int chatType, String chatID){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();




        //Check if they are at the city/place, if they are then make the chat bar visible
        switch (chatType){
            case CITY_CHAT:
                if(mainActivity.locationItem.toString().equals(chatID) || mainActivity.isAdmin){
                    //Make the chat bar visible and set send onClick
                    messageBar.setVisibility(View.VISIBLE);
                    chatOffBar.setVisibility(View.INVISIBLE);
                    setSendClick("cities",chatID);
                }else{
                    //Make the chat bar invisible with the not attending text bar
                    messageBar.setVisibility(View.GONE);
                    chatOffBar.setVisibility(View.VISIBLE);
                    chatOffBar.setText(CHAT_SPECTATOR);
                }
                break;
            case PLACE_CHAT:
                if(mainActivity.locationItem.placeKey.equals(chatID) || mainActivity.isAdmin){
                    //Make the chat bar visible and set send onClick
                    messageBar.setVisibility(View.VISIBLE);
                    chatOffBar.setVisibility(View.INVISIBLE);
                    setSendClick("places",chatID);
                }else{
                    //Make the chat bar invisible with the not attending text bar
                    messageBar.setVisibility(View.GONE);
                    chatOffBar.setVisibility(View.VISIBLE);
                    chatOffBar.setText(CHAT_SPECTATOR);
                }
                break;
            case HOT_CHAT:
                messageBar.setVisibility(View.INVISIBLE);
                chatOffBar.setVisibility(View.INVISIBLE);
                break;
        }
        //Check if the user is signed in, this way the chat bar will be invisible
        if(!mainActivity.isSignedIn) {
            //Not signed in, make chat bar invisible with not signed in text bar
            messageBar.setVisibility(View.INVISIBLE);
            chatOffBar.setVisibility(View.VISIBLE);
            chatOffBar.setText(NOT_SIGNED_IN);
        }

        //Set up the chat in view//
        //Make sure chat is clear
        if (messageItems != null) {
            messageItems.clear();
        }
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
        chatRecyclerAdapter.notifyDataSetChanged();
        //set the strings to be used in the reference
        String collection = "";
        String chatCollection = "";
        switch (chatType){
            case CITY_CHAT:
                collection = "cities";
                chatCollection = "messages";
                break;
            case PLACE_CHAT:
                collection = "places";
                chatCollection = "messages";
                break;
            case HOT_CHAT:
                collection = "cities";
                chatCollection = "hot";
                break;
        }



        CollectionReference collectionRef = db.collection(collection);
        DocumentReference collectionDoc = collectionRef.document(chatID);
        final CollectionReference messages = collectionDoc.collection(chatCollection);
        if(chatType != HOT_CHAT) {
            listenerRegistration = messages.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    QueryDocumentSnapshot snapshot = dc.getDocument();
                                    String message = snapshot.getString("Message");
                                    String color = snapshot.getString("Color");
                                    String posterID = snapshot.getString("PosterID");
                                    String timestamp = snapshot.getId();
                                    String image = snapshot.getString("Image");
                                    final String timestampID = timestamp;
                                    timestamp = convertTimestamp(timestamp);
                                    String username = snapshot.getString("ScreenName");

                                    final MessageItem messageItem = new MessageItem();
                                    messageItem.likerList = new ArrayList<>();
                                    messageItem.messageText = message;
                                    messageItem.textColor = color;
                                    messageItem.username = username;
                                    messageItem.timestamp = timestamp;
                                    messageItem.posterID = posterID;
                                    messageItem.imgPath = image;

                                    DocumentReference messageRef = messages.document(timestampID);
                                    messageItem.likesRef = messageRef.collection("likes");

                                    messageItems.add(messageItem);
                                    chatRecyclerAdapter.notifyDataSetChanged();

                                    boolean atBottom = chatRecycler.canScrollVertically(-1);
                                    if (atBottom)
                                        chatRecycler.scrollToPosition(chatRecyclerAdapter.getItemCount() - 1);
                                    break;
                            }
                        }
                    }
                }
            });
        }else{
            listenerRegistration = null;
            Query query = messages.limit(30).orderBy("Likes", Query.Direction.DESCENDING);
            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    for(DocumentSnapshot snapshot : queryDocumentSnapshots){

                        String message = snapshot.getString("Message");
                        String color = snapshot.getString("Color");
                        String posterID = snapshot.getString("PosterID");
                        String timestamp = snapshot.getId();
                        String image = snapshot.getString("Image");
                        final String timestampID = timestamp;
                        timestamp = convertTimestamp(timestamp);
                        String username = snapshot.getString("ScreenName");

                        final MessageItem messageItem = new MessageItem();
                        messageItem.likerList = new ArrayList<>();
                        messageItem.messageText = message;
                        messageItem.textColor = color;
                        messageItem.username = username;
                        messageItem.timestamp = timestamp;
                        messageItem.posterID = posterID;
                        messageItem.imgPath = image;

                        DocumentReference messageRef = messages.document(timestampID);
                        messageItem.likesRef = messageRef.collection("likes");

                        messageItems.add(messageItem);
                    }
                    chatRecyclerAdapter.notifyDataSetChanged();
                }
            });
        }

    }
    private void setSendClick(final String collection,final String chatID){
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference collectionRef = db.collection(collection);
                DocumentReference collectionDoc = collectionRef.document(chatID);
                final CollectionReference messages = collectionDoc.collection("messages");
                String message = messageEdit.getText().toString();
                if(!message.trim().isEmpty()) {
                    String color = mainActivity.userColor;
                    String screenname = mainActivity.username;
                    if (isAnonymous) screenname = "Anonymous";
                    String posterID = FirebaseAuth.getInstance().getUid();
                    String time;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = new Date(System.currentTimeMillis());
                    time = sdf.format(date);
                    time = time + " +0000";
                    HashMap<String, Object> messageMap = new HashMap<>();
                    messageMap.put("Message", message);
                    messageMap.put("Color", color);
                    messageMap.put("ScreenName", screenname);
                    messageMap.put("PosterID", posterID);

                    messages.document(time).set(messageMap);
                    messageEdit.setText("");
                }else{
                    //Blank message, pop this toast to the user
                    Toast.makeText(mainActivity,"Cannot send a blank message",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

//    private void updatePlaceName() {
//        try {
//            final String uid = FirebaseAuth.getInstance().getUid();
//            final FirebaseFirestore db = FirebaseFirestore.getInstance();
//            Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
//            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
//                @Override
//                public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
//                    PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
//                    Place mostLikelyPlace = null;
//                    double likelihood = 0.0;
//                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
//                        if (mostLikelyPlace == null) {
//                            mostLikelyPlace = placeLikelihood.getPlace();
//                            likelihood = placeLikelihood.getLikelihood();
//                        } else {
//                            if (placeLikelihood.getLikelihood() > likelihood) {
//                                mostLikelyPlace = placeLikelihood.getPlace();
//                                likelihood = placeLikelihood.getLikelihood();
//                            }
//                        }
////                                            Log.i(TAG, String.format("Place '%s' has likelihood: %g",
////                                                    placeLikelihood.getPlace().getName(),
////                                                    placeLikelihood.getLikelihood()));
//                    }
//
//                    String placeKey = mostLikelyPlace.getId();
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("Place", placeKey);
//                    db.collection("users").document(uid).update(map);
////                    locationItem.placeKey = placeKey;
////                    locationItem.placeName = mostLikelyPlace.getName().toString();
//                    mainActivity.locationItem.placeKey = placeKey;
//                    mainActivity.locationItem.placeName = mostLikelyPlace.getName().toString();
//                    placeTab.setText(mainActivity.locationItem.placeName);
//
//                    likelyPlaces.release();
//                }
//            });
//        } catch (SecurityException securityException) {
//
//        }
//    }

    private String convertTimestamp(String timestamp) {
        String converted = "";
        Date convertedDate;
        int NUM = 7;
        converted = timestamp.substring(0, timestamp.length() - 7);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            convertedDate = simpleDateFormat.parse(converted);
            Calendar cal = Calendar.getInstance();

            SimpleDateFormat convSDF = new SimpleDateFormat("MMM d, h:mm a ", Locale.US);
            TimeZone tz = cal.getTimeZone();
            convSDF.setTimeZone(tz);

            converted = convSDF.format(convertedDate);
        } catch (ParseException ignore) {

        }

        return converted;
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        //Create the dialog here
        final Dialog previewDialog = new Dialog(mainActivity, R.style.DialogTheme);
        previewDialog.setContentView(R.layout.image_preview_dialog);
        final ImageView imagePreview = previewDialog.findViewById(R.id.image_preview);
        ImageView cancelButton = previewDialog.findViewById(R.id.cancel_btn);
        ImageView sendButton = previewDialog.findViewById(R.id.send_btn);
        final ImageView anonymousButton = previewDialog.findViewById(R.id.anonymous_btn);
        final EditText messageInput = previewDialog.findViewById(R.id.message_input);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previewDialog.dismiss();
            }
        });

        anonymousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isAnonymousImg) {
                    isAnonymousImg = true;
                    anonymousButton.setColorFilter(getResources().getColor(R.color.colorPrimary));
                } else {
                    isAnonymousImg = false;
                    anonymousButton.setColorFilter(Color.parseColor("#5B5B5B"));
                }

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String time;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = new Date(System.currentTimeMillis());
                time = sdf.format(date);
                time = time + " +0000";

                String message = messageInput.getText().toString();
                String imgExt = "chat/";
                imgExt += FirebaseAuth.getInstance().getUid() + "/";
                imgExt += time + ".png";


                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference collectionRef = db.collection("cities");
                String chatID = mainActivity.locationItem.toString();
                DocumentReference collectionDoc = collectionRef.document(chatID);
                final CollectionReference messages = collectionDoc.collection("messages");
                //String message = messageEdit.getText().toString();

                    String color = mainActivity.userColor;
                    String screenname = mainActivity.username;
                    if (isAnonymous) screenname = "Anonymous";
                    String posterID = FirebaseAuth.getInstance().getUid();
//                    String time;
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
//                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//                    Date date = new Date(System.currentTimeMillis());
//                    time = sdf.format(date);
//                    time = time + " +0000";
                    HashMap<String, Object> messageMap = new HashMap<>();
                    if(message.trim().isEmpty()){
                        messageMap.put("Message", "");
                    }else{
                        messageMap.put("Message", message);
                    }

                    messageMap.put("Color", color);
                    messageMap.put("ScreenName", screenname);
                    messageMap.put("PosterID", posterID);
                    messageMap.put("Image", imgExt);

                    messages.document(time).set(messageMap);
                    //messageEdit.setText("");

                FirebaseStorage storage = FirebaseStorage.getInstance();

                StorageReference storageRef = storage.getReference(imgExt);

                try {
                    byte[] byteArray = getBytes(mainActivity, imgUri);
                    storageRef.putBytes(byteArray);
                }catch(IOException exception){

                }
                previewDialog.dismiss();


            }
        });
        switch(requestCode) {
            case 0: //From Camera
                if(resultCode == RESULT_OK){
                    //Pop up the image preview dialog and set the image
                    previewDialog.show();
                    Uri selectedImage = imageReturnedIntent.getData();
                    imagePreview.setImageURI(imgUri);
                }

                break;
            case 1: //TODO: From Gallery
                if(resultCode == RESULT_OK){
                    previewDialog.show();
                    imagePreview.setImageBitmap(getPicture(imageReturnedIntent.getData(),mainActivity));
                    Uri selectedImage = imageReturnedIntent.getData();
                    imagePreview.setImageURI(selectedImage);
                    imgUri = selectedImage;
                }
                break;
        }
    }
    public static Bitmap getPicture(Uri selectedImage, MainActivity mainActivity) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = mainActivity.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return BitmapFactory.decodeFile(picturePath);
    }
    public static byte[] getBytes(Context context, Uri uri) throws IOException {
        InputStream iStream = context.getContentResolver().openInputStream(uri);
        try {
            return getBytes(iStream);
        } finally {
            // close the stream
            try {
                iStream.close();
            } catch (IOException ignored) { /* do nothing */ }
        }
    }
    public static byte[] getBytes(InputStream inputStream) throws IOException {

        byte[] bytesResult = null;
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        try {
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            bytesResult = byteBuffer.toByteArray();
        } finally {
            // close the stream
            try{ byteBuffer.close(); } catch (IOException ignored){ /* do nothing */ }
        }
        return bytesResult;
    }
    //String imageFilePath;
    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                mainActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        //imageFilePath = image.getAbsolutePath();
        return image;
    }
}
