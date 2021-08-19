package mineplex.core.gadget.gadgets.item;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ItemPaintballGun extends ItemGadget
{
	private final Set<Projectile> _balls = new HashSet<>();

	public ItemPaintballGun(GadgetManager manager)
	{
		super(manager, "Paintball Gun", 
				UtilText.splitLineToArray(C.cWhite + "PEW PEW PEW!", LineFormat.LORE), 
				-1, Material.GOLD_BARDING, (byte) 0, 200, new Ammo("Paintball Gun", "100 Paintballs", Material.GOLD_BARDING,
				(byte) 0, new String[]
					{
						C.cWhite + "100 Paintballs for you to shoot!"
					}, 500, 100));
	}

	@Override
	public void ActivateCustom(Player player)
	{
		Projectile proj = player.launchProjectile(EnderPearl.class);
		proj.setShooter(null);
		proj.setVelocity(proj.getVelocity().multiply(2));
		_balls.add(proj);

		// Sound
		player.getWorld().playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1.5f, 1.2f);
	}

	@EventHandler
	public void Paint(ProjectileHitEvent event)
	{
		Projectile projectile = event.getEntity();

		if (!_balls.remove(event.getEntity()))
		{
			return;
		}

		projectile.remove();

		Location loc = projectile.getLocation();
		Vector vec = projectile.getVelocity().normalize().multiply(0.05);

		if (vec.length() > 0)
		{
			int i = 0;

			while (UtilBlock.airFoliage(loc.getBlock()))
			{
				loc.add(vec);

				if (i++ > 50)
					break;
			}
		}

		loc.getWorld().playSound(loc, Sound.DIG_STONE, 1.3F, 1.3F);

		UtilParticle.PlayParticle(ParticleType.RED_DUST, loc, 0.2F, 0.2F, 0.2F, 1, 70, ViewDist.LONG, UtilServer.getPlayers());
	}

	@EventHandler
	public void cleanupBalls(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
		{
			return;
		}

		_balls.removeIf(ball -> ball.isDead() || !ball.isValid());
	}
}