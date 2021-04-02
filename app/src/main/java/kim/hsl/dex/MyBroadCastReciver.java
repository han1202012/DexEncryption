package kim.hsl.dex;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyBroadCastReciver extends BroadcastReceiver {
    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
         /*
            验证 Application 是否替换成功
            打印 Application , ApplicationContext , ApplicationInfo
         */
        Log.i("octopus.MyBroadCastReciver", "reciver:" + context);
        Log.i("octopus.MyBroadCastReciver", "reciver:" + context.getApplicationContext());
        Log.i("octopus.MyBroadCastReciver", "reciver:" + context.getApplicationInfo().className);
    }
}
