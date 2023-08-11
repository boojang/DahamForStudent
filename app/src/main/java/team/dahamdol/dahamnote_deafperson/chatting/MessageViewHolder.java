package team.dahamdol.dahamnote_deafperson.chatting;

import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import team.dahamdol.dahamnote_deafperson.R;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    TextView tv_name = itemView.findViewById(R.id.tv_name);
    TextView tv_content = itemView.findViewById(R.id.tv_content);
    CheckBox checkBoxMessage = itemView.findViewById(R.id.checkbox_message);
    ConstraintLayout clItemMessage = itemView.findViewById(R.id.cl_item_message);
    ConstraintLayout clWrapItemMessage = itemView.findViewById(R.id.cl_wrap_item_message);
    SparseBooleanArray isCheckedList;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void bind(MessageData messageData, boolean isCheckboxVisible, SparseBooleanArray isCheckedList){
        this.isCheckedList = isCheckedList;
        setCheckboxVisibility(isCheckboxVisible);
        setListenerOnCheckboxMessage();
        tv_name.setText(messageData.getName());
        tv_content.setText(messageData.getContent());
        setMessageContentDesign(messageData.getName());
    }

    private void setCheckboxVisibility(boolean isCheckboxVisible){
        if(isCheckboxVisible){
            checkBoxMessage.setChecked(isCheckedList.get(getAdapterPosition()));
            checkBoxMessage.setVisibility(View.VISIBLE);
        } else {
            checkBoxMessage.setVisibility(View.GONE);
        }
    }

    private void setListenerOnCheckboxMessage(){
        checkBoxMessage.setOnClickListener(v -> {
            isCheckedList.put(getAdapterPosition(),((CheckBox)v).isChecked());
        });
    }

    private void setMessageContentDesign(String name){
        if (name.equals("나")){
            // message box right align
            tv_name.setVisibility(View.GONE);

            clItemMessage.setBackgroundResource(R.drawable.bg_message_me);
            ConstraintSet set = new ConstraintSet();
            set.clone(clWrapItemMessage);
            set.setHorizontalBias(clItemMessage.getId(),1);
            set.applyTo(clWrapItemMessage);

            ConstraintSet set2 = new ConstraintSet();
            set2.clone(clItemMessage);
            set2.connect(tv_content.getId(), ConstraintSet.RIGHT, clItemMessage.getId(), ConstraintSet.RIGHT);
            set2.connect(tv_content.getId(), ConstraintSet.TOP, clItemMessage.getId(), ConstraintSet.TOP);
            set2.applyTo(clItemMessage);
        } else {
            // message box left align
            tv_name.setVisibility(View.VISIBLE);

            clItemMessage.setBackgroundResource(R.drawable.bg_message_teacher);
            ConstraintSet set = new ConstraintSet();
            set.clone(clWrapItemMessage);
            set.setHorizontalBias(clItemMessage.getId(),0);
            set.applyTo(clWrapItemMessage);

            ConstraintSet set2 = new ConstraintSet();
            set2.clone(clItemMessage);
            set2.connect(tv_content.getId(), ConstraintSet.LEFT, tv_name.getId(), ConstraintSet.LEFT);
            set2.connect(tv_content.getId(), ConstraintSet.TOP, tv_name.getId(), ConstraintSet.BOTTOM, 10);
            set2.applyTo(clItemMessage);
        }
    }
}
