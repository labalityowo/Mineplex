package nautilus.game.arcade.kit.perks;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.kit.Perk;

public class PerkWitherArrows extends Perk
{

	private final Map<Arrow, Player> _proj = new WeakHashMap<>();

	public PerkWitherArrows() 
	{
		super("Fire Storm", new String[] 
				{ 
				C.cYellow + "Hold Block" + C.cGray + " to use " + C.cGreen + "Fire Storm"
				});
	}

	@EventHandler
	public void shoot(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !Manager.GetGame().IsLive())
		{
			return;
		}

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!Kit.HasKit(player))
				continue;
			
			if (!player.isBlocking())
			{
				player.setExp((float) Math.min(0.999, player.getExp()+(1f/20f)));
			}
			else if (player.getExp() > 0)
			{
				player.setExp((float) Math.max(0, player.getExp()-(1f/20f)));

				for (int i=0 ;  i<2 ; i++)
				{
					Arrow arrow = player.getWorld().spawnArrow(
							player.getEyeLocation().add(player.getLocation().getDirection()), 
							player.getLocation().getDirection(), 2, 6);
					
					arrow.setShooter(player);
					
					_proj.put(arrow, player);
				}

				//Effect
				player.getWorld().playSound(player.getLocation(), Sound.FIZZ, 0.1f, 0.5f);
			}
		}
	}

	@EventHandler
	public void update(UpdateEvent event) 
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		Iterator<Arrow> arrowIter = _proj.keySet().iterator();
		
		while (arrowIter.hasNext())
		{
			Arrow arrow = arrowIter.next();
			
			if (!arrow.isValid() || arrow.getTicksLived() > 60 || arrow.getLocation().getY() < 0 || arrow.isOnGround())
			{
				arrow.remove();
				arrowIter.remove();
				
				UtilParticle.PlayParticle(ParticleType.LAVA, arrow.getLocation(), 0, 0, 0, 0, 1,
						ViewDist.MAX, UtilServer.getPlayers());
			}
			else if (arrow.getTicksLived() > 1)
			{
				UtilParticle.PlayParticle(ParticleType.FLAME, arrow.getLocation(), 0, 0, 0, 0, 1,
						ViewDist.MAX, UtilServer.getPlayers());
			}
		}
	}
}
