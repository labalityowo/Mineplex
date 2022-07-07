package mineplex.core.gadget.gadgets.morph;

import java.time.Month;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseGhast;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MorphGhast extends MorphGadget
{

	private static final Vector UP = new Vector(0, 1.5, 0);

	private final Set<LargeFireball> _fireballs = new HashSet<>();

	public MorphGhast(GadgetManager manager)
	{
		super(manager, "Ghast Morph", UtilText.splitLinesToArray(new String[]{
				C.cGray + "A never before seen morph, the very spooky Ghast!",
				"",
				C.cGreen + "Left Click" + C.cWhite + " to shoot a fireball",
		}, LineFormat.LORE), CostConstants.POWERPLAY_BONUS, Material.GLASS, (byte) 0);

		setPPCYearMonth(YearMonth.of(2017, Month.OCTOBER));
		setDisplayItem(new ItemBuilder(Material.SKULL_ITEM, (byte) 3)
				.setPlayerHead("MHF_Ghast")
				.build());
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		UtilMorph.disguise(player, new DisguiseGhast(player), Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);

		player.setAllowFlight(false);
		player.setFlying(false);

		UtilMorph.undisguise(player, Manager.getDisguiseManager());
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!UtilEvent.isAction(event, ActionType.L) || !isActive(event.getPlayer()))
		{
			return;
		}

		event.setCancelled(true);

		if (!Recharge.Instance.use(player, "Fireball", 4000, true, true, "Cosmetics"))
		{
			return;
		}

		Sound sound;
		int random = UtilMath.r(3);

		if (random == 0)
		{
			sound = Sound.GHAST_SCREAM;
		}
		else if (random == 1)
		{
			sound = Sound.GHAST_SCREAM2;
		}
		else
		{
			sound = Sound.GHAST_FIREBALL;
		}

		player.getWorld().playSound(player.getLocation(), sound, 1, (float) (0.7 + Math.random() / 2));

		LargeFireball fireball = player.launchProjectile(LargeFireball.class);
		_fireballs.add(fireball);
	}

	@EventHandler
	public void flight(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player player : getActive())
		{
			if (UtilPlayer.isSpectator(player))
			{
				continue;
			}

			player.setAllowFlight(true);
			player.setFlying(true);

			if (UtilEnt.isGrounded(player))
			{
				UtilAction.velocity(player, UP);
			}
		}
	}

	@EventHandler
	public void projectileHit(ProjectileHitEvent event)
	{
		if (!_fireballs.contains(event.getEntity()))
		{
			return;
		}

		Location location = event.getEntity().getLocation();

		for (Player player : UtilPlayer.getNearby(location, 5))
		{
			if (Manager.selectEntity(this, player))
			{
				player.setVelocity(UP);
			}
		}

		UtilParticle.PlayParticleToAll(ParticleType.LAVA, location.add(0, 0.8, 0), 0.5F, 0.5F, 0.5F, 0.01F, 6, ViewDist.NORMAL);
		event.getEntity().remove();
	}
}
