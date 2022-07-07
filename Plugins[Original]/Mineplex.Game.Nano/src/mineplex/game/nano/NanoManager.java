package mineplex.game.nano;

import java.util.HashSet;
import java.util.Set;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.antihack.AntiHack;
import mineplex.core.boosters.BoosterManager;
import mineplex.core.chat.Chat;
import mineplex.core.chat.format.LevelFormatComponent;
import mineplex.core.chat.format.RankFormatComponent;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.donation.DonationManager;
import mineplex.core.game.GameDisplay;
import mineplex.core.game.nano.NanoFavourite;
import mineplex.core.incognito.IncognitoManager;
import mineplex.core.incognito.events.IncognitoStatusChangeEvent;
import mineplex.core.newnpc.NewNPCManager;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.serverConfig.ServerConfiguration;
import mineplex.core.stats.StatsManager;
import mineplex.core.task.TaskManager;
import mineplex.game.nano.commands.game.GameCommand;
import mineplex.game.nano.commands.spectator.SpectatorCommand;
import mineplex.game.nano.cycle.GameCycle;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.components.currency.GameCurrencyManager;
import mineplex.game.nano.game.components.damage.GameDamageManager;
import mineplex.game.nano.game.components.player.GamePlayerManager;
import mineplex.game.nano.game.components.team.GameTeam;
import mineplex.game.nano.game.components.world.GameWorldManager;
import mineplex.game.nano.lobby.AFKManager;
import mineplex.game.nano.lobby.LobbyManager;
import mineplex.game.nano.lobby.ReturnToHubManager;
import mineplex.game.nano.status.GameStatusManager;
import mineplex.game.nano.world.GameWorld;
import mineplex.minecraft.game.core.IRelation;
import mineplex.minecraft.game.core.condition.ConditionManager;
import mineplex.minecraft.game.core.damage.DamageManager;
import mineplex.serverdata.data.ServerGroup;

@ReflectivelyCreateMiniPlugin
public class NanoManager extends MiniPlugin implements IRelation
{

	public enum Perm implements Permission
	{
		GAME_COMMAND,
		SPECTATOR_COMMAND,
		AUTO_OP
	}

	private static final String HEADER_FOOTER = C.cDGreen + C.Strike + "=============================================";

	public static String getHeaderFooter()
	{
		return HEADER_FOOTER;
	}

	private static final GameDisplay GAME_DISPLAY = GameDisplay.NanoGames;

	public static GameDisplay getGameDisplay()
	{
		return GAME_DISPLAY;
	}

	// Standard
	private final CoreClientManager _clientManager;
	private final DonationManager _donationManager;

	// Achievement
	private final StatsManager _statsManager;
	private final AchievementManager _achievementManager;
	private final TaskManager _taskManager;

	// Chat
	private final Chat _chat;

	// Vanish
	private final IncognitoManager _incognitoManager;
	private final Set<Player> _spectators;

	// Conditions
	private final ConditionManager _conditionManager;

	// Disguise
	private final DisguiseManager _disguiseManager;

	// Cosmetics
	private final CosmeticManager _cosmeticManager;

	// Booster
	private final BoosterManager _boosterManager;

	// AntiCheat
	private final AntiHack _antiHack;

	// Packet
	private final PacketHandler _packetHandler;

	// NPC
	private final NewNPCManager _npcManager;

	// Damage
	private final DamageManager _damageManager;
	private final GameDamageManager _gameDamageManager;

	// World
	private final GameWorldManager _gameWorldManager;

	// Player
	private final GamePlayerManager _gamePlayerManager;

	// Lobby
	private final LobbyManager _lobbyManager;
	private final ReturnToHubManager _toHubManager;

	// Currency
	private final GameCurrencyManager _currencyManager;

	// Server
	private final ServerGroup _serverGroup;

	// Game
	private Game _game;
	private final GameCycle _gameCycle;
	private final NanoFavourite _favourite;

	private NanoManager()
	{
		super("Game");

		GameWorld.deleteOldFolders(this);

		_clientManager = require(CoreClientManager.class);
		_donationManager = require(DonationManager.class);

		_statsManager = require(StatsManager.class);
		_achievementManager = require(AchievementManager.class);
		_taskManager = require(TaskManager.class);

		_chat = require(Chat.class);
		_chat.setFormatComponents(
				player ->
				{
					if (_game == null || _game.isAlive(player))
					{
						return null;
					}

					TextComponent message = new TextComponent("Dead");
					message.setColor(ChatColor.GRAY);
					return message;
				},
				new LevelFormatComponent(_achievementManager),
				new RankFormatComponent(_clientManager),
				player ->
				{
					TextComponent message = new TextComponent(player.getName());
					message.setColor(ChatColor.YELLOW);
					return message;
				}
		);

		_incognitoManager = require(IncognitoManager.class);
		_spectators = new HashSet<>();

		_conditionManager = require(ConditionManager.class);

		_disguiseManager = require(DisguiseManager.class);

		_cosmeticManager = require(CosmeticManager.class);

		_boosterManager = require(BoosterManager.class);

		_antiHack = require(AntiHack.class);

		_packetHandler = require(PacketHandler.class);

		_npcManager = require(NewNPCManager.class);

		_damageManager = require(DamageManager.class);
		_gameDamageManager = require(GameDamageManager.class);

		_gameWorldManager = require(GameWorldManager.class);
		_gamePlayerManager = require(GamePlayerManager.class);

		_lobbyManager = require(LobbyManager.class);
		_toHubManager = require(ReturnToHubManager.class);

		_currencyManager = require(GameCurrencyManager.class);

		_serverGroup = require(ServerConfiguration.class).getServerGroup();

		require(GameStatusManager.class);
		require(AFKManager.class);

		_gameCycle = require(GameCycle.class);
		_favourite = require(NanoFavourite.class);

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.GAME_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.SPECTATOR_COMMAND, true, true);
		PermissionGroup.LT.setPermission(Perm.AUTO_OP, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new GameCommand(this));
		addCommand(new SpectatorCommand(this));
	}

	public boolean canStartGame()
	{
		int players = UtilServer.getPlayersCollection().size() - _spectators.size();
		return players >= _serverGroup.getMinPlayers() && _serverGroup.getGameAutoStart();
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		player.setOp(_clientManager.Get(player).hasPermission(Perm.AUTO_OP));

		if (isSpectator(player))
		{
			event.setJoinMessage(null);
		}
		else
		{
			event.setJoinMessage(F.sys("Join", player.getName()));
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		event.setQuitMessage(F.sys("Quit", player.getName()));
		_spectators.remove(player);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void playerVanish(IncognitoStatusChangeEvent event)
	{
		Player player = event.getPlayer();

		if (event.getNewState())
		{
			Bukkit.broadcastMessage(F.sys("Quit", player.getName()));

			if (_game != null && _game.isAlive(player))
			{
				_game.addSpectator(player, true, true);
			}
		}
		else
		{
			Bukkit.broadcastMessage(F.sys("Join", player.getName()));
		}
	}

	public void setSpectator(Player player, boolean spectator)
	{
		if (spectator)
		{
			_spectators.add(player);
		}
		else
		{
			_spectators.remove(player);
		}
	}

	public boolean isSpectator(Player player)
	{
		if (_incognitoManager.Get(player).Status)
		{
			_spectators.add(player);
			return true;
		}

		return _spectators.contains(player);
	}

	public Set<Player> getSpectators()
	{
		return _spectators;
	}

	@Override
	public boolean canHurt(Player a, Player b)
	{
		// Either safe
		if (isSafe(a) || isSafe(b))
		{
			return false;
		}

		// No Hook
		if (_gameDamageManager.getHook() == null)
		{
			return false;
		}

		// Self Damage
		if (a.equals(b))
		{
			return _gameDamageManager.getHook().isSelfEnabled();
		}

		// PVP
		if (!_gameDamageManager.getHook().isPvpEnabled())
		{
			return false;
		}

		GameTeam tA = _game.getTeam(a), tB = _game.getTeam(b);

		// No need for null check since isSafe has done that already
		if (tA.equals(tB))
		{
			return _gameDamageManager.getHook().isTeamSelfEnabled();
		}

		return true;
	}

	@Override
	public boolean canHurt(String a, String b)
	{
		return canHurt(UtilPlayer.searchExact(a), UtilPlayer.searchExact(b));
	}

	@Override
	public boolean isSafe(Player a)
	{
		return _game == null || !_game.isLive() || !_game.isAlive(a) || _game.hasRespawned(a);
	}

	public CoreClientManager getClientManager()
	{
		return _clientManager;
	}

	public DonationManager getDonationManager()
	{
		return _donationManager;
	}

	public StatsManager getStatsManager()
	{
		return _statsManager;
	}

	public AchievementManager getAchievementManager()
	{
		return _achievementManager;
	}

	public TaskManager getTaskManager()
	{
		return _taskManager;
	}

	public Chat getChat()
	{
		return _chat;
	}

	public IncognitoManager getIncognitoManager()
	{
		return _incognitoManager;
	}

	public ConditionManager getConditionManager()
	{
		return _conditionManager;
	}

	public DisguiseManager getDisguiseManager()
	{
		return _disguiseManager;
	}

	public CosmeticManager getCosmeticManager()
	{
		return _cosmeticManager;
	}

	public BoosterManager getBoosterManager()
	{
		return _boosterManager;
	}

	public AntiHack getAntiHack()
	{
		return _antiHack;
	}

	public PacketHandler getPacketHandler()
	{
		return _packetHandler;
	}

	public NewNPCManager getNpcManager()
	{
		return _npcManager;
	}

	public DamageManager getDamageManager()
	{
		return _damageManager;
	}

	public GameDamageManager getGameDamageManager()
	{
		return _gameDamageManager;
	}

	public GameWorldManager getGameWorldManager()
	{
		return _gameWorldManager;
	}

	public GamePlayerManager getGamePlayerManager()
	{
		return _gamePlayerManager;
	}

	public LobbyManager getLobbyManager()
	{
		return _lobbyManager;
	}

	public ReturnToHubManager getToHubManager()
	{
		return _toHubManager;
	}

	public GameCurrencyManager getCurrencyManager()
	{
		return _currencyManager;
	}

	public ServerGroup getServerGroup()
	{
		return _serverGroup;
	}

	public void setGame(Game game)
	{
		_game = game;
	}

	public Game getGame()
	{
		return _game;
	}

	public GameCycle getGameCycle()
	{
		return _gameCycle;
	}

	public NanoFavourite getFavourite()
	{
		return _favourite;
	}
}
