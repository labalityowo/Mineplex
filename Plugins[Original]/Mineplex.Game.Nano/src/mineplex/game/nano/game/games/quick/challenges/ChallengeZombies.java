package mineplex.game.nano.game.games.quick.challenges;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.disguises.DisguiseVillager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengeZombies extends Challenge
{

	public ChallengeZombies(Quick game)
	{
		super(game, ChallengeType.ZOMBIES);

		_timeout = TimeUnit.SECONDS.toMillis(25);
		_winConditions.setTimeoutWin(true);
	}

	@Override
	public void challengeSelect()
	{
		DisguiseManager manager = _game.getManager().getDisguiseManager();

		for (Player player : _players)
		{
			DisguiseVillager disguise = new DisguiseVillager(player);

			disguise.setName(_game.getPlayersTeam().getChatColour() + player.getName());
			disguise.setCustomNameVisible(true);

			manager.disguise(disguise);
		}
	}

	@Override
	public void disable()
	{
	}

	@EventHandler
	public void updateZombies(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		Location location = UtilAlg.Random(_game.getGreenPoints());

		if (location == null)
		{
			return;
		}

		_game.getWorldComponent().setCreatureAllowOverride(true);

		location.setYaw(UtilMath.r(360));

		Zombie zombie = location.getWorld().spawn(location, Zombie.class);
		zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
		zombie.setTarget(UtilAlg.Random(_players));

		_game.getWorldComponent().setCreatureAllowOverride(false);
	}

	@EventHandler
	public void entityCombust(EntityCombustEvent event)
	{
		event.setCancelled(true);
	}
}
