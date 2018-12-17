package com.example.kugou1;


import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import broadcastReceiver.AudioReceiver;
import broadcastReceiver.ButtonClickReceiver;
import http.Http;
import utils.Json;
import http.SongInfo;
import http.UrlHelper;
import utils.SearchHistoryDbHelper;


public class MainActivity extends AppCompatActivity {
    private SearchView mSearchView;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private ListView lv_search;
    private List<String> list;
    private List<String> history;
    private ArrayAdapter<String> adapter;
    private ArrayList<SongInfo> songs=new ArrayList<>();
    private ExecutorService fixedThreadPool=Executors.newFixedThreadPool(2);
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
        //注册前台服务点击按钮广播
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
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示搜索历史
                fixedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        history=new SearchHistoryDbHelper(MainActivity.this).queryDatabase();
                        Message msg=new Message();
                        msg.what=3;
                        handler.sendMessage(msg);

                    }
                });
            }
        });
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                history=new ArrayList<>();
                Message msg=new Message();
                msg.what=3;
                handler.sendMessage(msg);
                return false;
            }
        });
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
                fixedThreadPool.execute(new Runnable() {
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
        //保存搜索历史
        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                new SearchHistoryDbHelper(MainActivity.this).insertDatabase(songname);
            }
        });
        //搜索歌曲
        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
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
            if(msg.what==3){
                adapter=new ArrayAdapter<String>(
                        MainActivity.this,android.R.layout.simple_list_item_1,history);
                lv_search.setAdapter(adapter);
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
