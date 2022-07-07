package nautilus.game.arcade.kit.perks;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.kit.Perk;

public class PerkWitherArrowBlind extends Perk
{
	private ArrayList<Arrow> _arrows = new ArrayList<Arrow>();

	private int _proximityHit;
	
	public PerkWitherArrowBlind(int proximityHit) 
	{
		super("Smoke Arrow", new String[] 
				{
				"Your arrows give Blindness for 4 seconds"
				});
		
		_proximityHit = proximityHit;
	}

	@EventHandler
	public void FireBow(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		if (!(event.getProjectile() instanceof Arrow))
			return;

		Player player = (Player)event.getEntity();
		
		if (!Kit.HasKit(player))
			return;

		//Start 
		_arrows.add((Arrow)event.getProjectile());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
			return;
		
		if (event.GetProjectile() == null)
			return;

		if (event.GetDamagerPlayer(true) == null)
			return;

		if (!(event.GetProjectile() instanceof Arrow))
			return;
		
		Arrow arrow = (Arrow)event.GetProjectile();
		
		if (!_arrows.remove(arrow))
			return;
		
		Manager.GetCondition().Factory().Blind(GetName(), event.GetDamageeEntity(), null, 4, 0, false, false, false);
		
		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, arrow.getLocation(), 0, 0, 0, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
		
		event.SetCancelled("Smoke Arrow");
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Iterator<Arrow> arrowIterator = _arrows.iterator(); arrowIterator.hasNext();) 
		{
			Arrow arrow = arrowIterator.next();
			
			//Proxy
			if (_proximityHit > 0 && getWitherTeam() != null)
			{
				boolean hit = false;
				for (Player player : getWitherTeam().GetPlayers(true))
				{
					if (UtilMath.offset(player.getLocation().add(0, 3, 0), arrow.getLocation()) < _proximityHit)
					{
						Manager.GetCondition().Factory().Blind(GetName(), player, null, 4, 0, false, false, false);
						hit = true;
					}
				}
				
				if (hit)
				{
					UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, arrow.getLocation(), 0, 0, 0, 0, 1,
							ViewDist.MAX, UtilServer.getPlayers());
					arrowIterator.remove();
					arrow.remove();
					continue;
				}
					
			}

			//Dead
			if (arrow.isDead() || !arrow.isValid() || arrow.getTicksLived() > 120 || arrow.isOnGround())
			{
				arrow.remove();
				arrowIterator.remove();
			}
			//Particle
			else
			{
				UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, arrow.getLocation(), 0, 0, 0, 0, 1,
						ViewDist.MAX, UtilServer.getPlayers());
			}
		}
	}
	
	public GameTeam getWitherTeam()
	{
		if (Manager.GetGame() == null)
			return null;
		
		return Manager.GetGame().GetTeam(ChatColor.RED);
	}
}
