package controllers;

public class Peer implements Comparable<Peer> {

    int id;
    String hostname;
    int port;
    boolean hasFile;



    boolean choked = false;

    double downloadRate;

    public Peer(int id, String hostname, int port, boolean hasFile) {
        this.id = id;
        this.hostname = hostname;
        this.port = port;
        this.hasFile = hasFile;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isHasFile() {
        return hasFile;
    }

    public void setHasFile(boolean hasFile) {
        this.hasFile = hasFile;
    }

    public double getDownloadRate() {
        return downloadRate;
    }

    public void setDownloadRate(double downloadRate) {
        this.downloadRate = downloadRate;
    }

    public boolean isChoked() {
        return choked;
    }

    public void setChoked(boolean choked) {
        this.choked = choked;
    }

    @Override
    public int compareTo(Peer peer) {
        if (downloadRate == peer.downloadRate)
            return 0;
        else if (downloadRate > peer.downloadRate)
            return 1;
        else
            return -1;
    }
}
