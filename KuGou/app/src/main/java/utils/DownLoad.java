package utils;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownLoad {
    private Context mContext;
    private static ExecutorService downloadThreadPool=Executors.newSingleThreadExecutor();
    public DownLoad(Context ctx){
        this.mContext=ctx;
    }
    public boolean isExists(String filename){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String root = Environment.getExternalStorageDirectory().toString();
            String path=root+"/KGdownload/"+filename+".mp3";
            if(new File(path).exists())
                return true;
        }
        return false;
    }
    public boolean download(final String url,final String filename){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            final String path=Environment.getExternalStorageDirectory().toString();
            System.out.println("path:"+path);
            downloadThreadPool.execute(new Runnable() {
                @Override
                public void run() {

                    try {
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        File file = new File(path + "/KGdownload");
                        if (!file.exists()) {
                            file.mkdir();
                        }
                        request.setDestinationInExternalPublicDir("/KGdownload", filename + ".mp3");
                        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                        downloadManager.enqueue(request);
                        //Toast.makeText(mContext,"已添加到下载列表",Toast.LENGTH_LONG).show();

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
        //Looper.loop();
        return true;

    }
    public boolean checkPermission(){
        if(mContext.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED
                && mContext.checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            return false;
        }
    }

}
