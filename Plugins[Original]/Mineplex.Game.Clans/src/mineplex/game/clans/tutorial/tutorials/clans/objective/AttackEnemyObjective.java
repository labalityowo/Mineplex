package mineplex.game.clans.tutorial.tutorials.clans.objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mineplex.core.common.DefaultHashMap;
import mineplex.core.common.util.C;
import mineplex.core.common.util.EnclosedObject;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.siege.weapon.Cannon;
import mineplex.game.clans.tutorial.TutorialRegion;
import mineplex.game.clans.tutorial.TutorialSession;
import mineplex.game.clans.tutorial.objective.OrderedObjective;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.HoldItemGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.attackenemy.BlowUpWallGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.attackenemy.ClanInfoGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.attackenemy.GetMapGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.attackenemy.LoadCannonGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.attackenemy.MountCannonGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.goals.attackenemy.StealEnemyPotatoesGoal;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class AttackEnemyObjective extends OrderedObjective<ClansMainTutorial>
{
	private Map<String, Cannon> _cannon;
	
	private DefaultHashMap<String, List<Zombie>> _shooters;
	
	public AttackEnemyObjective(ClansMainTutorial clansMainTutorial, ClansManager clansManager, JavaPlugin javaPlugin)
	{
		super(clansMainTutorial, javaPlugin, "Enemy Clans Tutorial", "Attack and raid this enemy!");
		
		_cannon = new HashMap<>();
		_shooters = new DefaultHashMap<>(username -> new ArrayList<>());

		addGoal(new GetMapGoal(this));
		addGoal(new HoldItemGoal(
				this, Material.MAP,
				"Identify Enemy on Map",
				"Find the Red Square on your Map.",
				"Look at your map to help find where the Enemy Clan is. It's marked by " +
						"a " + C.cRed + "Red Square" + C.mBody + ".",
				40
		));
		addGoal(new ClanInfoGoal(this));
		addGoal(new MountCannonGoal(this, clansManager));
		addGoal(new LoadCannonGoal(this));
		addGoal(new BlowUpWallGoal(this));
		addGoal(new StealEnemyPotatoesGoal(this));

		setStartMessageDelay(60);
	}
	
	@Override
	protected void customStart(Player player)
	{
		super.customStart(player);
		
		TutorialSession session = getPlugin().getTutorialSession(player);
		session.setMapTargetLocation(getPlugin().getCenter(session.getRegion(), ClansMainTutorial.Bounds.ENEMY_LAND));
		
		addShooter("Chiss", getPlugin().getPoint(getPlugin().getRegion(player), ClansMainTutorial.Point.NPC_2), player);
		addShooter("defek7", getPlugin().getPoint(getPlugin().getRegion(player), ClansMainTutorial.Point.NPC_1), player);
	}
	
	private void addShooter(String name, Location location, Player active)
	{
		System.out.println("Adding shooter " + _shooters.get(active.getName()).size() + 1);
		
		Zombie shooter = location.getWorld().spawn(location.add(.5, 0, .8), Zombie.class);
		
		shooter.setCustomName(name);
		shooter.setCustomNameVisible(true);
		
		UtilEnt.vegetate(shooter);
		
		shooter.teleport(location);
		shooter.setHealth(shooter.getMaxHealth());
		
		shooter.getEquipment().setItemInHand(new ItemStack(Material.BOW, 1));
		shooter.getEquipment().setChestplate(new ItemStack(Material.GOLD_CHESTPLATE, 1));
		shooter.getEquipment().setBoots(new ItemStack(Material.GOLD_BOOTS, 1));
		shooter.getEquipment().setHelmet(new ItemStack(Material.GOLD_HELMET, 1));
		
		_shooters.get(active.getName()).add(shooter);
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() == UpdateType.FASTER)
		{
			for (Player player : getActivePlayers())
			{
				_shooters.get(player.getName()).forEach(shooter -> {
					shooter.setHealth(shooter.getMaxHealth());
					
					shooter.getEquipment().setItemInHand(new ItemStack(Material.BOW, 1));
					shooter.getEquipment().setChestplate(new ItemStack(Material.GOLD_CHESTPLATE, 1));
					shooter.getEquipment().setBoots(new ItemStack(Material.GOLD_BOOTS, 1));
					shooter.getEquipment().setHelmet(new ItemStack(Material.GOLD_HELMET, 1));
					
					if (player.getLocation().distance(shooter.getLocation()) > 16)
					{
						return;
					}
					
					UtilEnt.LookAt(shooter, player.getEyeLocation());
					
					if (Recharge.Instance.usable(player, "ShotBy" + shooter.getUniqueId().toString()))
					{
						Arrow arrow = shooter.shootArrow();
						
						arrow.setVelocity(UtilAlg.getTrajectory(arrow.getLocation(), player.getEyeLocation()).multiply(1.6));
						
						Recharge.Instance.use(player, "ShotBy" + shooter.getUniqueId().toString(), 500 + UtilMath.r(2000), false, false);
					}
				});
				
				EnclosedObject<Boolean> kill = new EnclosedObject<>(Boolean.FALSE);
				
				_shooters.get(player.getName()).forEach(shooter ->
				{
					if (player.getLocation().distance(shooter.getLocation()) < 5)
					{
						kill.Set(Boolean.TRUE);
					}
				});
				
				if (kill.Get())
				{
					_shooters.get(player.getName()).forEach(shooter -> {
						UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, shooter.getLocation().add(0, .5, 0), new Vector(0d, 0d, 0d), 0.2f, 15, ViewDist.MAX, player);
						UtilParticle.PlayParticle(ParticleType.SMOKE, shooter.getLocation().add(0, .5, 0), new Vector(0d, 0d, 0d), 0.2f, 15, ViewDist.MAX, player);
						player.playSound(player.getLocation(), Sound.LAVA_POP, 1.0f, 1.0f);
						shooter.remove();
					});
					
					_shooters.get(player.getName()).clear();
				}
				
				if (player.getHealth() <= 6)
				{
					player.setHealth(6);
				}
			}
		}
	}
	
	@Override
	protected void customLeave(Player player)
	{
	}
	
	@Override
	public void clean(Player player, TutorialRegion region)
	{
		super.clean(player, region);
		
		System.out.println("Clearing shooters");
		
		_shooters.get(player.getName()).forEach(shooter -> {
			shooter.remove();
		});
		
		_shooters.get(player.getName()).clear();
	}
	
	@Override
	protected void customFinish(Player player)
	{
		System.out.println("Clearing shooters");
		
		_shooters.get(player.getName()).forEach(shooter -> {
			shooter.remove();
		});
		
		_shooters.get(player.getName()).clear();
	}

	public Map<String, Cannon> getCannons()
	{
		return _cannon;
	}

	public DefaultHashMap<String, List<Zombie>> getShooters()
	{
		return _shooters;
	}
}
