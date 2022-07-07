package nautilus.game.arcade.game.games.smash.perks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.kit.Perk;

public class SmashUltimate extends Perk
{

	private static final int MIN_ULTIMATE_DISPLAY_TIME = 1000;

	private final Map<UUID, Long> _lastUltimate = new HashMap<>();

	private final Sound _sound;
	private int _length;

	public SmashUltimate(String name, String[] perkDesc, Sound sound, int length)
	{
		super(name, perkDesc);

		_sound = sound;
		_length = length;
	}

	@Override
	public void setupValues()
	{
		_length = getPerkTime("Duration", _length);
	}

	public void activate(Player player)
	{
		player.setHealth(player.getMaxHealth());
		_lastUltimate.put(player.getUniqueId(), System.currentTimeMillis());

		if (_length > MIN_ULTIMATE_DISPLAY_TIME)
		{
			Recharge recharge = Recharge.Instance;

			recharge.recharge(player, GetName());
			recharge.use(player, GetName(), _length, false, false);
			recharge.setDisplayForce(player, GetName(), true);
			recharge.setCountdown(player, GetName(), true);
		}
	}

	public void cancel(Player player)
	{
		if (_lastUltimate.remove(player.getUniqueId()) == null)
		{
			return;
		}

		if (_length > MIN_ULTIMATE_DISPLAY_TIME)
		{
			player.sendMessage(F.main("Game", "Deactivated " + F.skill(GetName()) + "."));
			player.playSound(player.getLocation(), Sound.BLAZE_DEATH, 1, 0);
		}

		player.getInventory().clear();
		Kit.GiveItems(player);
	}

	public boolean isUsable(Player player)
	{
		return true;
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || _length == 0)
		{
			return;
		}

		List<UUID> list = new ArrayList<>();
		list.addAll(_lastUltimate.keySet());

		for (UUID uuid : list)
		{
			Player player = UtilPlayer.searchExact(uuid);

			if (player == null)
			{
				continue;
			}

			if (UtilTime.elapsed(_lastUltimate.get(uuid), _length))
			{
				cancel(player);
			}
		}
	}

	@EventHandler
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (!(event.GetEvent().getEntity() instanceof Player))
		{
			return;
		}

		Player player = (Player) event.GetEvent().getEntity();

		if (!isUsingUltimate(player))
		{
			return;
		}

		cancel(player);
	}

	@EventHandler
	public void onGameStateChange(GameStateChangeEvent event)
	{
		for (Player player : UtilServer.getPlayersCollection())
		{
			cancel(player);
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_lastUltimate.remove(event.getPlayer().getUniqueId());
	}

	public boolean isUsingUltimate(Player player)
	{
		return isUsingUltimate(player.getUniqueId());
	}

	public boolean isUsingUltimate(UUID uuid)
	{
		return _lastUltimate.containsKey(uuid);
	}

	protected Map<UUID, Long> getLastUltimate()
	{
		return _lastUltimate;
	}

	public Sound getSound()
	{
		return _sound;
	}

	public int getLength()
	{
		return _length;
	}
}
