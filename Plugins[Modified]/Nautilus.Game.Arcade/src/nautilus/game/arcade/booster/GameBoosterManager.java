package nautilus.game.arcade.booster;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.boosters.Booster;
import mineplex.core.boosters.BoosterManager;
import mineplex.core.boosters.event.BoosterActivateEvent;
import mineplex.core.boosters.tips.BoosterThankManager;
import mineplex.core.boosters.tips.TipAddResult;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.jsonchat.ClickEvent;
import mineplex.core.common.jsonchat.HoverEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.managers.lobby.LobbyManager;

@ReflectivelyCreateMiniPlugin
public class GameBoosterManager extends MiniPlugin
{

	private static final List<String> TESTING_GROUPS = Collections.singletonList("testing");

	private final ArcadeManager _manager;
	private final BoosterManager _boosterManager;
	private final String _boosterGroup;

	private GameBoosterManager()
	{
		super("Arcade Boosters");

		_manager = require(ArcadeManager.class);

		_boosterGroup = _manager.GetServerConfig().BoosterGroup;
		_boosterManager = _manager.getBoosterManager();

		LobbyManager lobbyManager = _manager.GetLobby();

		if (_boosterGroup != null && _boosterGroup.length() > 0 && lobbyManager.getAmpStand() != null)
		{
			new BoosterPodium(this, _manager.getHologramManager(), lobbyManager.getAmpStand());
		}
	}

	public Booster getActiveBooster()
	{
		return _boosterManager.getActiveBooster(_boosterGroup);
	}

	public void attemptTip(Player player)
	{
		Booster active = getActiveBooster();

		if (active == null)
		{
			UtilPlayer.message(player, F.main("Thanks", "There is no active amplifier to Thank!"));
			return;
		}

		_boosterManager.getBoosterThankManager().addTip(player, active, result ->
		{
			if (result == TipAddResult.SUCCESS)
			{
				UtilPlayer.message(player, F.main("Thank", "You thanked " + F.name(active.getPlayerName()) + ". They earned " + F.currency(GlobalCurrency.TREASURE_SHARD, BoosterThankManager.TIP_FOR_SPONSOR) + " and you got "
						+ F.currency(GlobalCurrency.TREASURE_SHARD, BoosterThankManager.TIP_FOR_TIPPER)) + " in return!");
				player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1f);
			}
			else
			{
				if (result.getFriendlyMessage() != null)
				{
					UtilPlayer.message(player, F.main("Thanks", result.getFriendlyMessage()));
				}
			}
		});
	}

	@EventHandler
	public void onActivate(BoosterActivateEvent event)
	{
		Booster booster = event.getBooster();

		boolean isTesting = UtilServer.isTestServer();

		boolean canShowGroup = !isTesting && !TESTING_GROUPS.contains(event.getBoosterGroup().toLowerCase());

		// If the booster is for the server the player is currently on
		if (event.getBoosterGroup().equals(_boosterGroup))
		{
			Bukkit.broadcastMessage(F.main("Amplifier", F.name(booster.getPlayerName()) + " has activated a Game Amplifier for " + booster.getMultiplier() + "x Shards!"));
		}
		// If this is not currently a test server and the booster group is not blacklisted from
		// displaying on non-test servers
		else if (canShowGroup)
		{
			Bukkit.broadcastMessage(F.main("Amplifier", F.name(booster.getPlayerName()) + " has activated a Game Amplifier on " + F.elem(event.getBoosterGroup().replaceAll("_", " ")) + "!"));
		}

		if (event.getBoosterGroup().equals(_boosterGroup) || canShowGroup)
		{
			JsonMessage message = new JsonMessage(F.main("Amplifier", F.elem("Click here") + " to thank them and get " + F.currency(GlobalCurrency.TREASURE_SHARD, BoosterThankManager.TIP_FOR_TIPPER) + "!"));
			message.click(ClickEvent.RUN_COMMAND, "/amplifier thank " + event.getBoosterGroup());
			message.hover(HoverEvent.SHOW_TEXT, C.cGreen + "Click to Thank");
			message.send(JsonMessage.MessageType.CHAT_BOX, UtilServer.getPlayers());
		}
	}

	public ArcadeManager getManager()
	{
		return _manager;
	}
}
