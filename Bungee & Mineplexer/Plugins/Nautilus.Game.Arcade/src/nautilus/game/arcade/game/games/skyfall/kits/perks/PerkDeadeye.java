package nautilus.game.arcade.game.games.skyfall.kits.perks;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.game.games.skyfall.HomingArrow;
import nautilus.game.arcade.kit.Perk;

/**
 * Perk that lets Players shot
 * homing arrows which follow
 * flying players as soon as they
 * get in their range.
 *
 * @author xXVevzZXx
 */
public class PerkDeadeye extends Perk
{
	private ArrayList<HomingArrow> _arrows = new ArrayList<>();
	
	private int _range;
	
	/**
	 * Standard Constructor for PerkDeadeye
	 * 
	 * @param name of the perk
	 * @param range of the homing arrow
	 */
	public PerkDeadeye(String name, int range)
	{
		super(name, new String[]
		{
			C.cWhite + "Arrows will " + C.cGreen + "follow" + C.cWhite + " nearby flying enemies"
		});
		
		_range = range;
	}
	
	@EventHandler
	public void arrowShoot(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		
		Player shooter = (Player) event.getEntity();
		
		if (!hasPerk(shooter))
			return;
		
		_arrows.add(new HomingArrow(shooter, Manager.GetGame(), (Arrow) event.getProjectile(), null, _range, 15));
	}
	
	@EventHandler
	public void updateArrows(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		Iterator<HomingArrow> arrows = _arrows.iterator();
		while (arrows.hasNext())
		{
			HomingArrow arrow = arrows.next();
			if (!arrow.foundPlayer())
			{
				Player player = arrow.findPlayer();
				if (player != null)
					UtilPlayer.message(player, F.main(GetName(), "You have been targeted by a homing arrow!"));
				
				continue;
			}
			arrow.update();
			
			if (arrow.canRemove())
				arrows.remove();
		}
	}

}
