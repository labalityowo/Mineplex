package mineplex.core.teamspeak.redis;

import com.google.gson.annotations.SerializedName;

import mineplex.serverdata.commands.ServerCommand;

public class TeamspeakUnlinkResponse extends ServerCommand
{
	private final Response _response;
	private final TeamspeakUnlinkRequest _request;

	public TeamspeakUnlinkResponse(Response response, TeamspeakUnlinkRequest request)
	{
		this._response = response;
		this._request = request;
	}

	public Response getResponse()
	{
		return _response;
	}

	public TeamspeakUnlinkRequest getRequest()
	{
		return _request;
	}

	public enum Response
	{
		@SerializedName(value = "unlinked")
		UNLINKED
	}
}
