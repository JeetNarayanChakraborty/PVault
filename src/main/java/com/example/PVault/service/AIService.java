package com.example.PVault.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import org.apache.commons.codec.digest.DigestUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;



public class AIService 
{
	public boolean isPasswordPwned(String password) throws Exception 
	{
	    String sha1 = DigestUtils.sha1Hex(password).toUpperCase();
	    String prefix = sha1.substring(0, 5);
	    String suffix = sha1.substring(5);

	    URI uri = URI.create("https://api.pwnedpasswords.com/range/" + prefix);
	    URL url = uri.toURL();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
	    return reader.lines().anyMatch(line -> line.startsWith(suffix));
	}
	
	public String analyzePasswordWithAI(String password) throws IOException 
	{
	    OkHttpClient client = new OkHttpClient();

	    MediaType mediaType = MediaType.parse("application/json");
	    String bodyJson = """
	    {
	        "model": "gpt-3.5-turbo",
	        "messages": [{
	            "role": "system",
	            "content": "You are a security expert AI that reviews passwords."
	        }, {
	            "role": "user",
	            "content": "Analyze this password for security risks: '%s'"
	        }]
	    }
	    """.formatted(password);

	    Request request = new Request.Builder()
	        .url("https://api.openai.com/v1/chat/completions")
	        .post(RequestBody.create(bodyJson, mediaType))
	        .addHeader("Authorization", "Bearer YOUR_OPENAI_API_KEY")
	        .build();

	    try(Response response = client.newCall(request).execute()) 
	    {
	        return response.body().string(); 
	    }
	}

	
	
	
	

	

}
