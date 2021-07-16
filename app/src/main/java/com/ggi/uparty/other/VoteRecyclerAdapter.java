package com.ggi.uparty.other;

//import android.support.annotation.NonNull;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
        import android.view.View;
import android.view.ViewGroup;
        import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

        import com.ggi.uparty.R;
import com.ggi.uparty.create.CreateEventFragment;

        import java.util.ArrayList;

public class VoteRecyclerAdapter extends RecyclerView.Adapter<VoteRecyclerAdapter.ViewHolder> {

    private CreateEventFragment createEventFragment;
    private ArrayList<String> typeTopicArray;
    private int typeTopicState;
    private String placeID;

    public VoteRecyclerAdapter(CreateEventFragment createEventFragment, ArrayList<String> typeTopicArray, int typeTopicState, String placeID){
        this.createEventFragment = createEventFragment;
        this.typeTopicArray = typeTopicArray;
        this.typeTopicState = typeTopicState;
        this.placeID = placeID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_vote_item,parent,false);
        return new VoteRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final int ii = i;
        viewHolder.itemText.setText(typeTopicArray.get(i));
        viewHolder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(typeTopicState == 1){
                    //Selecting type
                    createEventFragment.selectedType = typeTopicArray.get(ii);
                    createEventFragment.setState(3);
                }else if(typeTopicState == 2){
                    //selecting topic
                    createEventFragment.selectedTopic = typeTopicArray.get(ii);
                    //Go to the last fragment
                    createEventFragment.setState(4);
                }
//                else if(typeTopicState == 5){
//                    createEventFragment.attendingEvent = typeTopicArray.get(ii);
//                    createEventFragment.setState(2);
//
//                }

            }
        });


    }

    @Override
    public int getItemCount() {
        return typeTopicArray.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public View item;
        public TextView itemText;

        public ViewHolder(@NonNull View view) {
            super(view);
            item = view;
            itemText = view.findViewById(R.id.item_text);
        }
    }
}
