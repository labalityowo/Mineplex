package mineplex.core.gadget.gadgets.morph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseWither;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.gadgets.mount.DragonMount;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.inventory.ClientItem;
import mineplex.core.inventory.data.Item;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MorphWither extends MorphGadget
{
	private ArrayList<WitherSkull> _skulls = new ArrayList<WitherSkull>();
	
	public MorphWither(GadgetManager manager)
	{
		super(manager, "Wither Morph", UtilText.splitLinesToArray(new String[] 
				{
				C.cGray + "Legends have foretold the coming of a powerful Wither...",
				C.blankLine,
				"#" + C.cWhite + "Left Click to use Wither Skull",
				}, LineFormat.LORE),
				-12,
				Material.SKULL_ITEM, (byte)1);
	}

	@Override
	public void enableCustom(final Player player, boolean message)
	{
		Gadget mount = Manager.getActive(player, GadgetType.MOUNT);
		if (mount != null && mount instanceof DragonMount)
		{
			UtilPlayer.message(player, F.main("Gadget", "You cannot enable the " + F.elem(mount.getName()) + " and the " + F.elem(getName()) + " at the same time"));
			Manager.removeActive(player, mount);
		}
		this.applyArmor(player, message);

		player.setMaxHealth(300);
		player.setHealth(300);
		
		DisguiseWither disguise = new DisguiseWither(player);
		UtilMorph.disguise(player, disguise, Manager, true);

		player.setMaxHealth(20);
		player.setHealth(20);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		this.removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());

		player.setAllowFlight(false);
		player.setFlying(false);
		
		player.setMaxHealth(20);
		player.setHealth(20);
	}

	@EventHandler
	public void witherSkull(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(player))
			return;

		if (!UtilEvent.isAction(event, ActionType.L))
			return;
		
		if (!Recharge.Instance.use(player, getName(), 2500, false, false, "Cosmetics"))
			return;
		
		Vector offset = player.getLocation().getDirection();
		if (offset.getY() < 0)
			offset.setY(0);
		
		_skulls.add(player.launchProjectile(WitherSkull.class));
		 
		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.WITHER_SHOOT, 0.5f, 1f);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void explode(EntityExplodeEvent event)
	{
		if (!_skulls.contains(event.getEntity()))
			return;
		
		event.setCancelled(true);
		
		event.getEntity().remove();
		
		WitherSkull skull = (WitherSkull)event.getEntity();
		
		UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, skull.getLocation(), 0, 0, 0, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
		skull.getWorld().playSound(skull.getLocation(), Sound.EXPLODE, 2f, 1f);
		
		HashMap<Player, Double> players = UtilPlayer.getInRadius(event.getLocation(), 6);
		for (Player player : players.keySet())
		{	
			if (!Manager.selectEntity(this, player))
			{
				continue;
			}

			double mult = players.get(player);
					
			//Knockback
			UtilAction.velocity(player, UtilAlg.getTrajectory(event.getLocation(), player.getLocation()), 2 * mult, false, 0, 0.6 + 0.4 * mult, 2, true);
		}
	}
	
	@EventHandler
	public void clean(UpdateEvent event)  
	{
		if (event.getType() != UpdateType.FAST)
			return;

		Iterator<WitherSkull> skullIterator = _skulls.iterator();
		
		while (skullIterator.hasNext())
		{
			WitherSkull skull = skullIterator.next();
			
			if (!skull.isValid() || skull.getTicksLived() > 60)
			{
				skullIterator.remove();
				skull.remove();
				continue;
			}
		}
	}
	
	@EventHandler
	public void flight(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : getActive())
		{
			if (UtilPlayer.isSpectator(player))
				continue;
			
			player.setAllowFlight(true);
			player.setFlying(true);
			
			if (UtilEnt.isGrounded(player))
				UtilAction.velocity(player, new Vector(0,1,0));
		}
	}
	
	@EventHandler
	public void legendOwner(PlayerJoinEvent event)
	{
		// TODO HARDCODED Wither Morph Database Item Id - 550
		if (Manager.getClientManager().Get(event.getPlayer()).hasPermission(GadgetManager.Perm.LEGEND_MORPH))
		{
			Manager.getInventoryManager().Get(event.getPlayer()).addItem(new ClientItem(new Item(550, getName()), 1));
		}
	}

	public void setWitherData(String text, double healthPercent)
	{
		Iterator<Player> activeIterator = getActive().iterator();
		
		while (activeIterator.hasNext())
		{
			Player player = activeIterator.next();
			
			DisguiseBase disguise = Manager.getDisguiseManager().getDisguise(player);
			
			if (disguise == null || !(disguise instanceof DisguiseWither))
			{
				disableCustom(player, true);
				activeIterator.remove();
				continue;
			}
			
			((DisguiseWither)disguise).setName(text);
			((DisguiseWither)disguise).setHealth((float) (healthPercent * 300));
			Manager.getDisguiseManager().updateDisguise(disguise);
		}		
	}
}