package io.goshin.bukadarkness.sited;

import org.json.JSONObject;

public class Packet {
    private JSONObject request;
    private String response;
    private Status status;

    public Packet(JSONObject request) {
        this.request = request;
        status = Status.PENDING;
    }

    public JSONObject getRequest() {
        return request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
        status = Status.COMPLITED;
    }

    public Status getStatus() {
        return status;
    }

    public enum Status {
        PENDING, COMPLITED
    }
}
