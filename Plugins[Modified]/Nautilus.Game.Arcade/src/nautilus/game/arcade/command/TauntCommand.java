package nautilus.game.arcade.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.command.CommandBase;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.gadget.event.TauntCommandEvent;
import mineplex.core.game.GameDisplay;
import mineplex.minecraft.game.core.combat.CombatManager;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game;

public class TauntCommand extends CommandBase<ArcadeManager>
{
	private ArcadeManager _arcadeManager;

	public TauntCommand(ArcadeManager manager)
	{
		super(manager, ArcadeManager.Perm.TAUNT_COMMAND, "taunt");
		_arcadeManager = manager;
	}

	@Override
	public void Execute(Player player, String[] args)
	{
		CombatManager combatManager = Managers.get(CombatManager.class);
		Game game = _arcadeManager.GetGame();
		GameDisplay gameDisplay = null;
		if (game != null)
		{
			gameDisplay = game.GetType().getDisplay();
		}
		TauntCommandEvent event = new TauntCommandEvent(player, _arcadeManager.isGameInProgress(),
				_arcadeManager.GetGame().IsAlive(player), UtilPlayer.isSpectator(player), combatManager.getLog(player).GetLastCombatEngaged(),
				gameDisplay);
		Bukkit.getPluginManager().callEvent(event);
	}
}