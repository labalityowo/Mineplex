package nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.recharge.Recharge;

import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.event.PerkLeapEvent;

public class PerkLeap extends Perk
{

	private final String _name;
	private final double _power, _heightMax;
	private final long _recharge;
	private final int _maxUses;
	
	private final Map<String, Integer> _uses = new HashMap<>();
	
	public PerkLeap(String name, double power, double heightLimit, long recharge) 
	{
		super("Leaper", new String[]  
				{
				C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + name
				});
		
		_name = name;
		_power = power;
		_heightMax = heightLimit;
		_recharge = recharge;
		_maxUses = 0;
	}
	
	public PerkLeap(String name, double power, double heightLimit, long recharge, int uses) 
	{
		super("Leaper", new String[]  
				{
				C.cYellow + "Right-Click" + C.cGray + " with Axe to " + C.cGreen + name + C.cGray + "  (" + C.cWhite + uses + " Charges" + C.cGray + ")"
				});
		
		_name = name;
		_power = power;
		_heightMax = heightLimit;
		_recharge = recharge;
		_maxUses = uses;
	}

	@EventHandler(ignoreCancelled = true)
	public void Leap(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R) || UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (itemStack == null || !UtilItem.isAxe(itemStack) || !hasPerk(player))
		{
			return;
		}
		
		//Check Uses
		if (_maxUses > 0)
		{
			int count = _uses.computeIfAbsent(player.getName(), k -> _maxUses);
			
			if (count <= 0)
			{
				UtilPlayer.message(player, F.main("Skill", "You cannot use " + F.skill(_name) + " anymore."));
				return;
			}
		}
		
		//Energy
		if (!Recharge.Instance.use(player, _name, _recharge, false, true))
		{
			return;
		}

		//Use Use
		if (_maxUses > 0)
		{
			int count = _uses.get(player.getName());
			count--;
			
			player.setExp(Math.min(0.99f, (float)count/(float)_maxUses));
			player.getItemInHand().setAmount(count);

			_uses.put(player.getName(), count);
		}
		
		Entity ent = player;
		
		if (player.getVehicle() != null)
			if (player.getVehicle() instanceof Horse)
				ent = player.getVehicle();
		
		UtilAction.velocity(ent, _power, 0.2, _heightMax, true);
		
		player.setFallDistance(0);
		
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(_name) + "."));
		
		player.playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 8);
		
		UtilServer.CallEvent(new PerkLeapEvent(player));
		Manager.getCosmeticManager().getGadgetManager().playLeapEffect(player);
	}

	@EventHandler
	public void removeDataOnQuit(PlayerQuitEvent event)
	{
		_uses.remove(event.getPlayer().getName());
	}

	@EventHandler
	public void removeDataOnDeath(PlayerDeathEvent event)
	{
		_uses.remove(event.getEntity().getName());
	}

	@Override
	public void registeredEvents()
	{
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!hasPerk(player))
			{
				continue;
			}

			for (ItemStack itemStack : player.getInventory().getContents())
			{
				if (UtilItem.isAxe(itemStack))
				{
					itemStack.setAmount(_maxUses);
					break;
				}
			}
		}
	}

	@Override
	public void unregisteredEvents()
	{
		_uses.clear();
	}
}
