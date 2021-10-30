package kr.kro.hurdoo.ytcc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HttpRequestParser {
    private static final Gson gson = new Gson();

    public static void loop(HttpRequestData data) {
        try {
            parseData(data);

            if(data.getHeader().get("Connection").equalsIgnoreCase("Upgrade")) {
                String ws_key = data.getHeader().get("Sec-WebSocket-Key") + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
                System.out.println("origin : " + ws_key);
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                digest.update(ws_key.getBytes());
                byte[] sha1 = digest.digest();
                System.out.println("sha1(byte) : " + sha1);
                System.out.println("sha1(string) : " + new String(sha1));
                byte[] base64 = Base64.getEncoder().encode(sha1);
                System.out.println("base64(byte) : " + base64);
                System.out.println("base64(string) : " + new String(base64));
                sendMessage(data,"HTTP/1.1 101 Switching Protocols\r\n"
                        + "Connection: Upgrade\r\n"
                        + "Upgrade: websocket\r\n"
                        + "Sec-WebSocket-Accept: " + new String(base64)
                        //+ Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((data.getHeader().get("Sec-WebSocket-Key") + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8)))
                        + "\r\n\r\n");

                InputStream in = data.getIn();
                StringBuilder str = new StringBuilder();
                while(true) {
                    /*data.getSocket().getOutputStream().write("Waiting for input\n".getBytes());
                    data.getSocket().getOutputStream().flush();*/
                    if(in.available() <= 0) continue;

                    int type = in.read() & 127;
                    //if((in.read() & 127) != 1) throw new IOException("Not a text message");
                    int length = in.read() & 127;

                    int[] key = new int[4];
                    for(int i=0;i<4;i++) {
                        key[i] = in.read();
                    }

                    byte[] msg = new byte[length];
                    for(int i=0;i<length;i++) {
                        msg[i] = (byte) (in.read() ^ key[i%4]);
                    }

                    str.append(new String(msg));

                    boolean socketClosed = false;
                    switch (type) {
                        case 0:
                            break;
                        case 1:
                            System.out.println(str);
                            break;
                        case 8:
                            socketClosed = true;
                            break;
                        default:
                            throw new IOException("Unknown WebSocket type: " + type);
                    }
                    if(socketClosed) break;
                }
            }
            else {
                sendMessage(data, SocketAcceptingServer.BROWSER_MESSAGE);
            }
            data.getSocket().getOutputStream().close();
            data.getSocket().close();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessage(HttpRequestData data,String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(data.getSocket().getOutputStream()));
        writer.write(content);
        writer.flush();
    }

    private static void handleSocketInput(HttpRequestData data) throws IOException {
        InputStream in = data.getIn();
        StringBuilder str = new StringBuilder();

        if(in.available() <= 0) return;

        int type = in.read();
        boolean fin = (type & 128) > 0;
        if((type & 64) > 0 || (type & 32) > 0 || (type & 16) > 0)
            throw new IOException("Cannot use RSV code; received byte: " + type);
        // OPCode

        boolean mask = (type & 8) > 0;

        int lengthByte = in.read();
        int length = lengthByte & 127;

        int[] key = new int[4];
        for(int i=0;i<4;i++) {
            key[i] = in.read();
        }

        byte[] msg = new byte[length];
        for(int i=0;i<length;i++) {
            msg[i] = (byte) (in.read() ^ key[i%4]);
        }

        str.append(new String(msg));

        boolean socketClosed = false;
        switch (type) {
            case 0:
                break;
            case 1:
                System.out.println(str);
                break;
            case 8:
                socketClosed = true;
                break;
            default:
                throw new IOException("Unknown WebSocket type: " + type);
        }
    }

    public static void parseData(HttpRequestData data) throws IOException {

        int i;
        InputStreamReader reader = data.getReader();
        StringBuilder str = new StringBuilder();

        while(true) { // REQUEST_METHOD
            try {
                i = reader.read();
                if(i == ' ') break;
                if(i == -1) continue;
                str.append(Character.toString(i));
            } catch(IOException e) {
                e.printStackTrace();
                return;
            }
        }
        data.setRequestMethod(str.toString());
        str.setLength(0);

        while(true) { // PATH
            i = reader.read();
            if(i == ' ') break;
            str.append(Character.toString(i));
        }
        data.setPath(str.toString());
        str.setLength(0);

        while(true) { // HTTP_VERSION
            i = reader.read();
            if(i == '\r') continue;
            if(i == '\n') break;
            str.append(Character.toString(i));
        }
        data.setHttpVersion(str.toString());
        str.setLength(0);

        while(true) { // headers
            String key = null;

            while(true) { // header
                i = reader.read();
                if(i == ':') {
                    key = str.toString();
                    str.setLength(0);
                    reader.read(); // remove space
                    continue;
                }
                if(i == '\r') continue;
                if(i == '\n') break;
                str.append(Character.toString(i));
            }

            if(key == null) break;
            data.getHeader().put(key,str.toString());
            str.setLength(0);
        }

        if(!data.getHeader().containsKey("Content-length")) return;
        int length = Integer.parseInt(data.getHeader().get("Content-length"));
        for(int j=0;j<length;j++) {
            i = reader.read();
            if(i > 127) j += 2;
            str.append(Character.toString(i));
        }
        data.setBody(gson.fromJson(str.toString(),JsonObject.class));
    }
}
