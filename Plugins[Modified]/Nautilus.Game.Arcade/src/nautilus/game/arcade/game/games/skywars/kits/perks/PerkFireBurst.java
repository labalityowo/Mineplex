package nautilus.game.arcade.game.games.skywars.kits.perks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;

public class PerkFireBurst extends SkywarsPerk
{

	private static final int FIRE_TICKS = 100;
	private static final double LOCATION_Y_MODIFIER = 1;

	private final long _cooldown;
	private final double _range, _damage;

	public PerkFireBurst(ItemStack itemStack, long cooldown, double range, double damage)
	{
		super("Fire Burst", itemStack);

		_cooldown = cooldown;
		_range = range;
		_damage = damage;
	}

	@Override
	public void onUseItem(Player player)
	{
		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		Location location = player.getLocation().add(0, LOCATION_Y_MODIFIER, 0);

		for (Player other : UtilPlayer.getNearby(location, _range, true))
		{
			if (other.equals(player) || isTeamDamage(player, other))
			{
				continue;
			}

			other.setFireTicks(FIRE_TICKS);
			Manager.GetDamage().NewDamageEvent(other, player, null, DamageCause.CUSTOM, _damage, false, true, true, player.getName(), GetName());
		}

		for (double radius = 0; radius < _range; radius += 0.5)
		{
			for (double theta = 0; theta < 2 * Math.PI; theta += Math.PI / 20)
			{
				double x = radius * Math.cos(theta);
				double z = radius * Math.sin(theta);

				location.add(x, 0, z);

				UtilParticle.PlayParticleToAll(ParticleType.FLAME, location, 0, 0, 0, 0.01F, 1, ViewDist.NORMAL);

				location.subtract(x, 0, z);
			}
		}
	}
}
