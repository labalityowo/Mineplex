package mineplex.core.gadget.gadgets.morph;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseEnderman;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MorphEnderman extends MorphGadget
{
	public MorphEnderman(GadgetManager manager)
	{
		super(manager, "Enderman Morph", UtilText.splitLinesToArray(new String[] 
				{
				C.cGray + "Using this morph is the ultimate diet! Guaranteed instant results!",
				C.blankLine,
				"#" + C.cWhite + "Double Jump to use Blink",
				}, LineFormat.LORE),
				30000,
				Material.ENDER_PEARL, (byte)0);
	}

	@Override
	public void enableCustom(final Player player, boolean message)
	{
		this.applyArmor(player, message);

		DisguiseEnderman disguise = new DisguiseEnderman(player);
		UtilMorph.disguise(player, disguise, Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		this.removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());

		player.setAllowFlight(false);
		player.setFlying(false);
	}

	@EventHandler
	public void teleport(PlayerToggleFlightEvent event)
	{
		Player player = event.getPlayer();

		if (player.getGameMode() == GameMode.CREATIVE)
			return;

		if (!isActive(player))
			return;

		event.setCancelled(true);
		player.setFlying(false);

		//Disable Flight
		player.setAllowFlight(false);

		//Set Recharge
		Recharge.Instance.use(player, getName(), 2000, false, false, "Cosmetics");

		//Smoke Trail
		Block lastSmoke = player.getLocation().getBlock();

		double curRange = 0;
		while (curRange <= 16)
		{
			Location newTarget = player.getLocation().add(new Vector(0,0.2,0)).add(player.getLocation().getDirection().multiply(curRange));

			if (!UtilBlock.airFoliage(newTarget.getBlock()) || 
					!UtilBlock.airFoliage(newTarget.getBlock().getRelative(BlockFace.UP)))
				break;

			//Progress Forwards
			curRange += 0.2;

			//Smoke Trail
			if (!lastSmoke.equals(newTarget.getBlock()))
			{
				lastSmoke.getWorld().playEffect(lastSmoke.getLocation(), Effect.SMOKE, 4);
			}

			lastSmoke = newTarget.getBlock();
		}

		//Modify Range
		curRange -= 0.4;
		if (curRange < 0)
			curRange = 0;

		//Destination
		Location loc = player.getLocation().add(player.getLocation().getDirection().multiply(curRange).add(new Vector(0, 0.4, 0)));

		if (curRange > 0)
		{
			//Firework
			FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.BLACK).with(Type.BALL).trail(false).build();

			try 
			{
				UtilFirework.playFirework(player.getEyeLocation(), effect);
				player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_UNFECT, 2f, 2f);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}

			player.teleport(loc);

			//Firework
			try 
			{
				UtilFirework.playFirework(player.getEyeLocation(), effect);
				player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_UNFECT, 2f, 2f);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}


		player.setFallDistance(0);
	}

	@EventHandler
	public void teleportUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : getActive())
		{
			if (player.getGameMode() == GameMode.CREATIVE)
				continue;

			if (Recharge.Instance.usable(player, getName()))
			{
				player.setAllowFlight(true);
			}
		}
	}
}
