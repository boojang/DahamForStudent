package team.dahamdol.dahamnote_deafperson.chatting;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import team.dahamdol.dahamnote_deafperson.R;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {
    private Context context;
    public Boolean isCheckboxVisible = false;
    private SparseBooleanArray isCheckedList = new SparseBooleanArray(0);
    public ArrayList<MessageData> messages;

    public MessageAdapter(Context context) {
        this.context = context;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, viewGroup, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(messages.get(position), isCheckboxVisible, isCheckedList);
    }

    @Override
    public int getItemCount() {
        return (null != messages ? messages.size() : 0);
    }

    public void setCheckBoxVisible(boolean isVisible) {
        this.isCheckboxVisible = isVisible;
        int MessageCnt = getItemCount();
        for (int position = 0; position < MessageCnt; position++) {
            isCheckedList.put(position, true);
        }
        notifyDataSetChanged();
    }

    public ArrayList<MessageData> getCheckedMessages() {
        ArrayList<MessageData> checkedMessages = new ArrayList<>();
        int MessageCnt = getItemCount();
        for (int position = 0; position < MessageCnt; position++) {
            if (isCheckedList.get(position)) {
                checkedMessages.add(messages.get(position));
            }
        }
        return checkedMessages;
    }
}