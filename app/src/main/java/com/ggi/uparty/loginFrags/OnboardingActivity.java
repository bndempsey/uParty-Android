package com.ggi.uparty.loginFrags;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.ggi.uparty.OnboardingPageAdapter;
import com.ggi.uparty.PageAdapter;
import com.ggi.uparty.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;



import java.util.ArrayList;

public class OnboardingActivity extends AppCompatActivity implements OnboardingFragment.OnFragmentInteractionListener {

    ArrayList<OnboardingScreenInfo> onboardingList;
    ArrayList<OnboardingFragment> onboardingFragments;

    long highestNewVersionSeen;

    ViewPager pager;
    OnboardingPageAdapter pageAdapter;
    TabLayout tabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        onboardingList = new ArrayList<>();
        highestNewVersionSeen = (long)0.0;
        pager = findViewById(R.id.onboarding_viewpager);
        tabLayout = findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(pager, true);
        onboardingFragments = new ArrayList<>();
        onboardingList = new ArrayList<>();


        //Check last version of screens seen
        long lastVersionSeen = (long) -1.0;
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        lastVersionSeen = sharedPreferences.getLong("lastVersionSeen", lastVersionSeen);
        final long lastVersionFinal = lastVersionSeen;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference introScreensCol = db.collection("intro-screens");
        introScreensCol.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot snap : queryDocumentSnapshots) {

                    Double version = snap.getDouble("Version");
                    if (version == null || version > lastVersionFinal) {
                        //pull data of this screen and add it to the arraylist of screens to be made
                        if(version != null && version > highestNewVersionSeen) highestNewVersionSeen = version.longValue();
                        String title = snap.getString("Title");
                        String description = snap.getString("Description");
                        String imagePath = snap.getString("Image");

                        OnboardingScreenInfo newScreen = new OnboardingScreenInfo();
                        newScreen.title = title;
                        newScreen.description = description;
                        newScreen.imagePath = imagePath;

                        onboardingList.add(newScreen);
                    }


                }
                //Update the highest version seen in SharedPreferences
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putLong("lastVersionSeen", highestNewVersionSeen);
                editor.commit();
                //Generate a Fragment for each of the new screens
                for(int i = 0; i < onboardingList.size(); i++){
                    OnboardingScreenInfo newScreen = onboardingList.get(i);
                    Bundle bundle = new Bundle();
                    bundle.putString("title",newScreen.title);
                    bundle.putString("description", newScreen.description);
                    bundle.putString("imagePath", newScreen.imagePath);
                    if(i == onboardingList.size() - 1){
                        bundle.putBoolean("isLast",true);
                    }else{
                        bundle.putBoolean("isLast",false);
                    }

                    OnboardingFragment newFragment = OnboardingFragment.newInstance(bundle);
                    newFragment.setArguments(bundle);
                    onboardingFragments.add(newFragment);
                }
                pageAdapter = new OnboardingPageAdapter(getSupportFragmentManager(),onboardingFragments);
                pager.setAdapter(pageAdapter);
            }
        });


        //Check to see what new screens need to be added
    }

    @Override
    public void onBackPressed() {
        //Do nothing
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
