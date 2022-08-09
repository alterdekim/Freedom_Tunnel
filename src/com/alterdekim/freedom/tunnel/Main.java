package com.alterdekim.freedom.tunnel;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.security.Security;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        Security.addProvider(new BouncyCastleProvider());
        File config = new File("config.json");
        if( config.exists() ) {
            try {
                Scanner scanner = new Scanner(new FileInputStream(config));
                String c = "";
                String line = "";
                try {
                    while ((line = scanner.nextLine()) != null) {
                        c += line;
                    }
                } catch ( Exception e ) {
                    //e.printStackTrace();
                }
                JSONObject cObj = new JSONObject(c);
                Settings.reseed_server_url = cObj.get("reseed").toString();
                scanner.close();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        } else {
            try {
                config.createNewFile();
                PrintWriter pw = new PrintWriter(new FileOutputStream(config));
                JSONObject c = new JSONObject();
                c.put("reseed", Const.reseed_server_url);
                pw.println(c.toString());
                pw.flush();
                pw.close();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
        try {
            new Tunnel(Settings.insidePort);
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}
