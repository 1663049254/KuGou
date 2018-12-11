package service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.kugou1.MainActivity;
import com.example.kugou1.SongListActivity;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import http.SongInfo;


public class MediaPlayService extends Service {
    //private Context mContext;
    private static MediaPlayer mMp=null;
    private static ExecutorService setProgressThreadPool=Executors.newSingleThreadExecutor();
    /*private static MyMediaPlayer instance=new MyMediaPlayer();
    public static MyMediaPlayer getInstance(){
        return instance;
    }*/

    public MediaPlayService(){}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SongInfo songInfo=intent.getParcelableExtra("song");
        String path=SongListActivity.getRoot()+"/KGdownload/"+songInfo.filename+".mp3";
        if(new File(path).exists()){
            playMp3FromSDCard(path);
        }else {
            if(songInfo.url!=null && !songInfo.url.equals(""))
                playMp3FromUrl(songInfo.url);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void playMp3FromSDCard(String path){playMp3(path);}
    private void playMp3FromUrl(String url){playMp3(url);}
    private void playMp3(String url){
        if(mMp!=null && mMp.isPlaying()){
            mMp.stop();
        }

        try {
            if(mMp!=null)
                mMp.release();
            mMp=new MediaPlayer();
            mMp.setDataSource(url);
            mMp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMp.prepareAsync();
            mMp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    //设置进度条线程
                    setProgressThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            while(!Thread.currentThread().isInterrupted()){
                                try {
                                    if(mMp.isPlaying()){
                                        float f = (mMp.getCurrentPosition() * 1.0f) / (mMp.getDuration() * 1.0f);
                                        int progress = (int) (f * 100);
                                        SongListActivity.setPlayProgress(progress);
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                }
            });

            getmMp().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(MainActivity.getCheckState())
                        mMp.start();
                    else {
                        SongListActivity.setPlayButton(false);
                        ForegroundService.setPlayButton(false);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isPlaying(){
        if(mMp!=null)
            return mMp.isPlaying();
        return false;
    }

    public static void stop(){
        if(mMp!=null)
            mMp.stop();
    }
    public static void pause(){
        if(mMp!=null)
            mMp.pause();
    }
    public static void start(){
        if(mMp!=null)
            mMp.start();
    }
    public static int getDuration_(){
        if(mMp!=null)
            return mMp.getDuration();
        return 0;

    }
    public static void seekTo(int position){
        //System.out.println("seekto:"+position);
        if(mMp!=null && mMp.isPlaying())
            mMp.seekTo((int)(position*0.01f*mMp.getDuration()));
    }

    public MediaPlayer getmMp(){return this.mMp;}
}
