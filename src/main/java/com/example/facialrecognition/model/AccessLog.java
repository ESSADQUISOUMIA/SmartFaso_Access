package com.example.facialrecognition.model;

public class AccessLog {
    private int id;
    private String userName;
    private String timestamp;
    private String status;
    private String details;

    // Constructeurs
    public AccessLog() {}

    public AccessLog(int id, String userName, String timestamp, String status, String details) {
        this.id = id;
        this.userName = userName;
        this.timestamp = timestamp;
        this.status = status;
        this.details = details;
    }

    // ✅ Ajout du constructeur manquant utilisé dans AccessLogRepository
    public AccessLog(int id, String timestamp, String status, Integer userId, String userName) {
        this.id = id;
        this.timestamp = timestamp;
        this.status = status;
        this.details = "";
        this.userName = userName != null ? userName : "Inconnu";
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName != null ? userName : "Inconnu";
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    public String getDetails() {
        return details != null ? details : "";
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "AccessLog{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", status='" + status + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}
