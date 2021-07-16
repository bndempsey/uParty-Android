package com.ggi.uparty.viewpagerFrags;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ggi.uparty.MainActivity;
import com.ggi.uparty.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExploreFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExploreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExploreFragment extends Fragment {


    private OnFragmentInteractionListener mListener;
    MainActivity mainActivity;
    RecyclerView exploreRecycler;
    ExploreRecyclerAdapter exploreRecyclerAdapter;
    ArrayList<ExploreItem> exploreItems;

    private AdView exploreAdView;

    public ExploreFragment() {
        // Required empty public constructor
    }


    public static ExploreFragment newInstance() {
        ExploreFragment fragment = new ExploreFragment();
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
        exploreItems = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        exploreRecycler = view.findViewById(R.id.explore_recycler);
        exploreRecyclerAdapter = new ExploreRecyclerAdapter(mainActivity,exploreItems);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext() ,RecyclerView.VERTICAL, false);
        exploreRecycler.setLayoutManager(layoutManager);
        exploreRecycler.setAdapter(exploreRecyclerAdapter);

//        exploreAdView = view.findViewById(R.id.ad_explore);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        exploreAdView.loadAd(adRequest);
        //Do Firestore query
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference cities = db.collection("cities");

        Query ordered = cities.orderBy("Population",Query.Direction.DESCENDING).limit(50);
        ordered.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for(DocumentSnapshot doc : snapshots.getDocuments()){
                    String cityID = doc.getId();
                    Double population = doc.getDouble("Population");

                    String cityName = cityID.substring(0, cityID.indexOf("@"));
                    int nextIndex = cityID.indexOf("@") + 1;
                    String stateName = cityID.substring(nextIndex, cityID.indexOf("@", nextIndex));
                    String parsedCityState = cityName + ", " + stateName;
                    ExploreItem exploreItem = new ExploreItem();
                    exploreItem.cityString = parsedCityState;
                    exploreItem.population = population;
                    exploreItem.cityName = cityName;
                    exploreItem.cityID = cityID;
                    exploreItems.add(exploreItem);
                }
                exploreRecyclerAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            TextView currentLocation = mainActivity.findViewById(R.id.city_name);
            currentLocation.setText("Explore");
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
