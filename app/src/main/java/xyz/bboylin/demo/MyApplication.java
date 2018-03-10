package xyz.bboylin.demo;

import android.app.Application;

/**
 * Created by lin on 2018/3/2.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);
    }
}
