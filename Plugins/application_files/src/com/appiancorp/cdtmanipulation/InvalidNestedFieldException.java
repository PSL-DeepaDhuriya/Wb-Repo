package com.appiancorp.cdtmanipulation;

public class InvalidNestedFieldException extends Exception {
	    
    private String errorCode="InvalidCdt";
     
    public InvalidNestedFieldException(String message, String errorCode){
        super(message);
        this.errorCode=errorCode;
    }
    
    public InvalidNestedFieldException(String message){
        super(message);
    }
     
    public String getErrorCode(){
        return this.errorCode;
    }
    
}
