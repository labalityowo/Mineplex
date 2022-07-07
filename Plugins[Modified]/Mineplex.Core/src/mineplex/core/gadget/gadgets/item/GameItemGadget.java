package mineplex.core.gadget.gadgets.item;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import mineplex.core.common.Pair;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.HoverEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.gadget.types.ItemGadget;

public abstract class GameItemGadget extends ItemGadget
{

	private static final long COOLDOWN_INVITE = TimeUnit.SECONDS.toMillis(8);

	protected final Map<String, Pair<String, Long>> _invites;
	private final String _command;

	public GameItemGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data, Ammo ammo)
	{
		super(manager, name, desc, cost, mat, data, COOLDOWN_INVITE, ammo);

		_invites = new HashMap<>(3);
		_command = "/play-" + name.toLowerCase().replace(" ", "-");
	}

	@Override
	public void ActivateCustom(Player player)
	{
		for (Player nearby : UtilPlayer.getNearby(player.getLocation(), 3))
		{
			if (player.equals(nearby))
			{
				continue;
			}

			sendGameInvite(nearby, player);
			return;
		}
	}

	protected abstract void startGame(Player invitee, Player inviter);

	private void sendGameInvite(Player invitee, Player inviter)
	{
		inviter.sendMessage(F.main(Manager.getName(), "You challenged " + F.name(invitee.getName()) + " to play " + F.name(getName()) + "!"));

		new JsonMessage(F.main(Manager.getName(), F.color("CLICK HERE", C.cYellowB) + " to play " + F.name(getName()) + " with " + F.name(inviter.getName()) + "!"))
				.hover(HoverEvent.SHOW_TEXT, C.cYellow + "Click to play " + F.name(getName()) + " with " + F.name(inviter.getName()))
				.click(ClickEvent.RUN_COMMAND, _command + " " + inviter.getName())
				.sendToPlayer(invitee);

		invitee.playSound(invitee.getLocation(), Sound.NOTE_PIANO, 1, 1);
		inviter.playSound(inviter.getLocation(), Sound.NOTE_PIANO, 1, 1);

		_invites.put(inviter.getName(), Pair.create(invitee.getName(), System.currentTimeMillis()));
	}

	@EventHandler
	public void commandProcess(PlayerCommandPreprocessEvent event)
	{
		String command = event.getMessage();
		String[] split = command.split(" ");

		if (split.length != 2 || !split[0].equals(_command))
		{
			return;
		}

		event.setCancelled(true);

		Player caller = event.getPlayer();
		String player = split[1];
		Pair<String, Long> pair = _invites.get(player);

		if (pair == null || !pair.getLeft().equals(caller.getName()) || UtilTime.elapsed(pair.getRight(), COOLDOWN_INVITE))
		{
			return;
		}

		Player inviter = UtilPlayer.searchExact(player);

		if (inviter == null)
		{
			caller.sendMessage(F.main(Manager.getName(), "Looks like the player who challenged you is no longer online."));
			return;
		}

		_invites.remove(player);
		caller.sendMessage(F.main(Manager.getName(), F.name(inviter.getName()) + " challenged you to a game of " + F.name(getName()) + "!"));
		inviter.sendMessage(F.main(Manager.getName(), "You challenged " + F.name(caller.getName()) + " to a game of " + F.name(getName()) + "!"));
		startGame(caller, inviter);
	}

	protected void endGame(Player winner, Player loser)
	{
		UtilTextMiddle.display(C.cYellowB + "YOU WON", "", 10, 30, 10, winner);
		UtilTextMiddle.display(C.cRedB + "YOU LOST", "", 10, 30, 10, loser);
		winner.getWorld().playSound(winner.getLocation(), Sound.VILLAGER_YES, 1, 1);
		loser.getWorld().playSound(winner.getLocation(), Sound.VILLAGER_NO, 1, 1);
	}

	protected void drawGame(Player... players)
	{
		UtilTextMiddle.display(C.cRedB + "DRAW", "", 10, 30, 10, players);
	}
}
