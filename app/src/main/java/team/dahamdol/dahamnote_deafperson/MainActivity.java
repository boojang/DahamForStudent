package team.dahamdol.dahamnote_deafperson;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import team.dahamdol.dahamnote_deafperson.R;

import team.dahamdol.dahamnote_deafperson.chatting.OfflineActivity;
import team.dahamdol.dahamnote_deafperson.chatting.OnlineActivity;
import team.dahamdol.dahamnote_deafperson.lecture.LectureActivity;

public class MainActivity extends AppCompatActivity {

    private String[] Permissions2 = {Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.NEARBY_WIFI_DEVICES};
    private String[] Permissions = {Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btnMainOffline = findViewById(R.id.btn_main_offline);
        final Button btnMainOnline = findViewById(R.id.btn_main_online);
        final Button btnMainLectureList = findViewById(R.id.btn_main_lecture_list);



        btnMainOffline.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT)!= PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.NEARBY_WIFI_DEVICES)!= PackageManager.PERMISSION_GRANTED){
                Log.d("PERMISSION","++CONNECT_PERMISSION++");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.NEARBY_WIFI_DEVICES},1000);
            }
            else { //권한을 얻음
                Intent intent = new Intent(getApplicationContext(), OfflineActivity.class);
                startActivity(intent);
            }
        });

        btnMainOnline.setOnClickListener(v -> {

            Intent intent = new Intent(getApplicationContext(), OnlineActivity.class);
            startActivity(intent);
        });

        btnMainLectureList.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LectureActivity.class);
            startActivity(intent);
        });
    }
}