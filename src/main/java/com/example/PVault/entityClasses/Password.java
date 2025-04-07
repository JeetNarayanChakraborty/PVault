package com.example.PVault.entityClasses;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;



@Entity
@Table(name = "passwords")
public class Password
{
	@Id
	@Column(name = "ID")
	private String id;
	
	@Column(name = "username")
	private String username;
	
	@Column(name = "domainname")
	private String dname;
	
	@Column(name = "password")
	private String password;
	
	public Password()
	{
		this.id = UUID.randomUUID().toString(); // Automatically generate a UUID
	}

	public Password(String id, String username, String dname, String password) 
	{
		this.id = id;
		this.username = username;
		this.dname = dname;
		this.password = password;
	}

	public String getId() 
	{
		return id;
	}

	public void setId(String id) 
	{
		this.id = id;
	}

	public String getUsername() 
	{
		return username;
	}

	public void setUsername(String username) 
	{
		this.username = username;
	}

	public String getDname() 
	{
		return dname;
	}

	public void setDname(String dname) 
	{
		this.dname = dname;
	}

	public String getPassword() 
	{
		return password;
	}

	public void setPassword(String password) 
	{
		this.password = password;
	}
}








