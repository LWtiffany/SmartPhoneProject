package hku.cs.hkutopia.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hku.cs.hkutopia.R;
import hku.cs.hkutopia.model.HistoricalBuilding;

public class HistoricalBuildingAdapter extends RecyclerView.Adapter<HistoricalBuildingAdapter.BuildingViewHolder> {

    private List<HistoricalBuilding> buildings;
    private int lastPosition = -1;

    public HistoricalBuildingAdapter(List<HistoricalBuilding> buildings) {
        this.buildings = buildings;
    }

    @NonNull
    @Override
    public BuildingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historical_building, parent, false);
        return new BuildingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BuildingViewHolder holder, int position) {
        HistoricalBuilding building = buildings.get(position);
        holder.buildingName.setText(building.getName());
        holder.buildingYear.setText(building.getYear());
        holder.buildingImage.setImageResource(building.getImageResId());

        // 添加进入动画
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return buildings.size();
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.item_animation_from_bottom);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    static class BuildingViewHolder extends RecyclerView.ViewHolder {
        ImageView buildingImage;
        TextView buildingName;
        TextView buildingYear;

        public BuildingViewHolder(@NonNull View itemView) {
            super(itemView);
            buildingImage = itemView.findViewById(R.id.buildingImage);
            buildingName = itemView.findViewById(R.id.buildingName);
            buildingYear = itemView.findViewById(R.id.buildingYear);
        }
    }
}