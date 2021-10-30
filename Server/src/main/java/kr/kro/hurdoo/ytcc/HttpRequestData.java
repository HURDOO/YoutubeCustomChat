package kr.kro.hurdoo.ytcc;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HttpRequestData {
    private final Socket socket;

    private final InputStream in;
    private final InputStreamReader reader;
    private final OutputStream out;

    private String path;
    private String requestMethod;
    private String httpVersion;

    private final HashMap<String,String> param = new HashMap<>();
    public final HashMap<String,String> header = new HashMap<>();

    private JsonObject body;

    public HttpRequestData(Socket socket) throws IOException {
        this.socket = socket;

        in = socket.getInputStream();
        reader = new InputStreamReader(in);
        out = socket.getOutputStream();
    }

    public Socket getSocket() {
        return socket;
    }
    public InputStream getIn() {
        return in;
    }
    public InputStreamReader getReader() {
        return reader;
    }
    public OutputStream getOut() {
        return out;
    }
    public String getPath() {
        return path;
    }
    public String getRequestMethod() {
        return requestMethod;
    }
    public String getHttpVersion() {
        return httpVersion;
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

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public void setBody(JsonObject body) {
        this.body = body;
    }
    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }
}
