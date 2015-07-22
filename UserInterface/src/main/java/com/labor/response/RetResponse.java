package com.labor.response;

/**
 * Created by wyp on 15-7-15.
 */
public class RetResponse {

    private String status;

    private Object data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
