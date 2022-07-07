package mineplex.core.party.rediscommands;

import com.google.gson.annotations.SerializedName;

import mineplex.serverdata.commands.ServerCommand;

public class PartyTransferResponse extends ServerCommand
{
	private final PartyTransferRequest _origin;
	private final Result _result;

	public PartyTransferResponse(PartyTransferRequest origin, Result result)
	{
		_origin = origin;
		_result = result;
	}

	public PartyTransferRequest getOrigin()
	{
		return _origin;
	}

	public Result getResult()
	{
		return _result;
	}

	public enum Result
	{
		@SerializedName("not-enough-room")
		NOT_ENOUGH_ROOM,
		@SerializedName("success")
		SUCCESS,
		UNKNOWN
	}
}
