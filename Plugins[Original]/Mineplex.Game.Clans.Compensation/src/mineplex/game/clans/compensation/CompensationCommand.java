package mineplex.game.clans.compensation;

import java.util.Random;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.game.clans.clans.ClansManager;

public class CompensationCommand extends CommandBase<ClansManager>
{
	private final ClansCompensation _main;
	private final String _secretKey;
	
	public CompensationCommand(ClansManager plugin, ClansCompensation main)
	{
		super(plugin, ClansCompensation.Perm.COMPENSATION_COMMAND, "compensation");
		
		_main = main;
		char[] characters = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		StringBuilder keyBuilder = new StringBuilder();
		Random rand = new Random();
		while (keyBuilder.length() < 10)
		{
			keyBuilder.append(characters[rand.nextInt(characters.length)]);
		}
		_secretKey = keyBuilder.toString();
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (_main.canClaim(caller.getUniqueId()))
		{
			if (args.length >= 1 && args[0].equals(_secretKey))
			{
				_main.claim(caller);
			}
			else
			{
				TextComponent message = new TextComponent("Confirm");
				message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/compensation " + _secretKey));
				message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Redeem your items").create()));
				message.setColor(ChatColor.GREEN);
				message.setBold(true);
				caller.sendMessage(C.cRedB + "WARNING: " + C.cGray + "You are about to claim several free items. Other players may attempt to steal these items from you, so it is highly recommended that you only run this command inside of your own base. Are you sure you wish to claim your items at this time?");
				caller.spigot().sendMessage(message);
			}
		}
		else
		{
			UtilPlayer.message(caller, F.main("Compensation", "You do not have a compensation package!"));
		}
	}
}