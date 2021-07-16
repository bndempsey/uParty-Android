package com.ggi.uparty.other;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.ggi.uparty.MainActivity;
import com.ggi.uparty.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OldEventDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OldEventDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OldEventDetailFragment extends Fragment {


    private MainActivity mainActivity;
    private OnFragmentInteractionListener mListener;
    TextView typeText;
    TextView topicText;
    TextView populationCount;
    TextView avgAge;
    TextView maleCount;
    TextView femaleCount;
    View viewChatBtn;
    RecyclerView remarksRecycler;

    public OldEventDetailFragment() {
        // Required empty public constructor
    }


    public static OldEventDetailFragment newInstance() {
        OldEventDetailFragment fragment = new OldEventDetailFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //Set params
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.old_event_detail_view, container, false);
        topicText = view.findViewById(R.id.topic);
        typeText = view.findViewById(R.id.type);
        populationCount = view.findViewById(R.id.population_count);
        avgAge = view.findViewById(R.id.avg_age);
        maleCount = view.findViewById(R.id.male_count);
        femaleCount = view.findViewById(R.id.female_count);
        viewChatBtn = view.findViewById(R.id.view_chat_btn);
        remarksRecycler = view.findViewById(R.id.remarks_recycler);

        viewChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        return view;
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
