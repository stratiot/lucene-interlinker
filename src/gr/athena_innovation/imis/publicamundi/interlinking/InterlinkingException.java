package gr.athena_innovation.imis.publicamundi.interlinking;

public class InterlinkingException extends Exception{
	
	//This enumeration contains all possible error codes defined by OpenLS "xls:ErrorCodeType" type
	enum ErrorType{MalformedRequest, ValueNotRecognized, Inconsistent, TimedOut, 
							InternalServerError, DataNotAvailable, Unknown};

	// This flag shows weather this exception is because of a user's error or not. It is used to decide whether to notify user or not  
	private boolean isUserError;
	

	// Error's type						
	private ErrorType errorType;						
	
	//The exception's constructor sets isUserError member as well - if not stated, the default value is "false"
	public InterlinkingException(String message){
		this(message, false, ErrorType.Unknown);
	}
	
	public InterlinkingException(String message, boolean userError, ErrorType errorType){
		super(message);
		this.isUserError = userError;
		this.errorType = errorType;
	}
	
	public boolean getIsUserError(){
		return this.isUserError;
	}
	
	public ErrorType getErrorType(){
		return this.errorType;
	}
}