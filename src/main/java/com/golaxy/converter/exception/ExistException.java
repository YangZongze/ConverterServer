package com.golaxy.converter.exception;

/**自定义异常类， 
 * 当保存返回已存在是发生ExistException 
 */ 
@SuppressWarnings("serial")
public class ExistException extends Exception {
	
	String message;
	
	public ExistException() {
		message = "file exists";
	}
	
	public String getMessage() {  
        return message;  
    }  
    public void setMessage(String message) {  
        this.message = message;  
    } 
}
