package com.expertise.filip.wrapper;

import com.expertise.filip.dto.TractorFormatedDto;

public class TractorWrapper {

    private final String message;
    private final TractorFormatedDto data;
    private final int status;

    public TractorWrapper(String message, TractorFormatedDto data, int statusCode) {
        this.message = message;
        this.data = data;
        this.status = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public TractorFormatedDto getData() {
        return data;
    }

    public int getStatus() {
        return status;
    }

}
