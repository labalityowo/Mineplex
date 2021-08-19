package mineplex.core.party.rediscommands;

import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import mineplex.serverdata.commands.ServerCommand;

public class PartyTransferRequest extends ServerCommand
{
	private final UUID _partyUUID;
	private final List<UUID> _allMembers;
	private final List<UUID> _unrankedMembers;
	private final boolean _canJoinFullServers;

	private final GameProfile _ownerGameProfile;

	public PartyTransferRequest(UUID partyUUID, GameProfile ownerGameProfile, List<UUID> allMembers, List<UUID> unrankedMembers, boolean canJoinFullServers, String destServer)
	{
		super(destServer);
		_partyUUID = partyUUID;
		_ownerGameProfile = ownerGameProfile;
		_allMembers = allMembers;
		_unrankedMembers = unrankedMembers;
		_canJoinFullServers = canJoinFullServers;
	}

	public UUID getPartyUUID()
	{
		return _partyUUID;
	}

	public List<UUID> getAllMembers()
	{
		return _allMembers;
	}

	public List<UUID> getUnrankedMembers()
	{
		return _unrankedMembers;
	}

	public boolean isCanJoinFullServers()
	{
		return _canJoinFullServers;
	}

	public GameProfile getOwnerGameProfile()
	{
		return _ownerGameProfile;
	}
}
