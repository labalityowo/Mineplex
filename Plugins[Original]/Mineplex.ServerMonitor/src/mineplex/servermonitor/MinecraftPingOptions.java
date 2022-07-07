package mineplex.servermonitor;

public class MinecraftPingOptions 
{
    private String hostname;
    private int port = 25565;
    private int timeout = 2000;
    private String charset = "UTF-8";

    public MinecraftPingOptions setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public MinecraftPingOptions setPort(int port) {
        this.port = port;
        return this;
    }

    public MinecraftPingOptions setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public MinecraftPingOptions setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    public String getHostname() {
        return this.hostname;
    }

    public int getPort() {
        return this.port;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public String getCharset() {
        return this.charset;
    }
}
