package com.dn.young;

import android.app.Application;

/**
 * @ author Michael
 * @ version
 * @ description
 */

public class App extends Application {

    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    /** 获取App实例对象 **/
    public static App getInstance(){
        return instance;
    }
}
