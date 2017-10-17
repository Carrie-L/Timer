package com.ripple.lib.standreminder;

import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ripple.lib.standreminder.databinding.ActivitySettingBinding;

import static com.ripple.lib.standreminder.MainActivity.BG_COLOR_RELAXING;
import static com.ripple.lib.standreminder.MainActivity.BG_COLOR_WORKING;
import static com.ripple.lib.standreminder.MainActivity.RELAS_PERIOD;
import static com.ripple.lib.standreminder.MainActivity.RELAS_WORDS;
import static com.ripple.lib.standreminder.MainActivity.TASK_PERIOD;
import static com.ripple.lib.standreminder.MainActivity.TASK_WORDS;

/**
 * Created by Carrie on 2017/9/28.
 * custom setting
 */

public class SettingActivity extends AppCompatActivity {
    public final ObservableField<String> taskPeriod = new ObservableField<>();
    public final ObservableField<String> relaxPeriod = new ObservableField<>();
    public final ObservableField<String> taskWords = new ObservableField<>();
    public final ObservableField<String> relaxWords = new ObservableField<>();
    public final ObservableInt bgTaskColor = new ObservableInt();
    public final ObservableInt bgRelaxColor = new ObservableInt();
    public final ObservableInt textTaskColor = new ObservableInt();
    public final ObservableInt textRelaxColor = new ObservableInt();

    public final Integer BG_COLOR_TASK = 0;
    public final Integer BG_COLOR_RELAX = 1;
    public final Integer TEXT_COLOR_TASK = 2;
    public final Integer TEXT_COLOR_RELAX = 3;

    public final ObservableBoolean isVibrate = new ObservableBoolean(true);
    public final ObservableBoolean isScreenOn = new ObservableBoolean(true);

    private SharedPreferences sp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_setting);
        binding.setModel(this);

        sp = getSharedPreferences("setting", MODE_PRIVATE);
        taskPeriod.set(sp.getString(TASK_PERIOD, "1"));
        relaxPeriod.set(sp.getString(RELAS_PERIOD, "1"));
        taskWords.set(sp.getString(TASK_WORDS, ""));
        relaxWords.set(sp.getString(RELAS_WORDS, ""));
        bgTaskColor.set(sp.getInt(BG_COLOR_WORKING,0));
        bgRelaxColor.set(sp.getInt(BG_COLOR_RELAXING,0));

    }

    public void onColorPalette(int which) {
//        Color
    }

    private void save() {
        sp.edit().putString(TASK_WORDS, taskWords.get()).putString(RELAS_WORDS, relaxWords.get())
                .putString(TASK_PERIOD,  String.valueOf(taskPeriod.get())).putString(RELAS_PERIOD, String.valueOf(relaxPeriod.get()) )
                .apply();
    }

    public void onBack() {
        save();
        finish();
    }

    @Override
    public void onBackPressed() {
        save();
        super.onBackPressed();
    }


    public void onResetAll() {

    }

}
