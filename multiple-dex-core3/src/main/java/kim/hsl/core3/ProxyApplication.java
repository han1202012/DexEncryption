package kim.hsl.core3;

import android.app.Application;
import android.util.Log;

public class ProxyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("ProxyApplication", "ProxyApplication");
    }
}
