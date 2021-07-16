package com.ggi.uparty.create;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ggi.uparty.MainActivity;
import com.ggi.uparty.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateEventTwoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateEventTwoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateEventTwoFragment extends Fragment {


    private OnFragmentInteractionListener mListener;
    MainActivity mainActivity;
    ImageView confirmButton;
    ImageView cancelButton;
    EditText remarkEdit;
    String remark;

    public CreateEventTwoFragment() {
        // Required empty public constructor
    }


    public static CreateEventTwoFragment newInstance(Bundle bundle) {
        CreateEventTwoFragment fragment = new CreateEventTwoFragment();

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_event_two, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        confirmButton = view.findViewById(R.id.create_confirm);
        cancelButton = view.findViewById(R.id.create_cancel);
        remarkEdit = view.findViewById(R.id.event_create_entry);
        confirmButton.setColorFilter(getResources().getColor(R.color.colorPrimary));
        cancelButton.setColorFilter(getResources().getColor(R.color.colorPrimary));

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eventID = getArguments().getString("eventID");
                String cityID = getArguments().getString("cityID");
                String time;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = new Date(System.currentTimeMillis());
                time = sdf.format(date);
                time = time + " +0000";
                remark = remarkEdit.getText().toString();
                if(remark.trim().equals("")){

                }else {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    String uID = FirebaseAuth.getInstance().getUid();
                    CollectionReference cities = db.collection("cities");
                    DocumentReference city = cities.document(cityID);
                    CollectionReference events = city.collection("events");
                    DocumentReference event = events.document(eventID);
                    CollectionReference remarks = event.collection("remarks");
                    DocumentReference remarkRef = remarks.document(uID);
                    HashMap<String,Object> remarkMap = new HashMap<>();
                    remarkMap.put("Comment",remark);
                    remarkMap.put("Date",time);
                    remarkRef.set(remarkMap);
                    mainActivity.onBackPressed();
                }

            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.onBackPressed();
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
}
