package nautilus.game.arcade.game.games.smash.perks.guardian;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.smash.TeamSuperSmash;
import nautilus.game.arcade.game.games.smash.perks.SmashUltimate;

public class SmashGuardian extends SmashUltimate
{

	private static final int GUARDIANS = 4;
	
	private int _radius;
	private int _damage;

	private Set<SmashAnimationData> _data = new HashSet<>();

	public SmashGuardian()
	{
		super("Rise of the Guardian", new String[] {}, Sound.AMBIENCE_THUNDER, 0);
	}

	@Override
	public void setupValues()
	{
		super.setupValues();

		_radius = getPerkInt("Radius");
		_damage = getPerkInt("Damage");
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);

		Manager.GetGame().CreatureAllowOverride = true;

		_data.add(new SmashAnimationData(Manager, player, GUARDIANS, getLength()));

		Manager.GetGame().CreatureAllowOverride = false;
	}
	
	@Override
	public void cancel(Player player)
	{
		super.cancel(player);
		
		Iterator<SmashAnimationData> iterator = _data.iterator();
		
		while (iterator.hasNext())
		{
			SmashAnimationData data = iterator.next();
			
			if (data.getPlayer().equals(player))
			{
				Map<Player, Double> inRadius = UtilPlayer.getInRadius(data.getTarget().getLocation(), _radius);
				List<Player> team = TeamSuperSmash.getTeam(Manager, player, true);
				for (Player other : inRadius.keySet())
				{
					if (team.contains(other))
					{
						continue;
					}
					
					Manager.GetDamage().NewDamageEvent(other, player, null, DamageCause.CUSTOM, _damage * inRadius.get(other), true, true, false, player.getName(), GetName());
				}
				
				data.getTarget().getWorld().strikeLightningEffect(data.getTarget().getLocation());
				UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, data.getTarget().getLocation().add(0, 2, 0), _radius, 1, _radius, 1F, 30, ViewDist.MAX);
				player.getWorld().playSound(data.getTarget().getLocation(), Sound.EXPLODE, 5, 0.5F);

				data.getElder().remove();
				data.getTarget().remove();
				
				for (ArmorStand guardian : data.getGuardians())
				{
					guardian.remove();
				}
				
				iterator.remove();
			}
		}
	}
	
	@Override
	public boolean isUsable(Player player)
	{
		boolean grounded = UtilEnt.isGrounded(player);
		
		if (!grounded)
		{
			player.sendMessage(F.main("Game", "You need to be on the ground to use " + F.skill(GetName()) + "."));
		}
		
		return grounded;
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		for (SmashAnimationData data : _data)
		{
			data.update();
		}
	}

}
