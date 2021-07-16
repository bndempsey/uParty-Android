package com.ggi.uparty.viewpagerFrags;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v4.app.Fragment;
//import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.ggi.uparty.LoginActivity;
import com.ggi.uparty.MainActivity;
import com.ggi.uparty.R;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    MainActivity mainActivity;
    View usernameButton;
    TextView currentUsername;
    ImageView settingsButton;
    TextView colorText;
    View rateButton;
    View resetButton;
    View logoutButton;
    View colorButton;
    String username;
    View submitButton;
    private AdView settingsAdView;

    String color;

    public SettingsFragment() {
        // Required empty public constructor
    }


    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        usernameButton = view.findViewById(R.id.username_button);
        currentUsername = view.findViewById(R.id.current_username);
        colorButton = view.findViewById(R.id.color_button);
        //settingsButton = view.findViewById(R.id.settings_img);
        rateButton = view.findViewById(R.id.rate_button);
        resetButton = view.findViewById(R.id.reset_button);
        logoutButton = view.findViewById(R.id.logout_button);
        colorText = view.findViewById(R.id.color);
        ((ImageView)view.findViewById(R.id.username_arrow)).setColorFilter(getResources().getColor(R.color.colorPrimary));
        ((ImageView)view.findViewById(R.id.color_arrow)).setColorFilter(getResources().getColor(R.color.colorPrimary));
        ((ImageView)view.findViewById(R.id.rate_arrow)).setColorFilter(getResources().getColor(R.color.colorPrimary));
        ((ImageView)view.findViewById(R.id.reset_arrow)).setColorFilter(getResources().getColor(R.color.colorPrimary));
        ((ImageView)view.findViewById(R.id.logout_arrow)).setColorFilter(getResources().getColor(R.color.red));
        //submitButton = view.findViewById(R.id.submit);
        //settingsAdView = view.findViewById(R.id.ad_settings);
        //AdRequest adRequest = new AdRequest.Builder().build();
        //settingsAdView.loadAd(adRequest);
        if(mainActivity.isSignedIn) {
            setColor();

            usernameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog usernameChanger = new Dialog(mainActivity);
                    usernameChanger.setContentView(R.layout.username_change_dialog);
                    EditText usernameEdit = usernameChanger.findViewById(R.id.username_edit);
                    usernameEdit.setText(username);
                    usernameChanger.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                            FirebaseUser user = mAuth.getCurrentUser();
                            EditText usernameEdit = usernameChanger.findViewById(R.id.username_edit);
                            //Update display name
                            username = usernameEdit.getText().toString();
                            if(!username.trim().isEmpty()) {
                                mainActivity.username = username;
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build();
                                user.updateProfile(profileUpdates);
                                Toast.makeText(mainActivity, "Username changed to " + username, Toast.LENGTH_SHORT).show();
                                currentUsername.setText(username);
                                usernameChanger.dismiss();
                            }else{
                                Toast.makeText(mainActivity,"Username cannot be blank",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    usernameChanger.show();
                }
            });

            colorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog colorPicker = new Dialog(mainActivity);

                    colorPicker.setContentView(R.layout.colorpicker_dialog);
                    colorPicker.findViewById(R.id.red).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { //
                            color = "Red";
                            changeColor();
                            colorPicker.dismiss();
                        }
                    });
                    colorPicker.findViewById(R.id.orange).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { //
                            color = "Orange";
                            changeColor();
                            colorPicker.dismiss();
                        }
                    });
                    colorPicker.findViewById(R.id.green).setOnClickListener(new View.OnClickListener() { //
                        @Override
                        public void onClick(View v) {
                            color = "Green";
                            changeColor();
                            colorPicker.dismiss();
                        }
                    });
                    colorPicker.findViewById(R.id.yellow).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { //
                            color = "Yellow";
                            changeColor();
                            colorPicker.dismiss();
                        }
                    });

                    colorPicker.findViewById(R.id.teal).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { //
                            color = "Teal";
                            changeColor();
                            colorPicker.dismiss();
                        }
                    });

                    colorPicker.findViewById(R.id.blue).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { //
                            color = "Blue";
                            changeColor();
                            colorPicker.dismiss();
                        }
                    });
                    colorPicker.findViewById(R.id.purple).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { //
                            color = "Purple";
                            changeColor();
                            colorPicker.dismiss();
                        }
                    });
                    colorPicker.findViewById(R.id.pink).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) { //
                            color = "Pink";
                            changeColor();
                            colorPicker.dismiss();
                        }
                    });

                    colorPicker.show();
                }
            });


            resetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getContext(),"Password reset email sent",Toast.LENGTH_SHORT).show();
                            }else{

                            }

                        }
                    });
                }
            });

        }else{
            usernameButton.setVisibility(View.GONE);
            //settingsButton.setVisibility(View.GONE);
            colorButton.setVisibility(View.GONE);
            resetButton.setVisibility(View.GONE);
            colorText.setVisibility(View.GONE);
            //submitButton.setVisibility(View.GONE);
//            view.findViewById(R.id.username_static).setVisibility(View.GONE);
//            view.findViewById(R.id.color_static).setVisibility(View.GONE);
//            view.findViewById(R.id.top_line).setVisibility(View.GONE);
//            view.findViewById(R.id.bottom_line).setVisibility(View.GONE);
//            view.findViewById(R.id.left_line).setVisibility(View.GONE);
//            view.findViewById(R.id.right_line).setVisibility(View.GONE);
        }
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mainActivity.isSignedIn) {
                    FirebaseAuth.getInstance().signOut();
                }
                Bundle bundle = new Bundle();
                bundle.putString("logout","logout");
                Intent intent = new Intent(mainActivity,LoginActivity.class);
                intent.putExtras(bundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        rateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=" + mainActivity.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + mainActivity.getPackageName())));
                }
            }
        });



        return view;
    }

    private void setColor() {
        username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        currentUsername.setText(username);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");
        String uid = FirebaseAuth.getInstance().getUid();
        DocumentReference user = users.document(uid);
        user.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                color = documentSnapshot.getString("Color");

                colorText.setText(color);
                switch (color){
                    case "Red":
                        colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.red));
                        break;
                    case "Orange":
                        colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.orange));
                        break;
                    case "Yellow":
                        colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.yellow));
                        break;
                    case "Green":
                        colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.green));
                        break;
                    case "Cyan":
                        colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.cyan));
                        break;
                    case "Teal":
                        colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.teal));
                        break;
                    case "Blue":
                        colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.blue));
                        break;
                    case "Navy":
                        colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.navy));
                        break;
                    case "Purple":
                        colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.purple));
                        break;
                    case "Pink":
                        colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.pink));
                        break;
                }
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
        if(context instanceof MainActivity){
            mainActivity = (MainActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

   private void changeColor(){
        colorText.setText(color);
        mainActivity.userColor = color;
       HashMap<String,Object> map = new HashMap<>();
       map.put("Color", color);
       FirebaseFirestore db = FirebaseFirestore.getInstance();
       String uID = FirebaseAuth.getInstance().getUid();
       CollectionReference users = db.collection("users");
       DocumentReference user = users.document(uID);
       user.update(map);
       switch (color){
           case "Red":
               colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.red));
               break;
           case "Orange":
               colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.orange));
               break;
           case "Yellow":
               colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.yellow));
               break;
           case "Green":
               colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.green));
               break;
           case "Cyan":
               colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.cyan));
               break;
           case "Teal":
               colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.teal));
               break;
           case "Blue":
               colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.blue));
               break;
           case "Navy":
               colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.navy));
               break;
           case "Purple":
               colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.purple));
               break;
           case "Pink":
               colorText.setTextColor(ContextCompat.getColor(mainActivity,R.color.pink));
               break;
       }
   }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            TextView currentLocation = mainActivity.findViewById(R.id.city_name);
            currentLocation.setText("Settings");
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
