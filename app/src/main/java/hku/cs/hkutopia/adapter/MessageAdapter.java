package hku.cs.hkutopia.adapter;

import android.content.Context;
import android.text.format.DateUtils;
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
import hku.cs.hkutopia.model.Message;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages;
    private Context context;
    private int lastPosition = -1;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case Message.TYPE_USER:
                View userView = inflater.inflate(R.layout.item_message_user, parent, false);
                return new UserMessageViewHolder(userView);
            case Message.TYPE_ADMIN:
                View adminView = inflater.inflate(R.layout.item_message_admin, parent, false);
                return new AdminMessageViewHolder(adminView);
            case Message.TYPE_SYSTEM:
            default:
                View systemView = inflater.inflate(R.layout.item_message_system, parent, false);
                return new SystemMessageViewHolder(systemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof UserMessageViewHolder) {
            bindUserMessage((UserMessageViewHolder) holder, message);
        } else if (holder instanceof AdminMessageViewHolder) {
            bindAdminMessage((AdminMessageViewHolder) holder, message);
        } else if (holder instanceof SystemMessageViewHolder) {
            bindSystemMessage((SystemMessageViewHolder) holder, message);
        }

        // Apply animation for new items
        setAnimation(holder.itemView, position);
    }

    private void bindUserMessage(UserMessageViewHolder holder, Message message) {
        holder.messageContent.setText(message.getContent());
        holder.messageSender.setText(message.getSender());
        holder.messageTime.setText(formatTime(message.getTimestamp()));

        // Set like button state
        updateLikeButton(holder.messageLike, message);

        holder.messageLike.setOnClickListener(v -> {
            message.toggleLike();
            updateLikeButton(holder.messageLike, message);
            notifyItemChanged(holder.getAdapterPosition());
        });
    }

    private void bindAdminMessage(AdminMessageViewHolder holder, Message message) {
        holder.messageContent.setText(message.getContent());
        holder.messageSender.setText(message.getSender());
        holder.messageTime.setText(formatTime(message.getTimestamp()));

        // Set like button state
        updateLikeButton(holder.messageLike, message);

        holder.messageLike.setOnClickListener(v -> {
            message.toggleLike();
            updateLikeButton(holder.messageLike, message);
            notifyItemChanged(holder.getAdapterPosition());
        });
    }

    private void bindSystemMessage(SystemMessageViewHolder holder, Message message) {
        holder.messageContent.setText(message.getContent());
        holder.messageTime.setText(formatTime(message.getTimestamp()));
    }

    private void updateLikeButton(ImageView likeButton, Message message) {
        if (message.isLiked()) {
            likeButton.setImageResource(R.drawable.ic_like_filled);
        } else {
            likeButton.setImageResource(R.drawable.ic_like);
        }

        // Show like count if there are likes
        if (message.getLikeCount() > 0) {
            likeButton.setTag(message.getLikeCount());
        } else {
            likeButton.setTag(null);
        }
    }

    private String formatTime(java.util.Date date) {
        return DateUtils.getRelativeTimeSpanString(
                date.getTime(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString();
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.item_animation_from_bottom);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    // ViewHolder classes
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageContent;
        TextView messageSender;
        TextView messageTime;
        ImageView messageLike;

        UserMessageViewHolder(View itemView) {
            super(itemView);
            messageContent = itemView.findViewById(R.id.messageContent);
            messageSender = itemView.findViewById(R.id.messageSender);
            messageTime = itemView.findViewById(R.id.messageTime);
            messageLike = itemView.findViewById(R.id.messageLike);
        }
    }

    static class AdminMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageContent;
        TextView messageSender;
        TextView messageTime;
        ImageView messageLike;

        AdminMessageViewHolder(View itemView) {
            super(itemView);
            messageContent = itemView.findViewById(R.id.messageContent);
            messageSender = itemView.findViewById(R.id.messageSender);
            messageTime = itemView.findViewById(R.id.messageTime);
            messageLike = itemView.findViewById(R.id.messageLike);
        }
    }

    static class SystemMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageContent;
        TextView messageTime;

        SystemMessageViewHolder(View itemView) {
            super(itemView);
            messageContent = itemView.findViewById(R.id.messageContent);
            messageTime = itemView.findViewById(R.id.messageTime);
        }
    }
}