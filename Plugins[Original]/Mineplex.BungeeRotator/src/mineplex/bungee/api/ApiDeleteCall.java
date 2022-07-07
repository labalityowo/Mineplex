package mineplex.bungee.api;

import org.apache.http.client.methods.HttpDelete;

public class ApiDeleteCall extends DnsMadeEasyApiCallBase
{
	public ApiDeleteCall(String apiUrl, int domainId, String category)
	{
		super(apiUrl, domainId, category);
	}
	
    public void Execute()
    {
		HttpDelete request = new HttpDelete(ApiUrl + DomainId + Category);

		execute(request);
    }
}
