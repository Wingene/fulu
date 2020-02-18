package cn.wingene.fulu;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import junze.androidxf.util.NotificationUtil;

/**
 * Created by Wingene on 2019/9/21.
 * 每一半小小时为一个周期
 * 前25分钏为工作，后5分
 * 早上9点到12点， 有6个周期，
 * 前3个周期 25-5，25-5，25-5，后3个周期 5-25,5-25,5-25
 * 下午2点到6点，有 8个周期
 * 前3个周期 25-5，25-5，25-5，后5个周期 5-25,5-25,5-25,5-25,5-25
 */
public class ClockActivity extends Activity {
    private Boolean mWorking;
    private Timer mTimer;
    private PeriodGroup mPeriod;

    private RelativeLayout rlytWindow;
    private TextView tvTime;
    private TextView tvState;

    protected void initComponent() {
        rlytWindow = (RelativeLayout) super.findViewById(R.id.rlyt_window);
        tvTime = (TextView) super.findViewById(R.id.tv_time);
        tvState = (TextView) super.findViewById(R.id.tv_state);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_color);
        initComponent();
        initSoundPool();
        mPeriod = new PeriodGroup();
//                mPeriod.add("17:39", 1, 1, 100, 100);
        mPeriod.add("09:00", 25, 5, 6, 3);
        mPeriod.add("14:00", 25, 5, 8, 3);
        mPeriod.add("20:00", 25, 5, 4);
        startTimer();
    }

    @Override
    protected void onDestroy() {
        stopTimer();
        super.onDestroy();
    }

    private void startTimer() {
        stopTimer();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showTime();
                    }
                });
            }
        }, 0, 500);
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void showTime() {
        PeriodInfo info = mPeriod.getInfo();
        boolean stateChange = false;
        String strTime = "空闲";
        String strState = "";
        int color = R.color.colorComplete;
        if (info != null) {
            strTime = new SimpleDateFormat("mm:ss").format(new Date(info.remain));
            strState = info.working ? "工作中" : "休息中";
            color = info.working ? R.color.colorWork : R.color.colorRest;
            stateChange = mWorking != null && mWorking != info.working;
            mWorking = info.working;
        } else {
            mWorking = false;
        }
        tvTime.setText(strTime);
        tvState.setText(strState);
        final int resColor = getResources().getColor(color);
        rlytWindow.setBackgroundColor(resColor);
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            this.getWindow().setStatusBarColor(resColor);
        }
        //        if(mWorking != null && )
        if (stateChange) {
            showNotify(mWorking);
        }

    }


    private SoundPool mSoundPool;
    private int mSoundId;
    private long lastSoundTime;

    private void initSoundPool() {
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        mSoundId = mSoundPool.load(this, R.raw.water, 1);
    }

    public synchronized void showNotify(boolean work) {
        long now = new Date().getTime();
        if (now - lastSoundTime < 5000) {
            return;
        }
        lastSoundTime = now;
        //        mSoundPool.play(mSoundId, 0.2f, 0.2f, 1, 0, 1);
        //        Vibrator vibrator = (Vibrator) getSystemService(Activity.VIBRATOR_SERVICE);
        //        vibrator.vibrate(1000L);
        if (work) {
            _showNotify(this, work, 0, "该上班了", "上班", "上班中...");
        } else {
            _showNotify(this, work, 0, "该休息了", "休息", "休息中...");
        }

    }


    public void _showNotify(Context context, boolean workchannel, int notifyId, String ticker, String title,
            String text) {
        final NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channel = "work";
        String sign = "上班提醒";
        if (!workchannel) {
            channel = "rest";
            sign = "下班提醒";
        }
        final NotificationCompat.Builder builder = NotificationUtil.createBuilder(context, channel, sign,
                NotificationUtil.IMPORTANCE_HIGH);
        builder.setOngoing(true);
        builder.setTicker(ticker);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setColor(getResources().getColor(R.color.darkgreen));
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.color.darkgreen));
        builder.setSmallIcon(R.mipmap.ic_launcher_foreground);
        builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
        builder.setAutoCancel(true); // 点击消失

        //        Intent tempIntent = new Intent(Intent.ACTION_MAIN);
        //        tempIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        //        tempIntent.setClass(context, ClockActivity.class);
        //        tempIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        //        builder.setContentIntent(PendingIntent.getActivity(context, 0, tempIntent, 0));//
        //        这句和点击消失那句是“Notification
        // 点击消失但不会跳转”的必须条件，如果只有点击消失那句，这个功能是不能实现的
        Notification notification = builder.build();
        manager.notify(notifyId, notification);
    }
}

class PeriodGroup {
    List<Period> mPeriods;

    public PeriodGroup() {
        mPeriods = new ArrayList<>();
    }

    public void add(Period p) {
        mPeriods.add(p);
    }


    public void add(String start, int workMinute, int restMinute, int periodCount) {
        add(start, workMinute, restMinute, periodCount, -1);
    }

    /**
     * @param start
     * @param workMinute
     * @param restMinute
     * @param periodCount
     * @param half        start from 1
     */
    public void add(String start, int workMinute, int restMinute, int periodCount, int half) {
        List<Integer> list = new ArrayList<>(periodCount * 2);
        for (int i = 0; i < periodCount; i++) {
            int index = i + 1;
            if (half == -1 || index < half) {
                list.add(workMinute);
                list.add(-restMinute);
            } else if (index == half) {
                list.add(workMinute);
                if (half != periodCount) {
                    list.add(-restMinute * 2);
                } else {
                    list.add(-restMinute);
                }
            } else if (index == half + 1) {
                list.add(workMinute);
            } else {
                list.add(-restMinute);
                list.add(workMinute);
            }
        }
        this.add(new Period(start, list));
    }

    public PeriodInfo getInfo() {
        for (Period it : mPeriods) {
            final PeriodInfo info = it.getInfo();
            if (info != null) {
                return info;
            }
        }
        return null;
    }
}

class Period {
    private String start;
    //    private int cursor;
    private List<Integer> list;

    /**
     * @param start 09:00
     * @param list
     */
    public Period(String start, List<Integer> list) {
        this.start = start;
        this.list = list;
    }


    private Calendar getStartTime() {
        Calendar calendar = Calendar.getInstance();
        try {
            Date start = new SimpleDateFormat("HH:mm").parse(this.start);
            calendar.set(Calendar.HOUR_OF_DAY, start.getHours());
            calendar.set(Calendar.MINUTE, start.getMinutes());
            calendar.set(Calendar.SECOND, start.getSeconds());
            return calendar;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Calendar getEndTime() {
        Calendar cStart = getStartTime();
        int sum = 0;
        for (Integer i : list) {
            sum += Math.abs(i);
        }
        int mm = cStart.get(Calendar.MINUTE);
        mm += sum;
        //            int HH = mm / 60;
        //            mm = mm % 60;
        //            int dd = HH / 24;
        //            HH = HH % 24;
        cStart.add(Calendar.MINUTE, mm);
        return cStart;
    }


    public PeriodInfo getInfo() {
        Calendar now = Calendar.getInstance();
        if (now.before(getStartTime()) || now.after(getEndTime())) {
            return null;
        }
        Calendar cStart = getStartTime();
        for (Integer i : list) {
            cStart.add(Calendar.MINUTE, Math.abs(i));
            if (cStart.after(now)) {
                return new PeriodInfo(i > 0, cStart.getTime().getTime() - now.getTime().getTime());
            }
        }
        return null;
    }
}

class PeriodInfo {
    public boolean working;
    public long remain; //

    public PeriodInfo(boolean working, long remain) {
        this.working = working;
        this.remain = remain;
    }
}