package kim.hsl.dex;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*
            验证 Application 是否替换成功
            打印 Application , ApplicationContext , ApplicationInfo
         */
        Log.i("octopus.MyService", "Application : " + getApplication());
        Log.i("octopus.MyService", "ApplicationContext : " + getApplicationContext());
        Log.i("octopus.MyService", "ApplicationInfo.className : " + getApplicationInfo().className);
    }
}
