package hku.cs.hkutopia.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hku.cs.hkutopia.R;
import hku.cs.hkutopia.model.Alumni;

public class AlumniAdapter extends RecyclerView.Adapter<AlumniAdapter.AlumniViewHolder> {

    private List<Alumni> alumniList;
    private int lastPosition = -1;

    public AlumniAdapter(List<Alumni> alumniList) {
        this.alumniList = alumniList;
    }

    @NonNull
    @Override
    public AlumniViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alumni, parent, false);
        return new AlumniViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlumniViewHolder holder, int position) {
        Alumni alumni = alumniList.get(position);
        holder.alumniName.setText(alumni.getName());
        holder.alumniDegree.setText(alumni.getDegree());
        holder.alumniAchievement.setText(alumni.getAchievement());
        holder.alumniImage.setImageResource(alumni.getImageResId());

        // 添加进入动画
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return alumniList.size();
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.item_animation_from_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    static class AlumniViewHolder extends RecyclerView.ViewHolder {
        CircleImageView alumniImage;
        TextView alumniName;
        TextView alumniDegree;
        TextView alumniAchievement;

        public AlumniViewHolder(@NonNull View itemView) {
            super(itemView);
            alumniImage = itemView.findViewById(R.id.alumniImage);
            alumniName = itemView.findViewById(R.id.alumniName);
            alumniDegree = itemView.findViewById(R.id.alumniDegree);
            alumniAchievement = itemView.findViewById(R.id.alumniAchievement);
        }
    }
}