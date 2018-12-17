package com.example.kugou1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import http.Http;
import http.SongInfo;
import utils.Json;

public class LyricActivity extends AppCompatActivity {
    private ExecutorService singleThreadPool=Executors.newSingleThreadExecutor();
    private TextView txt_lyric;
    private LinearLayout linear_lyric;
    private String lyric;
    private SongInfo songInfo;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.lyric_layout);
        txt_lyric=(TextView)findViewById(R.id.txt_lyric);
        txt_lyric.setMovementMethod(ScrollingMovementMethod.getInstance());
        linear_lyric=(LinearLayout)findViewById(R.id.linearLayout_lyric);
        Intent intent=getIntent();
        songInfo =intent.getParcelableExtra("songinfo");
        if(songInfo!=null) {
            singleThreadPool.execute(getLyricThread);
            singleThreadPool.execute(getAuthorThread);
        }
    }
    Runnable getLyricThread=new Runnable() {
        @Override
        public void run() {
            String url="http://www.kugou.com/yy/index.php?r=play/getdata&http://www.kugou.com/yy/index.php?r=play/getdata&hash="+songInfo.hash;
            lyric=Http.getMethod("get").doGet(url);
            if(!lyric.equals("")){
                lyric=unicodeToString(lyric);
                lyric=Json.parseLyric(lyric);
                //删除歌词的时间
                lyric=deleteLyricTime(lyric);
                Message msg=new Message();
                msg.what=1;
                handler.sendMessage(msg);
            }
        }
    };
    private String deleteLyricTime(String str){
        String[] arr=str.split("\\r\\n");
        StringBuilder builder=new StringBuilder();
        for(int i=0;i<arr.length;i++){
            builder.append(arr[i].replace(arr[i].substring(0,10),"")+"\r\n");
        }
        return builder.toString();
    }

    Runnable getAuthorThread=new Runnable() {
        @Override
        public void run() {
            //设置作者图片为背景
            try {
                final Bitmap bitmap=BitmapFactory.decodeStream(new URL(songInfo.author_img).openStream());
                //linear_lyric.setBackgroundDrawable(new BitmapDrawable(bitmap));
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        linear_lyric.setBackgroundDrawable(new BitmapDrawable(bitmap));
                    }
                });
                } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    private Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    txt_lyric.setText(lyric);
                    break;
            }
        }
    };
    public static String unicodeToString(String str){
        StringBuilder builder=new StringBuilder();
        builder.append(str);
        int n=0;
        for(int i=0;i<str.length();i++){
            //System.out.println(str.charAt(i));
            if(str.charAt(i)=='\\' && str.charAt(i+1)=='u'){
                int data=Integer.parseInt(str.substring(i+2,i+6),16);
                String str2=Character.toString((char)data);
                builder.replace(i-n,i+6-n,str2);
                n+=5;
            }

        }
        return builder.toString();
    }
}
