package nautilus.game.arcade.game.games.typewars.spells;

import java.util.ArrayList;
import java.util.Iterator;

import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.typewars.ActivateNukeSpellEvent;
import nautilus.game.arcade.game.games.typewars.Minion;
import nautilus.game.arcade.game.games.typewars.Spell;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SpellKillEverything extends Spell
{
	
	public SpellKillEverything(ArcadeManager manager)
	{
		super(manager, "Zombie Smash", 0, Material.TNT, 1000L, 0, 20000, true);
	}

	@Override
	public ParticleType trail()
	{
		return null;
	}

	@Override
	public ParticleType hit()
	{
		return null;
	}
	
	@Override
	public boolean execute(Player player, Location location)
	{
		
		UtilTextMiddle.display("", getManager().GetGame().GetTeam(player).GetColor() + player.getName() + " used a Zombie Smash", 0, 40, 0);
		ArrayList<Minion> minionList = new ArrayList<>();
		Iterator<Minion> minionIterator = getTypeWars().getActiveMinions().iterator();
		while(minionIterator.hasNext())
		{
			Minion minion = minionIterator.next();
			
			if(getTypeWars().GetTeam(player) == minion.getTeam())
				continue;
			
			minionList.add(minion);
		}
		getTypeWars().addNuke(player);
		Bukkit.getPluginManager().callEvent(new ActivateNukeSpellEvent(player, minionList));
		return true;
	}

}
