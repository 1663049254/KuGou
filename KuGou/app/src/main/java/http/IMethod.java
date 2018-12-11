package http;

public interface IMethod {
    String doGet(String url);
    String doPost(String url, String param);
}
