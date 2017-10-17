package com.ripple.lib.standreminder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ripple.lib.standreminder.databinding.ActivityMainBinding;
import com.ripple.lib.standreminder.utils.DateUtil;
import com.ripple.lib.standreminder.utils.LogUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

/**
 * todo 倒计时进度条
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    //    public static String MSG_WORKING ;
//    public static String MSG_RELAX ;
    private long WORK_SECONDS;
    private long RELAX_SECONDS;
    private int times = 0;//回合

    private static final int STATUS_START = 0;
    private static final int STATUS_PAUSE = -1;
    public static final int STATUS_WORKING = 1;
    public static final int STATUS_RELAXING = 2;

    public final ObservableField<String> taskPeriod = new ObservableField<>("1");
    public final ObservableField<String> relaxPeriod = new ObservableField<>("1");
    public final ObservableField<String> taskWords = new ObservableField<>("WORKING!!\n成为社畜!!!");
    public final ObservableField<String> relaxWords = new ObservableField<>("Have a break~~\n(*^▽^*)");
    public final ObservableBoolean isStatusWorking = new ObservableBoolean(true);

    public static final String TASK_PERIOD = "taskPeriod";
    public static final String RELAS_PERIOD = "relaxPeriod";
    public static final String TASK_WORDS = "taskWords";
    public static final String RELAS_WORDS = "relaxWords";
    public static final String BG_COLOR_WORKING = "BgColorWorking";
    public static final String BG_COLOR_RELAXING = "BgColorRelaxing";

    private SpannableString ss;
    private TextView tvTimer;
    private AbsoluteSizeSpan span;
    private SharedPreferences sp;
    private SharedPreferences spRecorder;
    private Intent intent;

    @IntDef({STATUS_START, STATUS_WORKING, STATUS_RELAXING, STATUS_PAUSE})
    @Retention(RetentionPolicy.SOURCE)
    private @interface status {
    }

    //    public final ObservableField<String> msg = new ObservableField<>();
    public final ObservableField<Integer> bg = new ObservableField<>();

    private Disposable mDisposable;
    private long mRestSecond;//剩余秒数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setActivity(this);

        LogUtil.e(TAG, "onCreate");

        bg.set(getResources().getColor(R.color.orange));
        setStatusColor(getResources().getColor(R.color.orange));

        sp = getSharedPreferences("setting", MODE_PRIVATE);
        spRecorder = getSharedPreferences("recorder", MODE_APPEND);

        span = new AbsoluteSizeSpan(48, true);
        tvTimer = binding.tvTimer;
        tvTimer.setText(getString(R.string.start));

        getScreenSize();

    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.e(TAG, "onResume");

        if (sp.getBoolean("FirstRunning", true)) {//初始化数值
            sp.edit().putString(TASK_WORDS, taskWords.get()).putString(RELAS_WORDS, relaxWords.get())
                    .putString(TASK_PERIOD, String.valueOf(taskPeriod.get())).putString(RELAS_PERIOD, String.valueOf(relaxPeriod.get()))
//                    .putInt(BG_COLOR_WORK,getResources().getColor(R.color.orange))
//                    .putInt(BG_COLOR_RELAX,getResources().getColor(R.color.green))
                    .putBoolean("FirstRunning", false)
                    .apply();
        } else {
//            MSG_WORKING = sp.getString(TASK_WORDS, "");
//            MSG_RELAX = sp.getString(RELAS_WORDS, "");
            taskPeriod.set(sp.getString(TASK_PERIOD, "1"));
            relaxPeriod.set(sp.getString(RELAS_PERIOD, "1"));
            taskWords.set(sp.getString(TASK_WORDS, ""));
            relaxWords.set(sp.getString(RELAS_WORDS, ""));
        }
//        taskWords.set(MSG_WORKING);
//        relaxWords.set(MSG_RELAX);
//        MSG_WORKING=taskWords.get();
//        MSG_RELAX=relaxWords.get();

        WORK_SECONDS = Long.valueOf(taskPeriod.get()) * 60;
        RELAX_SECONDS = Long.valueOf(relaxPeriod.get()) * 60;

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtil.e(TAG, "onNewIntent");
    }

    private void timer(final long second) {
        Observable.interval(1, 1, TimeUnit.SECONDS)
                .take(second)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long v) throws Exception {
                        return second - v;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        mDisposable = d;
                        if (ss != null) {
                            tvTimer.setText(ss);
                        } else {
                            tvTimer.setText(getString(R.string.prepare));
                        }

                        intent = new Intent(MainActivity.this, TimerService.class);
                        if (STATUS == STATUS_WORKING) {
                            times += 1;//开始下一次工作计时了才算一个回合过去了
                            isStatusWorking.set(true);
                            bg.set(getResources().getColor(R.color.orange));
                            setStatusColor(getResources().getColor(R.color.orange));
                        } else if (STATUS == STATUS_RELAXING) {
                            isStatusWorking.set(false);
                            bg.set(getResources().getColor(R.color.green));
                            setStatusColor(getResources().getColor(R.color.green));
                        }
                        intent.putExtra("msg", taskWords.get());
                        intent.putExtra("STATUS", STATUS);
                        intent.putExtra("times", times);
                        startService(intent);
                    }

                    @Override
                    public void onNext(@NonNull Long aLong) {
                        mRestSecond = aLong;
                        if (aLong % 60 == 0) {
                            LogUtil.i(TAG, "count down : aLong=" + aLong + "," + (aLong % 60));
                            ss = new SpannableString(String.format(getString(R.string.timer), aLong / 60));
                            ss.setSpan(span, 0, ss.length() - 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            tvTimer.setText(ss);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        LogUtil.i(TAG, "----------倒计时结束------------times=" + times);
                        if (STATUS == STATUS_RELAXING) {
                            STATUS = STATUS_WORKING;
                            isStatusWorking.set(true);
                            timer(WORK_SECONDS);
                        } else if (STATUS == STATUS_WORKING) {
                            STATUS = STATUS_RELAXING;
                            isStatusWorking.set(false);
                            timer(RELAX_SECONDS);
                        }
                    }
                });
    }


    @status
    private int STATUS = STATUS_START;

    public void click() {
        switch (STATUS) {
            case STATUS_START:// 状态为【尚未开始】，点击开始
                start();
                break;
            case STATUS_PAUSE:// 状态为【暂停】，点击继续
                STATUS = STATUS_WORKING;
                isStatusWorking.set(true);
                times -= 1;
                timer(mRestSecond);
                break;
            case STATUS_WORKING://状态为【工作中】，点击暂停
                STATUS = STATUS_PAUSE;
                tvTimer.setText(getString(R.string.pause));
                mDisposable.dispose();
                break;
            case STATUS_RELAXING://状态为【休息中】，不允许暂停
                STATUS = STATUS_RELAXING;
                break;
        }

    }

    public void exit() {
        finish();
    }

    public void setting() {
//        FrameLayout frameLayout= (FrameLayout) findViewById(R.id.content_frame);
//        if(frameLayout.getVisibility()==View.INVISIBLE){
//            frameLayout.setVisibility(View.VISIBLE);
//        }else{
//            frameLayout.setVisibility(View.INVISIBLE);
//        }
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
//        isShowCustom.set(!isShowCustom.get());
    }

    private void start() {
        spRecorder.edit().putString(DateUtil.getCurrentDateTime(), "start").apply();
        isStatusWorking.set(true);
        STATUS = STATUS_WORKING;
        WORK_SECONDS = Long.valueOf(taskPeriod.get()) * 60;
        RELAX_SECONDS = Long.valueOf(relaxPeriod.get()) * 60;
        timer(WORK_SECONDS);
    }

    public void restart() {
        dispose();
        ss = null;
        times = 0;  //重新计数
        start();
    }

    private void setStatusColor(int color) {
        //状态栏透明，但ToolBar会延伸到状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0及以上
            getWindow().setStatusBarColor(color);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4到5.0. ！！未测试
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
            // 生成一个状态栏大小的矩形
            View statusView = createStatusView(this, color);
            // 添加 statusView 到布局中
            ViewGroup decorView = (ViewGroup) this.getWindow().getDecorView();
            decorView.addView(statusView);
            // 设置根布局的参数
            ViewGroup rootView = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
            rootView.setFitsSystemWindows(true);
            rootView.setClipToPadding(true);
        }
    }

    /**
     * 生成一个和状态栏大小相同的矩形条
     *
     * @param activity 需要设置的activity
     * @param color    状态栏颜色值
     * @return 状态栏矩形条
     */
    private static View createStatusView(Activity activity, int color) {
        // 获得状态栏高度
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);

        // 绘制一个和状态栏一样高的矩形
        View statusView = new View(activity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                statusBarHeight);
        statusView.setLayoutParams(params);
        statusView.setBackgroundColor(color);
        return statusView;
    }

    public void getScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(point);
        } else {
            display.getSize(point);
        }
        int width = point.x;
        int height = point.y;
        LogUtil.i(TAG, "device 的宽高为：width=" + width + ",height=" + height);
        sp.edit().putInt("ScreenWidth", width).putInt("ScreenHeight", height).apply();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtil.e(TAG, "onConfigurationChanged");

    }

    @Override
    public void onBackPressed() {
//        moveTaskToBack(true);
        moveTaskToBack(false);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.e(TAG, "onDestroy");
        spRecorder.edit().putString(DateUtil.getCurrentDateTime(), "destroy").apply();
        dispose();
    }

    private void dispose() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode==KeyEvent.KEYCODE_BACK){
//            Toast.makeText(this,"cannot back",Toast.LENGTH_SHORT).show();
//            return true;
//        }else
//        return super.onKeyDown(keyCode, event);
//    }


}
