package mineplex.core.antihack.actions;

import org.bukkit.Bukkit;

import com.mineplex.anticheat.api.PlayerViolationEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;

public class ImmediateKickAction extends AntiHackAction
{
	private static final String USER_HAS_BEEN_KICKED = F.main("GWEN", "%s was removed from the game due to suspicious activity. I am always watching.");
	
	public ImmediateKickAction(int vl)
	{
		super(vl);
	}

	@Override
	public void handle(PlayerViolationEvent event)
	{
		final String message = C.cRed + "[GWEN Cheat Detection]\n\nYou have been removed from the server due to unnatural movement.";

		if (event.getViolations() >= (Math.floor(getMinVl() * .9)) && event.getPlayer().getMetadata("GWENEXTREMEPREJUDICE").get(0).asBoolean())
		{
			event.getPlayer().kickPlayer(message);
			Bukkit.getServer().broadcastMessage(String.format(USER_HAS_BEEN_KICKED, event.getPlayer().getName()));
			return;
		}
		if (event.getViolations() >= this.getMinVl())
		{
			event.getPlayer().kickPlayer(message);
			Bukkit.getServer().broadcastMessage(String.format(USER_HAS_BEEN_KICKED, event.getPlayer().getName()));
		}
	}
}
