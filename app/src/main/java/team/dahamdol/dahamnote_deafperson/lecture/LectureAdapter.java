package team.dahamdol.dahamnote_deafperson.lecture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import team.dahamdol.dahamnote_deafperson.R;
import team.dahamdol.dahamnote_deafperson.database.LectureEntity;

import java.util.List;

public class LectureAdapter extends RecyclerView.Adapter<LectureViewHolder> {
    private final Context context;

    public LectureAdapter(Context context) {
        this.context = context;
    }

    List<LectureEntity> lectureEntities;

    @Override
    public LectureViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lecture, viewGroup, false);
        return new LectureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LectureViewHolder holder, int position) {
        holder.bind(lectureEntities.get(position), context);
    }

    @Override
    public int getItemCount() {
        return (null != lectureEntities ? lectureEntities.size() : 0);
    }

}