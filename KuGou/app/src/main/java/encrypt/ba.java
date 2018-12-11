package encrypt;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class ba {
    private static String[] e={"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f"};
    public ba(){
        a();
    }
    public String a(String hash){

        //String var1=new String(hash);
        //ba(var1,hash);
        MessageDigest md5=null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] bytes=md5.digest(hash.getBytes());
        return c(bytes);

    }
    private static String c(byte[] bytes){
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<bytes.length;i++){
            sb.append(a(bytes[i]));
        }
        return sb.toString();
    }
    private static String a(byte b){
        //System.out.println(b);
        int i=(int)b;
        if(i<0){

            i=i+256;
            //System.out.println(b+","+b2);
        }
        int var1=i/16;
        int var2=i%16;
        StringBuilder sb=new StringBuilder();
        String var3=e[var1];
        sb.append(var3);
        String var4=e[var2];
        sb.append(var4);
        return sb.toString();
        //return null;
    }

    private void a(){
		/*c[0]=0;
		c[1]=0;
		b[0]=0x67452301;
		b[1]=0xefcdab89L;
		b[2]=0x98badcfeL;
		b[3]=0x10325476;*/
    }
    public String k(String str){
        BigInteger b1=new BigInteger("0");
        BigInteger b2=new BigInteger("16");
        String str1=new ba().a(str);
        //System.out.println(str1);
        for(int i=0;i<str1.length();i++){
            //System.out.println(i);
            BigInteger b3;
            StringBuilder sb=new StringBuilder();
            String str2="";
            sb.append(str2);
            char c1=str1.charAt(i);
            sb.append(c1);
            String str3=sb.toString();
            int n1=0x10;
            b3=new BigInteger(str3,n1);
            int n2=-0x1;
            int n3=str1.length()+n2;
            n3=n3-i;
            BigInteger b4=b2.pow(n3);
            //System.out.println(b4);
            b3=b3.multiply(b4);
            b1=b1.add(b3);
        }
        //System.out.println(b1.toString());
        return b1.toString();
    }
}
