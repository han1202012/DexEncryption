package kim.hsl.multipledex;

import android.app.Application;
import android.content.Context;

public class ProxyApplication extends Application {

    /**
     * 在 Application 在 ActivityThread 中被创建之后,
     * 一个调用的方法是 attachBaseContext 函数.
     * 该函数是 Application 中最先执行的函数.
     * @param base
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        /*
            在该 Application 中主要进行两个操作 :
            1 . 解密并加载多个 DEX 文件
            2 . 将真实的 Application 替换成应用的主 Application
         */

        // I . 解密与加载多 DEX 文件

        /*
            先进行解密, 然后再加载解密之后的 DEX 文件
         */

    }
}
