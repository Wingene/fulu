package cn.wingene.fulu;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Wingene on 2020/2/17.
 */
public class ClockService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    public static void start(PeriodGroup group){

    }

//    public static void startService(Context context, PeriodGroup group) {
//        Intent in = new Intent(context, ClockService.class);
//        in.putExtra(PARAMS_MSG, Gsons.toJson(new PushLoginInfo(userId, token, pushMsgUrl, uploadUrl)));
//        context.startService(in);
//    }
}
