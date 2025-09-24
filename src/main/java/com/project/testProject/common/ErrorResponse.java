package com.project.testProject.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ErrorResponse implements BaseResponse{
    private ErrorMetaData meta;

    public ErrorResponse(String errorMessage) {
        this.meta = new ErrorMetaData();
        this.meta.message = nullToString(errorMessage);
    }

    public ErrorResponse(String errorMessage, String errorCode) {
        this.meta = new ErrorMetaData();
        this.meta.message = nullToString(errorMessage);
        this.meta.code = nullToString(errorCode);
    }

    public ErrorResponse(MetaData meta) {
        this.meta = new ErrorMetaData();
        this.meta.message=nullToString(meta.getMessage());
        this.meta.code = meta.getCode();
    }

    @Getter
    @Setter
    private class ErrorMetaData{
        private boolean result = false;
        private String message = "";
        private String code = "";
    }

    public static String nullToString(Object obj) {
        return (obj==null)?"":obj.toString();
    }
}
