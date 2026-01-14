package com;

import java.io.IOException;
import java.net.ServerSocket;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() throws InterruptedException, IOException {
        ServerSocket server = new ServerSocket(9999, 2);

        System.out.println("Server is listening on port 9999...");
        System.out.println("I am going to sleep and ignoring all calls.");

        // We never call server.accept(). We just sleep.
        Thread.sleep(1000000);
    }
}
