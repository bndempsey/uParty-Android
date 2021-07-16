package com.ggi.uparty.other;

//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ggi.uparty.MainActivity;
import com.ggi.uparty.R;

import java.util.ArrayList;

public class EventRemarksAdapter extends RecyclerView.Adapter<EventRemarksAdapter.ViewHolder> {

    private MainActivity mainActivity;
    private ArrayList<String> remarks;

    public EventRemarksAdapter(MainActivity mainActivity, ArrayList<String> remarks){
        this.mainActivity = mainActivity;
        this.remarks = remarks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.remark_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        String remark = remarks.get(i);
        viewHolder.remarkText.setText(remark);
    }

    @Override
    public int getItemCount() {
        return remarks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView remarkText;

        public ViewHolder(View view) {
            super(view);
            remarkText = view.findViewById(R.id.remark_text);
        }
    }
}
