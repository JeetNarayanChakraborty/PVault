package com.example.PVault.entityClasses;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "users")
public class User 
{
	@Id
	@Column(name = "ID")
	private String id;
	
	@Column(name = "username")
	private String username;
	
	@Column(name = "password")
	private String password;
	
	public User() 
	{
        this.id = UUID.randomUUID().toString(); // Automatically generate a UUID
    }
	
	public String getId() 
	{
		return id;
	}

	public String getUsername() 
	{
		return username;
	}

	public void setUsername(String username) 
	{
		this.username = username;
	}

	public String getPassword() 
	{
		return password;
	}

	public void setPassword(String password) 
	{
		this.password = password;
	}

	public void setId(int i) 
	{
		this.id = String.valueOf(i);		
	}

	public void setId(long long1) 
	{
		this.id = String.valueOf(long1);		
	}
}








