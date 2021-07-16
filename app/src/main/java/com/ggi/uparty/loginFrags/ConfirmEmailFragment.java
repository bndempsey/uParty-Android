package com.ggi.uparty.loginFrags;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.ggi.uparty.LoginActivity;
import com.ggi.uparty.MainActivity;
import com.ggi.uparty.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.auth.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConfirmEmailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConfirmEmailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfirmEmailFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    LoginActivity loginActivity;

    View resendButton;

    public ConfirmEmailFragment() {
        // Required empty public constructor
    }


    public static ConfirmEmailFragment newInstance(String password) {
        ConfirmEmailFragment fragment = new ConfirmEmailFragment();
        Bundle args = new Bundle();
        //arguments
        args.putString("password",password);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
           //Something went wrong
        }
        resendButton = null;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirm_email, container, false);
        //Bind views
        resendButton = view.findViewById(R.id.resend_button);

        //setOnClickListener
        resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    //Send verification email
                    user.sendEmailVerification();
                }
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
    public void onResume() {
        super.onResume();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        SharedPreferences sharedPref = loginActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("upartyPassword", getArguments().getString("password"));
        editor.putString("upartyEmail", user.getEmail());
        editor.commit();

        if(user != null && user.isEmailVerified()){
            Intent intent = new Intent(loginActivity.getApplicationContext(),MainActivity.class);
            startActivity(intent);
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
        if(context instanceof LoginActivity){
            loginActivity = (LoginActivity) context;
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
