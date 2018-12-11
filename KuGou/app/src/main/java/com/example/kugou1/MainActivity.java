package com.example.kugou1;



import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import broadcastReceiver.AudioReceiver;
import broadcastReceiver.ButtonClickReceiver;
import http.Http;
import http.Json;
import http.SongInfo;
import http.UrlHelper;


public class MainActivity extends AppCompatActivity {
    private SearchView mSearchView;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private ListView lv_search;
    private List<String> list;
    private ArrayAdapter<String> adapter;
    private ArrayList<SongInfo> songs=new ArrayList<>();
    private ExecutorService singleThreadPool=Executors.newSingleThreadExecutor();
    private String songname;
    private static Switch switch_circle;
    private BroadcastReceiver clickReceiver;
    private BroadcastReceiver audioReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        lv_search=(ListView)findViewById(R.id.lv_search);
        lv_search.setOnItemClickListener(new searchItemClick());
        switch_circle=(Switch) findViewById(R.id.switch_circle);
        Intent intent=getIntent();
        boolean b=intent.getBooleanExtra("fromSonglistActivity",false);
        if(b){
            this.finish();
        }

        //注册耳机拔出广播
        audioReceiver=new AudioReceiver();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(audioReceiver, intentFilter);
        //注册点击播放广播
        /*try{
            unregisterReceiver(clickReceiver);
        }catch(Exception e){}*/
        clickReceiver=new ButtonClickReceiver();
        IntentFilter intentFilter2=new IntentFilter();
        intentFilter2.addAction("BUTTON_PLAY_CLICK");
        registerReceiver(clickReceiver, intentFilter2);

    }
    class searchItemClick implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //System.out.println(mAdapter.getItem(position));
            songname=adapter.getItem(position);
            executeSearchThread(songname);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_view, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        mSearchView = (SearchView) searchItem.getActionView();
        //mSearchView.setIconified(false);
        mSearchView.setQueryHint("搜索");
        mSearchAutoComplete=(SearchView.SearchAutoComplete)mSearchView.findViewById(R.id.search_src_text);
        mSearchAutoComplete.setHintTextColor(Color.WHITE);
        mSearchAutoComplete.setTextColor(Color.WHITE);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                songname=query;
                executeSearchThread(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                final String keyword=mSearchAutoComplete.getText().toString();
                if(keyword.equals("")) {
                    list=new ArrayList<>();
                    Message msg=new Message();
                    msg.what=1;
                    handler.sendMessage(msg);
                    return false;
                }
                singleThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                       String url=UrlHelper.getKeywordMatchUrl(keyword);
                        String str=Http.getMethod("get").doGet(url);
                        if(!str.equals("")){
                            //System.out.println(str);
                            list=Json.parseKeyword(str);
                            Message msg=new Message();
                            msg.what=1;
                            handler.sendMessage(msg);
                        }

                    }
                });

                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
    private void executeSearchThread(final String songname){
        singleThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                //String url="http://mobilecdn.kugou.com/api/v3/search/song?tag=1&tagtype=%E5%85%A8%E9%83%A8&area_code=1&highlight=em&plat=0&sver=5&api_ver=1&showtype=14&tag_aggr=1&version=9108&keyword="
                        //+URLEncoder.encode(songname)+"&correct=1&page=1&pagesize=30&with_res_tag=1";
                String url= UrlHelper.getSongListUrl(songname,1);
                String str=Http.getMethod("get").doGet(url);
                if(!str.equals("")){
                    songs=Json.parseSong(str);
                    Message msg=new Message();
                    msg.what=2;
                    handler.sendMessage(msg);
                }
            }
        });
    }
    public static boolean getCheckState(){
        return switch_circle.isChecked();
    }
    Handler handler=new Handler(){
        public void handleMessage(Message msg){
            if(msg.what==1){
                adapter=new ArrayAdapter<String>(
                        MainActivity.this,android.R.layout.simple_list_item_1,list);
                lv_search.setAdapter(adapter);
            }
            if(msg.what==2){
                Intent intent=new Intent(MainActivity.this,SongListActivity.class);
                intent.putParcelableArrayListExtra("songlist",songs);
                intent.putExtra("songname",songname);
                startActivity(intent);
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(audioReceiver);
        unregisterReceiver(clickReceiver);
        super.onDestroy();
    }
}
