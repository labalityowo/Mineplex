package mineplex.core.common.api;

import com.google.gson.Gson;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import mineplex.core.common.api.enderchest.HashesNotEqualException;

/**
 * @author Shaun Bennett
 */
public class ApiWebCall
{
	private String _url;
	private Gson _gson;
	private PoolingHttpClientConnectionManager _cm;
	private CloseableHttpClient _httpClient;

	public ApiWebCall(String url)
	{
		this(url, new Gson());
	}

	public ApiWebCall(String url, Gson gson)
	{
		_url = url;
		_gson = gson;

		_cm = new PoolingHttpClientConnectionManager();
		_cm.setMaxTotal(200);
		_cm.setDefaultMaxPerRoute(20);
		_httpClient = HttpClients.custom().setConnectionManager(_cm).build();
	}

	public <T> T get(String resource, Class<T> clazz)
	{
		return get(resource, (Type)clazz);
	}

	public <T> T get(String resource, Type type)
	{
		T returnData = null;

		HttpGet httpGet = new HttpGet(_url + resource);
		try (CloseableHttpResponse response = _httpClient.execute(httpGet)) {
			returnData = parseResponse(response, type);
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		return returnData;
	}

	public <T> T post(String resource, Class<T> clazz, Object data)
	{
		T returnData = null;

		HttpPost httpPost = new HttpPost(_url + resource);
		try
		{
			if (data != null)
			{
				StringEntity params = new StringEntity(_gson.toJson(data));
				params.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				httpPost.setEntity(params);
			}

			try (CloseableHttpResponse response = _httpClient.execute(httpPost))
			{
				returnData = parseResponse(response, clazz);
			} catch (IOException e)
			{
				e.printStackTrace();
			}

		} catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		return returnData;
	}

	public File getFile(String resource, String savePath) throws HashesNotEqualException, IOException
	{
		HttpGet httpGet = new HttpGet(_url + resource);
		File file = new File(savePath);

		FileOutputStream fos = null;
		DigestInputStream dis = null;
		try (CloseableHttpResponse response = _httpClient.execute(httpGet))
		{
			MessageDigest md = DigestUtils.getMd5Digest();
			HttpEntity entity = response.getEntity();
			dis = new DigestInputStream(entity.getContent(), md);
			fos = new FileOutputStream(file);
			IOUtils.copy(dis, fos);

			String calculatedHash = Hex.encodeHexString(md.digest());
			Header hashHeader = response.getFirstHeader("Content-MD5");

			if (hashHeader != null && !calculatedHash.equals(hashHeader.getValue()))
			{
				file.delete();
				throw new HashesNotEqualException(hashHeader.getValue(), calculatedHash);
			}
		} finally {
			try
			{
				if (fos != null) fos.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}

			try
			{
				if (dis != null) dis.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return file;
	}

	private <T> T parseResponse(CloseableHttpResponse response, Type type) throws IOException
	{
		HttpEntity entity = response.getEntity();
		T parsed = _gson.fromJson(new InputStreamReader(entity.getContent()), type);
		if (parsed instanceof HttpStatusCode && response.getStatusLine() != null)
		{
			((HttpStatusCode) parsed).setStatusCode(response.getStatusLine().getStatusCode());
		}
		return parsed;
	}
}
