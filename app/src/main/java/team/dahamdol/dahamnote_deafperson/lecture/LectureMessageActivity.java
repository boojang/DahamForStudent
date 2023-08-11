package team.dahamdol.dahamnote_deafperson.lecture;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import team.dahamdol.dahamnote_deafperson.R;
import team.dahamdol.dahamnote_deafperson.chatting.MessageAdapter;
import team.dahamdol.dahamnote_deafperson.chatting.MessageData;
import team.dahamdol.dahamnote_deafperson.database.LectureDatabase;
import team.dahamdol.dahamnote_deafperson.database.LectureEntity;
import team.dahamdol.dahamnote_deafperson.database.LectureHasMessage;
import team.dahamdol.dahamnote_deafperson.database.MessageEntity;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LectureMessageActivity extends AppCompatActivity {
    private static final String TAG = "LectureMessageActivity";
    LectureDatabase lectureDatabase = null;
    MessageAdapter messageAdapter;
    RecyclerView rvLectureMessages;
    TextView tvLectureName;
    TextView tvLectureDate;
    Button btnLectureMessagesBack;
    Button btnLectureDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_message);
        tvLectureName = findViewById(R.id.tv_lecture_messages_name);
        tvLectureDate = findViewById(R.id.tv_lecture_messages_date);

        long lectureId = getIntent().getLongExtra(getString(R.string.lecture_id), -1);

        messageAdapter = new MessageAdapter(getApplicationContext());
        rvLectureMessages = findViewById(R.id.rv_lecture_messages);
        rvLectureMessages.setAdapter(messageAdapter);

        lectureDatabase = LectureDatabase.getInstance(this);
        readLectureMessages(lectureId);

        btnLectureMessagesBack = (Button) findViewById(R.id.btn_lecture_messages_back);
        btnLectureMessagesBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnLectureDelete = (Button) findViewById(R.id.btn_lecture_delete);
        btnLectureDelete.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View view2) {
                final Dialog dialog = new Dialog(LectureMessageActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_lecture_delete);
                dialog.show();

                //Display 사이즈의 90%
                Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                Point point = new Point();
                display.getRealSize(point);
                int width =  (int)(point.x * 0.9);
                dialog.getWindow().getAttributes().width = width;
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();

                final Button btnDialogLectureDeleteConfirm = (Button) dialog.findViewById(R.id.btn_dialog_lecture_delete_confirm);
                final Button btnDialogLectureDeleteCancel = (Button) dialog.findViewById(R.id.btn_dialog_lecture_delete_cancel);

                btnDialogLectureDeleteConfirm.setOnClickListener(view -> {
                    dialog.dismiss();
                    deleteLectureById(lectureId);
                    finish();
                });

                btnDialogLectureDeleteCancel.setOnClickListener(view -> dialog.dismiss());
            }
        });
    }

    private void deleteLectureById(long lectureId) {
        Runnable addRunnable = () -> {
            LectureEntity lecture = (LectureEntity) lectureDatabase.lectureDao().readLectureByLectureId(lectureId);
            lectureDatabase.lectureDao().deleteLecture(lecture);
        };
        Executor diskIO = Executors.newSingleThreadExecutor();
        diskIO.execute(addRunnable);
    }


    private void readLectureMessages(long lectureId) {
        if (lectureId == -1) {
            Log.e(TAG, "lectureId is -1");
            return;
        }

        Runnable addRunnable = () -> {
            LectureHasMessage lectureMessages = lectureDatabase.lectureHasMessageDao().readLectureHasMessageByLectureId(lectureId);
            tvLectureName.setText(lectureMessages.lectureEntity.lectureName);
            tvLectureDate.setText(lectureMessages.lectureEntity.lectureDate);

            ArrayList<MessageData> messages = new ArrayList<>();
            for (MessageEntity messageEntity : lectureMessages.messageEntities) {
                messages.add(new MessageData(messageEntity.messageName, messageEntity.messageContent));
            }
            messageAdapter.messages = messages;
            messageAdapter.notifyDataSetChanged();
        };
        Executor diskIO = Executors.newSingleThreadExecutor();
        diskIO.execute(addRunnable);
    }
}