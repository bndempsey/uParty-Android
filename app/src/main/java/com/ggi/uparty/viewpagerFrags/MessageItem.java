package com.ggi.uparty.viewpagerFrags;

import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class MessageItem {
    public String username;
    public String messageText;
    public String timestamp;
    public String textColor;
    public String posterID;
    //public boolean isLiked;
    public int likeCount;
    public CollectionReference likesRef;
    public ListenerRegistration listenerRegistration;
    public ArrayList<String> likerList;
    public String imgPath;
}
