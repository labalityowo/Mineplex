package nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.kit.Perk;

public class PerkVampire extends Perk
{
	private int _recover;
	
	public PerkVampire(int recover) 
	{
		super("Vampire", new String[] 
				{ 
				C.cGray + "You heal " + C.cYellow + recover + " Hearts" + C.cGray + " when you kill someone",
				});
		
		_recover = recover;
	}
		
	@EventHandler
	public void PlayerKillAward(CombatDeathEvent event)
	{
		Game game = Manager.GetGame();
		if (game == null)	return;

		if (!(event.GetEvent().getEntity() instanceof Player))
			return;

		if (event.GetLog().GetKiller() == null)
			return;

		Player killer = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());
		if (killer == null)
			return;

		UtilPlayer.health(killer, _recover);
	}
}
