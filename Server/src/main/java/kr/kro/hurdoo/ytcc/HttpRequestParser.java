package kr.kro.hurdoo.ytcc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.LinkedBlockingQueue;

public class HttpRequestParser {
    private static final Gson gson = new Gson();
    public static LinkedBlockingQueue<HttpRequestData> waiting = new LinkedBlockingQueue<>();

    public static void addToWaitingQueue(Socket socket,String data) {
        waiting.offer(new HttpRequestData(socket,data));
    }

    public static void loop() {
        try {
            HttpRequestData data = parseData(getNewData());
            if(data.getHeader().get("Connection").equalsIgnoreCase("Upgrade")) {
                sendMessage(data,"HTTP/1.1 101 Switching Protocols\r\n"
                        + "Connection: Upgrade\r\n"
                        + "Upgrade: websocket\r\n"
                        + "Sec-WebSocket-Accept: "
                        + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((data.getHeader().get("Sec-WebSocket-Key") + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8)))
                        + "\r\n\r\n");

                InputStream in = data.getSocket().getInputStream();
                while(true) {
                    /*data.getSocket().getOutputStream().write("Waiting for input\n".getBytes());
                    data.getSocket().getOutputStream().flush();*/
                    if(in.available() <= 0) continue;

                    if((in.read() & 127) != 1) throw new IOException("Not a text message");
                    int length = in.read() & 127;

                    int[] key = new int[4];
                    for(int i=0;i<4;i++) {
                        key[i] = in.read();
                    }

                    byte[] msg = new byte[length];
                    for(int i=0;i<length;i++) {
                        msg[i] = (byte) (in.read() ^ key[i%4]);
                    }

                    System.out.println(new String(msg));
                }
            }
            else {
                sendMessage(data,MainServer.BROWSER_MESSAGE);
            }
        } catch (InterruptedException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessage(HttpRequestData data,String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(data.getSocket().getOutputStream()));
        writer.write(content);
        writer.flush();
        //writer.close();
    }

    public static HttpRequestData parseData(HttpRequestData data) {
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
        while(line.length() > 0 && cnt < raw.length) {
            String[] lines = line.split(":");
            String key = lines[0];
            StringBuilder value = new StringBuilder();
            for(int i=1;i<lines.length;i++) value.append(lines[i]);
            data.getHeader().put(key, value.substring(1));

            line = raw[cnt];
            cnt++;
        }

        StringBuilder body = new StringBuilder();
        while(cnt < raw.length) {
            body.append(raw[cnt]).append("\n");
            cnt++;
        }
        data.setBody(gson.fromJson(body.toString(), JsonObject.class));
        return data;
    }

    private static synchronized HttpRequestData getNewData() throws InterruptedException {
        return waiting.take();
    }
}
