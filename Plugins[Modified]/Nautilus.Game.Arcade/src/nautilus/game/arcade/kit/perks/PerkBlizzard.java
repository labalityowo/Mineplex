package nautilus.game.arcade.kit.perks;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilMath;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.smash.TeamSuperSmash;
import nautilus.game.arcade.kit.Perk;

public class PerkBlizzard extends Perk
{

	private float _energyTick;
	private float _energyPerBall;

	private Map<Projectile, Player> _snowball = new WeakHashMap<>();

	public PerkBlizzard() 
	{
		super("Blizzard", new String[] 
				{ 
				C.cYellow + "Hold Block" + C.cGray + " to use " + C.cGreen + "Blizzard"
				});
	}

	@Override
	public void setupValues()
	{
		_energyTick = getPerkFloat("Energy Per Tick");
		_energyPerBall = getPerkFloat("Energy Per Ball");
	}

	@EventHandler
	public void EnergyUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!Kit.HasKit(player))
				continue;

			if (player.isBlocking())
				continue;
			
			player.setExp((float) Math.min(0.999, player.getExp()+ _energyTick));
		}
	}

	@EventHandler
	public void Snow(UpdateEvent event) 
	{
		if (event.getType() != UpdateType.FASTEST)
			return;

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!player.isBlocking())
				continue;

			if (!Kit.HasKit(player))
				continue;

			//Energy
			if (player.getExp() < 0.1)
				continue;

			player.setExp(Math.max(0, player.getExp() - _energyPerBall));

			for (int i=0 ; i<4 ; i++)
			{
				Snowball snow = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), Snowball.class);
				double x = 0.1 - (UtilMath.r(20)/100d);
				double y = UtilMath.r(20)/100d;
				double z = 0.1 - (UtilMath.r(20)/100d);
				snow.setShooter(player);
				snow.setVelocity(player.getLocation().getDirection().add(new Vector(x,y,z)).multiply(2));
				_snowball.put(snow, player);
			}

			//Effect
			player.getWorld().playSound(player.getLocation(), Sound.STEP_SNOW, 0.1f, 0.5f);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void Snowball(CustomDamageEvent event)
	{		
		if (event.GetCause() != DamageCause.PROJECTILE)
		{
			return;
		}

		Projectile proj = event.GetProjectile();
		if (proj == null) 
		{
			return;
		}

		if (!(proj instanceof Snowball))
		{
			return;
		}

		Player shooter = _snowball.get(proj);
		if (shooter == null)
		{
			return;
		}

		LivingEntity damagee = event.GetDamageeEntity();
		if (damagee == null)
		{
			return;
		}

		event.SetCancelled("Blizzard");
		
		if(TeamSuperSmash.getTeam(Manager, shooter, true).contains(damagee))
		{
			return;
		}

		UtilAction.velocity(damagee, proj.getVelocity().multiply(0.15).add(new Vector(0, 0.15, 0)));
		
		//Damage Event
		if (damagee instanceof Player)
		{
			if (Recharge.Instance.use((Player)damagee, GetName(), 200, false, false))
			{
				Manager.GetDamage().NewDamageEvent(damagee, shooter, null, DamageCause.CUSTOM, 1, false, true, false, shooter.getName(), GetName());
			}
		}
	}

	@EventHandler
	public void SnowballForm(ProjectileHitEvent event)
	{
		if (!(event.getEntity() instanceof Snowball))
			return;

		if (_snowball.remove(event.getEntity()) == null)
			return;

		Manager.GetBlockRestore().snow(event.getEntity().getLocation().getBlock(), (byte) 1, (byte) 7, 2000, 250, 0);
	}
}
