package com.novoda.downloadmanager.demo.extended;

import android.app.Application;
import android.hardware.SensorManager;

import com.facebook.stetho.Stetho;
import com.novoda.downloadmanager.lib.DownloadBatch;
import com.novoda.downloadmanager.lib.DownloadClientReadyChecker;

public class DemoApplication extends Application implements DownloadClientReadyChecker {

    private OneRuleToBindThem oneRuleToBindThem;

    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build()
        );

        oneRuleToBindThem = new OneRuleToBindThem();
    }

    @Override
    public boolean isAllowedToDownload(DownloadBatch downloadBatch) {
        // Here you would add any reasons you may not want to download
        // For instance if you have some type of geo-location lock on your download capability
        return oneRuleToBindThem.shouldWeDownload(downloadBatch);
    }

    private static final class OneRuleToBindThem {

        /**
         * @return for our demo we expect always to return true ... unless you want to conquer the galaxy
         */
        public boolean shouldWeDownload(DownloadBatch downloadBatch) {
            return downloadBatch.getTotalSize() > SensorManager.GRAVITY_DEATH_STAR_I;
        }
    }
}
