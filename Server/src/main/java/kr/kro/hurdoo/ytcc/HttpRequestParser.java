package kr.kro.hurdoo.ytcc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class HttpRequestParser {
    private static final Gson gson = new Gson();

    public static void parseData(HttpRequestData data) {
        String[] raw = data.getRaw().split(Character.toString(13) + Character.toString(10));

        String[] connInfo = raw[0].split(" ");
        data.setRequestMethod(connInfo[0]);
        String[] path = connInfo[1].replace("%3F","?").split("\\?");
        data.setPath(path[0]);
        String[] queries = connInfo[1].replace("%26","&").split("&");
        int cnt=1;
        while(cnt < path.length) {
            String[] query = queries[cnt].replace("%3D","=").split("=");
            data.getParam().put(query[0],query[1]);
            cnt++;
        }

        cnt=1;
        String line = raw[cnt];
        while(line.length() > 0) {
            String[] lines = line.split(":");
            String key = lines[0], value = "";
            for(int i=1;i<lines.length;i++) value += lines[i];
            data.getHeader().put(key,value);

            line = raw[cnt];
            cnt++;
        }

        String body = "";
        while(cnt < raw.length) {
            body += raw[cnt] + "\n";
            cnt++;
        }
        data.setBody(gson.fromJson(body, JsonObject.class));
    }
}
