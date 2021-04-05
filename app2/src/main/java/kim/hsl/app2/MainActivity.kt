package kim.hsl.app2

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 测试 ContentProvider
        contentResolver.delete(
            Uri.parse("content://kim.hsl.dex.myprovider"),
            null,
            null
        )
    }
}