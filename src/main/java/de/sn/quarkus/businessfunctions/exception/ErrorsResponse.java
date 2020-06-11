package de.sn.quarkus.businessfunctions.exception;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ErrorsResponse {
	private List<ErrorResponse> errorList = new ArrayList<>();

	//Timestamp error occured
    private String timestamp = Instant.now().toString();
	
    public List<ErrorResponse> getErrorList() {
		return errorList;
	}

	public void setErrorList(List<ErrorResponse> errorList) {
		this.errorList = errorList;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	
	
}
