package mineplex.ddos.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

public abstract class DnsMadeEasyApiCallBase
{
	protected String ApiUrl = "http://api.dnsmadeeasy.com/V2.0/dns/managed/";
	protected int DomainId = 962728;
	protected String Category = "/records/";
	
	public DnsMadeEasyApiCallBase(String apiUrl, int domainId, String category)
	{
		ApiUrl = apiUrl;
		DomainId = domainId;
		Category = category;
	}
	
	protected String execute(HttpRequestBase request)
	{
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", 80, PlainSocketFactory.getSocketFactory()));
	
		PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(schemeRegistry);
		connectionManager.setMaxTotal(200);
		connectionManager.setDefaultMaxPerRoute(20);
		
	    HttpClient httpClient = new DefaultHttpClient(connectionManager);
	    InputStream in = null;
	    String response = "";
	    
	    try
	    {
	        String timeStamp = getServerTime();
	        SecretKeySpec keySpec = new SecretKeySpec("8c9af8cc-d306-4df3-8de8-944deafa8239".getBytes(), "HmacSHA1");
	        Mac mac = Mac.getInstance("HmacSHA1");
	        mac.init(keySpec);
	        byte[] hashBytes = mac.doFinal((timeStamp + "").getBytes());
	        Hex.encodeHexString(hashBytes);
	        
	        request.addHeader("x-dnsme-apiKey", "610e21ee-4250-4b55-b637-a1fcc3847850");
	        request.addHeader("x-dnsme-requestDate", timeStamp + "");
	        request.addHeader("x-dnsme-hmac", Hex.encodeHexString(hashBytes));
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
	        System.out.println("DnsMadeEasyApiCall Error:\n" + ex.getMessage());
	        
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

	protected String getServerTime()
	{
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(calendar.getTime());
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
