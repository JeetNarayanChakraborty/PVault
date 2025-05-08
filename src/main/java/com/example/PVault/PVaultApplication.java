package com.example.PVault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {"com.example.PVault", "com.example.PVault.entityClasses", 
		                       "com.example.PVault.service", "messageQueue", "security"})
public class PVaultApplication 
{
	public static void main(String[] args) 
	{
		SpringApplication.run(PVaultApplication.class, args);
	}
}
