package team.dahamdol.dahamnote_deafperson.lecture;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import team.dahamdol.dahamnote_deafperson.R;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class LectureSaveDialog {
    private Context context;
    private LectureSaveDialogListener lectureSaveDialogListener;
    int mYear, mMonth, mDay;

    private EditText edtLectureName;
    private DatePicker dpLectureDate;
    private Button btnLectureAddConfirm;
    private Button btnLectureAddCancel;

    public LectureSaveDialog(Context context) {
        this.context = context;
    }

    //다이얼로그를 호출한 액티비티로 값을 전달하기 위한 리스너 인터페이스 선언
    public interface LectureSaveDialogListener {
        void onConfirmClick(String lectureName, String lectureDate);
    }

    //다이얼로그 리스너 초기화
    public void setDialogListener(LectureSaveDialogListener lectureSaveDialogListener) {
        this.lectureSaveDialogListener = lectureSaveDialogListener;
    }

    //액티비티에서 호출할 함수
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void show() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_save);

        //Display 사이즈의 90%
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);
        int width = (int) (point.x * 0.9);
        dialog.getWindow().getAttributes().width = width;
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        edtLectureName = (EditText) dialog.findViewById(R.id.edt_lecture_name);
        dpLectureDate = (DatePicker) dialog.findViewById(R.id.dp_lecture_date);
        btnLectureAddConfirm = (Button) dialog.findViewById(R.id.btn_lecture_add_confirm);
        btnLectureAddCancel = (Button) dialog.findViewById(R.id.btn_lecture_add_cancel);

        setCurrentDate();
        setDatePicker();
        //setDateDiolog(tvLectureDate);

        btnLectureAddConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lectureName = edtLectureName.getText().toString();
                String lectureDate = mYear + "/" + (mMonth + 1) + "/" + mDay;
                if (lectureName.replace(" ", "").length() == 0) {
                    Toast.makeText(context, R.string.lecture_name_required, Toast.LENGTH_LONG).show();
                } else {
                    lectureSaveDialogListener.onConfirmClick(lectureName, lectureDate);
                    dialog.dismiss();
                }
            }
        });

        btnLectureAddCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void setDatePicker() {
        DatePicker.OnDateChangedListener listener = new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mYear = year;
                mMonth = monthOfYear; // 1월 == 0
                mDay = dayOfMonth;
            }
        };

        dpLectureDate.init(mYear, mMonth, mDay, listener);
    }

/*    //날짜 대화상자 리스너 설정
    public void setDateDiolog(TextView textView){

        DatePickerDialog.OnDateSetListener mDateSetListener =
                (view, year, monthOfYear, dayOfMonth) -> {
                    mYear = year;
                    mMonth = monthOfYear+1;
                    mDay = dayOfMonth;
                    textView.setText(mYear + "년 " + mMonth + "월 " + mDay + "일");
                };

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(context, mDateSetListener, mYear, mMonth-1, mDay).show();
            }
        });
    }*/

    //현재 날짜 가져오기
    public void setCurrentDate() {
        //현재 날짜와 시간을 가져오기위한 Calendar 인스턴스 선언
        Calendar cal = Calendar.getInstance();
        mYear = cal.get(Calendar.YEAR);
        mMonth = cal.get(Calendar.MONTH); // 1월 == 0
        mDay = cal.get(Calendar.DAY_OF_MONTH);
    }

}
