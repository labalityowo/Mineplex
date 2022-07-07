package mineplex.core.party.rediscommands;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.google.gson.annotations.SerializedName;

import mineplex.serverdata.commands.ServerCommand;

public class PartyCrossServerInviteResponse extends ServerCommand
{
	private final Result _result;
	private final PartyCrossServerInviteCommand _origin;

	private final String _targetName;
	private final UUID _targetUUID;

	public PartyCrossServerInviteResponse(Result result, PartyCrossServerInviteCommand origin, Player target)
	{
		_result = result;
		_origin = origin;

		_targetName = target.getName();
		_targetUUID = target.getUniqueId();
	}

	public Result getResult()
	{
		return _result == null ? Result.UNKNOWN : _result;
	}

	public PartyCrossServerInviteCommand getOrigin()
	{
		return _origin;
	}

	public String getTargetName()
	{
		return _targetName;
	}

	public UUID getTargetUUID()
	{
		return _targetUUID;
	}

	public enum Result
	{
		@SerializedName("success-in-party")
		SUCCESS_IN_PARTY,
		@SerializedName("not-accepting-invites")
		TARGET_NOT_ACCEPTING_INVITES,
		@SerializedName("success")
		SUCCESS,
		UNKNOWN
	}
}
