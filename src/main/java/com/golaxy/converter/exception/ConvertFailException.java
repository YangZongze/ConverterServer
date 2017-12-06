package com.golaxy.converter.exception;

/**
 * 自定义异常类，
 * 转换失败
 */ 
@SuppressWarnings("serial")
public class ConvertFailException extends Exception {
	
	String message;
	
	public ConvertFailException() {
		message = "convert failed";
	}

    public ConvertFailException(String message) {
        message = message;
    }
	
	public String getMessage() {  
        return message;  
    }  
    public void setMessage(String message) {  
        this.message = message;  
    } 
}
