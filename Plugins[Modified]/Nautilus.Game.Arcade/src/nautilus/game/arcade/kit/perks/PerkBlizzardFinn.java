package nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.WeakHashMap;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.kit.Perk;

public class PerkBlizzardFinn extends Perk
{
	private HashMap<Player, Long> _active = new HashMap<Player, Long>();
	private WeakHashMap<Projectile, Player> _snowball = new WeakHashMap<Projectile, Player>();

	public PerkBlizzardFinn() 
	{
		super("Blizzard", new String[] 
				{ 
				C.cYellow + "Block" + C.cGray + " with Diamond Sword to use " + C.cGreen + "Blizzard"
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

		if (!event.getPlayer().getItemInHand().getType().toString().contains("DIAMOND_SWORD"))
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (!Recharge.Instance.use(player, GetName(), 4000, true, true))
			return;

		_active.put(player, System.currentTimeMillis());
	}

	@EventHandler
	public void Update(UpdateEvent event)  
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (!_active.containsKey(player))
				continue;

			if (!player.isBlocking())
			{
				_active.remove(player);
				continue;
			}

			if (UtilTime.elapsed(_active.get(player), 1000))
			{
				_active.remove(player);
				continue;
			}

			//Snowball
			for (int i=0 ; i<4 ; i++)
			{
				Snowball snow = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), Snowball.class);
				double x = 0.1 - (UtilMath.r(20)/100d);
				double y = UtilMath.r(20)/100d;
				double z = 0.1 - (UtilMath.r(20)/100d);
				snow.setVelocity(player.getLocation().getDirection().add(new Vector(x,y,z)).multiply(2));
				_snowball.put(snow, player);
			}

			//Effect
			player.getWorld().playSound(player.getLocation(), Sound.STEP_SNOW, 0.1f, 0.5f);
		}
	}

	@EventHandler
	public void RemoveSnowball(UpdateEvent event)  
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<Projectile> projIterator = _snowball.keySet().iterator();

		while (projIterator.hasNext())
		{
			Projectile proj = projIterator.next();

			if (proj.getTicksLived() > 20)
			{
				proj.remove();
				projIterator.remove();
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void Snowball(CustomDamageEvent event)
	{		
		if (event.GetCause() != DamageCause.PROJECTILE)
			return;

		Projectile proj = event.GetProjectile();
		if (proj == null)		return;

		if (!(proj instanceof Snowball))
			return;

		if (!_snowball.containsKey(proj))
			return;

		LivingEntity damagee = event.GetDamageeEntity();
		if (damagee == null)	return;

		if (damagee instanceof Player)
		{
			event.SetCancelled("Player Cancel");
			return;
		}

		damagee.setVelocity(proj.getVelocity().multiply(0.15).add(new Vector(0, 0.15, 0)));

		event.AddMod(GetName(), GetName(), 0.6, false);
	}
}
