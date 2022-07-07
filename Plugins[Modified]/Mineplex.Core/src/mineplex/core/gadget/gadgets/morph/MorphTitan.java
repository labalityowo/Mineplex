package mineplex.core.gadget.gadgets.morph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
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
import mineplex.core.disguise.disguises.DisguiseGuardian;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.recharge.Recharge;
import mineplex.core.recharge.RechargedEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MorphTitan extends MorphGadget
{

	private final Map<Player, ArmorStand> _targets = new HashMap<>();

	public MorphTitan(GadgetManager manager)
	{
		super(manager, "Elder Guardian Morph", UtilText.splitLinesToArray(new String[] 
				{
				C.cGray + "From the depths of the sea, the Elder Guardian possesses powers more amazing than any seen before!",
				C.blankLine,
				"#" + C.cWhite + "Left-Click to use Guardians Laser", 
				}, LineFormat.LORE),
				-13,
				Material.PRISMARINE_CRYSTALS, (byte)0);
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		this.applyArmor(player, message);

		DisguiseGuardian disguise = new DisguiseGuardian(player);
		disguise.setElder(true);
		UtilMorph.disguise(player, disguise, Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		this.removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());

		player.setAllowFlight(false);
		player.setFlying(false);

		Entity ent = _targets.remove(player);
		if (ent != null)
			ent.remove();
	}

	@EventHandler
	public void lazer(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(player))
			return;

		if (!UtilEvent.isAction(event, ActionType.L))
			return;

		if (!Recharge.Instance.use(player, "Guardians Laser", 6000, true, false, "Cosmetics"))
			return;

		DisguiseBase base = Manager.getDisguiseManager().getDisguise(player);
		if (base == null || !(base instanceof DisguiseGuardian))
			return;

		DisguiseGuardian disguise = (DisguiseGuardian)base;

		HashSet<Material> ignore = new HashSet<Material>();
		ignore.add(Material.AIR);

		Location loc = player.getTargetBlock(ignore, 64).getLocation().add(0.5, 0.5, 0.5);

		if (!Manager.selectLocation(this, loc))
		{
			Manager.informNoUse(player);
			return;
		}
		
		ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);

		stand.setVisible(false);
		stand.setGhost(true);
		stand.setGravity(false);

		_targets.put(player, stand);

		disguise.setTarget(stand.getEntityId());

		Manager.getDisguiseManager().updateDisguise(disguise);

		//Fake Head
		UtilEnt.setFakeHead(player, true);
		Recharge.Instance.useForce(player, getName() + " FakeHead", 2000);
	}

	@EventHandler
	public void lazerEnd(RechargedEvent event)
	{
		if (event.GetAbility().equals(getName() + " FakeHead"))
		{
			UtilEnt.setFakeHead(event.GetPlayer(), false);

			//Explode
			ArmorStand stand = _targets.remove(event.GetPlayer());
			if (stand != null)
			{
				UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, stand.getLocation(), 3f, 3f, 3f, 0, 32, ViewDist.MAX, UtilServer.getPlayers());

				HashMap<Player, Double> players = UtilPlayer.getInRadius(stand.getLocation(), 12d);
				for (Player ent : players.keySet())
				{
					if (!Manager.selectEntity(this, ent))
					{
						continue;
					}

					double mult = players.get(ent);

					//Knockback
					UtilAction.velocity(ent, UtilAlg.getTrajectory(stand.getLocation(), ent.getLocation()), 2 * mult, false, 0, 1 + 1 * mult, 10, true);
				}

				//Sound				
				stand.getWorld().playSound(stand.getLocation(), Sound.ZOMBIE_REMEDY, 6f, 0.75f);

				stand.remove();
			}

			//Disguise
			DisguiseBase base = Manager.getDisguiseManager().getDisguise(event.GetPlayer());
			if (base == null || !(base instanceof DisguiseGuardian))
				return;

			DisguiseGuardian disguise = (DisguiseGuardian)base;
			disguise.setTarget(0);

			Manager.getDisguiseManager().updateDisguise(disguise);	
		}	
	}

	@EventHandler
	public void selfParticles(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : _targets.keySet())
		{
			Vector dir = UtilAlg.getTrajectory( player.getLocation().add(0, 1.5, 0), _targets.get(player).getLocation());
			dir.multiply(8);

			UtilParticle.PlayParticle(ParticleType.MAGIC_CRIT, 
					player.getLocation().add(0, 1.5, 0), 
					(float)dir.getX(), 
					(float)dir.getY(), 
					(float)dir.getZ(), 
					1, 0, ViewDist.LONG, UtilServer.getPlayers());

			player.playSound(player.getLocation(), Sound.FIREWORK_TWINKLE2, 2f, 2f);
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
	public void titanOwner(PlayerJoinEvent event)
	{
		if (Manager.getClientManager().Get(event.getPlayer()).hasPermission(GadgetManager.Perm.TITAN_MORPH))
		{
			Manager.getDonationManager().Get(event.getPlayer()).addOwnedUnknownSalesPackage(getName());
		}	
	}

	@EventHandler
	public void clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;

		Iterator<Player> playerIter = _targets.keySet().iterator();

		while (playerIter.hasNext())
		{
			Player player = playerIter.next();

			if (!player.isOnline())
			{
				Entity ent = _targets.get(player);
				ent.remove();
				playerIter.remove();
			}
		}
	}
}