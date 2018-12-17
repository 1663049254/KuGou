package broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.kugou1.SongListActivity;
import com.example.kugou1.SongListActivityChild;

import service.ForegroundService;
import service.MediaPlayService;

public class AudioReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context,"耳机拔出",Toast.LENGTH_LONG).show();
        if(MediaPlayService.isPlaying()){
            MediaPlayService.pause();
            SongListActivityChild.setPlayButton(false);
            ForegroundService.setPlayButton(false);
        }

    }
}
