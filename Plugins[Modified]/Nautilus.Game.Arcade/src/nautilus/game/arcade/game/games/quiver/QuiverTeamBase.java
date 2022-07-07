package nautilus.game.arcade.game.games.quiver;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.TeamGame;
import nautilus.game.arcade.game.games.quiver.kits.KitBarrage;
import nautilus.game.arcade.game.games.quiver.kits.KitBeserker;
import nautilus.game.arcade.game.games.quiver.kits.KitNecromancer;
import nautilus.game.arcade.game.games.quiver.kits.KitNewNinja;
import nautilus.game.arcade.game.games.quiver.kits.KitPyromancer;
import nautilus.game.arcade.game.games.quiver.kits.KitSkyWarrior;
import nautilus.game.arcade.game.games.quiver.module.ModuleKillstreak;
import nautilus.game.arcade.game.games.quiver.module.ModulePowerup;
import nautilus.game.arcade.game.games.quiver.module.ModuleSuperArrow;
import nautilus.game.arcade.game.games.quiver.module.ModuleUltimate;
import nautilus.game.arcade.game.games.quiver.module.QuiverTeamModule;
import nautilus.game.arcade.game.games.quiver.module.game.QuiverPayload;
import nautilus.game.arcade.game.modules.SpawnShieldModule;
import nautilus.game.arcade.game.modules.TeamArmorModule;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.stats.WinWithoutBowStatTracker;

public class QuiverTeamBase extends TeamGame
{
	
	//private static final String CUSTOM_LOCATION_GAME_KOTH = "KOTH";
	
	public static final String OVERTIME = C.cGold + "!!! " + C.cDRedB + "OVERTIME" + C.cGold + " !!!";
	
	private Map<Class<? extends QuiverTeamModule>, QuiverTeamModule> _modules = new HashMap<>();
	
	public QuiverTeamBase(ArcadeManager manager)
	{
		this(manager, new Kit[] {

				new KitBeserker(manager), new KitNewNinja(manager), new KitBarrage(manager), new KitSkyWarrior(manager), new KitPyromancer(manager), new KitNecromancer(manager),

		});
	}
	
	@SuppressWarnings("unchecked")
	public QuiverTeamBase(ArcadeManager manager, Kit[] kits)
	{
		super(manager, GameType.QuiverPayload, kits, new String[] {});
		
		this.DeathOut = false;
		this.DamageSelf = false;
		this.DamageTeamSelf = false;
		this.DamageFall = false;
		this.HungerSet = 20;
		
		registerStatTrackers(new WinWithoutBowStatTracker(this, "Bow"));
		
		registerChatStats(
				Kills,
				Deaths,
				KDRatio,
				BlankLine,
				Assists,
				DamageTaken,
				DamageDealt
		);
		
		getQuiverTeamModule(ModuleSuperArrow.class);
		getQuiverTeamModule(ModulePowerup.class);
		getQuiverTeamModule(ModuleUltimate.class);
		getQuiverTeamModule(ModuleKillstreak.class);

//		if (WorldData.GetCustomLocs(CUSTOM_LOCATION_GAME_KOTH) != null)
//		{
//			getQuiverTeamModule(QuiverKOTH.class);
//			return;
//		}
		
		// There was no game identifier in the map. Use Payload as the default
		getQuiverTeamModule(QuiverPayload.class);

		new CompassModule()
				.setGiveCompass(true)
				.setGiveCompassToSpecs(true)
				.setGiveCompassToAlive(false)
				.register(this);

		new TeamArmorModule()
				.giveTeamArmor()
				.giveHotbarItem()
				.register(this);
	}

	@Override
	public void ParseData()
	{
		new SpawnShieldModule()
				.registerShield(GetTeam(ChatColor.RED), WorldData.GetCustomLocs("73"))
				.registerShield(GetTeam(ChatColor.AQUA), WorldData.GetCustomLocs("21"))
				.register(this);
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}
		
		for (QuiverTeamModule module : _modules.values())
		{
			module.updateScoreboard();
		}
	}
	
	@EventHandler
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() == GameState.Prepare)
		{
			for (QuiverTeamModule module : _modules.values())
			{
				module.setup();
			}
		}
		else if (event.GetState() == GameState.End)
		{
			for (QuiverTeamModule module : _modules.values())
			{
				module.finish();
			}
		}
	}
	
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (!IsLive())
		{
			return;
		}
		
		for (QuiverTeamModule module : _modules.values())
		{
			module.update(event.getType());
		}
	}
	
	public double getGems(GemAwardReason reason)
	{
		return 0.5;
	}
	
	public <T extends QuiverTeamModule> T getQuiverTeamModule(Class<T> clazz)
	{
		if (!_modules.containsKey(clazz))
		{
			try
			{
				_modules.put(clazz, clazz.getConstructor(QuiverTeamBase.class).newInstance(this));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return clazz.cast(_modules.get(clazz));
	}
	
	public static enum GemAwardReason
	{
		KILL, ASSIST, KILLSTEAK, WIN
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
	{
		if (!UtilServer.isTestServer())
		{
			return;
		}
		
		String cmd = event.getMessage();
		Player player = event.getPlayer();
		
		if (cmd.startsWith("/max"))
		{
			event.setCancelled(true);
			getQuiverTeamModule(ModuleUltimate.class).incrementUltimate(player, 100);
		}
		else if (cmd.startsWith("/win"))
		{
			event.setCancelled(true);
			WinnerTeam = GetTeam(event.getPlayer());
			AnnounceEnd(WinnerTeam);
			SetState(GameState.End);
		}
		else if (cmd.startsWith("/ks"))
		{
			event.setCancelled(true);
			ModuleKillstreak killstreak = getQuiverTeamModule(ModuleKillstreak.class);
			
			killstreak.getKillstreakAmount().put(player.getUniqueId(), Integer.parseInt(cmd.split(" ")[1]));
			killstreak.getKillstreakTime().put(player.getUniqueId(), System.currentTimeMillis());
		}
	}
	
}
