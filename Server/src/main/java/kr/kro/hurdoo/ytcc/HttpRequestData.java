package kr.kro.hurdoo.ytcc;

import com.google.gson.JsonObject;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HttpRequestData {
    private Socket socket;
    private final String raw;

    private String requestMethod;
    private String path;
    private final HashMap<String,String> param = new HashMap<>();
    public final HashMap<String,String> header = new HashMap<>();

    private JsonObject body;

    public HttpRequestData(Socket socket,String raw) {
        this.socket = socket;
        this.raw = raw;
    }


    public Socket getSocket() {
        return socket;
    }
    public String getRaw() {
        return raw;
    }
    public String getRequestMethod() {
        return requestMethod;
    }
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public HashMap<String, String> getParam() {
        return param;
    }
    public HashMap<String, String> getHeader() {
        return header;
    }
    public JsonObject getBody() {
        return body;
    }
    public void setBody(JsonObject body) {
        this.body = body;
    }
}
