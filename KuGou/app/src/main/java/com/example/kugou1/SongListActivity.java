package com.example.kugou1;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import adapter.SongListAdapter;
import broadcastReceiver.AudioReceiver;
import broadcastReceiver.ButtonClickReceiver;
import encrypt.ba;
import http.Http;
import http.Json;
import http.SongInfo;
import http.UrlHelper;
import service.ForegroundService;
import service.MediaPlayService;
import utils.DownLoad;


public class SongListActivity extends AppCompatActivity implements SongListAdapter.CallBack {
    private ImageButton btn_back;
    private static ListView lv_songs;
    private static ImageButton btn_play;
    private static ImageView img_album;
    private static SeekBar seekBar;
    private static int pro=0;
    private static TextView txt_songname_bottom;
    private static TextView txt_singername_bottom;
    private static ImageButton btn_paly_bottom;
    private static Context mContext;
    private static Animation mRotateAnimation;
    private static int mCurrentPlay;
    private static ArrayList<SongInfo> mSongList;
    private String mid;
    private static ExecutorService threadPool_getSong=Executors.newSingleThreadExecutor();
    private static ExecutorService threadPool_getMoreSongs=Executors.newSingleThreadExecutor();
    //private static ExecutorService threadPool_checkDownload=Executors.newSingleThreadExecutor();
    private String songname;
    private String singername;
    private Bitmap bitmap;
    private static SongListAdapter mAdapter;
    private String name;
    private int lastItem;
    private int page;
    private static int firstItem;
    private static int visibleItem;
    //private static int totalItem;
    //private static BroadcastReceiver buttonClickReceiver=new ButtonClickReceiver();
    //private static AudioReceiver audioReceiver=new AudioReceiver();
    //private static IntentFilter intentFilter=new IntentFilter();
    private static Intent mediaPlayServiceIntent;
    private static Intent foregroundServiceIntent;
    private static String root;
    private Intent intent;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.songlist_layout);
        init();
    }
    public static String getRoot(){return root;}
    private void init(){
        intent=getIntent();
        mContext=this;
        mSongList=intent.getParcelableArrayListExtra("songlist");
        page=1;
        mCurrentPlay=-1;
        name=intent.getStringExtra("songname");
        //Toast.makeText(this,name,Toast.LENGTH_LONG).show();

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            root=Environment.getExternalStorageDirectory().toString();
            //path=root+"/KGdownload/"+mData.get(i).filename+".mp3";
        }
        btn_back=(ImageButton)findViewById(R.id.btn_back);
        lv_songs=(ListView)findViewById(R.id.lv_songs) ;
        img_album=(ImageView)findViewById(R.id.img_album);
        seekBar=(SeekBar) findViewById(R.id.seekbar);
        txt_songname_bottom=(TextView)findViewById(R.id.txt_songname_bottom);
        txt_singername_bottom=(TextView)findViewById(R.id.txt_singername_bottom);
        btn_paly_bottom=(ImageButton)findViewById(R.id.btn_play_bottom);
        btn_play=(ImageButton)findViewById(R.id.btn_play);
        mediaPlayServiceIntent=new Intent(SongListActivity.this,MediaPlayService.class);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SongListActivity.this.finish();
            }
        });
        if(mSongList!=null) {
            mAdapter = new SongListAdapter(mSongList, this, name, this);
            lv_songs.setAdapter(mAdapter);
            lv_songs.setOnScrollListener(new ListScroll());


            mid = new ba().k(getMid());
            seekBar.setOnSeekBarChangeListener(new SeekBarChange());
            btn_paly_bottom.setOnClickListener(new PlayBottomClick());

            mRotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            mRotateAnimation.setFillAfter(true);
            mRotateAnimation.setDuration(15000);
            mRotateAnimation.setRepeatCount(-1);
            mRotateAnimation.setInterpolator(new LinearInterpolator());



        }else{
            Intent intent3=new Intent(this,MainActivity.class);
            //intent3.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent3.putExtra("fromSonglistActivity",true);
            startActivity(intent3);
            SongListActivity.this.finish();


        }


    }
    class ListScroll implements AbsListView.OnScrollListener{
       @Override
       public void onScrollStateChanged(AbsListView view, int scrollState) {

           if(lastItem==mAdapter.getCount() && scrollState==AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
               //System.out.println("已到底部");
               page++;
               threadPool_getMoreSongs.execute(new Runnable() {
                   @Override
                   public void run() {
                       if(name==null) return;
                       //System.out.println(name+","+page);
                       String url= UrlHelper.getSongListUrl(name,page);
                       String str=Http.getMethod("get").doGet(url);

                       //System.out.println(str);
                       if(!str.equals("")) {
                           ArrayList<SongInfo> newsonglist = Json.parseSong(str);
                           if(newsonglist.size()>0){
                               mSongList.addAll(newsonglist);
                               SongListAdapter.setMdata(mSongList);
                               Message msg=new Message();
                               msg.what=5;
                               handler.sendMessage(msg);
                           }
                       }
                   }
               });

           }

       }
       @Override
       public void onScroll(AbsListView view, final int firstVisibleItem, final int visibleItemCount, int totalItemCount) {
           //System.out.println(firstVisibleItem+","+visibleItemCount+","+totalItemCount);
           lastItem=firstVisibleItem+visibleItemCount;
           firstItem=firstVisibleItem;
           visibleItem=visibleItemCount;
           String path="";
           for(int i=firstVisibleItem;i<firstVisibleItem+visibleItemCount;i++){
               //System.out.println(i);
               path=root+"/KGdownload/"+mSongList.get(i).filename+".mp3";
               if(new File(path).exists()){
                   lv_songs.getChildAt(i-firstVisibleItem).findViewById(R.id.img_isDownload).setVisibility(View.VISIBLE);
                   //禁用下载按钮
                   lv_songs.getChildAt(i-firstVisibleItem).findViewById(R.id.btn_download).setEnabled(false);
                   lv_songs.getChildAt(i-firstVisibleItem).findViewById(R.id.btn_download).setBackgroundResource(R.drawable.download_forbid);
               }else{
                   lv_songs.getChildAt(i-firstVisibleItem).findViewById(R.id.img_isDownload).setVisibility(View.GONE);
                   lv_songs.getChildAt(i-firstVisibleItem).findViewById(R.id.btn_download).setEnabled(true);
                   lv_songs.getChildAt(i-firstVisibleItem).findViewById(R.id.btn_download).setBackgroundResource(R.drawable.download);
               }
           }

       }
   }


    class SeekBarChange implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //System.out.println(progress);
            pro=progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            MediaPlayService.seekTo(pro);
        }
    }
    @Override
    public void click(View v) {

        if(v.getId()==R.id.btn_play){
            //System.out.println("clickplay");
            play((int)v.getTag());
        }else{
            //System.out.println("clickdownload");
            download((int)v.getTag());
        }

    }
    //播放音乐
    private void play(int i){
        //System.out.println("play :"+i);

        if(i==mCurrentPlay){//当前项
            mCurrentPlay=i;
            //发送广播
            /*Intent intent=new Intent();
            intent.setAction("BUTTON_PLAY_CLICK");
            sendBroadcast(intent);*/
            if(MediaPlayService.isPlaying()){
                ForegroundService.setPlayButton(false);
                setPlayButton(false);
                MediaPlayService.pause();

            }else{
                ForegroundService.setPlayButton(true);
                setPlayButton(true);
                MediaPlayService.start();
            }
            return;
        }else{//非当前项

            if(mCurrentPlay>-1) {
                //System.out.println("设置为不播放："+mCurrentPlay+","+firstItem);
                //setLastPlay(false);//之前的按钮设置为不播放
                if(mCurrentPlay>=firstItem && mCurrentPlay-firstItem<=visibleItem)
                    lv_songs.getChildAt(mCurrentPlay-firstItem).findViewById(R.id.btn_play).setBackgroundResource(R.drawable.pause);
            }
            lv_songs.getChildAt(i-firstItem).findViewById(R.id.btn_play).setBackgroundResource(R.drawable.play);
        }
        mCurrentPlay=i;
        //System.out.println("currentplay1:"+mCurrentPlay);
        final SongInfo songInfo=mSongList.get(i);

        //获取歌曲地址，播放歌曲的线程
        songInfo.key=getKey(mSongList.get(i).hash,mid);
        //获取歌曲的线程
        threadPool_getSong.execute(new GetSongUrlThread(songInfo));

        //获取封面的线程
        threadPool_getSong.execute(new getAlbumUrlThread(songInfo));//使用同一个线程池


    }
    //下载音乐
    private void download(final int i){
        final DownLoad downLoad=new DownLoad(mContext);
        final SongInfo songInfo=mSongList.get(i);
        if(songInfo.url==null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    songInfo.key=getKey(songInfo.hash,mid);
                    String url=UrlHelper.getSongUrl(songInfo,mid);
                    String str=Http.getMethod("get").doGet(url);
                    if(!str.equals(""))
                        songInfo.url=Json.parseSongUrl(str);
                    else
                        songInfo.url="";
                    Looper.prepare();
                    if(songInfo.url.equals("need buy")){
                        Toast.makeText(mContext,"会员专属歌曲不能试听和下载",Toast.LENGTH_LONG).show();
                    }else if(songInfo.url.equals("no copyright")) {
                        Toast.makeText(mContext,"该歌曲无版权",Toast.LENGTH_LONG).show();
                    }else if(songInfo.url.equals("")){
                        Toast.makeText(mContext,"无网络",Toast.LENGTH_LONG).show();
                    } else {
                        if(downLoad.checkPermission()) {
                            downLoad.download(songInfo.url, songInfo.filename);
                            Toast.makeText(mContext,"已添加到下载列表",Toast.LENGTH_LONG).show();
                        } else{
                            Toast.makeText(mContext,"请开启存储文件权限",Toast.LENGTH_LONG).show();
                        }
                    }
                    Looper.loop();

                }

            }).start();

        }else{
            if(!songInfo.url.equals("")){
                if(downLoad.checkPermission()) {
                    if(songInfo.url.equals("need buy")){
                        Toast.makeText(mContext,"会员专属歌曲不能试听和下载",Toast.LENGTH_LONG).show();
                    }else if(songInfo.url.equals("no copyright")) {
                        Toast.makeText(mContext,"该歌曲无版权",Toast.LENGTH_LONG).show();
                    } else {
                        downLoad.download(songInfo.url,songInfo.filename);
                        Toast.makeText(mContext,"已添加到下载列表",Toast.LENGTH_LONG).show();
                    }

                }
                else{
                    Toast.makeText(mContext,"请开启存储文件权限",Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(mContext,"无网络",Toast.LENGTH_LONG).show();
            }

            //Toast.makeText(mContext,"已添加到下载列表",Toast.LENGTH_LONG).show();
        }
    }
    class GetSongUrlThread implements Runnable{
        private SongInfo songInfo;
        public GetSongUrlThread(SongInfo songInfo){
            this.songInfo=songInfo;
        }
        @Override
        public void run() {
            String url=UrlHelper.getSongUrl(songInfo,mid);
            String str=Http.getMethod("get").doGet(url);
            if(!str.equals(""))
                songInfo.url=Json.parseSongUrl(str);
            else
                songInfo.url="";
            //System.out.println(songInfo.url);
            songname=songInfo.songname;
            singername=songInfo.singername;

            if(songInfo.url.equals("need buy")){
                Message msg=new Message();
                msg.what=6;
                handler.sendMessage(msg);
                //Toast.makeText(mContext,"会员专属歌曲不能试听和下载",Toast.LENGTH_LONG).show();
            }else if(songInfo.url.equals("no copyright")) {
                //Toast.makeText(mContext, "该歌曲无版权", Toast.LENGTH_LONG).show();
                Message msg=new Message();
                msg.what=7;
                handler.sendMessage(msg);
            } else {
                Message msg=new Message();
                msg.what=4;
                handler.sendMessage(msg);
                //启动后台播放音乐服务
                mediaPlayServiceIntent.putExtra("song",songInfo);
                startService(mediaPlayServiceIntent);

            }

        }
    }
    class getAlbumUrlThread implements Runnable{
        private SongInfo songInfo;
        public getAlbumUrlThread(SongInfo songInfo){
            this.songInfo=songInfo;
        }
        @Override
        public void run() {

            String temp="";
            if(!songInfo.album_id.equals("")){
                temp="\"album_id\":"+songInfo.album_id+",";
            }
            String temp2=songInfo.filename;
            if(temp2.contains("\""))
                temp2=temp2.replace("\"","'");
            String param="{\"album_image_type\":\"-3\",\"clienttime\":\"" +
                    System.currentTimeMillis()+
                    "\",\"author_image_type\":\"4,5\",\"clientver\":\"9108\",\"data\":[" +
                    "{"+temp+
                    "\"filename\":\""+temp2+"\"," +
                    "\"hash\":\""+songInfo.hash+"\"," +
                    "\"album_audio_id\":"+ songInfo.album_audio_id+
                    "}],\"appid\":\"1005\"," +
                    "\"mid\":\""+mid+"\"," +
                    "\"key\":\""+songInfo.key+"\"}";
            //System.out.println(param);
            String str=Http.getMethod("post").doPost("http://kmr.service.kugou.com/container/v1/image",param);
            if(!str.equals(""))
                songInfo.album_url= Json.parseAlbum(str);
            else
                songInfo.album_url="";
            //启动前台服务
            foregroundServiceIntent=new Intent(SongListActivity.this,ForegroundService.class);
            foregroundServiceIntent.putExtra("songinfo",songInfo);
            startService(foregroundServiceIntent);

            if(!songInfo.album_url.equals("")){
                try {
                    bitmap=BitmapFactory.decodeStream(new URL(songInfo.album_url).openStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Message msg=new Message();
                msg.what=3;
                handler.sendMessage(msg);
            }

        }
    }

    private String getMid(){
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if(mContext.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE)==PackageManager.PERMISSION_GRANTED){
            return tm.getDeviceId();
        }
        return "";
    }
    private String getKey(String hash,String mid){
        StringBuilder sb=new StringBuilder();
        sb.append(hash);
        sb.append("57ae12eb6890223e355ccfcb74edf70d");
        sb.append(1005);
        sb.append(mid);
        sb.append(0);
        return new ba().a(sb.toString());
    }
    private static void setLastPlay(boolean isStart){
        if(mCurrentPlay>=firstItem && mCurrentPlay-firstItem<=visibleItem){
            if(isStart)
                lv_songs.getChildAt(mCurrentPlay-firstItem).findViewById(R.id.btn_play).setBackgroundResource(R.drawable.play);
            else
                lv_songs.getChildAt(mCurrentPlay-firstItem).findViewById(R.id.btn_play).setBackgroundResource(R.drawable.pause);
        }

    }
    class PlayBottomClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(MediaPlayService.isPlaying()){
                setPlayButton(false);
                MediaPlayService.pause();
            }
            else{
                setPlayButton(true);
                MediaPlayService.start();
            }
        }
    }

    public static void setPlayProgress(int progress){
        seekBar.setProgress(progress);
    }
    /*public static void setAlbum(Bitmap bitmap){
        img_album.setImageBitmap(bitmap);
    }*/
    public static void setSongnameBottomText(String str){txt_songname_bottom.setText(str);}
    public static void setSingernameBottomText(String str){txt_singername_bottom.setText(str);}
    private static void setAlbumRotate(boolean isRotate){
        if(isRotate)
            img_album.startAnimation(mRotateAnimation);
        else
            img_album.clearAnimation();

    }

    public static void setPlayButton(boolean isPlay){
        if(isPlay) {
            setAlbumRotate(true);
            btn_paly_bottom.setBackgroundResource(R.drawable.play2);
            setLastPlay(true);
        } else {
            setAlbumRotate(false);
            btn_paly_bottom.setBackgroundResource(R.drawable.pause2);
            setLastPlay(false);
        }

    }
    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 3:
                    //setAlbum(bitmap);
                    img_album.setImageBitmap(bitmap);
                    break;
                case 4:
                    setSongnameBottomText(songname);
                    setSingernameBottomText(singername);
                    setPlayButton(true);
                    break;
                case 5:
                    mAdapter.notifyDataSetChanged();
                    break;
                case 6:
                    Toast.makeText(mContext,"会员专属歌曲不能试听和下载",Toast.LENGTH_LONG).show();
                    setLastPlay(false);
                    break;
                case 7:
                    Toast.makeText(mContext,"该歌曲无版权",Toast.LENGTH_LONG).show();
                    setLastPlay(false);
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        System.out.println("ondestroy");
        try {
            //unregisterReceiver(audioReceiver);
            stopService(mediaPlayServiceIntent);
            stopService(foregroundServiceIntent);
        }catch(Exception e){
        }
        super.onDestroy();
    }
}
