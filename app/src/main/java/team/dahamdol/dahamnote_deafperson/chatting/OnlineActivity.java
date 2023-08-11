package team.dahamdol.dahamnote_deafperson.chatting;

import static team.dahamdol.dahamnote_deafperson.chatting.Constants.MESSAGE_LECTURE_SAVE_RESULT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import team.dahamdol.dahamnote_deafperson.R;
import team.dahamdol.dahamnote_deafperson.database.LectureDatabase;
import team.dahamdol.dahamnote_deafperson.database.LectureEntity;
import team.dahamdol.dahamnote_deafperson.database.MessageEntity;
import team.dahamdol.dahamnote_deafperson.lecture.LectureActivity;
import team.dahamdol.dahamnote_deafperson.lecture.LectureSaveDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class OnlineActivity extends AppCompatActivity {
    private static final String TAG_ACTIVITY = "OnlineActivity";
    private static final String TAG_RECOGNIZER = "Recognizer";
    private static final boolean D = true;

    private LectureDatabase lectureDatabase = null;
    MessageAdapter messageAdapter;
    RecyclerView rvChattingMessages;
    ArrayList<MessageData> messages = new ArrayList<MessageData>();

    private Button btnSetting;
    private Button btnSaveLecture;
    private Button btnLectureList;
    private ConstraintLayout clLectureSave;
    private ImageView btnLectureSaveBack;
    private TextView btnSaveLectureStep2;
    private FloatingActionButton fabRec;
    private View lineRec;
    private ImageView imgOnlineStart;
    private Animation fadeoutAnim;

    // 음성 인식
    private Boolean isRecOn = false;
    private Intent SttIntent;
    private SpeechRecognizer mRecognizer;
    AudioManager audioManager;
    public static int silenceLength = 3000;
    public static String selectedShowingLanguage = "한국어";
    public static String selectedLanguage = "ko-KR";
    public static int selectedLanguageId = R.id.rd_korean;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D) Log.e(TAG_ACTIVITY, "+++ ON CREATE +++");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_online);

        btnSetting = findViewById(R.id.btn_setting);
        btnSaveLecture = findViewById(R.id.btn_save_lecture);
        btnLectureList = findViewById(R.id.btn_lecture_list);
        rvChattingMessages = findViewById(R.id.rv_chatting_messages);
        clLectureSave = findViewById(R.id.cl_lecture_save);
        btnLectureSaveBack = findViewById(R.id.btn_lecture_save_back);
        btnSaveLectureStep2 = findViewById(R.id.btn_save_lecture_step2);
        fabRec = findViewById(R.id.fab_rec);
        lineRec = findViewById(R.id.line_rec);
        imgOnlineStart = findViewById(R.id.img_online_start);
        fadeoutAnim = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out);

        setup();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setup() {
        Log.d(TAG_ACTIVITY, "setup()");

        // DB
        lectureDatabase = LectureDatabase.getInstance(this);

        // 음성인식
        SttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        setLanguage();
        setSilenceLength();
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(voiceRecogListener);
        fabRec.setOnClickListener(fabRecOnclickListener);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE); // 음성인식 중 mute를 위함

        // RecyclerView
        setRecyclerView();

        // Button
        setListenerOnBtns();
    }

    /**
     * 메시지 리사이클러뷰 관련
     */

    private void setRecyclerView() {
        // Initialize recycler view
        messageAdapter = new MessageAdapter(getApplicationContext());
        rvChattingMessages.setAdapter(messageAdapter);
        messageAdapter.messages = messages;
        messageAdapter.notifyDataSetChanged();
    }

    private void addMessage(String name, String content) {
        messageAdapter.messages.add(new MessageData(name, content));
        messageAdapter.notifyDataSetChanged();
        rvChattingMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
    }

    /**
     * 버튼 관련
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setListenerOnBtns() {
        setListenerOnBtnSetting();
        setListenerOnBtnLectureList();
        setListenerOnBtnSaveLecture();
        setListenerOnBtnLectureSaveBack();
        setListenerOnBtnSaveLectureStep2();
    }

    private void setListenerOnBtnSetting() {
        btnSetting.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
        });
    }

    private void setListenerOnBtnLectureList() {
        btnLectureList.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LectureActivity.class);
            startActivity(intent);
        });
    }

    /**
     * 저장 관련
     */
    private void setListenerOnBtnSaveLecture() {
        btnSaveLecture.setOnClickListener(v -> showSaveLectureView());
    }

    private void setListenerOnBtnLectureSaveBack() {
        btnLectureSaveBack.setOnClickListener(v -> closeLectureSaveView());
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setListenerOnBtnSaveLectureStep2() {
        btnSaveLectureStep2.setOnClickListener(v -> {
            LectureSaveDialog lectureSaveDialog = new LectureSaveDialog(OnlineActivity.this);
            lectureSaveDialog.setDialogListener((lectureName, lectureDate) -> {
                if (lectureDatabase != null) {
                    saveLecture(lectureName, lectureDate, messageAdapter.getCheckedMessages());
                } else {
                    Log.e(TAG_ACTIVITY, "database is null");
                }
            });
            lectureSaveDialog.show();
        });
    }

    public void saveLecture(final String lectureName, final String lectureDate, final ArrayList<MessageData> messages) {
        Runnable addRunnable = () -> {
            boolean isLectureSaved;
            List<LectureEntity> lectures = lectureDatabase.lectureDao().readLectureByLectureName(lectureName);
            if (!lectures.isEmpty()) {
                isLectureSaved = false;
            } else {
                long lectureId = lectureDatabase.lectureDao().createLecture(new LectureEntity(lectureName, lectureDate));
                List<MessageEntity> messageEntities = new ArrayList<>();
                for (MessageData message : messages) {
                    messageEntities.add(new MessageEntity(lectureId, message.name, message.content));
                }
                lectureDatabase.messageDao().createMessages(messageEntities);
                isLectureSaved = true;
            }

            // 메인스레드에서만 ui 변경 가능하여 핸들러 이용
            messageHandler.obtainMessage(MESSAGE_LECTURE_SAVE_RESULT, -1, -1, isLectureSaved)
                    .sendToTarget();
        };
        Executor diskIO = Executors.newSingleThreadExecutor();
        diskIO.execute(addRunnable);
    }

    @SuppressLint("HandlerLeak")
    private final Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_LECTURE_SAVE_RESULT:
                    showSaveResult(msg);
            }
        }
    };

    private void showSaveResult(Message msg) {
        boolean isLectureSaved = (boolean) msg.obj;
        if (isLectureSaved) {
            closeLectureSaveView();
            Toast.makeText(this, getString(R.string.lecture_saved), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.lecture_exist), Toast.LENGTH_SHORT).show();
        }
    }

    private void showSaveLectureView() {
        fabRec.setVisibility(View.INVISIBLE);
        clLectureSave.setVisibility(View.VISIBLE);
        btnSaveLectureStep2.setVisibility(View.VISIBLE);
        messageAdapter.setCheckBoxVisible(true);
        lineRec.setVisibility(View.GONE);
        closeStartImg();
    }

    private void closeStartImg() {
        if (imgOnlineStart.getVisibility() == View.VISIBLE) {
            imgOnlineStart.startAnimation(fadeoutAnim);
            imgOnlineStart.setVisibility(View.GONE);
        }
    }

    private void closeLectureSaveView() {
        fabRec.setVisibility(View.VISIBLE);
        clLectureSave.setVisibility(View.GONE);
        btnSaveLectureStep2.setVisibility(View.GONE);
        messageAdapter.setCheckBoxVisible(false);
        lineRec.setVisibility(View.VISIBLE);
    }

    /**
     * 음성인식 관련
     */
    void setSilenceLength() {
/*        SttIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, silenceLength);
        SttIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, silenceLength);*/
    }

    void setLanguage() {
        SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguage);
    }

    void changeAlertSound() {
        if (isRecOn) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI);
        } else {
            audioManager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        }
    }

    private final View.OnClickListener fabRecOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 권한체크 먼저
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { // 권한이 없거나 거부한 경우
                ActivityCompat.requestPermissions(OnlineActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            } else { //권한을 허용한 경우
                try {
                    if (!isRecOn) { // 현재까지 상태: 음성인식 꺼있음 -> 이제 켜야함
                        isRecOn = true;

                        closeStartImg();
                        rvChattingMessages.setVisibility(View.VISIBLE);
                        lineRec.setVisibility(View.VISIBLE);
                        fabRec.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_rec_on));

                        mRecognizer.startListening(SttIntent);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.start_recognition), Toast.LENGTH_SHORT).show();
                    } else { // 현재까지 상태: 음성인식 켜져있음 -> 이제 꺼야함
                        isRecOn = false;

                        fabRec.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_rec_off));

                        mRecognizer.stopListening();
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.stop_recognition), Toast.LENGTH_SHORT).show();
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }

            changeAlertSound(); // 음성인식 on off에 따라서 알림소리 조절
        }
    };

    private final RecognitionListener voiceRecogListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            //mRecognizer.startListening(SttIntent);
        }

        @Override
        public void onBeginningOfSpeech() {
            if (isRecOn) {
                fabRec.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_rec_on_ing_2));
            }
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
            if (isRecOn) {
                fabRec.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_rec_on));
            }
        }

        @Override
        public void onError(int error) {
            String message;
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER 이미 실행중";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                default:
                    message = "알 수 없는 오류임";
            }

            if (isRecOn) {
                if (error != SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    restartRecognition();
                }
            }

            Log.d(TAG_RECOGNIZER, "OnError(): " + message);
        }

        @Override
        public void onResults(Bundle results) {
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줍니다.
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            restartRecognition();
            addMessage(getString(R.string.teacher), matches.get(0));
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    };

    void restartRecognition() {
        if (isRecOn) {
            mRecognizer.startListening(SttIntent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setSilenceLength();
        setLanguage();
    }

    /**
     * 강연(회의) 종료 관련
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBackPressed() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_exit);

        //Display 사이즈의 90%
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);
        int width = (int) (point.x * 0.9);
        dialog.getWindow().getAttributes().width = width;
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        final Button btnDialogExitConfirm = dialog.findViewById(R.id.btn_dialog_exit_confirm);
        final Button btnDialogExitCancel = dialog.findViewById(R.id.btn_dialog_exit_cancel);

        btnDialogExitConfirm.setOnClickListener(view -> {
            dialog.dismiss();
            finish();
        });

        btnDialogExitCancel.setOnClickListener(view -> dialog.dismiss());
    }
}