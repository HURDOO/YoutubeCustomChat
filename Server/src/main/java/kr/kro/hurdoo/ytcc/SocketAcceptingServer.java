package kr.kro.hurdoo.ytcc;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketAcceptingServer {
    public static SocketAcceptingServer instance;

    private final ServerSocket server;
    private final ExecutorService executor = Executors.newFixedThreadPool(40);

    public SocketAcceptingServer(int port) throws IOException {
        instance = this;

        server = new ServerSocket(port);
        loop();
    }

    private void loop() {
        while(true) {
            try {
                Socket socket = server.accept();
                executor.execute(() -> {
                    try {
                        HttpRequestParser.loop(new HttpRequestData(socket));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized Socket getSocket() throws IOException {
        // return server.accept();
        return null;
    }

    public static final String BROWSER_MESSAGE = "" +
            "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html; charset=UTF-8\r\n" +
            "\r\n" +
            "<h4>인증이 완료되었습니다. 이 창을 닫으셔도 됩니다!</h4>\r\n" +
            "<script>window.history.replaceState({}, document.title, '/'); window.close();</script>";
}
