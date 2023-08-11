package team.dahamdol.dahamnote_deafperson.lecture;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import team.dahamdol.dahamnote_deafperson.R;
import team.dahamdol.dahamnote_deafperson.database.LectureEntity;

public class LectureViewHolder extends RecyclerView.ViewHolder {
    TextView tvLectureName = itemView.findViewById(R.id.tv_lecture_name);
    TextView tvLectureDate = itemView.findViewById(R.id.tv_lecture_date);

    public LectureViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void bind(final LectureEntity lectureEntity, Context context){
        tvLectureName.setText(lectureEntity.lectureName);
        tvLectureDate.setText(lectureEntity.lectureDate);
        itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, LectureMessageActivity.class);
            intent.putExtra(context.getString(R.string.lecture_id), lectureEntity.lectureId);
            context.startActivity(intent);
        });

    }
}
