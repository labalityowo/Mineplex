package mineplex.game.clans.tutorial;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.events.PlayerRecieveBroadcastEvent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.donation.DonationManager;
import mineplex.core.hologram.HologramManager;
import mineplex.core.npc.NpcManager;
import mineplex.core.task.TaskManager;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.ScoreboardManager;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.elements.ScoreboardElement;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.message.ClansMessageManager;
import mineplex.game.clans.tutorial.command.TutorialCommand;
import mineplex.game.clans.tutorial.gui.TutorialShop;
import mineplex.game.clans.tutorial.tutorials.clans.ClansMainTutorial;

public class TutorialManager extends MiniPlugin implements ScoreboardElement
{
	public enum Perm implements Permission
	{
		TUTORIAL_COMMAND,
		START_TUTORIAL_COMMAND,
		FINISH_TUTORIAL_COMMAND,
	}

	private CoreClientManager _clientManager;
	private DonationManager _donationManager;
	private ClansMessageManager _clansMessageManager;

	private EnumMap<TutorialType, Tutorial> _tutorialMap;
	private EnumMap<TutorialType, TutorialShop> _shopMap; // Don't need to do anything with shops currently

	public TutorialManager(JavaPlugin plugin, CoreClientManager clientManager, DonationManager donationManager, HologramManager hologram, ClansManager clansManager, NpcManager npcManager, TaskManager taskManager)
	{
		super("Clans Tutorial", plugin);

		_clientManager = clientManager;
		_donationManager = donationManager;
		_clansMessageManager = new ClansMessageManager(plugin);

		_tutorialMap = new EnumMap<TutorialType, Tutorial>(TutorialType.class);
		_shopMap = new EnumMap<TutorialType, TutorialShop>(TutorialType.class);

		addTutorial(TutorialType.MAIN, new ClansMainTutorial(plugin, clansManager, _clansMessageManager, hologram, npcManager, taskManager));
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		if (UtilServer.isTestServer())
		{
			PermissionGroup.PLAYER.setPermission(Perm.TUTORIAL_COMMAND, true, true);
		} else
		{
			PermissionGroup.ADMIN.setPermission(Perm.TUTORIAL_COMMAND, true, true);
		}
		if (UtilServer.isTestServer())
		{
			PermissionGroup.PLAYER.setPermission(Perm.FINISH_TUTORIAL_COMMAND, true, true);
		} else
		{
			PermissionGroup.ADMIN.setPermission(Perm.FINISH_TUTORIAL_COMMAND, true, true);
		}
		PermissionGroup.DEV.setPermission(Perm.START_TUTORIAL_COMMAND, true, true);
	}
	
	@EventHandler
	public void broadcast(PlayerRecieveBroadcastEvent event)
	{
		if (!inTutorial(event.getPlayer()))
		{
			return;
		}
		
		if (!event.getMessage().startsWith(C.cBlue + "Event>"))
		{
			return;
		}
		
		event.setCancelled(true);
	}

	@EventHandler
	public void playerChat(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();

		if (inTutorial(player))
		{
			player.sendMessage(F.main("Clans", "You are not allowed to speak while in a tutorial."));
			event.setCancelled(true);
			return;
		}

		event.getRecipients().removeIf(this::inTutorial);
	}
	
	@Override
	public void addCommands()
	{
		addCommand(new TutorialCommand(this));
	}
	
	private void addTutorial(TutorialType type, Tutorial tutorial)
	{
		if (_tutorialMap.containsKey(type))
		{
			_tutorialMap.remove(type).unregisterAll();
		}

		_tutorialMap.put(type, tutorial);
		_shopMap.put(type, new TutorialShop(this, _clientManager, _donationManager, tutorial));
		getPlugin().getServer().getPluginManager().registerEvents(tutorial, getPlugin());
	}

	public boolean inTutorial(Player player)
	{
		return getTutorial(player) != null;
	}

	public Tutorial getTutorial(Player player)
	{
		for (Tutorial tutorial : _tutorialMap.values())
		{
			if (tutorial.isInTutorial(player))
				return tutorial;
		}

		return null;
	}

	public void finishTutorial(Player player)
	{
		Tutorial tutorial = getTutorial(player);
		if (tutorial != null)
		{
			tutorial.finish(player);
		}
	}

	public boolean startTutorial(Player player, TutorialType type)
	{
		if (inTutorial(player))
		{
			UtilPlayer.message(player, F.main("Tutorial", "You are already in a tutorial"));
			return false;
		}

		if (_tutorialMap.containsKey(type))
		{
			UtilPlayer.message(player, F.main("Tutorial", "Starting Tutorial: " + type.name()));
			_tutorialMap.get(type).start(player);
			return true;
		}

		return false;
	}

	public void openTutorialMenu(Player player, TutorialType type)
	{
		if (_shopMap.containsKey(type))
		{
			_shopMap.get(type).attemptShopOpen(player);
		}
	}

	public ClansMessageManager getMessageManager()
	{
		return _clansMessageManager;
	}

	public Tutorial getTutorial(TutorialType type)
	{
		return _tutorialMap.get(type);
	}

	@Override
	public List<String> getLines(ScoreboardManager manager, Player player, List<String> out)
	{
		Tutorial tutorial = getTutorial(player);

		if (tutorial != null)
		{
			out.clear();
			return tutorial.getScoreboardLines(player);
		}

		return new ArrayList<String>(0);
	}
}