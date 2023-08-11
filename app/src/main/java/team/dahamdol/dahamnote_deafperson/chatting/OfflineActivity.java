/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package team.dahamdol.dahamnote_deafperson.chatting;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import team.dahamdol.dahamnote_deafperson.bluetooth.BluetoothChattingService;
import team.dahamdol.dahamnote_deafperson.bluetooth.BluetoothDialogActivity;
import team.dahamdol.dahamnote_deafperson.R;
import team.dahamdol.dahamnote_deafperson.database.LectureEntity;
import team.dahamdol.dahamnote_deafperson.database.LectureDatabase;
import team.dahamdol.dahamnote_deafperson.database.MessageEntity;
import team.dahamdol.dahamnote_deafperson.lecture.LectureActivity;
import team.dahamdol.dahamnote_deafperson.lecture.LectureSaveDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class OfflineActivity extends Activity {
    private static final String TAG_ACTIVITY = "OfflineActivity";
    private static final boolean D = true;
    private static int VIBRATE = 1;
    private static int QUESTION = 2;

    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothChattingService chattingService = null;
    private StringBuffer outMessageStringBuffer;
    private String studentName = null;

    private LectureDatabase lectureDatabase = null;
    MessageAdapter messageAdapter;
    RecyclerView rvChattingMessages;
    ArrayList<MessageData> messages = new ArrayList<MessageData>();

    private Button btnBluetoothConnect;
    private Button btnSaveLecture;
    private Button btnLectureList;
    private Button BtnSendMessage;
    private EditText edtOutMessage;
    private ConstraintLayout clSendingMessage;
    private ConstraintLayout clLectureSave;
    private ImageView btnLectureSaveBack;
    private TextView btnSaveLectureStep2;
    private Boolean willClearAfterReconnect = false;
    private ImageView imgOfflineConnecting;
    private Animation fadeoutAnim;
    private Animation fadeinAnim;

    private String[] Permissions = {Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.NEARBY_WIFI_DEVICES};

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D) Log.e(TAG_ACTIVITY, "+++ ON CREATE +++");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_offline);

        // UI
        btnBluetoothConnect = (Button) findViewById(R.id.btn_bluetooth_connect);
        btnSaveLecture = (Button) findViewById(R.id.btn_save_lecture);
        btnLectureList = (Button) findViewById(R.id.btn_lecture_list);
        rvChattingMessages = findViewById(R.id.rv_chatting_messages);
        edtOutMessage = (EditText) findViewById(R.id.edit_text_out);
        BtnSendMessage = (Button) findViewById(R.id.button_send);
        clSendingMessage = (ConstraintLayout) findViewById(R.id.cl_sending_message);
        clLectureSave = (ConstraintLayout) findViewById(R.id.cl_lecture_save);
        btnLectureSaveBack = (ImageView) findViewById(R.id.btn_lecture_save_back);
        btnSaveLectureStep2 = (TextView) findViewById(R.id.btn_save_lecture_step2);
        imgOfflineConnecting = findViewById(R.id.img_offline_connecting);
        fadeoutAnim = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out);
        fadeinAnim = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);

        // DB
        lectureDatabase = LectureDatabase.getInstance(this);

        // Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 강연(회의)화면 시작하자마자 연결 다이얼로그 띄우기
        showBluetoothDialog();
    }

    /**
     * 블루투스 연결 관련
     */

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onStart() {
        super.onStart();
        if (D) Log.e(TAG_ACTIVITY, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChatting() will then be called during onActivityResult
        // Otherwise, setup the chat session
        try {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
            } else {
                if (chattingService == null) setup();
            }
        } catch (SecurityException e) {
            Log.e(TAG_ACTIVITY, "error");
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D) Log.e(TAG_ACTIVITY, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        // Only if the state is STATE_NONE, do we know that we haven't started already
        // Start the Bluetooth chat services

        if (chattingService != null) {
            if (chattingService.getState() == BluetoothChattingService.STATE_NONE) {
                chattingService.start();
            }
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        //권한 체크 결과를 불러온다
        if(requestCode == 1000) {
            boolean check_result = true;
            // 모든 퍼미션을 허용했는지 체크
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            // 권한 체크에 동의를 하지 않으면 안드로이드 종료
            if(check_result == true) { }
            else { finish(); } }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D) Log.d(TAG_ACTIVITY, "onActivityResult " + resultCode);
        switch (requestCode) {
            case Constants.REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    connectToChatting(data);
                    showConnectingImg();
                }
                break;
            case Constants.REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    setup();
                } else {
                    Log.d(TAG_ACTIVITY, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setup() {
        Log.d(TAG_ACTIVITY, "setup()");
        setListenerOnBtns();
        setRecyclerView();
        setupChatting();
    }

    private void setupChatting() {
        Log.d(TAG_ACTIVITY, "setupChatting()");

        // Initialize the BluetoothChatService to perform bluetooth connections and the buffer for outgoing messages
        chattingService = new BluetoothChattingService(this, messageHandler);
        outMessageStringBuffer = new StringBuffer("");
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setListenerOnBtnBluetoothConnect() {
        btnBluetoothConnect.setOnClickListener(v -> {
            if (messageAdapter.getItemCount() > 0) {
                showReconnectDialog();
            } else {
                showBluetoothDialog();
            }
        });
    }

    private void showBluetoothDialog() {
        Intent serverIntent = new Intent(getApplicationContext(), BluetoothDialogActivity.class);
        startActivityForResult(serverIntent, Constants.REQUEST_CONNECT_DEVICE);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void showReconnectDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_reconnect);

        //Display 사이즈의 90%
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);
        int width = (int) (point.x * 0.9);
        dialog.getWindow().getAttributes().width = width;
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        final Button btnDialogReconnectKeep = (Button) dialog.findViewById(R.id.btn_dialog_reconnect_keep);
        final Button btnDialogReconnectDelete = (Button) dialog.findViewById(R.id.btn_dialog_reconnect_delete);
        final ImageView btnDialogReconnectDismiss = (ImageView) dialog.findViewById(R.id.btn_dialog_reconnect_dismiss);

        btnDialogReconnectDelete.setOnClickListener(view -> {
            willClearAfterReconnect = true;
            showBluetoothDialog();
            dialog.dismiss();
        });

        btnDialogReconnectKeep.setOnClickListener(view -> {
            willClearAfterReconnect = false;
            showBluetoothDialog();
            dialog.dismiss();
        });

        btnDialogReconnectDismiss.setOnClickListener(view -> dialog.dismiss());
    }

    public void connectToChatting(Intent data) {
        String address = data.getExtras()
                .getString(BluetoothDialogActivity.EXTRA_DEVICE_ADDRESS);
        studentName = data.getExtras().getString(BluetoothDialogActivity.EXTRA_STUDENT_NAME);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        chattingService.connect(device, studentName);
    }

    private void showConnectingImg() {
        if (imgOfflineConnecting.getVisibility() == View.GONE) {
            imgOfflineConnecting.startAnimation(fadeinAnim);
            imgOfflineConnecting.setVisibility(View.VISIBLE);
        }
    }

    private void closeConnectingImg() {
        if (imgOfflineConnecting.getVisibility() == View.VISIBLE) { // 연결 중 이미지 사라지기
            imgOfflineConnecting.startAnimation(fadeoutAnim);
            imgOfflineConnecting.setVisibility(View.GONE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void showDisconnectedDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_disconnected);

        //Display 사이즈의 90%
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);
        int width = (int) (point.x * 0.9);
        dialog.getWindow().getAttributes().width = width;
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        final Button btnDialogDisconnectDismiss = (Button) dialog.findViewById(R.id.btn_dialog_disconnected_dismiss);
        btnDialogDisconnectDismiss.setOnClickListener(view -> {
            dialog.dismiss();
        });
    }


    /**
     * 버튼 관련
     */

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setListenerOnBtns() {
        setListenerOnBtnBluetoothConnect();
        //setListenerOnBtnDiscover();
        setListenerOnBtnSaveLecture();
        setListenerOnBtnLectureList();
        setListenerOnBtnSendMessage();
        setListenerOnBtnLectureSaveBack();
        setListenerOnBtnSaveLectureStep2();
    }

/*    private void setListenerOnBtnBluetoothConnect() {
        btnBluetoothConnect.setOnClickListener(v -> {
            Intent serverIntent = new Intent(getApplicationContext(), BluetoothDialogActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        });
    }*/


/*
    private void setListenerOnBtnDiscover() {
        Button discoverButton = (Button) findViewById(R.id.discovery_button);
        discoverButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                makeDiscoverable();
            }
        });
    }
*/

    private void setListenerOnBtnLectureList() {
        btnLectureList.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LectureActivity.class);
            startActivity(intent);
        });
    }

    private void setListenerOnBtnSendMessage() {
        BtnSendMessage.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String message = edtOutMessage.getText().toString();
                sendMessage(message);
            }
        });
    }


    /**
     * 저장 관련
     */
    private void setListenerOnBtnSaveLecture() {
        btnSaveLecture.setOnClickListener(v -> {
            clSendingMessage.setVisibility(View.GONE);
            clLectureSave.setVisibility(View.VISIBLE);
            btnSaveLectureStep2.setVisibility(View.VISIBLE);
            messageAdapter.setCheckBoxVisible(true);
        });
    }


    private void setListenerOnBtnLectureSaveBack() {
        btnLectureSaveBack.setOnClickListener(v -> {
            closeLectureSaveView();
        });
    }

    private void closeLectureSaveView() {
        clSendingMessage.setVisibility(View.VISIBLE);
        clLectureSave.setVisibility(View.GONE);
        btnSaveLectureStep2.setVisibility(View.GONE);
        messageAdapter.setCheckBoxVisible(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setListenerOnBtnSaveLectureStep2() {
        btnSaveLectureStep2.setOnClickListener(v -> {
            LectureSaveDialog lectureSaveDialog = new LectureSaveDialog(OfflineActivity.this);
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
            messageHandler.obtainMessage(Constants.MESSAGE_LECTURE_SAVE_RESULT, -1, -1, isLectureSaved)
                    .sendToTarget();
        };
        Executor diskIO = Executors.newSingleThreadExecutor();
        diskIO.execute(addRunnable);
    }

    private void showSaveResult(Message msg) {
        boolean isLectureSaved = (boolean) msg.obj;
        if (isLectureSaved) {
            closeLectureSaveView();
            Toast.makeText(this, getString(R.string.lecture_saved), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.lecture_exist), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 메시지 관련
     */

    private void setRecyclerView() {
        // Initialize recycler view
        messageAdapter = new MessageAdapter(getApplicationContext());
        rvChattingMessages.setAdapter(messageAdapter);
        messageAdapter.messages = messages;
        messageAdapter.notifyDataSetChanged();
    }

    private void sendAttendClassMessage() {
        if (studentName != null && chattingService.getState() == BluetoothChattingService.STATE_CONNECTED) {
            sendMessage(getString(R.string.attend_class));
            Toast.makeText(this, R.string.connected, Toast.LENGTH_SHORT).show();
        } else if (studentName != null) {
            Log.e(TAG_ACTIVITY, "student name is null");
        } else {
            Log.e(TAG_ACTIVITY, "chatting is not connected");
        }
    }

    private void sendMessage(String message) {
        if (chattingService.getState() != BluetoothChattingService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {
            String nameAppendedMessage = studentName + getString(R.string.separator) + message;
            byte[] send = nameAppendedMessage.getBytes();
            chattingService.write(send);
            outMessageStringBuffer.setLength(0);
            edtOutMessage.setText(outMessageStringBuffer);
        }
    }

    private void writeMessage(Message msg) {
        String writeMessage = new String((byte[]) msg.obj);
        String realMessage = writeMessage.split(getString(R.string.separator))[1];
        addMessage(getString(R.string.me), realMessage);
    }

    private void readMessage(Message msg) {
        String readMessage = new String((byte[]) msg.obj, 0, msg.arg1);
        String messageName = getString(R.string.teacher);

        int messageFlag = Integer.parseInt(readMessage.split(getString(R.string.separator))[0]);
        if (messageFlag == VIBRATE) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(500);
            messageName = getString(R.string.direct);
        } else if (messageFlag == QUESTION) {
            messageName = getString(R.string.question);
        }

        String realMessage = readMessage.split(getString(R.string.separator))[1];
        if (realMessage.length() > 0) {
            addMessage(messageName, realMessage);
        }
    }

    private void addMessage(String name, String content) {
        messageAdapter.messages.add(new MessageData(name, content));
        messageAdapter.notifyDataSetChanged();
        rvChattingMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
    }

    @SuppressLint("HandlerLeak")
    private final Handler messageHandler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG_ACTIVITY, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    if (msg.arg1 == chattingService.STATE_CONNECTED) {
                        closeConnectingImg();
                        if (willClearAfterReconnect) {
                            messages.clear();
                            messageAdapter.notifyDataSetChanged();
                        }
                        sendAttendClassMessage();
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    writeMessage(msg);
                    break;
                case Constants.MESSAGE_READ:
                    readMessage(msg);
                    break;
                case Constants.MESSAGE_CONNECTION_LOST:
                    //makeToast(msg);
                    closeConnectingImg();
                    showDisconnectedDialog();
                    break;
                case Constants.MESSAGE_LECTURE_SAVE_RESULT:
                    showSaveResult(msg);
            }
        }
    };

/*    private void makeDiscoverable() {
        if (D) Log.d(TAG_ACTIVITY, "make discoverable");
        if (bluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }*/

    /**
     * 종료 관련
     */

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chattingService != null) chattingService.stop();
        if (D) Log.e(TAG_ACTIVITY, "--- ON DESTROY ---");
    }

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

        final Button btnDialogExitConfirm = (Button) dialog.findViewById(R.id.btn_dialog_exit_confirm);
        final Button btnDialogExitCancel = (Button) dialog.findViewById(R.id.btn_dialog_exit_cancel);

        btnDialogExitConfirm.setOnClickListener(view -> {
            dialog.dismiss();
            if (chattingService != null)
                chattingService.stop(); // 해당과정을 수행하지 않으면 connectionLost()가 호출되고 연결끊김 다이얼로그가 떠 오류 발생
            finish();
        });

        btnDialogExitCancel.setOnClickListener(view -> dialog.dismiss());
    }
}