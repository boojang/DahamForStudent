package team.dahamdol.dahamnote_deafperson.chatting;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import team.dahamdol.dahamnote_deafperson.R;

public class SettingActivity extends AppCompatActivity {

    SeekBar seekBar;
    TextView tvSecond;
    Button btnBack;
    RadioButton rdKorean;
    RadioButton rdEnglish;
    RadioGroup radioGroup;

    String[] showingItems;
    String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        seekBar = findViewById(R.id.recognizer_seekBar);
        tvSecond = findViewById(R.id.tv_recognization_value);
        btnBack = findViewById(R.id.btn_setting_silence_back);

        rdKorean = findViewById(R.id.rd_korean);
        rdEnglish = findViewById(R.id.rd_english);
        rdKorean.setOnClickListener(radioButtonClickListener);
        rdEnglish.setOnClickListener(radioButtonClickListener);
        radioGroup = findViewById(R.id.rd_language_selection);
        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);


        showingItems = getResources().getStringArray(R.array.show_language_array);
        items = getResources().getStringArray(R.array.language_array);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                OnlineActivity.silenceLength = progress * 1000;
                tvSecond.setText(seekBar.getProgress() + " 초");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        btnBack.setOnClickListener(v -> finish());

    }

    //라디오 버튼 클릭 리스너
    RadioButton.OnClickListener radioButtonClickListener = new RadioButton.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

    //라디오 그룹 클릭 리스너
    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            OnlineActivity.selectedLanguageId = i;
            if (i == R.id.rd_korean) {
                OnlineActivity.selectedShowingLanguage = showingItems[0];
                OnlineActivity.selectedLanguage = items[0];
            } else if (i == R.id.rd_english) {
                OnlineActivity.selectedShowingLanguage = showingItems[1];
                OnlineActivity.selectedLanguage = items[1];
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        seekBar.setProgress(OnlineActivity.silenceLength / 1000);
        tvSecond.setText(seekBar.getProgress() + " 초");
        radioGroup.check(OnlineActivity.selectedLanguageId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "음성 인식 언어가 " + OnlineActivity.selectedShowingLanguage + "로 설정되었습니다.", Toast.LENGTH_SHORT).show();
    }
}