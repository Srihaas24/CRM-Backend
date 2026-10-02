package com.crm.BackendApp.exception;

public class ResourceNotFoundException extends RuntimeException 
{
	public ResourceNotFoundException(String message)
	{
		super(message);
	}
}
