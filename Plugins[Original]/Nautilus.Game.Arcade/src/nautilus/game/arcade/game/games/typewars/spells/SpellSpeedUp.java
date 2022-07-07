package nautilus.game.arcade.game.games.typewars.spells;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.typewars.Minion;
import nautilus.game.arcade.game.games.typewars.Spell;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SpellSpeedUp extends Spell
{

	public SpellSpeedUp(ArcadeManager manager)
	{
		super(manager, "Speed Boost", 6, Material.FEATHER, 5000L, 0, 0, false);
	}

	@Override
	public ParticleType trail()
	{
		return ParticleType.HAPPY_VILLAGER;
	}

	@Override
	public boolean execute(Player player, Location location)
	{
		
		location.getWorld().playSound(location.clone().add(0.5, 0.5, 0.5), Sound.FIREWORK_BLAST, 10F, 2.0F);
		
		for(int c = -1; c <= 1; c++)
		{
			for(int i = -10; i <= 10; i = i + 2)
			{
				for(double x = 0.2; x <= 2; x = x + 0.2)
				{
					Location loc = location.clone().add(i + x, 2*(x-1)*(x-1)*(x-1) -2*(x-1), c);
					loc.add(0, 0.3, 0);
					UtilParticle.PlayParticle(UtilParticle.ParticleType.WITCH_MAGIC, loc, 0, 0, 0, 0, 1, ViewDist.LONG, UtilServer.getPlayers());
					
					Location otherLocation = location.clone().add(c, 2*(x-1)*(x-1)*(x-1) -2*(x-1), i + x);
					otherLocation.add(0, 0.3, 0);
					UtilParticle.PlayParticle(UtilParticle.ParticleType.WITCH_MAGIC, otherLocation, 0, 0, 0, 0, 1, ViewDist.LONG, UtilServer.getPlayers());
				}
			}
		}
		
		for(Minion minion : getTypeWars().getActiveMinions())
		{
			if(UtilMath.offset2d(minion.getEntity().getLocation(), location) > 3)
				continue;
			
			if(getTypeWars().GetTeam(player) != minion.getTeam())
				continue;
			
			minion.increaseWalkSpeed((float) 0.5);
		}
		return true;
	}

}
