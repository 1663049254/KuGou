package http;

import java.net.URLEncoder;

public class UrlHelper {
    public static String getSongListUrl(String songname,int page){

        return  "http://mobilecdn.kugou.com/api/v3/search/song?tag=1&tagtype=%E5%85%A8%E9%83%A8&area_code=1&highlight=em&plat=0&sver=5&api_ver=1&showtype=14&tag_aggr=1&version=9108&keyword="
                +URLEncoder.encode(songname)+"&correct=1&page="+page+"&pagesize=30&with_res_tag=1";
    }
    public static String getKeywordMatchUrl(String keyword){
        String url="";
        try {
            url="http://mobilecdn.kugou.com/new/app/i/search.php?student=0&cmd=302&keyword="
                    + URLEncoder.encode(keyword, "UTF-8") + "&with_res_tag=1";
        }catch (Exception e){
            e.printStackTrace();
        }
        return url;
    }
    public static String getSongUrl(SongInfo songInfo,String mid){
        return "http://trackercdngz.kugou.com/i/v2/?pid=2" +
                "&mid=" + mid+
                "&cmd=26&token=&" +
                "hash=" +songInfo.hash+
                "&area_code=1&behavior=play&appid=1005&module=&vipType=65530&userid=0&mtype=0" +
                "&album_id=" +songInfo.album_id+
                "&pidversion=3001" +
                "&key=" +songInfo.key+
                "&version=9108" +
                "&album_audio_id=" +songInfo.album_audio_id+
                "&with_res_tag=1";

    }
}
