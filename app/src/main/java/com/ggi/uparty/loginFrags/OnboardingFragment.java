package com.ggi.uparty.loginFrags;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ggi.uparty.LoginActivity;
import com.ggi.uparty.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnboardingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OnboardingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OnboardingFragment extends Fragment {


    private OnFragmentInteractionListener mListener;
    OnboardingActivity onboardingActivity;
    TextView titleView;
    TextView descriptionView;
    ImageView imageView;
    RecyclerView viewpagerDotsRecycler;
    View actionButtonView;

    public OnboardingFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static OnboardingFragment newInstance(Bundle args) {
        OnboardingFragment fragment = new OnboardingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        String imagePath = getArguments().getString("imagePath");
        //Use the image path to pull the image from storage and set imageView to the produced image
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_onboarding, container, false);
        titleView = view.findViewById(R.id.title);
        descriptionView = view.findViewById(R.id.description);
        imageView = view.findViewById(R.id.image);
        actionButtonView = view.findViewById(R.id.action_button);

        titleView.setText(getArguments().getString("title"));
        descriptionView.setText(getArguments().getString("description"));
        boolean isLast = getArguments().getBoolean("isLast");
        if(isLast)actionButtonView.setVisibility(View.VISIBLE);
        else actionButtonView.setVisibility(View.GONE);


        actionButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onboardingActivity.finish();
                Intent intent = new Intent(onboardingActivity.getApplicationContext(), LoginActivity.class);
                startActivity(intent);
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
        if(context instanceof  OnboardingActivity){
            onboardingActivity = (OnboardingActivity) context;
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
