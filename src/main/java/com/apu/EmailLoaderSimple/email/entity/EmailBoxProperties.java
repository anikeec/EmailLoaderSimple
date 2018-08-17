package com.apu.EmailLoaderSimple.email.entity;

public class EmailBoxProperties {
    
    private String directory;
    private String host;
    private String login;
    private String password;
    private String server;
    private String storageFileName;
    
    public String getDirectory() {
        return directory;
    }
    public void setDirectory(String directory) {
        this.directory = directory;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getServer() {
        return server;
    }
    public void setServer(String server) {
        this.server = server;
    }
    public String getStorageFileName() {
        return storageFileName;
    }
    public void setStorageFileName(String storageFileName) {
        this.storageFileName = storageFileName;
    }

}
