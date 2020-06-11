package de.sn.quarkus.businessfunctions.exception;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ErrorResponse
{
    public ErrorResponse(String code, String message, String parameter, String value, List<String> details) {
        super();
        this.code = code;
        this.parameter = parameter;
        this.value = value;
        this.message = message;
        this.details = details;
    }
 
    //Unique error code
    private String code;
    
    //General error message about nature of error
    private String message;
 
    //Input parameter, which caused the error
    private String parameter;

    //Input parameter, which caused the error
    private String value;

    //Specific errors in API request processing
    private List<String> details;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	
	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public List<String> getDetails() {
		return details;
	}

	public void setDetails(List<String> details) {
		this.details = details;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
    
}