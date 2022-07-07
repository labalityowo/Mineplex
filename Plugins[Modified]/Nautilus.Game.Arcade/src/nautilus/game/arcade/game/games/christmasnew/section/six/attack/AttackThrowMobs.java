package nautilus.game.arcade.game.games.christmasnew.section.six.attack;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.christmasnew.section.six.phase.BossPhase;

public class AttackThrowMobs extends BossAttack
{

	private static final long DURATION = TimeUnit.SECONDS.toMillis(4);
	private static final int HEALTH = 8;
	private static final ItemStack[] IN_HAND =
			{
					new ItemStack(Material.WOOD_SWORD),
					new ItemStack(Material.STONE_SWORD),
					new ItemStack(Material.IRON_AXE),
			};

	private final int _min;
	private final int _max;

	public AttackThrowMobs(BossPhase phase, int min, int max)
	{
		super(phase);

		_min = min;
		_max = max;
	}

	@Override
	public boolean isComplete()
	{
		return UtilTime.elapsed(_start, DURATION);
	}

	@Override
	public void onRegister()
	{
		_phase.getHost().CreatureAllowOverride = true;

		Location location = _boss.getEyeLocation();
		int max = UtilMath.rRange(_min, _max);

		for (int i = 0; i < max; i++)
		{
			Zombie zombie = location.getWorld().spawn(location, Zombie.class);
			zombie.setHealth(HEALTH);
			zombie.getEquipment().setItemInHand(UtilMath.randomElement(IN_HAND));

			Vector direction = location.getDirection();
			direction.add(new Vector((Math.random() - 0.5) / 2, (Math.random() / 3) + 0.4, (Math.random() - 0.5) / 2));

			UtilAction.velocity(zombie, direction);
		}

		_phase.getHost().CreatureAllowOverride = false;
	}

	@Override
	public void onUnregister()
	{

	}

	@EventHandler
	public void damage(CustomDamageEvent event)
	{
		if (event.GetCause() == DamageCause.FALL && event.GetDamageeEntity() instanceof Zombie)
		{
			event.SetCancelled("Spawned from Boss");
		}
	}
}
