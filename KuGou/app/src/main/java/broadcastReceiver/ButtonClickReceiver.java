package broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.kugou1.SongListActivity;

import service.ForegroundService;
import service.MediaPlayService;

public class ButtonClickReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context,"receive",Toast.LENGTH_SHORT).show();
        if(MediaPlayService.isPlaying()) {
            ForegroundService.setPlayButton(false);
            MediaPlayService.pause();
            SongListActivity.setPlayButton(false);
        }
        else{
            ForegroundService.setPlayButton(true);
            MediaPlayService.start();
            SongListActivity.setPlayButton(true);
        }

    }
}
