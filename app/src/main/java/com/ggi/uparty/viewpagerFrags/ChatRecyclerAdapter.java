package com.ggi.uparty.viewpagerFrags;

import android.app.Dialog;
//import android.support.annotation.NonNull;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.widget.RecyclerView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ggi.uparty.MainActivity;
import com.ggi.uparty.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.Nullable;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.ViewHolder> {

    private MainActivity mainActivity;
    private ArrayList<MessageItem> messageItems;

    public ChatRecyclerAdapter(MainActivity mainActivity, ArrayList<MessageItem> messageItems){
        this.mainActivity = mainActivity;
        this.messageItems = messageItems;
    }

    @NonNull
    @Override
    public ChatRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item,parent,false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ChatRecyclerAdapter.ViewHolder holder, int position) {

        MessageItem message = messageItems.get(position);
        holder.timestamp.setText(message.timestamp);
        holder.username.setText(message.username);
        holder.messageText.setText(message.messageText);
        final int ii = position;
        final String color = message.textColor;
        switch (color){
            case "Red":
                holder.username.setTextColor(ContextCompat.getColor(mainActivity,R.color.red));
                break;
            case "Orange":
                holder.username.setTextColor(ContextCompat.getColor(mainActivity,R.color.orange));
                break;
            case "Yellow":
                holder.username.setTextColor(ContextCompat.getColor(mainActivity,R.color.yellow));
                break;
            case "Green":
                holder.username.setTextColor(ContextCompat.getColor(mainActivity,R.color.green));
                break;
            case "Cyan":
                holder.username.setTextColor(ContextCompat.getColor(mainActivity,R.color.cyan));
                break;
            case "Teal":
                holder.username.setTextColor(ContextCompat.getColor(mainActivity,R.color.teal));
                break;
            case "Blue":
                holder.username.setTextColor(ContextCompat.getColor(mainActivity,R.color.blue));
                break;
            case "Navy":
                holder.username.setTextColor(ContextCompat.getColor(mainActivity,R.color.navy));
                break;
            case "Purple":
                holder.username.setTextColor(ContextCompat.getColor(mainActivity,R.color.purple));
                break;
            case "Pink":
                holder.username.setTextColor(ContextCompat.getColor(mainActivity,R.color.pink));
                break;
        }
        holder.commentItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final Dialog reportDialog = new Dialog(mainActivity);
                reportDialog.setContentView(R.layout.report_chat_dialog);
                ImageView yes = reportDialog.findViewById(R.id.yes);
                ImageView no = reportDialog.findViewById(R.id.no);
                //TextView reportedComment = reportDialog.findViewById(R.id.reported_comment);
                yes.setColorFilter(mainActivity.getResources().getColor(R.color.colorPrimary));
                no.setColorFilter(mainActivity.getResources().getColor(R.color.colorPrimary));
                //reportedComment.setText(messageItems.get(ii).messageText);
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        reportDialog.dismiss();
                    }
                });
                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        CollectionReference reported = db.collection("reportedMessages");
                        HashMap<String,Object> map = new HashMap<>();
                        map.put("Message",messageItems.get(ii).messageText);
                        map.put("ScreenName",messageItems.get(ii).username);
                        map.put("TimeStamp",messageItems.get(ii).timestamp);
                        map.put("PosterID",messageItems.get(ii).posterID);
                        reported.add(map);
                        reportDialog.dismiss();
                    }
                });
                reportDialog.show();
                return false;
            }
        });
        if(messageItems.get(ii).listenerRegistration == null) {
            //check if the message has been liked
            messageItems.get(ii).listenerRegistration = messageItems.get(ii).likesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    if(ii < messageItems.size()) {
                                        //Add one to the count and update the counter
                                        messageItems.get(ii).likeCount++;
                                        String count = messageItems.get(ii).likeCount + "";
                                        holder.heartCounter.setText(count);
                                        holder.heartCounter.setVisibility(View.VISIBLE);
                                        String key = dc.getDocument().getId();
                                        String uID = FirebaseAuth.getInstance().getUid();
                                        if (key.equals(uID)) {
                                            holder.heartButton.setImageResource(R.drawable.heart_orange);
                                        } else {
                                            if (!messageItems.get(ii).likerList.contains(uID)) {
                                                holder.heartButton.setImageResource(R.drawable.heart_grey);
                                            }
                                        }
                                    }

                                    break;
                                case REMOVED:
                                    if(ii < messageItems.size()) {
                                        messageItems.get(ii).likeCount--;
                                        String count2 = messageItems.get(ii).likeCount + "";
                                        holder.heartCounter.setText(count2);
                                        String key2 = dc.getDocument().getId();
                                        String uID2 = FirebaseAuth.getInstance().getUid();
                                        if (key2.equals(uID2)) {
                                            holder.heartButton.setImageResource(R.drawable.heart_grey);
                                            //messageItems.get(ii).likerList.remove(uID2);
                                        }
                                        if (messageItems.get(ii).likeCount == 0) {
                                            //Set heart to outline
                                            holder.heartCounter.setVisibility(View.INVISIBLE);
                                            holder.heartButton.setImageResource(R.drawable.heart_outline);
                                        }
                                    }

                                    break;
                            }
                        }
                    }
                }
            });
        }

        if(messageItems.get(ii).likeCount > 0){
            holder.heartCounter.setVisibility(View.VISIBLE);
            String county = messageItems.get(ii).likeCount+"";
            holder.heartCounter.setText(county);
        }else {
            holder.heartCounter.setVisibility(View.INVISIBLE);
        }
        holder.heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uID = FirebaseAuth.getInstance().getUid();
                boolean isLiked = false;
                if(messageItems.get(ii).likerList.contains(uID))isLiked = true;
                else isLiked = false;

                if(!isLiked) {
                    //The message liked list does not contain the users uID, therefor this is a new like
                    //Add the uID to the list, add the message to the database
                    if (uID != null) {
                        DocumentReference likeDoc = messageItems.get(ii).likesRef.document(uID);
                        String time;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        Date date = new Date(System.currentTimeMillis());
                        time = sdf.format(date);
                        time = time + " +0000";
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("Time", time);
                        messageItems.get(ii).likerList.add(uID);
                        likeDoc.set(map);
                    }
                }else {
                    //The messages liked list contains the uID, therefor this is an unlike
                    DocumentReference likeDoc = messageItems.get(ii).likesRef.document(uID);
                    messageItems.get(ii).likerList.remove(uID);
                    likeDoc.delete();
                }

            }
        });

        String imgPath = messageItems.get(ii).imgPath;
        if(imgPath != null && !imgPath.equals("")) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference pathReference = storageRef.child(imgPath);
            final long MAX_SIZE = 2048 * 2048;
            final ImageView img = holder.chatImg;

            pathReference.getBytes(MAX_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    if (bytes != null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                        float imageOriginalWidthHeightRatio = (float) bmp.getWidth() / (float) bmp.getHeight();

                        int imageToShowWidth = img.getWidth();
                        int imageToShowHeight = (int) (imageToShowWidth / imageOriginalWidthHeightRatio);

                        Bitmap imageToShow = Bitmap.createScaledBitmap(bmp, imageToShowWidth, imageToShowHeight, true);




                        img.setImageBitmap(imageToShow);
                        img.setVisibility(View.VISIBLE);
                        //mapEventItem.image = bytes;
                    }
                }
            });
        }



    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return messageItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView timestamp;
        public TextView username;
        public TextView messageText;
        public View commentItem;
        public ImageView heartButton;
        public TextView heartCounter;
        public ImageView chatImg;

        public ViewHolder(View view) {
            super(view);
            timestamp = view.findViewById(R.id.timestamp);
            username = view.findViewById(R.id.username);
            messageText = view.findViewById(R.id.message_text);
            heartButton = view.findViewById(R.id.heart);
            heartCounter = view.findViewById(R.id.heart_count);
            chatImg = view.findViewById(R.id.chat_img);
            commentItem = view;
        }
    }
}



