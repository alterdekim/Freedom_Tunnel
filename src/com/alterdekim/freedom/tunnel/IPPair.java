package com.alterdekim.freedom.tunnel;

public class IPPair {

    public String uuid = "";

    public Tunnel.Initiator.Reader r1;

    public Tunnel.Initiator.Reader r2;
    public String ip1 = "";
    public String ip2 = "";

    public IPPair( String ip1, Tunnel.Initiator.Reader r1, String h ) {
        this.ip1 = ip1;
        this.r1 = r1;
        this.uuid = h;
    }

    public String getIp1() {
        return ip1;
    }

    public void setIp1(String ip1) {
        this.ip1 = ip1;
    }

    public String getIp2() {
        return ip2;
    }

    public void setIp2(String ip2, Tunnel.Initiator.Reader reader2 ) {
        this.ip2 = ip2;
        this.r2 = reader2;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Tunnel.Initiator.Reader getR1() {
        return r1;
    }

    public void setR1(Tunnel.Initiator.Reader r1) {
        this.r1 = r1;
    }

    public Tunnel.Initiator.Reader getR2() {
        return r2;
    }

    public void setR2(Tunnel.Initiator.Reader r2) {
        this.r2 = r2;
    }
}
