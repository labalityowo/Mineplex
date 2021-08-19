package mineplex.core.gadget.gadgets.morph.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;

public class SoulManager
{

	/**
	 * Steals souls from players
	 */

	private Map<UUID, Long> _timer = new HashMap<>();
	private Map<UUID, Integer> _stolenSouls = new HashMap<>();

	/**
	 * Removes the soul from the player
	 * @param stealer Player that stole the soul
	 * @param player Player whose soul was stolen
	 * @return if the player had their soul stolen already or not
	 */
	public boolean stealSoul(Player stealer, Player player)
	{
		if (_timer.containsKey(player.getUniqueId()))
		{
			return false;
		}
		if (_stolenSouls.containsKey(stealer))
		{
			if (_stolenSouls.get(stealer) == 20)
			{
				return false;
			}
		}
		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100000, 1, true, false));
		UtilPlayer.message(player, F.main("Soul", "Your soul was stolen by " + F.name(stealer.getName()) + "!"));
		UtilPlayer.message(stealer, F.main("Soul", "You stole " + F.name(player.getName()) + "'s soul!"));
		_timer.put(player.getUniqueId(), System.currentTimeMillis());
		_stolenSouls.put(stealer.getUniqueId(),
				(_stolenSouls.containsKey(stealer.getUniqueId()) ? _stolenSouls.get(stealer.getUniqueId()) + 1 : 1));
		return true;
	}

	/**
	 * Gives the soul back to the players
	 */
	public void giveSoul()
	{
		for (Player player : UtilServer.getPlayers())
		{
			if (!_timer.containsKey(player.getUniqueId()))
			{
				continue;
			}
			long timeStarted = _timer.get(player.getUniqueId());
			long currentTime = System.currentTimeMillis();
			if (timeStarted + 5000 < currentTime)
			{
				player.removePotionEffect(PotionEffectType.BLINDNESS);
				UtilPlayer.message(player, F.main("Soul", "Oh well, I wasn't using it anyway I guess"));
				_timer.remove(player.getUniqueId());
			}
		}
	}

	/**
	 * Forces giving a soul to that player
	 * @param player The player that will receive the soul
	 */
	public void giveSoul(Player player)
	{
		if (_timer.containsKey(player.getUniqueId()))
		{
			player.removePotionEffect(PotionEffectType.BLINDNESS);
			UtilPlayer.message(player, F.main("Soul", "Oh well, I wasn't using it anyway I guess"));
			_timer.remove(player.getUniqueId());
		}
	}

	/**
	 * Checks how many souls that player has stolen
	 * @param player The player
	 * @return souls that they stole
	 */
	public int checkSouls(Player player)
	{
		if (_stolenSouls.containsKey(player.getUniqueId()))
		{
			return _stolenSouls.get(player.getUniqueId());
		}
		return 0;
	}

	/**
	 * Resets the souls that the player stole
	 * @param player The player
	 */
	public void resetSouls(Player player)
	{
		_stolenSouls.remove(player.getUniqueId());
	}

}
