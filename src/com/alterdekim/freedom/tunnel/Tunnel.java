package com.alterdekim.freedom.tunnel;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Tunnel {

    private PrintWriter initPW;
    private InputStream initSC;

    private PrintWriter destPW;
    private InputStream destSC;

    private String init;
    private int initp;
    private String dest;
    private int destp;

    private int status = 0;

    private Initiator initiator;
    private Destination destination;

    private DestListener destListener;

    private InitListener initListener;

    public Tunnel( String init, int initp, String dest, int destp ) {
        this.dest = dest;
        this.destp = destp;
        this.init = init;
        this.initp = initp;
        initiator = new Initiator();
        initiator.start();
        destination = new Destination();
        destination.start();
    }

    private void destroy() {
        initiator.interrupt();
        destination.interrupt();
        destListener.interrupt();
        initListener.interrupt();
        TunnelReseed.freePorts(initp, destp);
        TunnelReseed.tunnels.remove(this);
    }

    private class InitListener extends Thread {
        @Override
        public void run() {
            try {
                try(BufferedReader br = new BufferedReader(new InputStreamReader(initSC))) {
                    String s;
                    while((s=br.readLine())!=null){
                        destPW.println(s);
                        destPW.flush();
                        if( s.contains("exit") ) {
                            destroy();
                        }
                    }
                } catch(Exception ex) {
                   ex.printStackTrace();
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    private class DestListener extends Thread {
        @Override
        public void run() {
            try {
                try(BufferedReader br = new BufferedReader(new InputStreamReader(destSC))) {
                    String s;
                    while((s=br.readLine())!=null){
                        initPW.println(s);
                        initPW.flush();
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }
    private class Initiator extends Thread {

        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(initp);
                Socket client;
                while( (client = serverSocket.accept()) != null ) {
                    if( client.getRemoteSocketAddress().toString().substring(1).split("\\:")[0].equals(init) ) {
                        status++;
                        initPW = new PrintWriter(client.getOutputStream());
                        initSC = client.getInputStream();
                        if( status >= 2 ) {
                            doConnStuff();
                        }
                    } else {
                        client.close();
                    }
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    private void doConnStuff() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("act", "connected");
        initPW.println(jsonObject.toString());
        initPW.flush();
        destPW.println(jsonObject.toString());
        destPW.flush();
        initListener = new InitListener();
        initListener.start();
        destListener = new DestListener();
        destListener.start();
    }

    private class Destination extends Thread {

        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(destp);
                Socket client;
                while( (client = serverSocket.accept()) != null ) {
                    if( client.getRemoteSocketAddress().toString().substring(1).split("\\:")[0].equals(dest) ) {
                        status++;
                        destPW = new PrintWriter(client.getOutputStream());
                        destSC = client.getInputStream();
                        if( status >= 2 ) {
                            doConnStuff();
                        }
                    } else {
                        client.close();
                    }
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }
}
