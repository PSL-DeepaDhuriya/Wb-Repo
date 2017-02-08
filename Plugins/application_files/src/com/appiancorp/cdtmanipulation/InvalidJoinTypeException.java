package com.appiancorp.cdtmanipulation;

public class InvalidJoinTypeException extends Exception {
	 private static final long serialVersionUID = 4664456874499611218L;
     
    private String errorCode="InvalidJoinType";
     
    public InvalidJoinTypeException(String message, String errorCode){
        super(message);
        this.errorCode=errorCode;
    }
    
    public InvalidJoinTypeException(String message){
        super(message);
    }
     
    public String getErrorCode(){
        return this.errorCode;
    }
}
