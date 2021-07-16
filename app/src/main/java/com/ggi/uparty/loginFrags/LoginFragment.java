package com.ggi.uparty.loginFrags;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.ggi.uparty.LoginActivity;
import com.ggi.uparty.MainActivity;
import com.ggi.uparty.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    LoginActivity loginActivity;

    EditText emailEdit;
    EditText passwordEdit;
    View loginButton;
    View resetButton;


    public LoginFragment() {
        // Required empty public constructor
    }


    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        //arguments
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //parameters
        }
        emailEdit = null;
        passwordEdit = null;
        loginButton = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        //Bind views to the variables
        emailEdit = view.findViewById(R.id.email_edit);
        passwordEdit = view.findViewById(R.id.password_edit);
        loginButton = view.findViewById(R.id.login_button);
        resetButton = view.findViewById(R.id.reset);

        //onClickListener
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Go to account recovery
                Fragment fragment = null;
                Class fragmentClass = RecoveryFragment.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                FragmentManager fragmentManager = loginActivity.getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.fullscreen_frame, fragment,"RECOVER").commit();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEdit.getText().toString().trim();
                final String password = passwordEdit.getText().toString();
                final FirebaseAuth mAuth = FirebaseAuth.getInstance();
                if (!email.isEmpty() && !password.isEmpty()){
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null && user.isEmailVerified()) {
                                    SharedPreferences sharedPref = loginActivity.getPreferences(Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("upartyPassword", password);
                                    editor.putString("upartyEmail", user.getEmail());
                                    editor.commit();
                                    //Proceed to MainActivity w/logged in user
                                    Intent intent = new Intent(loginActivity.getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    //Go to email verification
                                    Fragment fragment = null;
                                    Class fragmentClass = ConfirmEmailFragment.class;
                                    try {
                                        fragment = (Fragment) fragmentClass.newInstance();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    FragmentManager fragmentManager = loginActivity.getSupportFragmentManager();
                                    fragmentManager.beginTransaction().replace(R.id.fullscreen_frame, fragment, "CONFIRMATION").commit();
                                }

                            } else {
                                //Error Message
                                Toast.makeText(loginActivity, "Authentication Failed", Toast.LENGTH_SHORT).show();
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
