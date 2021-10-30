package kr.kro.hurdoo.ytcc;

import java.io.IOException;

public class Main {
    public static void main2(String[] args) {
        int port;
        if(System.getenv().containsKey("PORT")) port = Integer.parseInt(System.getenv("PORT"));
        else port = 8080;

        try {
            SocketAcceptingServer server = new SocketAcceptingServer(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        main2(args);
    }
}
