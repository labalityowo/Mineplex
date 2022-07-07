package mineplex.game.clans.clans.observer;

import java.util.EnumSet;

import org.bukkit.entity.Player;

import mineplex.core.common.player.PlayerSnapshot;

public class ObserverData
{
	private PlayerSnapshot _snapshot;
	private EnumSet<ObserverSettings> _settings;

	public ObserverData(Player player)
	{
		_snapshot = PlayerSnapshot.getSnapshot(player);
		_settings = EnumSet.noneOf(ObserverSettings.class);
	}

	public PlayerSnapshot getSnapshot()
	{
		return _snapshot;
	}

	public EnumSet<ObserverSettings> getSettings()
	{
		return _settings;
	}
}