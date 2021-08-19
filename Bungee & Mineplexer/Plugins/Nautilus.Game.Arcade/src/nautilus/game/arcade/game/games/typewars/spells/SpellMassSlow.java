package nautilus.game.arcade.game.games.typewars.spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.typewars.Minion;
import nautilus.game.arcade.game.games.typewars.Spell;

public class SpellMassSlow extends Spell
{

	public SpellMassSlow(ArcadeManager manager)
	{
		super(manager, "Mass Slow spell", 8, Material.ANVIL, 2000L, 0, 0, false);
	}

	@Override
	public ParticleType trail()
	{
		return ParticleType.WITCH_MAGIC;
	}

	@Override
	public boolean execute(Player player, Location location)
	{
		location.getWorld().playSound(location.clone().add(0.5, 0.5, 0.5), Sound.ENDERDRAGON_DEATH, 10F, 2.0F);
		
		for(int c = -1; c <= 1; c++)
		{
			for(int i = -10; i <= 10; i = i + 2)
			{
				for(double x = 0.2; x <= 2; x = x + 0.2)
				{
					Location loc = location.clone().add(i + x, 2*(x-1)*(x-1)*(x-1) -2*(x-1), c);
					loc.add(0, 0.3, 0);
					UtilParticle.PlayParticle(UtilParticle.ParticleType.HAPPY_VILLAGER, loc, 0, 0, 0, 0, 1, ViewDist.LONG, UtilServer.getPlayers());
					
					Location otherLocation = location.clone().add(c, 2*(x-1)*(x-1)*(x-1) -2*(x-1), i + x);
					otherLocation.add(0, 0.3, 0);
					UtilParticle.PlayParticle(UtilParticle.ParticleType.HAPPY_VILLAGER, otherLocation, 0, 0, 0, 0, 1, ViewDist.LONG, UtilServer.getPlayers());
				}
			}
		}
		
		for(Minion minion : getTypeWars().getActiveMinions())
		{	
			if(getTypeWars().GetTeam(player) == minion.getTeam())
				continue;
			
			minion.increaseWalkSpeed(-0.3F);
		}
		return true;
	}
	
	/*@Override
	public void trailAnimation(Location location, int frame)
	{
		double radius = 0.6;
		int particleAmount = frame / 2;
		for (int i = 0; i < particleAmount; i++)
		{
			double xDiff = Math.sin(i/(double)particleAmount * 2 * Math.PI) * radius;
			double zDiff = Math.cos(i/(double)particleAmount * 2 * Math.PI) * radius;

			Location loc = location.clone().add(xDiff, 0, zDiff);
			UtilParticle.PlayParticle(UtilParticle.ParticleType.ENCHANTMENT_TABLE, loc, 0, 0, 0, 0, 1,
					ViewDist.NORMAL, UtilServer.getPlayers());
		}
	}*/

}
