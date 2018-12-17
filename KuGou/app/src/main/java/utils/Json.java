package utils;



import android.os.Looper;

import com.example.kugou1.MainActivity;
import com.example.kugou1.SongListActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

import http.SongInfo;

public class Json {
    public static List<String> parseKeyword(String str){
        str=str.substring(23,str.length()-21);
        List<String> list=new ArrayList<>();

        //System.out.println(str);

        try {
            JsonParser parser = new JsonParser();
            JsonObject object1 = (JsonObject) parser.parse(str);
            //System.out.println(json.get("recordcount").getAsInt());
            JsonArray array = object1.get("data").getAsJsonArray();
            for(int i=0;i<array.size();i++){
                JsonObject object2=array.get(i).getAsJsonObject();
                //int a=object2.get("songcount").getAsInt();
                //int b=object2.get("searchcount").getAsInt();
                String keyword=object2.get("keyword").getAsString();
                list.add(keyword);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return list;
    }
    public static ArrayList<SongInfo> parseSong(String str){
        str=str.substring(23,str.length()-21);
        ArrayList<SongInfo> list=new ArrayList<>();
        JsonParser parser=new JsonParser();
        JsonObject object1= (JsonObject) parser.parse(str);
        JsonObject object2=object1.get("data").getAsJsonObject();
        JsonArray array = object2.get("info").getAsJsonArray();

        for(int i=0;i<array.size();i++){
            SongInfo songInfo=new SongInfo();
            songInfo.singername=array.get(i).getAsJsonObject().get("singername").getAsString();
            songInfo.singername=replaceEm(songInfo.singername);
            songInfo.topic=array.get(i).getAsJsonObject().get("topic").getAsString();
            songInfo.topic=replaceEm(songInfo.topic);
            songInfo.filename=array.get(i).getAsJsonObject().get("filename").getAsString();
            songInfo.filename=replaceEm(songInfo.filename);
            songInfo.songname=array.get(i).getAsJsonObject().get("songname").getAsString();
            songInfo.songname=replaceEm(songInfo.songname);
            songInfo.hash=array.get(i).getAsJsonObject().get("hash").getAsString();
            songInfo.album_audio_id=array.get(i).getAsJsonObject().get("album_audio_id").getAsInt();
            songInfo.album_id=array.get(i).getAsJsonObject().get("album_id").getAsString();
            songInfo.album_name=array.get(i).getAsJsonObject().get("album_name").getAsString();
            songInfo.album_name=replaceEm(songInfo.album_name);
            songInfo.filesize=array.get(i).getAsJsonObject().get("320filesize").getAsInt();
            list.add(songInfo);
        }
        return list;
    }
    private static String replaceEm(String str){
        String newStr="";
        newStr=str.replace("<em>","");
        newStr=newStr.replace("</em>","");
        return  newStr;
    }
    public static String parseSongUrl(String str){
        //System.out.println(str);

        if (str.contains("\"buy\"]}")){
            return "need buy";
        }
        if(str.contains("{\"status\":3}"))
            return "no copyright";

        str=str.substring(23,str.length()-21);
        //System.out.println(str);
        JsonParser parser=new JsonParser();

        JsonObject object = (JsonObject) parser.parse(str);
        JsonArray array = object.get("url").getAsJsonArray();
        if(array.size()>0){
            return array.get(0).getAsString();
        }

        return "";

    }
    public static String parseAlbum(String str){
        //System.out.println(str);
        if(str.contains("{\"status\":0,\""))
            return "";
        JsonParser parser=new JsonParser();
        JsonObject object=(JsonObject) parser.parse(str);
        JsonArray array = object.get("data").getAsJsonArray();
        if(array.size()>0){
            String url="";
            try{
                url=array.get(0).getAsJsonObject().get("album").getAsJsonArray().get(0).getAsJsonObject().get("sizable_cover").getAsString();
            }catch (Exception e){
                e.printStackTrace();
            }

            if(url.equals("")){
                try {
                    url = array.get(0).getAsJsonObject().get("author").getAsJsonArray().get(0).getAsJsonObject().get("sizable_avatar").getAsString();
                }catch(Exception e){
                    e.printStackTrace();
                    return "";
                }
            }
            return url.replace("{size}","480");
        }
        return "";
    }
    public static String parseLyric(String str){
        JsonParser parser=new JsonParser();
        JsonObject object=(JsonObject) parser.parse(str);
        return object.get("data").getAsJsonObject().get("lyrics").getAsString();
    }
    public static String parseAuthorImage(String str){
        JsonParser parser=new JsonParser();
        JsonObject object=(JsonObject) parser.parse(str);
        try {
            return object.get("data").getAsJsonArray().get(0).getAsJsonObject().get("author").getAsJsonArray()
                    .get(0).getAsJsonObject().get("imgs").getAsJsonObject().get("4").getAsJsonArray()
                    .get(0).getAsJsonObject().get("sizable_portrait").getAsString();
        }catch (Exception e){
            return "";
        }
    }
}
