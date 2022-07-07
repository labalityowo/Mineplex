package mineplex.core.gadget.gadgets.morph;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseChicken;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MorphChicken extends MorphGadget
{
	public MorphChicken(GadgetManager manager)
	{
		super(manager, "Chicken Morph", UtilText.splitLinesToArray(new String[] 
				{
				C.cGray + "Soar through the air like a fat Chicken!",
				C.blankLine,
				"#" + C.cWhite + "Left Click to use Egg Shot",
				"#" + C.cWhite + "Double Jump to use Flap",
				}, LineFormat.LORE),
				20000,
				Material.FEATHER, (byte)0);
	}

	@Override
	public void enableCustom(final Player player, boolean message)
	{
		this.applyArmor(player, message);

		DisguiseChicken disguise = new DisguiseChicken(player);
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
	public void Egg(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(player))
			return;

		if (!UtilEvent.isAction(event, ActionType.L))
			return;
		
		if (!Recharge.Instance.use(player, getName(), 100, false, false, "Cosmetics"))
			return;
		
		Vector offset = player.getLocation().getDirection();
		if (offset.getY() < 0)
			offset.setY(0);

		Egg egg = player.getWorld().spawn(player.getLocation().add(0, 0.5, 0).add(offset), Egg.class);
		egg.setVelocity(player.getLocation().getDirection().add(new Vector(0,0.2,0)));
		egg.setShooter(player);
		 
		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 0.5f, 1f);
	}
	
	@EventHandler
	public void Flap(PlayerToggleFlightEvent event)
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
		
		double power = 0.4 + (0.5 * player.getExp());
		
		//Velocity
		UtilAction.velocity(player, player.getLocation().getDirection(), power, true, power, 0, 10, true);
		
		//Sound
		player.getWorld().playSound(player.getLocation(), Sound.BAT_TAKEOFF, (float)(0.3 + player.getExp()), (float)(Math.random()/2+1));
		
		//Set Recharge
		Recharge.Instance.use(player, getName(), 80, false, false);
		
		//Energy
		player.setExp(Math.max(0f, player.getExp() - (1f/9f)));
	}
	
	@EventHandler
	public void FlapUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : getActive())
		{
			if (player.getGameMode() == GameMode.CREATIVE)
				continue;

			if (UtilEnt.isGrounded(player) || UtilBlock.solid(player.getLocation().getBlock().getRelative(BlockFace.DOWN))) 
			{
				player.setExp(0.999f);
				player.setAllowFlight(true);
			}
			else if (Recharge.Instance.usable(player, getName()) && player.getExp() > 0)
			{
				player.setAllowFlight(true);
			}
		}
	}
	
	@EventHandler
	public void EggHit(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() instanceof Egg)
		{
			event.getEntity().setVelocity(new Vector(0,0,0));
		}
	}
}
