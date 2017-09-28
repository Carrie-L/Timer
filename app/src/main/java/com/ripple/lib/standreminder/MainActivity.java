package com.ripple.lib.standreminder;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TextAppearanceSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ripple.lib.standreminder.databinding.ActivityMainBinding;
import com.ripple.lib.standreminder.utils.LogUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

import static android.R.attr.fragment;
import static com.ripple.lib.standreminder.R.string.timer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private final String MSG_WORKING = "WORKING!!";
    private final String MSG_RELAX = "Have a relax~~\n(*^▽^*)";
    final long WORK_SECONDS = 5 * 60;
    final long RELAX_SECONDS = 1 * 60;

    private static final int STATUS_START = 0;
    private static final int STATUS_RUNNING = 1;
    private static final int STATUS_PAUSE = -1;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private SpannableString ss;
    private TextView tvTimer;
    private AbsoluteSizeSpan span;
    private String mSountPath;

    @IntDef({STATUS_START, STATUS_RUNNING, STATUS_PAUSE})
    @Retention(RetentionPolicy.SOURCE)
    private @interface status {
    }

    public final ObservableField<String> msg = new ObservableField<>();
    public final ObservableField<Integer> bg = new ObservableField<>();

    private Disposable mDisposable;
    private long mRestSecond;//剩余秒数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setModel(this);

        bg.set(getResources().getColor(R.color.orange));
        setStatusColor(getResources().getColor(R.color.orange));



        span = new AbsoluteSizeSpan(48, true);
        tvTimer = binding.tvTimer;
        tvTimer.setText(getString(R.string.start));

        initNotification();

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .add(R.id.content_frame, new SettingFragment()).commit();

    }

    private void timer(final long second) {
        Observable.interval(1, 1, TimeUnit.SECONDS)
                .take(second)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long v) throws Exception {
                        LogUtil.i(TAG, "apply : v=" + v);
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
                    }

                    @Override
                    public void onNext(@NonNull Long aLong) {
                        LogUtil.i(TAG, "count down : aLong=" + aLong);
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
                        LogUtil.i(TAG, "----------倒计时结束------------");

                        if (second == RELAX_SECONDS) {
                            bg.set(getResources().getColor(R.color.orange));
                            setStatusColor(getResources().getColor(R.color.orange));
                            mBuilder.setContentText(MSG_WORKING)
                                    .setSmallIcon(R.drawable.ic_busy)
                                    .setTicker(MSG_WORKING);
                            msg.set(MSG_WORKING);
                            timer(WORK_SECONDS);
                        } else {
                            bg.set(getResources().getColor(R.color.green));
                            setStatusColor(getResources().getColor(R.color.green));
                            mBuilder.setContentText(MSG_RELAX)
                                    .setSmallIcon(R.drawable.ic_relax)
                                    .setTicker(MSG_RELAX);
                            msg.set(MSG_RELAX);
                            timer(RELAX_SECONDS);
                        }
                        mNotificationManager.notify(1, mBuilder.build());


                    }
                });
    }

    @status
    private int status = STATUS_START;

    public void click() {
        switch (status) {
            case STATUS_START:// 状态为【尚未开始】，点击开始
                status = STATUS_RUNNING;
                msg.set(MSG_WORKING);
                timer(WORK_SECONDS);
                break;
            case STATUS_PAUSE:// 状态为【暂停】，点击继续
                status = STATUS_RUNNING;
                timer(mRestSecond);
                break;
            case STATUS_RUNNING://状态为【计时中】，点击暂停
                status = STATUS_PAUSE;
                tvTimer.setText(getString(R.string.pause));
                mDisposable.dispose();
                break;
        }

    }

    public void exit() {
        finish();
    }

    public void setting() {
        FrameLayout frameLayout= (FrameLayout) findViewById(R.id.content_frame);
        if(frameLayout.getVisibility()==View.INVISIBLE){
            frameLayout.setVisibility(View.VISIBLE);
        }else{
            frameLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void initNotification() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(MainActivity.this)
                .setSmallIcon(R.drawable.ic_busy)
                .setContentTitle(getString(R.string.app_name));
        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        mSountPath = "android.resource://com.ripple.lib.standreminder/";
        mBuilder.setSound(Uri.parse(mSountPath + R.raw.mps));
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

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.e(TAG, "onDestroy");
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

    //监听home键,无法阻止Home键的退出
//    @Override
//    protected void onPause() {
//        if(isApplicationSentToBackground(this)){
//            Toast.makeText(this,"cannot home",Toast.LENGTH_SHORT).show();
//
//        }
//        super.onPause();
//    }
//
//    public boolean isApplicationSentToBackground(final Context context) {
//        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
//        if (!tasks.isEmpty()) {
//            ComponentName topActivity = tasks.get(0).topActivity;
//            if (!topActivity.getPackageName().equals(context.getPackageName())) {
//                return true;
//            }
//        }
//        return false;
//    }

}
