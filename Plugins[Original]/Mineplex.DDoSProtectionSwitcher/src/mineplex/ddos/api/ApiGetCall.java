package mineplex.ddos.api;

import java.lang.reflect.Type;

import org.apache.http.client.methods.HttpGet;

import com.google.gson.Gson;

public class ApiGetCall extends DnsMadeEasyApiCallBase
{
	private String _action;
	
	public ApiGetCall(String apiUrl, int domainId, String category, String action)
	{
		super(apiUrl, domainId, category);
		
		_action = action;
	}

	public void Execute()
    {
		HttpGet request = new HttpGet(ApiUrl + DomainId + Category + _action);
        
		System.out.println(execute(request));
    }
	
    public <T> T Execute(Type returnType)
    {
		HttpGet request = new HttpGet(ApiUrl + DomainId + Category + _action);

		String response = execute(request);
		System.out.println(response);
		return new Gson().fromJson(response, returnType);
    }
}
