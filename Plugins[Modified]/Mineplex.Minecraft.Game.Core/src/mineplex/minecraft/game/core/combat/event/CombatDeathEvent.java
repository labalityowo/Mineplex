package mineplex.minecraft.game.core.combat.event;

import java.util.ArrayList;
import java.util.List;

import mineplex.minecraft.game.core.combat.ClientCombat;
import mineplex.minecraft.game.core.combat.CombatLog;
import mineplex.minecraft.game.core.combat.DeathMessageType;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;

public class CombatDeathEvent extends Event
{
	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final PlayerDeathEvent _event;
	private final ClientCombat _clientCombat;
	private final CombatLog _log;
	private final List<Player> _playersToInform;

	private DeathMessageType _messageType = DeathMessageType.Detailed;
	private String _killedWord, _suffix;

	public CombatDeathEvent(PlayerDeathEvent event, ClientCombat clientCombat, CombatLog log, String killedWord)
	{
		_event = event;
		_clientCombat = clientCombat;
		_log = log;
		_killedWord = killedWord;
		_suffix = "";
		_playersToInform = new ArrayList<>(event.getEntity().getWorld().getPlayers());
	}
 
	public HandlerList getHandlers()
	{
		return HANDLER_LIST;
	}
 
	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	public ClientCombat GetClientCombat()
	{
		return _clientCombat;
	}

	public CombatLog GetLog() 
	{
		return _log;
	}

	public PlayerDeathEvent GetEvent()
	{
		return _event;
	}

	public List<Player> getPlayersToInform()
	{
		return _playersToInform;
	}

	public void SetBroadcastType(DeathMessageType value)
	{
		_messageType = value;
	}
	
	public DeathMessageType GetBroadcastType()
	{
		return _messageType;
	}

	public String getKilledWord()
	{
		return _killedWord;
	}

	public void setKilledWord(String killedWord)
	{
		_killedWord = killedWord;
	}

	public void setSuffix(String suffix)
	{
		_suffix = suffix;
	}

	public String getSuffix()
	{
		return _suffix;
	}
}
