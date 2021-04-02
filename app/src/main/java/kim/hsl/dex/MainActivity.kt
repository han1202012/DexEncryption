package kim.hsl.dex

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
            验证 Application 是否替换成功
            打印 Application , ApplicationContext , ApplicationInfo
         */
        Log.i("octopus.MainActivity", "Application : " + application)
        Log.i("octopus.MainActivity", "ApplicationContext : " + applicationContext)
        Log.i("octopus.MainActivity", "ApplicationInfo.className : " + applicationInfo.className)


        startService(Intent(this, MyService::class.java))

        val intent = Intent("kim.hsl.dex.broadcast")
        intent.component = ComponentName(packageName, MyBroadCastReciver::class.java.name)
        sendBroadcast(intent)

        contentResolver.delete(
            Uri.parse("content://kim.hsl.dex.myprovider"),
            null,
            null
        )



        // Example of a call to a native method
        sample_text.text = stringFromJNI()
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
