package com.canwdev.noise;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.canwdev.noise.util.BaseActivity;

public class SetTimeActivity extends BaseActivity {

    private static final String TAG = "STA##";
    private EditText editText_hour;
    private EditText editText_minute;
    private EditText editText_second;
    private Switch switch_autoExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_time);
        editText_hour = (EditText) findViewById(R.id.editText_hour);
        editText_minute = (EditText) findViewById(R.id.editText_minute);
        editText_second = (EditText) findViewById(R.id.editText_second);
        switch_autoExit = (Switch) findViewById(R.id.switch_autoExit);
        Button buttonOK = (Button) findViewById(R.id.button_ok);

        editText_minute.setFocusable(true);
        editText_minute.setFocusableInTouchMode(true);
        editText_minute.requestFocus();

        buttonOK.setOnClickListener(i -> {
            Intent intent = new Intent();
            intent.putExtra("millisecond", getMillisecond());
            intent.putExtra("autoExit", switch_autoExit.isChecked());
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private long getMillisecond() {
        String sHour = editText_hour.getText().toString();
        String sMinute = editText_minute.getText().toString();
        String sSecond = editText_second.getText().toString();

        if (sHour.isEmpty()) sHour = String.valueOf(0);
        if (sMinute.isEmpty()) sMinute = String.valueOf(0);
        if (sSecond.isEmpty()) sSecond = String.valueOf(0);

        int hour = 0;
        int minute = 0;
        int second = 0;
        try {

            hour = Math.abs(Integer.parseInt(sHour));
            minute = Math.abs(Integer.parseInt(sMinute));
            second = Math.abs(Integer.parseInt(sSecond));
            Log.d(TAG, hour + ":" + minute + ":" + second);

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        long millisecond;
        millisecond = hour * 60 * 60 * 1000 + minute * 60 * 1000 + second * 1000;
        return millisecond;
    }
}
