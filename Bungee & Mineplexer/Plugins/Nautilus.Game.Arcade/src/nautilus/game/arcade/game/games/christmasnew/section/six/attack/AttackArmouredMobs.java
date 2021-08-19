package nautilus.game.arcade.game.games.christmasnew.section.six.attack;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilTime;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.christmasnew.section.six.phase.BossPhase;

public class AttackArmouredMobs extends BossAttack
{

	private static final long DURATION = TimeUnit.SECONDS.toMillis(10);
	private static final ItemStack[] ARMOUR =
			{
					new ItemStack(Material.CHAINMAIL_BOOTS),
					new ItemStack(Material.CHAINMAIL_LEGGINGS),
					new ItemStack(Material.CHAINMAIL_CHESTPLATE),
					new ItemStack(Material.CHAINMAIL_HELMET)
			};
	private static final ItemStack[] IN_HAND =
			{
					new ItemStack(Material.IRON_AXE),
					new ItemStack(Material.DIAMOND_SWORD),
					new ItemStack(Material.IRON_SWORD),
			};
	private static final PotionEffect SPEED_EFFECT = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false);

	private final int _max;

	public AttackArmouredMobs(BossPhase phase, int max)
	{
		super(phase);

		_max = max;

		setAllowsMovement(true);
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

		for (int i = 0; i < _max; i++)
		{
			Location location = UtilAlg.getRandomLocation(_phase.getBossSpawn(), 5, 0, 5);
			Zombie zombie = location.getWorld().spawn(location, Zombie.class);
			zombie.addPotionEffect(SPEED_EFFECT);

			zombie.getEquipment().setItemInHand(UtilMath.randomElement(IN_HAND));
			zombie.getEquipment().setArmorContents(ARMOUR);

			UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, location.add(0, 1.2, 0), 1, 1, 1, 0.1F, 50, ViewDist.LONG);
			location.getWorld().playSound(location, Sound.ENDERMAN_SCREAM, 1.5F, 0.7F);
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
