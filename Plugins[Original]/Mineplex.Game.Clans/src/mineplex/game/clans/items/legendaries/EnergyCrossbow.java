package mineplex.game.clans.items.legendaries;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.RGBData;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilCollections;
import mineplex.core.common.util.UtilColor;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTrig;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;

/*
 @deprecated Code probably doesn't work and needs a rewrite
 */
@Deprecated
public class EnergyCrossbow extends LegendaryItem
{
	private static final List<Vector> PRE_CALCULATED_SPHERE = Collections.unmodifiableList(
		UtilTrig.GetSpherePoints(new Vector(0, 0, 0), 1.8d, 1.8d, true, .4d)
	);

	private long _interactWait;
	
	public EnergyCrossbow()
	{
		super("Energy Crossbow", UtilText.splitLinesToArray(new String[]
		{
			C.cWhite + "Legend says ",
			" ",
			"#" + C.cYellow + "Right-Click" + C.cWhite + " to fire Crossbow."
		}, LineFormat.LORE), Material.RECORD_6);
	}
	
	@Override
	public void update(Player wielder)
	{
		if (timeSinceLastBlock() < 98 && (System.currentTimeMillis() - _interactWait) >= 98)
		{
			if (Recharge.Instance.use(wielder, "Crossbow", 6500, true, true))
			{
				fire(wielder);
				
				_interactWait = System.currentTimeMillis();
			}
		}
	}
	
	private void fire(final Player player)
	{
		UtilServer.RegisterEvents(new Listener()
		{
			private Location _lastLoc;
			
			private Arrow _arrow;
			private Player _player;
			
			private RGBData[] colors = { UtilColor.RgbLightRed, UtilColor.RgbLightRed.Lighten(), UtilColor.RgbLightRed.Darken() };
			
			{
				// Pretty sure this won't work
				_player = player;
				
				Arrow arrow = _player.shootArrow();
				
				arrow.setVelocity(arrow.getVelocity().multiply(3.4444444444444));
				
				arrow.setShooter(_player);
				
				_arrow = arrow;
				
				_player.playSound(_arrow.getLocation(), Sound.BAT_TAKEOFF, 0.1f, 2.f);
				_player.playSound(_arrow.getLocation(), Sound.ZOMBIE_WOODBREAK, 0.5f, .5f);
			}
			
			@EventHandler
			public void update(UpdateEvent event)
			{
				if (_arrow == null || _arrow.isDead())
				{
					HandlerList.unregisterAll(this);
					
					return;
				}
				
				if (_lastLoc != null)
				{
					Location lastLoc = _lastLoc.clone();
					
					while (UtilMath.offset(lastLoc, _arrow.getLocation()) > 0.1)
					{
						lastLoc.add(UtilAlg.getTrajectory(lastLoc, _arrow.getLocation()).multiply(0.1));

						UtilParticle.PlayParticleToAll(ParticleType.RED_DUST, lastLoc, UtilCollections.random(colors).ToVector(), 1f, 0, ViewDist.MAX);
					}
				}
				
				_lastLoc = _arrow.getLocation();
			}
			
			private void hit()
			{
				HandlerList.unregisterAll(this);
				
				for (Vector vector : PRE_CALCULATED_SPHERE)
				{
					UtilParticle.PlayParticleToAll(ParticleType.RED_DUST, _arrow.getLocation().add(vector), UtilColor.RgbLightRed.ToVector(), 1.0f, 0, ViewDist.MAX);
				}
				
				HashMap<LivingEntity, Double> targets = UtilEnt.getInRadius(_arrow.getLocation(), 3.d);
				for (LivingEntity entity : targets.keySet())
				{
					if (entity.equals(_arrow.getShooter()))
						continue;

					ClansManager.getInstance().getDamageManager().NewDamageEvent(entity, _player, _arrow, 
							DamageCause.CUSTOM, 8, true, true, false,
							_player.getName(), "Energy Crossbow");
				}
				
				_arrow.remove();
				_arrow = null;
			}
			
			@EventHandler
			public void projectileHit(ProjectileHitEvent event)
			{
				if (!event.getEntity().equals(_arrow))
				{
					return;
				}
				
				ClansManager.getInstance().runSyncLater(this::hit, 1);
			}
			
			@EventHandler
			public void entityHurt(EntityDamageByEntityEvent event)
			{
				if (!event.getDamager().equals(_arrow))
				{
					return;
				}
				
				if (event.getEntity().equals(_arrow.getShooter()) || !(event.getEntity() instanceof LivingEntity))
				{
					_arrow.remove();
					_arrow = null;
					
					return;
				}
				
				event.setCancelled(true);
				
				ClansManager.getInstance().getDamageManager().NewDamageEvent((LivingEntity) event.getEntity(), _player, _arrow, 
						DamageCause.CUSTOM, 1.5d, true, true, false,
						_player.getName(), "Energy Crossbow");
				
				hit();
			}
		});
	}
}