package nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilGear;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkInfernoFinn extends Perk
{
	private HashMap<Player, Long> _active = new HashMap<Player, Long>();
	
	public PerkInfernoFinn() 
	{
		super("Inferno", new String[] 
				{ 
				C.cYellow + "Block" + C.cGray + " with Gold Sword to use " + C.cGreen + "Inferno"
				});
	}
	
	@EventHandler
	public void Activate(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		if (UtilBlock.usable(event.getClickedBlock()))
			return;
		
		if (!event.getPlayer().getItemInHand().getType().toString().contains("GOLD_SWORD"))
			return;
		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		if (!Recharge.Instance.use(player, "Inferno", 2000, true, true))
			return;
		
		_active.put(player, System.currentTimeMillis());
	}
	
	@EventHandler
	public void Update(UpdateEvent event)  
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player cur : UtilServer.getPlayers())
		{
			if (!_active.containsKey(cur))
				continue;
			
			if (!cur.isBlocking())
			{
				_active.remove(cur);
				continue;
			}
			
			if (UtilTime.elapsed(_active.get(cur), 1000))
			{
				_active.remove(cur);
				continue;
			}

			//Fire
			Item fire = cur.getWorld().dropItem(cur.getEyeLocation(), ItemStackFactory.Instance.CreateStack(Material.BLAZE_POWDER));
			Manager.GetFire().Add(fire, cur, 0.7, 0, 2, 2, "Inferno", false);

			fire.teleport(cur.getEyeLocation());
			double x = 0.07 - (UtilMath.r(14)/100d);
			double y = 0.07 - (UtilMath.r(14)/100d);
			double z = 0.07 - (UtilMath.r(14)/100d);
			fire.setVelocity(cur.getLocation().getDirection().add(new Vector(x,y,z)).multiply(1.6));

			//Effect
			cur.getWorld().playSound(cur.getLocation(), Sound.GHAST_FIREBALL, 0.1f, 1f);
		}
	}

	@EventHandler
	public void Refresh(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;
		
		for (Player player : UtilServer.getPlayers())
		{
			if (_active.containsKey(player))
				continue;
			
			if (!Kit.HasKit(player))
				continue;
			
			if (!UtilGear.isMat(player.getItemInHand(), Material.GOLD_SWORD))
				continue;
			
			if (player.getItemInHand().getDurability() == player.getItemInHand().getType().getMaxDurability())
				player.getItemInHand().setDurability((short) (player.getItemInHand().getDurability()+1));
			else
				player.getItemInHand().setDurability((short) (player.getItemInHand().getDurability()-1));
		}
	}
}
