package team.dahamdol.dahamnote_deafperson.lecture;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import team.dahamdol.dahamnote_deafperson.R;
import team.dahamdol.dahamnote_deafperson.database.LectureDatabase;
import team.dahamdol.dahamnote_deafperson.database.LectureEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LectureActivity extends AppCompatActivity {
    private static final String TAG = "LectureActivity";
    LectureDatabase lectureDatabase = null;
    LectureAdapter lectureAdapter;
    RecyclerView rvLectures;
    Button btnLectureBack;
    ConstraintLayout clLectureEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        clLectureEmpty = findViewById(R.id.cl_lecture_empty);
        rvLectures = findViewById(R.id.rv_lectures);

        lectureAdapter = new LectureAdapter(this);
        rvLectures.setAdapter(lectureAdapter);
        lectureDatabase = LectureDatabase.getInstance(this);
        readLectures();
        lectureAdapter.notifyDataSetChanged();

        btnLectureBack = findViewById(R.id.btn_lecture_back);
        btnLectureBack.setOnClickListener(view -> finish());
    }

    private void readLectures() {
        Runnable addRunnable = () -> {
            List<LectureEntity> lectureEntities = lectureDatabase.lectureDao().readAllLectures();
            if (lectureEntities.isEmpty()) {
                clLectureEmpty.setVisibility(View.VISIBLE);
                rvLectures.setVisibility(View.GONE);
            } else {
                clLectureEmpty.setVisibility(View.GONE);
                rvLectures.setVisibility(View.VISIBLE);
                lectureAdapter.lectureEntities = lectureEntities;
            }
        };
        Executor diskIO = Executors.newSingleThreadExecutor();
        diskIO.execute(addRunnable);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        readLectures();
        lectureAdapter.notifyDataSetChanged();
    }
}