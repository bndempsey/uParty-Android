package com.ggi.uparty.loginFrags;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.ggi.uparty.LoginActivity;
import com.ggi.uparty.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewAccountFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewAccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewAccountFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    LoginActivity loginActivity;
    EditText usernameEdit;
    EditText emailEdit;
    EditText passwordEdit;
    DatePicker datePicker;
    ImageView female;
    ImageView male;
    View signupButton;
    View termsButton;
    boolean isMale;

    public NewAccountFragment() {
        // Required empty public constructor
    }


    public static NewAccountFragment newInstance() {
        NewAccountFragment fragment = new NewAccountFragment();
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
        isMale = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_account, container, false);
        //Bind views
        usernameEdit = view.findViewById(R.id.username_edit);
        emailEdit = view.findViewById(R.id.email_edit);
        passwordEdit = view.findViewById(R.id.password_edit);
        female = view.findViewById(R.id.female);
        male = view.findViewById(R.id.male);
        signupButton = view.findViewById(R.id.signup_button);
        datePicker = view.findViewById(R.id.date_picker);
        termsButton = view.findViewById(R.id.terms_click);
        male.setColorFilter(getResources().getColor(R.color.colorPrimary));




        //setOncClickListeners
        termsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.upartyapp.com/terms-of-service"));
                startActivity(browserIntent);
            }
        });
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMale = false;
                female.setColorFilter(getResources().getColor(R.color.colorPrimary));
                male.setColorFilter(Color.parseColor("#5B5B5B"));

            }
        });
        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMale = true;
                female.setColorFilter(Color.parseColor("#5B5B5B"));
                male.setColorFilter(getResources().getColor(R.color.colorPrimary));
            }
        });
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FirebaseAuth mAuth = FirebaseAuth.getInstance();
                String email = emailEdit.getText().toString().trim();
                final String password = passwordEdit.getText().toString().trim();
                final String username = usernameEdit.getText().toString().trim();
                int birthYear = datePicker.getYear();
                int birthMonth = datePicker.getMonth() + 1;
                int birthDate = datePicker.getDayOfMonth();
                final int age = getAge(birthYear, birthMonth, birthDate);

                String monthString = Integer.toString(birthMonth);
                String dateString = Integer.toString(birthDate);
                String yearString = Integer.toString(birthYear);
                if(birthMonth < 10) monthString = "0" + monthString;
                if(birthDate < 10) dateString = "0" + dateString;

                final String birthdayString = monthString + "/" + dateString + "/" + yearString;
                String gender;
                if(isMale){
                    gender = "male";
                }else{
                    gender = "female";
                }
                final String genderFinal = gender;

                boolean validSignup = false;

                if(email.equals("") || password.equals("") || username.equals("")){
                    Toast.makeText(loginActivity, "Please enter information in all fields", Toast.LENGTH_SHORT).show();
                }else{
                    if (age >= 18) {
                        validSignup = true;
                    }else{
                        Toast.makeText(loginActivity, "Must be 18 to create an account", Toast.LENGTH_SHORT).show();
                    }
                }


                if(validSignup) {

                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(loginActivity, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                //Update display name
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build();
                                user.updateProfile(profileUpdates);

                                String color = "";
                                Random rand = new Random();
                                int randomNum = rand.nextInt((9) + 1) + 1;
                                switch (randomNum){
                                    case 1:
                                        color = "Red";
                                        break;
                                    case 2:
                                        color = "Orange";
                                        break;
                                    case 3:
                                        color = "Yellow";
                                        break;
                                    case 4:
                                        color = "Green";
                                        break;
                                    case 5:
                                        color = "Cyan";
                                        break;
                                    case 6:
                                        color = "Teal";
                                        break;
                                    case 7:
                                        color = "Blue";
                                        break;
                                    case 8:
                                        color = "Navy";
                                        break;
                                    case 9:
                                        color = "Purple";
                                        break;
                                    case 10:
                                        color = "Pink";
                                        break;
                                        default:
                                            color = "Orange";
                                            break;
                                }

                                FirebaseFirestore db = FirebaseFirestore.getInstance();

                                //Add birthday and gender to user node
                                Map<String,String> map = new HashMap<>();
                                map.put("Gender",genderFinal);
                                map.put("Birthday",birthdayString);
                                map.put("Color",color);
                                db.collection("users").document(user.getUid()).set(map);

                                //Send verification email
                                user.sendEmailVerification();

                                //Go to email verification
                                Fragment fragment = null;
                                try {
                                    fragment = ConfirmEmailFragment.newInstance(password);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                FragmentManager fragmentManager = loginActivity.getSupportFragmentManager();
                                fragmentManager.beginTransaction().replace(R.id.fullscreen_frame, fragment, "CONFIRMATION").commit();
                            } else {
                                //Task not successful, error message
                                Toast.makeText(loginActivity, "Authentication failed: Check entered email address", Toast.LENGTH_SHORT).show();
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
        if (context instanceof LoginActivity) {
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

    private int getAge(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }
}
