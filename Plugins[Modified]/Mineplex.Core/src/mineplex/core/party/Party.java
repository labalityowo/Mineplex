package mineplex.core.party;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import mineplex.core.Managers;
import mineplex.core.common.util.UtilServer;
import mineplex.core.party.constants.PartyRemoveReason;
import mineplex.core.party.rediscommands.PartyTransferRequest;
import mineplex.core.utils.UtilGameProfile;

/**
 * The main object for Parites.
 */
public class Party implements Listener
{
	/**
	 * The maximum amount of players a party can have.
	 */
	private static final int PARTY_MAX_SIZE = 16;

	private final PartyManager _partyManager = Managers.require(PartyManager.class);

	/**
	 * The unique ID assigned to this party
	 */
	private UUID _uniqueId = UUID.randomUUID();

	/**
	 * Metadata related to the current owner of this party, stored in serializable form for transferring
	 */
	private GameProfile _owner;

	private List<Player> _members = Lists.newArrayList();

	/**
	 * This party's max size
	 */
	private int _size = PARTY_MAX_SIZE;

	private List<UUID> _pendingUnrankedMembers = new ArrayList<>();
	private List<UUID> _pendingMembers = new ArrayList<>();

	public Party(PartyTransferRequest request)
	{
		_uniqueId = request.getPartyUUID();
		_owner = request.getOwnerGameProfile();

		_pendingMembers.addAll(request.getAllMembers());
		_pendingUnrankedMembers.addAll(request.getUnrankedMembers());


		_partyManager.runSyncLater(() ->
		{
			// No one joined :(
			for (UUID uuid : _pendingUnrankedMembers)
			{
				_partyManager.getClientManager().unreserve(uuid);
			}
			for (UUID uuid : _pendingMembers)
			{
				_partyManager.removePendingJoin(uuid);
			}
			// No one joined :(
			if (_pendingMembers.size() == request.getAllMembers().size())
			{
				_partyManager.removeParty(Party.this);
			}
		}, 20 * 10L);
	}

	/**
	 * Creates a new party instance
	 *
	 * @param owner The owner / leader of the party.
	 */
	public Party(Player owner)
	{
		_owner = UtilGameProfile.getGameProfile(owner);
		_members.add(owner);
	}

	public String getOwnerName()
	{
		return _owner.getName();
	}

	public GameProfile getOwner()
	{
		return _owner;
	}

	public Optional<Player> getOwnerAsPlayer()
	{
		return Optional.ofNullable(Bukkit.getPlayer(_owner.getId()));
	}

	public UUID getUniqueId()
	{
		return this._uniqueId;
	}

	/**
	 * Send's a message to the party
	 *
	 * @param message The string message to send to all players in the party
	 */
	public void sendMessage(String message)
	{
		getMembers().forEach(player -> player.sendMessage(message));
	}

	public int getSize()
	{
		return _size;
	}

	/**
	 * Set's the new owner for this party instance
	 *
	 * @param owner The new owner's name
	 */
	public void setOwner(Player owner)
	{
		_owner = UtilGameProfile.getGameProfile(owner);
	}

	public boolean isMember(Player player)
	{
		return _members.contains(player);
	}

	public void addMember(Player player)
	{
		if (_members.contains(player))
		{
			return;
		}
		_members.add(player);
		_pendingMembers.remove(player.getUniqueId());
		_pendingUnrankedMembers.remove(player.getUniqueId());
		Lang.ADD_MEMBER.send(this, player.getName());
		getMembers().forEach(player1 -> player1.playSound(player1.getLocation(), Sound.NOTE_PLING, 1.0F, 10.0F));
	}

	public List<Player> getMembers()
	{
		return Collections.unmodifiableList(_members);
	}

	public boolean isOwner(Player caller)
	{
		return caller.getUniqueId().equals(this._owner.getId());
	}

	public void clear()
	{
		_owner = null;
		_members.clear();
		UtilServer.Unregister(this);
	}

	public void removeMember(Player player)
	{
		_members.remove(player);

		if (_members.size() <= 0)
		{
			return;
		}

		System.out.println("Removing member: " + player.getName() + " "+  player.getUniqueId() + " owner is " + _owner.getId());

		if (player.getUniqueId().equals(_owner.getId()) && _members.size() > 0)
		{
			_owner = UtilGameProfile.getGameProfile(_members.get(0));
			Lang.TRANSFER_OWNER.send(this, player.getName(), _owner.getName());
		}
	}
}
