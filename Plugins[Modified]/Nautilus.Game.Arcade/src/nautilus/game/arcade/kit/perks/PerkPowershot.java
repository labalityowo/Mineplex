package nautilus.game.arcade.kit.perks;

import java.util.Iterator;
import java.util.WeakHashMap;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkPowershot extends Perk
{
	private WeakHashMap<Player, Integer> _charge = new WeakHashMap<Player, Integer>();
	private WeakHashMap<Player, Long> _chargeLast = new WeakHashMap<Player, Long>();
	
	private WeakHashMap<Arrow, Integer> _arrows = new WeakHashMap<Arrow, Integer>();

	private int _max;
	private long _tick;
	
	public PerkPowershot(int max, long tick) 
	{
		super("Power Shot", new String[] 
				{
				C.cYellow + "Charge" + C.cGray + " your Bow to use " + C.cGreen + "Power Shot",
				"Arrows deal up to +15 damage"
				});
		
		_max = max;
		_tick = tick;
	}

	@EventHandler
	public void DrawBow(PlayerInteractEvent event)
	{		
		Player player = event.getPlayer();

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (player.getItemInHand() == null || player.getItemInHand().getType() != Material.BOW)
			return;

		if (!Kit.HasKit(player))
			return;

		if (!player.getInventory().contains(Material.ARROW))
			return;

		if (event.getClickedBlock() != null)
			if (UtilBlock.usable(event.getClickedBlock()))
				return;

		//Start Charge
		_charge.put(player, 0);
		_chargeLast.put(player, System.currentTimeMillis());
	}

	@EventHandler
	public void ChargeBow(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player cur : UtilServer.getPlayers())
		{
			//Not Charging
			if (!_charge.containsKey(cur))
				continue;

			//Max Charge
			if (_charge.get(cur) >= _max)
				continue;

			//Charge Interval
			if (_charge.get(cur) == 0)
			{
				if (!UtilTime.elapsed(_chargeLast.get(cur), 1000))
					continue;
			}
			else
			{
				if (!UtilTime.elapsed(_chargeLast.get(cur), _tick))
					continue;
			}

			//No Longer Holding Bow
			if (cur.getItemInHand() == null || cur.getItemInHand().getType() != Material.BOW)
			{
				_charge.remove(cur);
				_chargeLast.remove(cur);
				continue;
			}

			//Increase Charge
			_charge.put(cur, _charge.get(cur) + 1);
			_chargeLast.put(cur, System.currentTimeMillis());

			//Effect
			cur.playSound(cur.getLocation(), Sound.CLICK, 1f, 1f + (0.1f * _charge.get(cur)));
		}
	}

	@EventHandler
	public void FireBow(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		if (!(event.getProjectile() instanceof Arrow))
			return;

		Player player = (Player)event.getEntity();

		if (!_charge.containsKey(player))
			return;

		//Start 
		_arrows.put((Arrow)event.getProjectile(), _charge.remove(player));
		_chargeLast.put(player, System.currentTimeMillis());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void Damage(CustomDamageEvent event)
	{
		if (event.GetProjectile() == null)
			return;

		if (event.GetDamagerPlayer(true) == null)
			return;

		if (!(event.GetProjectile() instanceof Arrow))
			return;
		
		Arrow arrow = (Arrow)event.GetProjectile();
		
		if (!_arrows.containsKey(arrow))
			return;
		
		int charge = _arrows.remove(arrow);
		
		event.AddMod("Power Shot", "Power Shot", charge * 3, true);
	}

	@EventHandler
	public void Clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Iterator<Arrow> arrowIterator = _arrows.keySet().iterator(); arrowIterator.hasNext();) 
		{
			Arrow arrow = arrowIterator.next();

			if (arrow.isDead() || !arrow.isValid())
				arrowIterator.remove();
		}
	}

	@EventHandler
	public void Quit(PlayerQuitEvent event) 
	{
		Player player = event.getPlayer();

		_charge.remove(player);
		_chargeLast.remove(player);
	}
}
