package nautilus.game.arcade.game.games.typewars.spells;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilParticle.ParticleType;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.typewars.Minion;
import nautilus.game.arcade.game.games.typewars.MinionSize;
import nautilus.game.arcade.game.games.typewars.Spell;

public class SpellSniper extends Spell
{

	public SpellSniper(ArcadeManager manager)
	{
		super(manager, "Sniper spell", 4, Material.ARROW, 2000L, 0, 0, false);
	}

	@Override
	public ParticleType trail()
	{
		return ParticleType.EXPLODE;
	}

	@Override
	public boolean execute(Player player, Location location)
	{
		Iterator<Minion> minionIterator = getTypeWars().getActiveMinions().iterator();
		
		ArrayList<Location> locs = UtilShapes.getLinesDistancedPoints(player.getEyeLocation(), location, 0.5);
		while(minionIterator.hasNext())
		{
			Minion minion = minionIterator.next();
			
			for(Location loc : locs)
			{
				if(UtilMath.offset2d(minion.getEntity().getLocation(), loc) > 1)
					continue;
				
				if(getTypeWars().GetTeam(player) == minion.getTeam())
					continue;
				
				minion.despawn(player, true);
				if(!minion.hasLives())
				{
					minionIterator.remove();
					getTypeWars().getDeadMinions().add(minion);
				}
				break;
			}
		}
		return true;
	}

}
