package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

public class Post implements IMethod {
    @Override
    public String doGet(String url) {
        return null;
    }

    @Override
    public String doPost(String url, String param) {
        BufferedReader reader=null;
        try {
            URL url1=new URL(url);
            URLConnection connection=url1.openConnection();
            connection.setRequestProperty("accept","*/*");
            connection.setRequestProperty("connection","keep-Alive");
            connection.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            //发送参数
            PrintWriter out=new PrintWriter(connection.getOutputStream());
            out.print(param);
            out.flush();
            //connection.connect();
            reader=new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer sb=new StringBuffer();
            String temp=null;
            while((temp=reader.readLine())!=null){
                sb.append(temp);
                //System.out.println(temp);
                //temp+=temp;
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        finally {
            if(reader!=null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
