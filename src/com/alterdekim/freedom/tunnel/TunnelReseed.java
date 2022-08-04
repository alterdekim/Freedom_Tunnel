package com.alterdekim.freedom.tunnel;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class TunnelReseed extends Thread {

    private String address;
    private int port;

    public static ArrayList<Integer> freeports = new ArrayList<Integer>();
    public static ArrayList<Tunnel> tunnels = new ArrayList<Tunnel>();

    private PrintWriter pw;

    public TunnelReseed( String address, int port ) {
        this.address = address;
        this.port = port;

        int p = 20000;
        for( int i = 0; i < 5000; i++ ) {
            freeports.add(p);
            p++;
        }
    }

    public static void freePorts( int port1, int port2 ) {
        freeports.add(port1);
        freeports.add(port2);
    }

    public static int getPort() {
        int port = freeports.get(0);
        freeports.remove(0);
        return port;
    }

    private void write( String str ) {
        pw.println( str );
        pw.flush();
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(address, port));
            pw = new PrintWriter(socket.getOutputStream());
            Scanner scanner = new Scanner(socket.getInputStream());
            String line = "";
            try {
                while ((line = scanner.nextLine()) != null) {
                    JSONObject jsonObject = new JSONObject(line);
                    String guid = jsonObject.get("guid").toString();
                    jsonObject = jsonObject.getJSONObject("body");
                    if( jsonObject.get("act").toString().equals("connect") ) {
                        if( freeports.size() > 2 ) {
                            int portI = getPort();
                            int portD = getPort();
                            tunnels.add( new Tunnel( jsonObject.get("initiator").toString(), portI, jsonObject.get("dest").toString(), portD ) );
                            JSONObject response = new JSONObject();
                            response.put("guid", guid);
                            JSONObject body = new JSONObject();
                            body.put("initiator_port", portI);
                            body.put("dest_port", portD);
                            response.put("body", body);
                            write(response.toString());
                        }
                    }
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}
