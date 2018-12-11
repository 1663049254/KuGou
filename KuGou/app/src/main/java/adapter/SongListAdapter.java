package adapter;

import android.content.Context;
import android.os.Environment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.kugou1.R;

import java.io.File;
import java.util.ArrayList;
import http.SongInfo;
import service.MediaPlayService;

public class SongListAdapter extends BaseAdapter implements View.OnClickListener {
    private static ArrayList<SongInfo> mData;
    private Context mContext;
    private static int mCurrentPlay=-1;
    //private static ViewGroup mParent;
    private CallBack mCallback;
    private String songname;

    //接口回调
    public interface  CallBack{
        public void click(View v);
    }
    //private static ViewGroup getparent(){return mParent;}
    public SongListAdapter(ArrayList<SongInfo> data,Context ctx,String songname,CallBack callBack){
        this.mData=data;
        this.mContext=ctx;
        this.songname=songname;
        this.mCallback=callBack;
    }
    public static void setMdata(ArrayList<SongInfo> data){
        mData=data;
    }
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View convertView,ViewGroup parent) {
        //System.out.println(songname);
        ViewHolder holder;
        if(convertView==null){

            convertView=LayoutInflater.from(mContext).inflate(R.layout.songlist_item_layout,parent,false);
            holder=new ViewHolder();
            //holder.img_isDownload=(ImageView)convertView.findViewById(R.id.img_isDownload);
            holder.txt_songname=(TextView)convertView.findViewById(R.id.txt_songname);
            holder.txt_singer=(TextView)convertView.findViewById(R.id.txt_singer);
            holder.txt_topic=(TextView)convertView.findViewById(R.id.txt_topic);
            holder.btn_play=(ImageButton)convertView.findViewById(R.id.btn_play);
            holder.btn_download=(ImageButton)convertView.findViewById(R.id.btn_download);
            convertView.setTag(holder);

        }else{
            holder=(ViewHolder) convertView.getTag();
        }

        RenderItem(holder,i);
        holder.btn_play.setTag(i);
        holder.btn_play.setOnClickListener(this);
        holder.btn_download.setTag(i);
        holder.btn_download.setOnClickListener(this);

        //System.out.println(i+","+convertView);
        return convertView;
    }
    private void RenderItem(ViewHolder holder,int i){
        //System.out.println("exception i:"+i);
        holder.txt_songname.setText(mData.get(i).songname);
        /*String path="";
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            String root=Environment.getExternalStorageDirectory().toString();
            path=root+"/KGdownload/"+mData.get(i).filename+".mp3";
        }
        if(!path.equals("")){
            File file=new File(path);
            if(file.exists()){
                holder.img_isDownload.setVisibility(View.VISIBLE);
            }
        }*/
        if(!mData.get(i).album_name.equals(""))
            holder.txt_singer.setText(mData.get(i).singername+" -《"+mData.get(i).album_name+"》");
        else
            holder.txt_singer.setText(mData.get(i).singername);

        if(!mData.get(i).topic.equals(""))
            holder.txt_topic.setText(mData.get(i).topic);
        else
            holder.txt_topic.setVisibility(View.GONE);
        if(i==mCurrentPlay && MediaPlayService.isPlaying()){
            holder.btn_play.setBackgroundResource(R.drawable.play);
        }else
            holder.btn_play.setBackgroundResource(R.drawable.pause);

    }
    //响应按钮的点击事件，调用自定义接口，并传入view
    @Override
    public void onClick(View v) {
        //System.out.println(v.getId());
        mCurrentPlay=(int)v.getTag();
        mCallback.click(v);
    }
    class ViewHolder{
        //ImageView img_isDownload;
        TextView txt_songname;
        TextView txt_singer;
        TextView txt_topic;
        ImageButton btn_play;
        ImageButton btn_download;
    }

}
