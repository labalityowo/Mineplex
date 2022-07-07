package mineplex.core.common.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class UtilWeb
{
	public static String doPOST(String url, Map<String, Object> params)
	{
		try
		{
			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String,Object> param : params.entrySet())
			{
				if (postData.length() != 0)
				{
					postData.append('&');
				}
				
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
			
			byte[] postDataBytes = postData.toString().getBytes("UTF-8");
			
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			conn.setDoOutput(true);
			conn.getOutputStream().write(postDataBytes);
			
			Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			
			StringBuilder back = new StringBuilder();
			
			for (int $char; ($char = in.read()) >= 0;)
			{
				back.append((char) $char);
			}
			
			return back.toString();
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
			
			return null;
		}
	}

}
