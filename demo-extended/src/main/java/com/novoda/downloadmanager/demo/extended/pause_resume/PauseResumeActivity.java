package com.novoda.downloadmanager.demo.extended.pause_resume;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.demo.R;
import com.novoda.downloadmanager.demo.extended.BeardDownload;
import com.novoda.downloadmanager.demo.extended.QueryForDownloadsAsyncTask;
import com.novoda.downloadmanager.lib.DownloadManager;
import com.novoda.downloadmanager.notifications.NotificationVisibility;
import com.novoda.downloadmanager.lib.Query;
import com.novoda.downloadmanager.lib.Request;
import com.novoda.downloadmanager.lib.logger.LLog;

import java.util.ArrayList;
import java.util.List;

public class PauseResumeActivity extends AppCompatActivity implements QueryForDownloadsAsyncTask.Callback {
    private static final String BIG_FILE = "http://download.thinkbroadband.com/100MB.zip";
    private static final String BEARD_IMAGE = "http://i.imgur.com/9JL2QVl.jpg";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private DownloadManager downloadManager;
    private PauseResumeAdapter pauseResumeAdapter;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pause_resume);

        emptyView = findViewById(R.id.main_no_downloads_view);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_downloads_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        downloadManager = DownloadManagerBuilder.from(this)
                .build();

        PauseResumeAdapter.Listener clickListener = new PauseResumeAdapter.Listener() {
            @Override
            public void onItemClick(BeardDownload download) {
                if (download.isPaused()) {
                    downloadManager.resumeBatch(download.getBatchId());
                } else {
                    downloadManager.pauseBatch(download.getBatchId());
                }
                queryForDownloads();
            }
        };
        pauseResumeAdapter = new PauseResumeAdapter(new ArrayList<BeardDownload>(), clickListener);
        recyclerView.setAdapter(pauseResumeAdapter);

        findViewById(R.id.single_download_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        enqueueSingleDownload();
                    }
                }
        );

        setupQueryingExample();
    }

    private void setupQueryingExample() {
        queryForDownloads();
    }

    private void queryForDownloads() {
        Query orderedQuery = new Query().orderByLiveness();
        QueryForDownloadsAsyncTask.newInstance(downloadManager, this).execute(orderedQuery);
    }

    private void enqueueSingleDownload() {
        Uri uri = Uri.parse(BIG_FILE);
        final Request request = new Request(uri)
                .setTitle("A Single Beard")
                .setDescription("Fine facial hair")
                .setBigPictureUrl(BEARD_IMAGE)
                .setDestinationInInternalFilesDir(Environment.DIRECTORY_MOVIES, "pause_resume_example.beard")
                .setNotificationVisibility(NotificationVisibility.ACTIVE_OR_COMPLETE)
                .alwaysAttemptResume();

        long requestId = downloadManager.enqueue(request);
        LLog.d("Download enqueued with request ID: " + requestId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getContentResolver().registerContentObserver(downloadManager.getDownloadsWithoutProgressUri(), true, updateSelf);
    }

    private final ContentObserver updateSelf = new ContentObserver(handler) {

        @Override
        public void onChange(boolean selfChange) {
            queryForDownloads();
        }

    };

    @Override
    protected void onStop() {
        super.onStop();
        getContentResolver().unregisterContentObserver(updateSelf);
    }

    @Override
    public void onQueryResult(List<BeardDownload> beardDownloads) {
        pauseResumeAdapter.updateDownloads(beardDownloads);
        emptyView.setVisibility(beardDownloads.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
