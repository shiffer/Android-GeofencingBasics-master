package com.tinmegali.mylocation;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.tinmegali.mylocation.database.InfiDatabase;

/**
 * Created by ohadshiffer
 * on 07/05/2018.
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // init room database
        InfiDatabase.init(this);

        // init stetho
        Stetho.initialize(Stetho.newInitializerBuilder(this)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                .build());
    }
}
