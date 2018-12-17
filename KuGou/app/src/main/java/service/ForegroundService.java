package service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.kugou1.LyricActivity;
import com.example.kugou1.MainActivity;
import com.example.kugou1.R;
import com.example.kugou1.SongListActivity;

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import http.SongInfo;

public class ForegroundService extends Service {
    private SongInfo songInfo;
    private ExecutorService threadPool_getAlbum=Executors.newSingleThreadExecutor();
    private static Notification.Builder mBuilder;
    private static RemoteViews remoteViews;
    private static NotificationManager notificationManager;
    private static PendingIntent clickPendingIntent;
    //private Bitmap bitmap;
    public ForegroundService(){}
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mBuilder=new Notification.Builder(this);
        remoteViews=new RemoteViews(getPackageName(),R.layout.notification_layout);
        Intent intent = new Intent(this, LyricActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("","onStartCommand");
        songInfo=intent.getParcelableExtra("songinfo");
        showNotification();
        //return START_REDELIVER_INTENT;
        return super.onStartCommand(intent, flags, startId);
    }
    private void showNotification(){

        Callable c1=new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                Bitmap b = BitmapFactory.decodeStream(new URL(songInfo.album_url).openStream());
                return b;
            }
        };
        if(songInfo.album_url!=null && !songInfo.album_url.equals("")) {
            Future<Bitmap> bitmap = threadPool_getAlbum.submit(c1);
            try {
                remoteViews.setImageViewBitmap(R.id.notification_album, bitmap.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (songInfo.songname != null)
            remoteViews.setTextViewText(R.id.notification_songname, songInfo.songname);
        if (songInfo.singername != null)
            remoteViews.setTextViewText(R.id.notification_singername, songInfo.singername);
        remoteViews.setImageViewResource(R.id.notification_play, R.drawable.play2);
        //点击事件


        //接收广播
        Intent intent2=new Intent();
        intent2.setAction("BUTTON_PLAY_CLICK");
        clickPendingIntent=PendingIntent.getBroadcast(this,0,intent2,0);

        remoteViews.setOnClickPendingIntent(R.id.notification_play,clickPendingIntent);

        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContent(remoteViews);
        notificationManager.notify(0, mBuilder.getNotification());
        startForeground(0, mBuilder.getNotification());

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    public static void setPlayButton(boolean isPlay){
        if(isPlay)
            remoteViews.setImageViewResource(R.id.notification_play,R.drawable.play2);
        else
            remoteViews.setImageViewResource(R.id.notification_play,R.drawable.pause2);
        //mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        //mBuilder.setContent(remoteViews);
        notificationManager.notify(0, mBuilder.getNotification());
    }
}
