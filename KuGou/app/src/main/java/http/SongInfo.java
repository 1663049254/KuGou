package http;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class SongInfo implements Parcelable {
    public String singername;
    public String topic;
    public String filename;
    public String songname;
    public String hash;
    public String album_name;
    public String album_id;
    public String key;
    public String url;
    public String album_url;
    public String author_img;
    public int album_audio_id;
    public int filesize;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(singername);
        dest.writeString(topic);
        dest.writeString(filename);
        dest.writeString(songname);
        dest.writeString(hash);
        dest.writeString(album_name);
        dest.writeString(album_id);
        dest.writeString(key);
        dest.writeString(url);
        dest.writeString(album_url);
        dest.writeString(author_img);
        dest.writeInt(album_audio_id);
        dest.writeInt(filesize);
    }
    public static final Creator<SongInfo> CREATOR=new Creator<SongInfo>(){

        @Override
        public SongInfo createFromParcel(Parcel source) {
            return new SongInfo(source);
        }

        @Override
        public SongInfo[] newArray(int size) {
            return new SongInfo[size];
        }
    };
    private SongInfo(Parcel in){
        singername=in.readString();
        topic=in.readString();
        filename=in.readString();
        songname=in.readString();
        hash=in.readString();
        album_name=in.readString();
        album_id=in.readString();
        key=in.readString();
        url=in.readString();
        album_url=in.readString();
        author_img=in.readString();
        album_audio_id=in.readInt();
        filesize=in.readInt();
    }
    public SongInfo(){}
}
