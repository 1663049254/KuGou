package http;

public class Http {
    public static IMethod getMethod(String method){
        if(method.equalsIgnoreCase("get")){
            return new Get();
        }else if(method.equalsIgnoreCase("post")) {
            return new Post();
        }
        return null;
    }
}
