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

import hku.cs.hkutopia.R;
import hku.cs.hkutopia.model.TimelineEvent;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {

    private List<TimelineEvent> timelineEvents;
    private int lastPosition = -1;

    public TimelineAdapter(List<TimelineEvent> timelineEvents) {
        this.timelineEvents = timelineEvents;
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        TimelineEvent event = timelineEvents.get(position);
        holder.timelineYear.setText(event.getYear());
        holder.timelineTitle.setText(event.getTitle());
        holder.timelineDescription.setText(event.getDescription());

        // 设置时间线的顶部和底部线条
        if (position == 0) {
            holder.lineTop.setVisibility(View.INVISIBLE);
        } else {
            holder.lineTop.setVisibility(View.VISIBLE);
        }

        if (position == timelineEvents.size() - 1) {
            holder.lineBottom.setVisibility(View.INVISIBLE);
        } else {
            holder.lineBottom.setVisibility(View.VISIBLE);
        }

        // 添加进入动画
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return timelineEvents.size();
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.item_animation_from_right);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    static class TimelineViewHolder extends RecyclerView.ViewHolder {
        TextView timelineYear;
        TextView timelineTitle;
        TextView timelineDescription;
        View lineTop;
        View timelineDot;
        View lineBottom;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            timelineYear = itemView.findViewById(R.id.timelineYear);
            timelineTitle = itemView.findViewById(R.id.timelineTitle);
            timelineDescription = itemView.findViewById(R.id.timelineDescription);
            lineTop = itemView.findViewById(R.id.lineTop);
            timelineDot = itemView.findViewById(R.id.timelineDot);
            lineBottom = itemView.findViewById(R.id.lineBottom);
        }
    }
}