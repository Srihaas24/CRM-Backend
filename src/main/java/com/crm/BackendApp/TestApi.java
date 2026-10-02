package com.crm.BackendApp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestApi 
{
	@GetMapping("/")
	public String welcome()
	{
		return "Congrats!!";
	}
	
}
