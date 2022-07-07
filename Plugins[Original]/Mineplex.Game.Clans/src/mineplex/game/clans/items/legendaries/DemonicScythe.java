package mineplex.game.clans.items.legendaries;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.recharge.Recharge;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.ClansUtility;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class DemonicScythe extends LegendaryItem
{
	private long _interactWait;
	
	static
	{
		UtilServer.RegisterEvents(new Listener()
		{
			@EventHandler(priority = EventPriority.LOWEST)
			public void onDamagedByFireball(CustomDamageEvent event)
			{
				Projectile proj = event.GetProjectile();
				if (proj != null && UtilEnt.hasFlag(proj, "DemonicScythe.Projectile"))
				{
					event.SetCancelled("Unnecessary Scythe Damage");
					return;
				}
			}
			
			@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
			public void onHit(ProjectileHitEvent event)
			{
				if (event.getEntity() instanceof WitherSkull)
				{
					WitherSkull skull = (WitherSkull) event.getEntity();
					
					if (!UtilEnt.hasFlag(skull, "DemonicScythe.Projectile"))
					{
						return;
					}
					Location hit = skull.getLocation();
					boolean inverted = skull.isCharged();
					skull.remove();
					if (skull.getShooter() == null)
					{
						return;
					}
					Player shooter = (Player) skull.getShooter();
					
					for (Entity e : skull.getNearbyEntities(3, 3, 3))
					{
						if (e instanceof LivingEntity)
						{
							LivingEntity entity = (LivingEntity) e;
							if (UtilEnt.hasFlag(entity, "LegendaryAbility.IgnoreMe"))
							{
								continue;
							}
							if (ClansManager.getInstance().getClanUtility().isSafe(entity.getLocation()))
							{
								continue;
							}
							if (e instanceof Player)
							{
								Player target = (Player) e;
								if (ClansManager.getInstance().hasTimer(target))
								{
									continue;
								}
								if (ClansManager.getInstance().isInClan(shooter) && ClansManager.getInstance().getClan(shooter).isMember(target))
								{
									continue;
								}
								if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR)
								{
									continue;
								}
								if (ClansManager.getInstance().getIncognitoManager().Get(target).Hidden)
								{
									continue;
								}
								if (ClansManager.getInstance().isInClan(shooter) && ClansManager.getInstance().getClan(shooter).isAlly(ClansManager.getInstance().getClan(target)))
								{
									continue;
								}
								if (target.getEntityId() == shooter.getEntityId())
								{
									continue;
								}
							}
							
							//Damage Event
							ClansManager.getInstance().getDamageManager().NewDamageEvent(entity, shooter, null, 
									DamageCause.CUSTOM, 8, false, true, false,
									shooter.getName(), "Wither Skull");	

							//Velocity
							Vector trajectory;
							if (inverted)
							{
								trajectory = UtilAlg.getTrajectory2d(entity.getLocation().toVector(), hit.toVector());
							}
							else
							{
								trajectory = UtilAlg.getTrajectory2d(hit.toVector(), entity.getLocation().toVector());
							}
							UtilAction.velocity(entity,
									trajectory, 
									2.6, true, 0, 0.2, 1.4, true);
							
							//Condition
							ClansManager.getInstance().getCondition().Factory().Falling("Wither Skull", entity, shooter, 10, false, true);
						}
					}
				}
			}
		});
	}
	
	public DemonicScythe()
	{
		super("Scythe of the Fallen Lord", new String[]
		{
			C.cWhite + "An old blade fashioned of nothing more",
			C.cWhite + "stray bones, brave adventurers have",
			C.cWhite + "imbued it with the remnant powers of a",
			C.cWhite + "dark and powerful foe.",
			" ",
			C.cWhite + "Deals " + C.cYellow + "8 Damage" + C.cWhite + " with attack",
			C.cYellow + "Attack" + C.cWhite + " to use " + C.cGreen + "Leach Health",
			C.cYellow + "Right Click" + C.cWhite + " to use " + C.cGreen + "Skull Launcher",
			C.cYellow + "Shift-Right Click" + C.cWhite + " to use " + C.cGreen + "Gravity Skull Launcher",
		}, Material.RECORD_8);
	}
	
	private boolean isTeammate(Entity attacker, Entity defender)
	{
		if (attacker == null || defender == null) return false;
		// Don't count attacks towards teammates
		if (attacker instanceof Player && defender instanceof Player)
		{
			ClansUtility.ClanRelation relation = ClansManager.getInstance().getRelation((Player) attacker, (Player) defender);
			if (relation == ClansUtility.ClanRelation.ALLY
					|| relation == ClansUtility.ClanRelation.SAFE
					|| relation == ClansUtility.ClanRelation.SELF)
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void update(Player wielder)
	{
		if (timeSinceLastBlock() >= 98 || (System.currentTimeMillis() - _interactWait) < 98)
		{
			return;
		}
		if (ClansManager.getInstance().hasTimer(wielder))
		{
			UtilPlayer.message(wielder, F.main("Clans", "You are not allowed to fire skulls whilst protected from PvP. Run " + F.elem("/pvp") + " to enable PvP!"));
			return;
		}
		if (ClansManager.getInstance().getClanUtility().getClaim(wielder.getLocation()) != null && ClansManager.getInstance().getClanUtility().getClaim(wielder.getLocation()).isSafe(wielder.getLocation()))
		{
			UtilPlayer.message(wielder, F.main("Clans", "You are not allowed to fire skulls whilst in a safe zone."));
			return;
		}
		if (!Recharge.Instance.use(wielder, "Scythe Skull Launcher", 9000, true, false))
		{
			return;
		}
		WitherSkull skull = wielder.launchProjectile(WitherSkull.class);
		UtilEnt.addFlag(skull, "DemonicScythe.Projectile");
		if (wielder.isSneaking())
		{
			skull.setCharged(true);
		}
		skull.setBounce(false);
		skull.setIsIncendiary(false);
		skull.setYield(0);
		_interactWait = System.currentTimeMillis();
	}

	@Override
	public void onAttack(CustomDamageEvent event, Player wielder)
	{
		if (event.isCancelled())
		{
			return;
		}
		if (ClansManager.getInstance().isSafe(wielder))
		{
			return;
		}
		if (event.GetDamageeEntity() instanceof Player && ClansManager.getInstance().isSafe(event.GetDamageePlayer()))
		{
			return;
		}
		if (wielder.getGameMode().equals(GameMode.CREATIVE))
		{
			return;
		}
		if (isTeammate(wielder, event.GetDamageeEntity()))
		{
			return;
		}
		if (wielder.getHealth() <= 0)
		{
			return;
		}
		event.AddMod("Scythe of the Fallen Lord", 7);
		if (!(event.GetDamageeEntity() instanceof Horse) && Recharge.Instance.use(wielder, "Demonic Scythe Heal", 500, false, false))
		{
			wielder.setHealth(Math.min(wielder.getMaxHealth(), wielder.getHealth() + 3));
		}
	}
}