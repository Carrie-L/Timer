package com.ripple.lib.standreminder;

import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.ripple.lib.standreminder.databinding.ActivitySettingBinding;
import com.ripple.lib.standreminder.utils.LogUtil;

public class SettingActivity extends AppCompatActivity {
    public final ObservableField<String> taskPeriod = new ObservableField<>();
    public final ObservableField<String> relaxPeriod = new ObservableField<>();
    public final ObservableField<String> taskWords = new ObservableField<>();
    public final ObservableField<String> relaxWords = new ObservableField<>();

    public static final String TASK_PERIOD = "taskPeriod";
    public static final String RELAS_PERIOD = "relaxPeriod";
    public static final String TASK_WORDS = "taskWords";
    public static final String RELAS_WORDS = "relaxWords";
    private SharedPreferences sp;
    private EditText etTaskPeriod;
    private EditText etRelaxPeriod;
    private EditText etTaskWords;
    private EditText etRelaxWords;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_setting);
        binding.setModel(this);

        sp = getSharedPreferences("setting", MODE_PRIVATE);

        etTaskPeriod = binding.etTaskPeriod;
        etRelaxPeriod = binding.etRelaxPeriod;
        etTaskWords = binding.etTaskWords;
        etRelaxWords = binding.etRelaxWords;

        taskPeriod.set(sp.getString(TASK_PERIOD, "1"));
        relaxPeriod.set(sp.getString(RELAS_PERIOD, "1"));
        taskWords.set(sp.getString(TASK_WORDS, ""));
        relaxWords.set(sp.getString(RELAS_WORDS, ""));

//        if(!taskPeriod.get().isEmpty()){
//            etTaskPeriod.setSelection(taskPeriod.get().length());
//        }
//        etTaskWords.setSelection(taskWords.get().length());
//        etRelaxPeriod.setSelection(relaxPeriod.get().length());
//        etRelaxWords.setSelection(relaxWords.get().length());

        setSelection(etTaskPeriod,taskPeriod);
        setSelection(etTaskWords,taskWords);
        setSelection(etRelaxPeriod,relaxPeriod);
        setSelection(etRelaxWords,relaxWords);

        etTaskPeriod.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                sp.edit().putString(TASK_PERIOD, taskPeriod.get()).apply();
                LogUtil.i("SettingActivity", "TASK_PERIOD=" + sp.getString(TASK_PERIOD, ""));
            }
        });

        etTaskWords.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                sp.edit().putString(TASK_WORDS, taskWords.get()).apply();
                LogUtil.i("SettingActivity", "TASK_WORDS=" + sp.getString(TASK_WORDS, ""));
            }
        });

        etRelaxPeriod.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                sp.edit().putString(RELAS_PERIOD, relaxPeriod.get()).apply();
                LogUtil.i("SettingActivity", "RELAS_PERIOD=" + sp.getString(RELAS_PERIOD, ""));
            }
        });

        etRelaxWords.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                sp.edit().putString(RELAS_WORDS, relaxWords.get()).apply();
                LogUtil.i("SettingActivity", "RELAS_WORDS=" + sp.getString(RELAS_WORDS, ""));
            }
        });

    }

    private void setSelection(EditText editText,ObservableField<String> observableField){
        LogUtil.i("setSelection","observableField="+observableField.get());
//        if(!observableField.get().isEmpty()){
//            editText.setSelection(observableField.get().length());
//        }
    }

}
