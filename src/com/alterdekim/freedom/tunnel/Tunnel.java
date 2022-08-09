package com.alterdekim.freedom.tunnel;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Tunnel {

    private static int initp;

    public Tunnel( int initp ) {
        Tunnel.initp = initp;
        new Reseed().start();
        new Initiator().start();
    }

    private class Reseed extends Thread {
        @Override
        public void run() {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(Settings.reseed_server_url, 8081));
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    public class Initiator extends Thread {

        private final ArrayList<IPPair> map = new ArrayList<IPPair>();

        private boolean mapDenied = false;
        @Override
        public void run() {
            try {
                ServerSocket srv = new ServerSocket(initp);
                Socket client;
                while ((client = srv.accept()) != null) {
                    try {
                        new Reader(client).start();

                    } catch ( Exception e ) {
                        e.printStackTrace();
                    }
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }

        public class Reader extends Thread {

            private Socket client;

            private IPPair getByUUID( String uuid ) {
                for( IPPair p : map ) {
                    if( equalsString(p.getUuid(), uuid) ) {
                        return p;
                    }
                }
                return null;
            }

            private boolean containsByUUID( String uuid ) {
                for( IPPair p : map ) {
                    if( equalsString(p.getUuid(), uuid) ) {
                        return true;
                    }
                }
                return false;
            }

            private boolean equalsString( String str1, String str2 ) {
                if( str1.length() == str2.length() ) {
                    int num = 0;
                    for( int i = 0; i < str1.length(); i++ ) {
                        int x = (int) str1.charAt(i);
                        int y = (int) str2.charAt(i);
                        if( x == y ) {
                            num++;
                        }
                    }
                    if( num == str1.length() ) {
                        return true;
                    }
                }
                return false;
            }

            private DataListener listener;

            private boolean isSecond = false;

            private PrintWriter pw;

            public void write( String data ) {
                try {
                    pw.println(data);
                    pw.flush();
                } catch (Exception e ) {
                    e.printStackTrace();
                }
            }

            public Reader( Socket client ) {
                this.client = client;
            }
            @Override
            public void run() {
                try {
                    pw = new PrintWriter(client.getOutputStream());
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            JSONObject jsonObject = new JSONObject(line);
                            String ip = client.getRemoteSocketAddress().toString().substring(1).split("\\:")[0];
                            System.out.println(jsonObject.toString());
                            if (jsonObject.has("uuid")) {
                                while( mapDenied ) {
                                }
                                    mapDenied = true;
                                    String h = jsonObject.get("uuid").toString();
                                    if (containsByUUID(h)) {
                                        getByUUID(h).setIp2(ip, this);
                                        isSecond = true;

                                        getByUUID(h).r1.listener = new DataListener() {
                                            @Override
                                            public void dataReceived(String data) {
                                                getByUUID(h).r2.write(data);
                                            }

                                            @Override
                                            public void exit() {
                                                try {
                                                    getByUUID(h).r1.client.close();
                                                    getByUUID(h).r2.client.close();
                                                } catch ( Exception e ) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        };

                                        getByUUID(h).r2.listener = new DataListener() {
                                            @Override
                                            public void dataReceived(String data) {
                                                getByUUID(h).r1.write(data);
                                            }

                                            @Override
                                            public void exit() {
                                                try {
                                                    getByUUID(h).r1.client.close();
                                                    getByUUID(h).r2.client.close();
                                                } catch ( Exception e ) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        };

                                        JSONObject jsonObject1 = new JSONObject();
                                        jsonObject1.put("act", "connected");

                                        getByUUID(h).r1.write(jsonObject1.toString());
                                        getByUUID(h).r2.write(jsonObject1.toString());

                                    } else {
                                        map.add(new IPPair(ip, this, h));
                                        isSecond = false;
                                    }
                                    mapDenied = false;
                            } else {
                                if( listener != null ) {
                                    listener.dataReceived(line);
                                }
                            }
                        }
                    }
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
        }
    }
}
