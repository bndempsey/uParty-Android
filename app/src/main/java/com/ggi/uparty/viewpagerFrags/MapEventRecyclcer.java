package com.ggi.uparty.viewpagerFrags;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v4.app.FragmentManager;
//import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ggi.uparty.MainActivity;
import com.ggi.uparty.R;
import com.ggi.uparty.other.EventDetailView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.util.ArrayList;

public class MapEventRecyclcer extends RecyclerView.Adapter<MapEventRecyclcer.ViewHolder> {

    MainActivity mainActivity;
    ArrayList<MapEventItem> events;
    RecyclerView recyclerView;


    public MapEventRecyclcer(MainActivity mainActivity, ArrayList<MapEventItem> mapEventItems,RecyclerView recyclerView) {
        events = new ArrayList<>();
        events.addAll(mapEventItems);
        this.mainActivity = mainActivity;
        this.recyclerView = recyclerView;

//        MapEventItem blankItem = new MapEventItem();
//        blankItem.isBlank = true;
//        events.add(0,blankItem);
//        events.add(blankItem);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.map_event_item, viewGroup, false);
        return new MapEventRecyclcer.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final MapEventItem mapEventItem = events.get(i);
        viewHolder.item.setClipToOutline(true);
//        if (mapEventItem.isBlank) {
//            viewHolder.item.setVisibility(View.INVISIBLE);
//            viewHolder.item.setOnClickListener(null);
//            if(i == events.size() - 1){
//                //Last event loaded, try to force a scroll
//                recyclerView.smoothScrollToPosition(i-1);
//            }
//
//        } else {
//
//        }
        viewHolder.item.setVisibility(View.VISIBLE);

        viewHolder.item.setPadding(0,0,0,0);
        String name = mapEventItem.eventName;
        if (mapEventItem.eventName.length() > 30) {
            name = name.substring(0, 27);
            name = name + "...";
        }
        viewHolder.eventName.setText(name);
        String comingText = mapEventItem.populationAll + " people are going";
        String hereText = mapEventItem.populationThere + " people are here";
        viewHolder.comingAttendees.setText(comingText);
        viewHolder.currentAttendees.setText(hereText);
        if (mapEventItem.populationThere == 0) {
            viewHolder.currentAttendees.setVisibility(View.GONE);
        } else {
            viewHolder.currentAttendees.setVisibility(View.VISIBLE);
        }
        viewHolder.type.setText(mapEventItem.type);
//        if (viewHolder.eventImage != null) {
//            viewHolder.eventImage.setImageBitmap(mapEventItem.image);
//        }
        final ImageView img = viewHolder.eventImage;
        if(mapEventItem.imagePath != null && !mapEventItem.imagePath.equals("") && !mapEventItem.imagePath.startsWith("gs://")){
            String path = mapEventItem.imagePath;
            new DownloadImageTask(img).execute(path);

        }else{
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            String imgPath = "events/" + mapEventItem.eventID + ".png";
            StorageReference pathReference = storageRef.child(imgPath);
            final long MAX_SIZE = 2048 * 2048;

            pathReference.getBytes(MAX_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    if(bytes != null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        img.setImageBitmap(bmp);
                        mapEventItem.image = bytes;
                    }
                }
            });
        }


        viewHolder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("name", mapEventItem.eventName);
                bundle.putString("eventID",mapEventItem.eventID);
                bundle.putDouble("ageAll",mapEventItem.ageAll);
                bundle.putDouble("ageThere", mapEventItem.ageThere);
                bundle.putInt("femaleAll", mapEventItem.femaleAll);
                bundle.putInt("femaleThere", mapEventItem.femaleThere);
                bundle.putInt("maleAll", mapEventItem.maleAll);
                bundle.putInt("maleThere", mapEventItem.maleThere);
                bundle.putString("website", mapEventItem.website);
                bundle.putInt("populationAll", mapEventItem.populationAll);
                bundle.putInt("populationThere", mapEventItem.populationThere);
                bundle.putByteArray("image", mapEventItem.image);
                bundle.putDouble("lat",mapEventItem.lat);
                bundle.putDouble("lon", mapEventItem.lon);
                bundle.putString("cityID", mapEventItem.cityID);


                EventDetailView eventDetailView = null;
                try {
                    eventDetailView = EventDetailView.class.newInstance();
                    eventDetailView.setArguments(bundle);
                } catch (Exception e) {

                }
                if (eventDetailView != null) {
                    FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();
                    mainActivity.findViewById(R.id.fullscreen_frame).setVisibility(View.VISIBLE);
                    fragmentManager.beginTransaction().replace(R.id.fullscreen_frame, eventDetailView, "EVENT_DETAIL").commit();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView eventName;
        public TextView type;
        public TextView currentAttendees;
        public TextView comingAttendees;
        public ImageView eventImage;
        public View item;
        View leftSpacing;
        View rightSpacing;


        public ViewHolder(View view) {
            super(view);
            item = view.findViewById(R.id.container);
            type = view.findViewById(R.id.type);
            eventName = view.findViewById(R.id.event_title);
            currentAttendees = view.findViewById(R.id.num_here);
            comingAttendees = view.findViewById(R.id.num_going);
            eventImage = view.findViewById(R.id.event_img);
            leftSpacing = view.findViewById(R.id.left_line);
            rightSpacing = view.findViewById(R.id.right_line);

        }
    }
}
