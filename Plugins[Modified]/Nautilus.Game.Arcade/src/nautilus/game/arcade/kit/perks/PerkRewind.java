package nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.LinkedList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkRewind extends Perk
{
	private HashMap<Player, LinkedList<Location>> _locMap = new HashMap<Player, LinkedList<Location>>();
	
	public PerkRewind() 
	{
		super("Rewind", new String[] 
				{ 
				C.cYellow + "Right-Click" + C.cGray + " with Star to " + C.cGreen + "Rewind",
				});
	}
		
	@EventHandler
	public void skill(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (!UtilGear.isMat(event.getPlayer().getItemInHand(), Material.NETHER_STAR))
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;
		
		if (!Recharge.Instance.use(player, GetName(), 20000, true, true))
			return;
			
		event.setCancelled(true);

		LinkedList<Location> locs = _locMap.remove(player);
		if (locs == null)
			return;
		
		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_UNFECT, 2f, 2f);
		
		Location current = player.getLocation();
		Location target = locs.getLast();
		
		player.teleport(target);
		
		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
		
		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_UNFECT, 2f, 2f);
		
		while (UtilMath.offset(current, target) > 0.5)
		{
			UtilParticle.PlayParticle(ParticleType.WITCH_MAGIC, current, 0, 1f, 0, 0, 1,
					ViewDist.LONGER, UtilServer.getPlayers());
			current = current.add(UtilAlg.getTrajectory(current, target).multiply(0.1));
		}
	}

	@EventHandler
	public void StoreLocation(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (Manager.GetGame() == null)
			return;

		for (Player cur : Manager.GetGame().GetPlayers(true))
		{
			if (!Kit.HasKit(cur))
				continue;
			
			if (!_locMap.containsKey(cur))
				_locMap.put(cur, new LinkedList<Location>());
			
			_locMap.get(cur).addFirst(cur.getLocation());
			
			if (_locMap.get(cur).size() > 160)
				_locMap.get(cur).removeLast();
		}
	}
}
