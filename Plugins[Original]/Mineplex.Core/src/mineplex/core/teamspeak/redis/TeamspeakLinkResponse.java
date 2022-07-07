package mineplex.core.teamspeak.redis;

import com.google.gson.annotations.SerializedName;

import mineplex.serverdata.commands.ServerCommand;

public class TeamspeakLinkResponse extends ServerCommand
{
	private final Response _response;
	private final TeamspeakLinkRequest _request;
	private final int _id;

	public TeamspeakLinkResponse(Response response, TeamspeakLinkRequest request, int id)
	{
		_response = response;
		_request = request;
		_id = id;
	}

	public Response getResponse()
	{
		return _response;
	}

	public TeamspeakLinkRequest getRequest()
	{
		return _request;
	}

	public int getId()
	{
		return _id;
	}

	public enum Response
	{
		@SerializedName("token-valid")
		TOKEN_VALID,
		@SerializedName("token-invalid")
		TOKEN_INVALID
	}
}
