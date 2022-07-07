package nautilus.game.arcade.game.games.typewars.spells;

import java.util.Iterator;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.typewars.Minion;
import nautilus.game.arcade.game.games.typewars.MinionSize;
import nautilus.game.arcade.game.games.typewars.Spell;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class SpellFirebomb extends Spell
{
	
	public SpellFirebomb(ArcadeManager manager)
	{
		super(manager, "Firebomb", 4, Material.BLAZE_POWDER, 2000L, 5, 0, false);
	}

	@Override
	public boolean execute(Player player, Location location)
	{
		Iterator<Minion> minionIterator = getTypeWars().getActiveMinions().iterator();
		while(minionIterator.hasNext())
		{
			Minion minion = minionIterator.next();
			
			if(UtilMath.offset2d(minion.getEntity().getLocation(), location) > 3)
				continue;
			
			if(getTypeWars().GetTeam(player) == minion.getTeam())
				continue;
			
			if(minion.getType().getSize() != MinionSize.EASY)
			{
				UtilPlayer.message(player, F.main("Game", F.game(minion.getName()) + " is to strong to be killed with that."));
				continue;
			}
			
			minion.despawn(player, true);
			if(!minion.hasLives())
			{
				minionIterator.remove();
				getTypeWars().getDeadMinions().add(minion);
			}
		}
		return true;
	}

	@Override
	public ParticleType hit()
	{
		return ParticleType.HUGE_EXPLOSION;
	}
}
