package mineplex.bungee.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

import com.google.gson.Gson;

public class HttpCallBase
{
	private String _url;
	
    public HttpCallBase(String url)
	{
    	_url = url;
	}

	public <T> T Execute(Type returnType)
    {
		HttpGet request = new HttpGet(_url);

		String response = execute(request);
		System.out.println(response);
		return new Gson().fromJson(response, returnType);
    }
    
	protected String execute(HttpRequestBase request)
	{
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
	
		PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(schemeRegistry);
		connectionManager.setMaxTotal(200);
		connectionManager.setDefaultMaxPerRoute(20);
		
	    HttpClient httpClient = new DefaultHttpClient(connectionManager);
	    InputStream in = null;
	    String response = "";
	    
	    try
	    {
	    	request.addHeader("Content-Type", "application/json");
	        HttpResponse httpResponse = httpClient.execute(request);            
	
	        if (httpResponse != null)
	        {
	            in = httpResponse.getEntity().getContent();
	            response = convertStreamToString(in);
	        }
	    }
	    catch (Exception ex) 
	    {
	        System.out.println("HttpCall Error:\n" + ex.getMessage());
	        
	        for (StackTraceElement trace : ex.getStackTrace())
	        {
	        	System.out.println(trace);
	        }
	    } 
	    finally 
	    {
	        httpClient.getConnectionManager().shutdown();
	        
	        if (in != null)
	        {
	            try 
	            {
	                in.close();
	            } 
	            catch (IOException e) 
	            {
	                e.printStackTrace();
	            }
	        }
	    }
	    
	    return response;
	}

	protected String convertStreamToString(InputStream is)
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try
		{
			while ((line = reader.readLine()) != null)
			{
				sb.append(line + "\n");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
