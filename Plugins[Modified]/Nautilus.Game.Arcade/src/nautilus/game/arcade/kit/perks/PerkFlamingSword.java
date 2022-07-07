package nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkFlamingSword extends Perk
{
	private HashMap<Player, Long> _active = new HashMap<Player, Long>();
	
	public PerkFlamingSword() 
	{
		super("Flaming Sword", new String[] 
				{ 
				"Attacks ignite opponents for 4 seconds.",
				C.cYellow + "Hold Block" + C.cGray + " to use " + C.cGreen + "Inferno"
				});
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void IgniteTarget(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
			return;
		
		Player damager = event.GetDamagerPlayer(false);
		if (damager == null)	return;
		
		if (!Kit.HasKit(damager))
			return;
		
		Manager.GetCondition().Factory().Ignite("Flaming Sword", event.GetDamageeEntity(), damager, 4, false, false);
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
		
		if (!event.getPlayer().getItemInHand().getType().toString().contains("_SWORD"))
			return;
		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		if (!Recharge.Instance.use(player, "Inferno", 4000, true, true))
			return;
		
		_active.put(player, System.currentTimeMillis());

		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill("Inferno") + "."));
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
			
			if (UtilTime.elapsed(_active.get(cur), 1500))
			{
				_active.remove(cur);
				continue;
			}

			//Fire
			Item fire = cur.getWorld().dropItem(cur.getEyeLocation(), ItemStackFactory.Instance.CreateStack(Material.BLAZE_POWDER));
			Manager.GetFire().Add(fire, cur, 0.7, 0, 0.5, 1, "Inferno", false);

			fire.teleport(cur.getEyeLocation());
			double x = 0.07 - (UtilMath.r(14)/100d);
			double y = 0.07 - (UtilMath.r(14)/100d);
			double z = 0.07 - (UtilMath.r(14)/100d);
			fire.setVelocity(cur.getLocation().getDirection().add(new Vector(x,y,z)).multiply(1.6));

			//Effect
			cur.getWorld().playSound(cur.getLocation(), Sound.GHAST_FIREBALL, 0.1f, 1f);
		}
	}

}
