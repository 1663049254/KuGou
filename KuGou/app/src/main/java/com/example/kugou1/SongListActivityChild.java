package com.example.kugou1;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.AbsListView;
import android.widget.SeekBar;
import android.widget.Toast;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import adapter.SongListAdapter;
import encrypt.ba;
import http.Http;
import utils.Json;
import http.SongInfo;
import http.UrlHelper;
import service.ForegroundService;
import service.MediaPlayService;
import utils.DownLoad;

public class SongListActivityChild extends SongListActivity {

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
    static void setLastPlay(boolean isStart){
        if(mCurrentPlay>=firstItem && mCurrentPlay-firstItem<=visibleItem){
            if(isStart)
                lv_songs.getChildAt(mCurrentPlay-firstItem).findViewById(R.id.btn_play).setBackgroundResource(R.drawable.play);
            else
                lv_songs.getChildAt(mCurrentPlay-firstItem).findViewById(R.id.btn_play).setBackgroundResource(R.drawable.pause);
        }
    }
    private static void setAlbumRotate(boolean isRotate){
        if(isRotate)
            img_album.startAnimation(mRotateAnimation);
        else
            img_album.clearAnimation();

    }
    static String getMid(){
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        if(mContext.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE)==PackageManager.PERMISSION_GRANTED){
            return tm.getDeviceId();
        }
        return "";
    }
    static String getKey(String hash,String mid){
        StringBuilder sb=new StringBuilder();
        sb.append(hash);
        sb.append("57ae12eb6890223e355ccfcb74edf70d");
        sb.append(1005);
        sb.append(mid);
        sb.append(0);
        return new ba().a(sb.toString());
    }
    static class PlayBottomClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(MediaPlayService.isPlaying()){
                SongListActivityChild.setPlayButton(false);
                MediaPlayService.pause();
            }
            else{
                SongListActivityChild.setPlayButton(true);
                MediaPlayService.start();
            }
        }
    }
    static void download(final int i){
        final DownLoad downLoad=new DownLoad(mContext);
        final SongInfo songInfo=mSongList.get(i);
        if(songInfo.url==null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    songInfo.key=SongListActivityChild.getKey(songInfo.hash,mid);
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
        }
    }
    static class ListScroll implements AbsListView.OnScrollListener{
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

            if(lastItem==mAdapter.getCount() && scrollState==AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
                //System.out.println("已到底部");
                page++;
                singThreadPool.execute(new Runnable() {
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
    static void play(int i){
        if(i==mCurrentPlay){//当前项
            mCurrentPlay=i;
            if(MediaPlayService.isPlaying()){
                ForegroundService.setPlayButton(false);
                SongListActivityChild.setPlayButton(false);
                MediaPlayService.pause();

            }else{
                ForegroundService.setPlayButton(true);
                SongListActivityChild.setPlayButton(true);
                MediaPlayService.start();
            }
            return;
        }else{//非当前项
            if(mCurrentPlay>-1) {
                //之前的按钮设置为不播放
                if(mCurrentPlay>=firstItem && mCurrentPlay-firstItem<=visibleItem)
                    lv_songs.getChildAt(mCurrentPlay-firstItem).findViewById(R.id.btn_play).setBackgroundResource(R.drawable.pause);
            }
            lv_songs.getChildAt(i-firstItem).findViewById(R.id.btn_play).setBackgroundResource(R.drawable.play);
        }
        mCurrentPlay=i;
        final SongInfo songInfo=mSongList.get(i);

        //获取歌曲地址，播放歌曲的线程
        songInfo.key=SongListActivityChild.getKey(mSongList.get(i).hash,mid);
        //获取歌曲的线程

        singThreadPool.execute(new GetSongUrlThread(songInfo));

        //获取封面的线程
        singThreadPool.execute(new getAlbumUrlThread(songInfo));//使用同一个线程池

    }
    static class GetSongUrlThread implements Runnable{
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
                mContext.startService(mediaPlayServiceIntent);
            }

        }
    }
    static class getAlbumUrlThread implements Runnable{
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
            if(!str.equals("")) {
                songInfo.album_url = Json.parseAlbum(str);
                songInfo.author_img=Json.parseAuthorImage(str);
            }
            else
                songInfo.album_url="";
            //启动前台服务
            foregroundServiceIntent=new Intent(mContext, ForegroundService.class);
            foregroundServiceIntent.putExtra("songinfo",songInfo);
            mContext.startService(foregroundServiceIntent);

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
    static class SeekBarChange implements SeekBar.OnSeekBarChangeListener{
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
    private static Handler handler=new Handler(){
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

}
