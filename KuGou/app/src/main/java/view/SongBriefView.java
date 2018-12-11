package view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.example.kugou1.R;

import android.util.AttributeSet;


public class SongBriefView extends LinearLayout {
    public SongBriefView(Context context, AttributeSet attrs) {
        super(context,attrs);
        LayoutInflater.from(context).inflate(R.layout.song_brief_layout,this);

    }

}
