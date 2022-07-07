package nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.recharge.RechargedEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkSquidSniper extends Perk implements IThrown
{
	private HashMap<Firework, Vector> _fireworks = new HashMap<Firework, Vector>();

	public PerkSquidSniper() 
	{
		super("Ink Sniper", new String[] 
				{
				C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Ink Sniper"
				});
	}
	
	@EventHandler
	public void Recharge(RechargedEvent event)
	{
		if (!event.GetAbility().equals(GetName()))
			return;
		
		event.GetPlayer().playSound(event.GetPlayer().getLocation(), Sound.NOTE_STICKS, 3f, 1f);
	}

	@EventHandler
	public void Shoot(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (event.getPlayer().getItemInHand() == null)
			return;

		if (!event.getPlayer().getItemInHand().getType().toString().contains("_AXE"))
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (!Recharge.Instance.use(player, GetName(), 4500, true, true))
			return;

		event.setCancelled(true);

		UtilInv.Update(player);

		//Firework
		FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.RED).with(Type.BALL_LARGE).trail(false).build();

		try 
		{
			Vector vel = player.getLocation().getDirection().multiply(3);
			//Firework fw = Manager.GetFirework().launchFirework(player.getEyeLocation().subtract(0, 0.5, 0).add(player.getLocation().getDirection()), effect, vel);
			//_fireworks.put(fw, vel);
			
			//Projectile
			//Manager.GetProjectile().AddThrow(fw, player, this, -1, true, true, true, 3d, Manager.GetDisguise());
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.EXPLODE, 1f, 0.75f);
	}
	

	
	@EventHandler
	public void FireworkUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		Iterator<Firework> fwIterator = _fireworks.keySet().iterator();
		
		while (fwIterator.hasNext())
		{
			Firework fw = fwIterator.next();
			
			if (!fw.isValid())
			{
				fwIterator.remove();
				continue;
			}
			
			fw.setVelocity(_fireworks.get(fw));
		}
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		Explode(data);
		
		if (target == null)
			return;

		//Damage Event
		Manager.GetDamage().NewDamageEvent(target, data.getThrower(), null,
				DamageCause.PROJECTILE, 50, false, true, false,
				UtilEnt.getName(data.getThrower()), GetName());
		
		//Recharge
		if (data.getThrower() instanceof Player)
		{
			Player player = (Player)data.getThrower();
			Recharge.Instance.recharge(player, GetName());
			player.playSound(player.getLocation(), Sound.NOTE_STICKS, 3f, 1f);
		}
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		Explode(data);
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		Explode(data);
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	public void Explode(ProjectileUser data)
	{
		if (!(data.getThrown() instanceof Firework))
		{
			data.getThrown().remove();
			return;
		}
		
		Firework fw = (Firework)data.getThrown();
		
		try 
		{
			//Manager.GetFirework().detonateFirework(fw);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}