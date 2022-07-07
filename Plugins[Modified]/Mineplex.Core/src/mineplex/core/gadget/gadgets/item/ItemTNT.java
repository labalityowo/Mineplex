package mineplex.core.gadget.gadgets.item;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ItemTNT extends ItemGadget
{
	private final Map<TNTPrimed, Player> _tnt = new HashMap<>();

	public ItemTNT(GadgetManager manager)
	{
		super(manager, "TNT",
				UtilText.splitLineToArray(C.cWhite + "Throwing TNT at Mineplex Staff is highly encouraged.", LineFormat.LORE),
				-1,
				Material.TNT, (byte) 0,
				1000, new Ammo("TNT", "20 TNT", Material.TNT, (byte) 0, new String[]{C.cWhite + "20 TNT for you to explode!"}, 500, 20));
	}

	@Override
	public void ActivateCustom(Player player)
	{
		addTNT(player, player.getEyeLocation(), -1, true);

		//Inform
		UtilPlayer.message(player, F.main("Skill", "You threw " + F.skill(getName()) + "."));
	}

	@EventHandler
	public void updateTNT(EntityExplodeEvent event)
	{
		if (!(event.getEntity() instanceof TNTPrimed) || !_tnt.containsKey(event.getEntity()))
		{
			return;
		}

		Map<Player, Double> players = UtilPlayer.getInRadius(event.getLocation(), 8);

		for (Player player : players.keySet())
		{
			if (!Manager.selectEntity(this, player) || Manager.getCastleManager().isInsideCastle(player.getLocation()))
			{
				continue;
			}

			double mult = players.get(player);

			//Knockback
			UtilAction.velocity(player, UtilAlg.getTrajectory(event.getLocation(), player.getLocation()), 1 * mult, false, 0, 0.5 + 0.5 * mult, 10, true);
		}

		// Simulating explosion to prevent water from being evaporated.
		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, event.getLocation(), 0, 0, 0, 0, 1, ViewDist.NORMAL, UtilServer.getPlayers());
	}


	@EventHandler
	public void cleanTNT(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		Iterator<TNTPrimed> iterator = _tnt.keySet().iterator();

		while (iterator.hasNext())
		{
			TNTPrimed tnt = iterator.next();

			if (!tnt.isValid() || tnt.getTicksLived() > 200)
			{
				tnt.remove();
				iterator.remove();
			}
		}
	}

	public void addTNT(Player shooter, Location location, int fuseTicks, boolean applyVelocity)
	{
		TNTPrimed tnt = location.getWorld().spawn(location.add(location.getDirection()), TNTPrimed.class);
		tnt.setYield(0F); // Added in order to prevent water from being evaporated.

		if (fuseTicks >= 0)
		{
			tnt.setFuseTicks(fuseTicks);
		}

		if (applyVelocity)
		{
			UtilAction.velocity(tnt, location.getDirection(), 0.6, false, 0, 0.2, 1, false);
		}

		_tnt.put(tnt, shooter);
	}
}
