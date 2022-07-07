package mineplex.bungee.api;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public abstract class DnsMadeEasyApiCallBase
{
    public static final int TIMEOUT = (int) TimeUnit.SECONDS.toMillis(60); 
    
	protected String ApiUrl = "https://api.dnsmadeeasy.com/V2.0/dns/managed/";
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
		PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
		connectionManager.setMaxTotal(200);
		connectionManager.setDefaultMaxPerRoute(20);
		
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpClient httpClient = new DefaultHttpClient(connectionManager, params);
	    InputStream in = null;
	    String response = "";
	    
	    try
	    {
	        String timeStamp = getServerTime();
	        SecretKeySpec keySpec = new SecretKeySpec("9041bc01-5cbc-49cf-ae09-a23b98350c62".getBytes(), "HmacSHA1");
	        Mac mac = Mac.getInstance("HmacSHA1");
	        mac.init(keySpec);
	        byte[] hashBytes = mac.doFinal((timeStamp + "").getBytes());
	        Hex.encodeHexString(hashBytes);
	        
	        request.addHeader("x-dnsme-apiKey", "2039c697-6ca9-412f-abda-0aef659a382a");
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
	        System.out.println("DnsMadeEasyApiCall Error:");
	        ex.printStackTrace();
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
