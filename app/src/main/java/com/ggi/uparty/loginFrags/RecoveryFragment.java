package com.ggi.uparty.loginFrags;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ggi.uparty.LoginActivity;
import com.ggi.uparty.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecoveryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecoveryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecoveryFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    LoginActivity loginActivity;
    EditText emailEdit;
    View resetButton;

    public RecoveryFragment() {
        // Required empty public constructor
    }


    public static RecoveryFragment newInstance() {
        RecoveryFragment fragment = new RecoveryFragment();
        Bundle args = new Bundle();
        //arguments
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //Something went wrong
        }
        emailEdit = null;
        resetButton = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recovery, container, false);
        //Bind views
        emailEdit = view.findViewById(R.id.email_edit);
        resetButton = view.findViewById(R.id.reset_button);

        //setOnClickListener
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEdit.getText().toString();
                if (email.equals("")) {
                    Toast.makeText(getContext(),"Please enter an email address", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Recovery email sent", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Please make sure you have entered a valid email address", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        if(context instanceof LoginActivity){
            loginActivity = (LoginActivity)context;
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
