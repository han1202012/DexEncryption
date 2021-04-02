package kim.hsl.dex;

import android.app.Application;
import android.util.Log;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        /*
            验证 Application 是否替换成功
            打印 Application , ApplicationContext , ApplicationInfo
         */
        Log.i("octopus.MyApplication", "Application : " + this);
        Log.i("octopus.MyApplication", "ApplicationContext : " + getApplicationContext());
        Log.i("octopus.MyApplication", "ApplicationInfo.className : " + getApplicationInfo().className);
    }
}
