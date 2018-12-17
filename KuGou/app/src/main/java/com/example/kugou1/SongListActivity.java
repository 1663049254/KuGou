package com.example.kugou1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import adapter.SongListAdapter;
import encrypt.ba;
import http.Http;
import http.SongInfo;
import service.MediaPlayService;

public class SongListActivity extends AppCompatActivity implements SongListAdapter.CallBack {
    protected static ImageButton btn_back;
    protected static ListView lv_songs;
    protected static ImageButton btn_play;
    protected static ImageView img_album;
    protected static SeekBar seekBar;
    protected static int pro=0;
    protected static TextView txt_songname_bottom;
    protected static TextView txt_singername_bottom;
    protected static ImageButton btn_paly_bottom;
    protected static Context mContext;
    protected static Animation mRotateAnimation;
    protected static int mCurrentPlay;
    protected static ArrayList<SongInfo> mSongList;
    protected static String mid;
    protected static ExecutorService singThreadPool=Executors.newSingleThreadExecutor();
    //protected static ExecutorService threadPool_getMoreSongs=Executors.newSingleThreadExecutor();
    protected static String songname;
    protected static String singername;
    protected static Bitmap bitmap;
    protected static SongListAdapter mAdapter;
    protected static String name;
    protected static int lastItem;
    protected static int page;
    protected static int firstItem;
    protected static int visibleItem;
    protected static Intent mediaPlayServiceIntent;
    protected static Intent foregroundServiceIntent;
    protected static String root;
    protected static Intent intent;
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

        btn_back.setOnClickListener(new BackClick());
        img_album.setOnClickListener(new LyricClick());
        if(mSongList!=null) {
            mAdapter = new SongListAdapter(mSongList, this, name, this);
            lv_songs.setAdapter(mAdapter);
            lv_songs.setOnScrollListener(new SongListActivityChild.ListScroll());
            mid = new ba().k(SongListActivityChild.getMid());
            seekBar.setOnSeekBarChangeListener(new SongListActivityChild.SeekBarChange());
            btn_paly_bottom.setOnClickListener(new SongListActivityChild.PlayBottomClick());

            mRotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            mRotateAnimation.setFillAfter(true);
            mRotateAnimation.setDuration(15000);
            mRotateAnimation.setRepeatCount(-1);
            mRotateAnimation.setInterpolator(new LinearInterpolator());

        }else{
            /*Intent intent3=new Intent(this,MainActivity.class);
            //intent3.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent3.putExtra("fromSonglistActivity",true);
            startActivity(intent3);
            SongListActivity.this.finish();*/
        }
    }
    class LyricClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            //跳转到歌词activity
            Intent intent=new Intent(SongListActivity.this,LyricActivity.class);
            if(mCurrentPlay>-1){
                if(mSongList.get(mCurrentPlay)!=null){
                    intent.putExtra("songinfo",mSongList.get(mCurrentPlay));
                    startActivity(intent);
                }
            }
        }
    }

    class BackClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            SongListActivity.this.finish();
        }
    }

    @Override
    public void click(View v) {

        if(v.getId()==R.id.btn_play){
            //System.out.println("clickplay");
            SongListActivityChild.play((int)v.getTag());
        }else{
            //System.out.println("clickdownload");
            SongListActivityChild.download((int)v.getTag());
        }

    }

    public static void setPlayProgress(int progress){
        seekBar.setProgress(progress);
    }
    public static void setSongnameBottomText(String str){txt_songname_bottom.setText(str);}
    public static void setSingernameBottomText(String str){txt_singername_bottom.setText(str);}

    @Override
    protected void onDestroy() {
        System.out.println("ondestroy");
        try {
            stopService(mediaPlayServiceIntent);
            stopService(foregroundServiceIntent);
        }catch(Exception e){
        }
        super.onDestroy();
    }
}
