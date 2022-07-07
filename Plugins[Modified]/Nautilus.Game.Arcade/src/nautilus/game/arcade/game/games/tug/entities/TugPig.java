package nautilus.game.arcade.game.games.tug.entities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;

import nautilus.game.arcade.game.games.tug.TugOfWool;
import nautilus.game.arcade.game.games.tug.TugTeam;

public class TugPig extends TugEntity<Pig>
{

	public TugPig(TugOfWool host, TugTeam team, Location spawn)
	{
		super(host, team, spawn, 3, 500);
	}

	@Override
	public Pig spawn(Location location)
	{
		Pig pig = location.getWorld().spawn(location, Pig.class);

		pig.setCustomNameVisible(true);
		pig.setMaxHealth(20);
		pig.setHealth(pig.getMaxHealth());

		return pig;
	}

	@Override
	public void attack(LivingEntity other)
	{
		other.damage(8, _entity);

		int ticks = 20;
		Item item = UtilItem.dropItem(new ItemStack(Material.PORK), _entity.getLocation().add(0, 1, 0), false, false, ticks, false);
		item.setVelocity(new Vector((Math.random() - 0.5) / 2, 0.3, (Math.random() - 0.5) / 2));

		_host.getArcadeManager().runSyncLater(() ->
		{
			Location location = item.getLocation();

			UtilEnt.getInRadius(location, 3).forEach((entity, scale) ->
			{
				if (entity.equals(_entity) || entity instanceof Player && _team.getGameTeam().HasPlayer((Player) entity))
				{
					return;
				}

				_host.getArcadeManager().GetDamage().NewDamageEvent(entity, other, null, DamageCause.CUSTOM, 6 * scale, true, true, true, entity.getName(), "Explosive Pork");
			});

			item.getWorld().playSound(location, Sound.EXPLODE, 1, 1);
			UtilParticle.PlayParticleToAll(ParticleType.LARGE_EXPLODE, location, null, 0, 2, ViewDist.NORMAL);
		}, ticks);
	}
}
