package com.crm.BackendApp.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig 
{
	@Bean
	public OpenAPI customConfig()
	{
		return new OpenAPI()
				.info(new Info()
						.title("CRM API")
						.version("1.0.0")
						.description("Spring Boot CRM REST API Documentation")
						);
	}

	@Bean
	public GroupedOpenApi v1Api() {
		return GroupedOpenApi.builder()
				.group("v1")
				.pathsToMatch("/api/v1/**")
				.build();
	}

	@Bean
	public GroupedOpenApi allApi() {
		return GroupedOpenApi.builder()
				.group("all")
				.pathsToMatch("/**")
				.build();
	}
}
