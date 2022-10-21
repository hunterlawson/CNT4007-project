package models;

public class Peer {

    int id;
    String hostname;
    int port;
    boolean hasFile;

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

}
