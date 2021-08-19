package nautilus.game.arcade.game.games.smash.perks.sheep;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;
import nautilus.game.arcade.kit.perks.data.HomingSheepData;

public class SmashSheep extends SmashUltimate
{
	
	private int _damageRadius;
	private int _damage;
	private int _knockbackMagnitude;

	private List<HomingSheepData> _sheep = new ArrayList<>();

	public SmashSheep()
	{
		super("Homing Sheeples", new String[] {}, Sound.SHEEP_IDLE, 0);
	}

	@Override
	public void setupValues()
	{
		_damageRadius = getPerkInt("Damage Radius");
		_damage = getPerkInt("Damage");
		_knockbackMagnitude = getPerkInt("Knockback Magnitude");
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);
		
		Game game = Manager.GetGame();
		
		game.CreatureAllowOverride = true;
		
		// Fire Sheep
		for (Player target : Manager.GetGame().GetPlayers(true))
		{
			if (target.equals(player))
			{
				continue;
			}
			
			Sheep sheep = player.getWorld().spawn(player.getEyeLocation(), Sheep.class);

			sheep.setBaby();

			_sheep.add(new HomingSheepData(player, target, sheep));
		}
		
		game.CreatureAllowOverride = false;
	}

	@EventHandler
	public void sheepUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		Iterator<HomingSheepData> sheepIter = _sheep.iterator();

		while (sheepIter.hasNext())
		{
			HomingSheepData data = sheepIter.next();

			if (data.update())
			{
				sheepIter.remove();
				explode(data);
			}
		}
	}

	private void explode(HomingSheepData data)
	{
		double scale = 0.4 + 0.6 * Math.min(1, data.Sheep.getTicksLived() / 60d);

		// Players
		Map<Player, Double> players = UtilPlayer.getInRadius(data.Sheep.getLocation(), _damageRadius);
		
		for (Player player : players.keySet())
		{
			if (UtilPlayer.isSpectator(player))
			{
				continue;
			}
			
			// Damage Event
			Manager.GetDamage().NewDamageEvent(player, data.Shooter, null, DamageCause.CUSTOM, _damage * scale, true, true, false, data.Shooter.getName(), GetName());
		}

		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, data.Sheep.getLocation(), 0, 0, 0, 0, 1, ViewDist.MAX, UtilServer.getPlayers());
		data.Sheep.getWorld().playSound(data.Sheep.getLocation(), Sound.EXPLODE, 2f, 1f);

		data.Sheep.remove();
	}

	@EventHandler
	public void knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}
		
		event.AddKnockback(GetName(), _knockbackMagnitude);
	}
}
