package com.ggi.uparty.viewpagerFrags;

import android.graphics.Color;
//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ggi.uparty.MainActivity;
import com.ggi.uparty.R;

import java.util.ArrayList;

public class ExploreRecyclerAdapter extends RecyclerView.Adapter<ExploreRecyclerAdapter.ViewHolder> {

    MainActivity mainActivity;
    ArrayList<ExploreItem> exploreItems;

    public ExploreRecyclerAdapter(MainActivity mainActivity, ArrayList<ExploreItem> exploreItems){
        this.mainActivity = mainActivity;
        this.exploreItems = exploreItems;
    }

    @NonNull
    @Override
    public ExploreRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.explore_item,parent,false);
        return new ExploreRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExploreRecyclerAdapter.ViewHolder holder, int position) {
        final int i = position;
        ExploreItem exploreItem = exploreItems.get(position);
        holder.city.setText(exploreItem.cityString);
        String pop = exploreItem.population.toString();
        pop = pop.substring(0,pop.indexOf("."));
        holder.population.setText(pop);
        holder.exploreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(i<exploreItems.size()) {
                    ExploreItem item = exploreItems.get(i);
                    mainActivity.viewChatCityID = item.cityID;
                    mainActivity.viewChatCityName = item.cityName;
                    mainActivity.updateChat();
                    ((ImageView) mainActivity.findViewById(R.id.chat_img)).setColorFilter(mainActivity.getResources().getColor(R.color.colorPrimary));
                    ((ImageView) mainActivity.findViewById(R.id.explore_img)).setColorFilter(Color.parseColor("#5B5B5B"));
                    ((ImageView) mainActivity.findViewById(R.id.map_img)).setColorFilter(Color.parseColor("#5B5B5B"));
                    ((ImageView) mainActivity.findViewById(R.id.settings_img)).setColorFilter(Color.parseColor("#5B5B5B"));
                    mainActivity.pager.setCurrentItem(1);
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return exploreItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView city;
        public TextView population;
        public View exploreView;


        public ViewHolder(View view) {
            super(view);
            city = view.findViewById(R.id.city);
            population = view.findViewById(R.id.population);
            exploreView = view.findViewById(R.id.explore_view);
        }
    }
}
