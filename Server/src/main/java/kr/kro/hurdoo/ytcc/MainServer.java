package kr.kro.hurdoo.ytcc;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class MainServer {
    public static MainServer instance;

    private final ServerSocket server;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public MainServer(int port) throws IOException {
        instance = this;

        server = new ServerSocket(port);
        loop();
    }

    private void loop() {
        while(true) {
            try {
                Socket socket = server.accept();
                InputStreamReader reader = new InputStreamReader(socket.getInputStream());
                int cnt = 0;
                int i;
                String line = "";
                StringBuilder str = new StringBuilder();
                int content_length = -1;
                boolean bodyStart = false;

                while((i = reader.read()) != -1) {
                    //System.out.print(i + " ");
                    line += Character.toString(i);

                    if(i == 10) // \n (CR = 13, LF = 10)
                    {
                        str.append(line);
                        if(line.startsWith("Content-Length"))
                            content_length = Integer.parseInt(line.split(" ")[1].split(Character.toString(13))[0]);
                        if(line.equals(Character.toString(13) + Character.toString(10))) bodyStart = true;
                        line = "";
                    }

                    if(bodyStart)
                    {
                        cnt++;
                        assert content_length != -1;
                        //System.out.println(i + " " + cnt);
                        if(cnt > content_length)
                        {
                            str.append(line);
                            break;
                        }
                    }
                    if(i>127) cnt+=2;
                }
                if(str.length() <= 0)
                {
                    socket.close();
                    continue;
                }
                System.out.println(str);

                /*BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String s,str = "";
                while((s = reader.readLine()) != null) {
                    str += s + "\n";
                    System.out.println(s.length());
                }
                System.out.println(str);*/

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                writer.write(BROWSER_MESSAGE);
                writer.flush();
                writer.close();
                socket.close();

                Future<?> future = executor.submit(() -> {
                    HttpRequestParser.parseData(new HttpRequestData(socket,str.toString()));
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized Socket getSocket() throws IOException {
        return server.accept();
    }

    private static final String BROWSER_MESSAGE = "" +
            "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html; charset=UTF-8\r\n" +
            "\r\n" +
            "<h4>인증이 완료되었습니다. 이 창을 닫으셔도 됩니다!</h4>\r\n" +
            "<script>window.history.replaceState({}, document.title, '/'); window.close();</script>";
}
