package mineplex.core.titles.tracks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.Callback;
import mineplex.core.inventory.InventoryManager;
import mineplex.core.titles.commands.GiveTrackCommand;
import mineplex.core.titles.tracks.award.AlienInvasionTrack;
import mineplex.core.titles.tracks.award.AprilFools2017Track;
import mineplex.core.titles.tracks.award.Bridges2017Track;
import mineplex.core.titles.tracks.award.Bridges2017WinterTrack;
import mineplex.core.titles.tracks.award.CCIIPublicTrack;
import mineplex.core.titles.tracks.award.CCIITrack;
import mineplex.core.titles.tracks.award.CastleSiegeTesterTrack;
import mineplex.core.titles.tracks.award.ClansRaidTrack;
import mineplex.core.titles.tracks.award.FiveYearTrack;
import mineplex.core.titles.tracks.award.MapSub2018Track;
import mineplex.core.titles.tracks.award.Minestrike2017Track;
import mineplex.core.titles.tracks.award.NewHub2018Track;
import mineplex.core.titles.tracks.award.NewWebsiteTrack;
import mineplex.core.titles.tracks.custom.DongerTrack;
import mineplex.core.titles.tracks.custom.EarlyBirdTrack;
import mineplex.core.titles.tracks.custom.HappyGaryTrack;
import mineplex.core.titles.tracks.custom.LeaderTrack;
import mineplex.core.titles.tracks.custom.SnekTrack;
import mineplex.core.titles.tracks.custom.TableFlipTrack;
import mineplex.core.titles.tracks.custom.TrackBuilder;
import mineplex.core.titles.tracks.custom.WizardTrack;
import mineplex.core.titles.tracks.staff.BuilderTrack;
import mineplex.core.titles.tracks.staff.ModeratorTrack;
import mineplex.core.titles.tracks.staff.SeniorModeratorTrack;
import mineplex.core.titles.tracks.staff.TraineeTrack;
import mineplex.core.titles.tracks.standard.GemCollectorTrack;
import mineplex.core.titles.tracks.standard.GemHuntersTrack;
import mineplex.core.titles.tracks.standard.HolidayCheerTrack;
import mineplex.core.titles.tracks.standard.LevelerTrack;
import mineplex.core.titles.tracks.standard.LuckyTrack;
import mineplex.core.titles.tracks.standard.MineplexMasteryTrack;
import mineplex.core.titles.tracks.standard.MobaAssassinWinsTrack;
import mineplex.core.titles.tracks.standard.MobaHunterWinsTrack;
import mineplex.core.titles.tracks.standard.MobaKillsTrack;
import mineplex.core.titles.tracks.standard.MobaMageWinsTrack;
import mineplex.core.titles.tracks.standard.MobaWarriorWinsTrack;
import mineplex.core.titles.tracks.standard.PartyAnimalTrack;
import mineplex.core.titles.tracks.standard.PeacefulTrack;
import mineplex.core.titles.tracks.standard.PerfectionistTrack;
import mineplex.core.titles.tracks.standard.PowerPlayTrack;
import mineplex.core.titles.tracks.standard.SweetToothTrack;
import mineplex.core.titles.tracks.standard.TreasureHunterTrack;
import mineplex.core.titles.tracks.standard.UnluckyTrack;
import mineplex.core.titles.tracks.standard.WarriorTrack;

@ReflectivelyCreateMiniPlugin
public class TrackManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		HAPPY_GARY,
		LEADER,
		TABLE_FLIP,
		BUILDER,
		MOD,
		SR_MOD,
		TRAINEE,
		TRACK_COMMAND,
		GIVE_TRACK_COMMAND,
	}
	
	private final Map<Class<? extends Track>, Track> _registeredTracks = new LinkedHashMap<>();
	private final Map<String, Track> _trackById = new HashMap<>();

	private final InventoryManager _inventoryManager = require(InventoryManager.class);
	private final CoreClientManager _coreClientManager = require(CoreClientManager.class);

	private TrackManager()
	{
		super("Track Manager");

		registerTrack(new LeaderTrack());
		registerTrack(new EarlyBirdTrack());
		registerTrack(new SnekTrack());
		registerTrack(new DongerTrack());
		registerTrack(new WizardTrack());
		registerTrack(new TableFlipTrack());
		registerTrack(new HappyGaryTrack());

		registerTrack(new PowerPlayTrack());
		registerTrack(new MineplexMasteryTrack());
		registerTrack(new SweetToothTrack());
		registerTrack(new PartyAnimalTrack());
		registerTrack(new TreasureHunterTrack());
		registerTrack(new LuckyTrack());
		registerTrack(new UnluckyTrack());
		registerTrack(new HolidayCheerTrack());
//		registerTrack(new KitCollectorTrack());
		registerTrack(new GemCollectorTrack());
		registerTrack(new WarriorTrack());
		registerTrack(new PeacefulTrack());
//		registerTrack(new SurvivorTrack());
		registerTrack(new LevelerTrack());
		registerTrack(new PerfectionistTrack());
		registerTrack(new GemHuntersTrack());
		registerTrack(new MobaKillsTrack());
		registerTrack(new MobaAssassinWinsTrack());
		registerTrack(new MobaHunterWinsTrack());
		registerTrack(new MobaMageWinsTrack());
		registerTrack(new MobaWarriorWinsTrack());

		// Awarded tracks
		registerTrack(new Bridges2017Track());
		registerTrack(new AprilFools2017Track());
		registerTrack(new AlienInvasionTrack());
		registerTrack(new ClansRaidTrack());
		registerTrack(new CastleSiegeTesterTrack());
		registerTrack(new Minestrike2017Track());
		registerTrack(new CCIITrack());
		registerTrack(new CCIIPublicTrack());
		registerTrack(new Bridges2017WinterTrack());
		registerTrack(new NewHub2018Track());
		registerTrack(new MapSub2018Track());
		registerTrack(new FiveYearTrack());
		registerTrack(new NewWebsiteTrack());

		// Staff tracks
		registerTrack(new BuilderTrack());
		registerTrack(new TraineeTrack());
		registerTrack(new ModeratorTrack());
		registerTrack(new SeniorModeratorTrack());

		// Custom tracks
//		registerTrack(track("lenny", "Lenny", "( ͡° ͜ʖ ͡°)"));
//		registerTrack(track("lenny-disgusted", "Disgusted Lenny", "( ͡ಠ ʖ̯ ͡ಠ)"));
//		registerTrack(track("lenny-winking", "Winking Lenny", "( ͡~ ͜ʖ ͡°)"));
		registerTrack(track("ayyye", "Ayyye", "(づ￣ ³￣)づ"));
		registerTrack(track("ameno", "Ameno", "༼ つ ◕_◕ ༽つ Gib me gems"));
//		registerTrack(track("unknown", "Unknown", "(☞ﾟヮﾟ)☞"));
		registerTrack(track("shrug", "Shrug", "¯\\_(ツ)_/¯"));
		registerTrack(track("tableflip", "Tableflip", "(╯°□°）╯︵ ┻━┻"));
		registerTrack(track("tablerespecter", "Table Respecter", "┬─┬ノ(ಠ_ಠノ)"));
		registerTrack(track("tableflip-disgusted", "Disgusted Flip", "Disgusted Tableflip", "(╯ಠ_ಠ）╯︵ ┳━┳"));
//		registerTrack(track("tableflip-donger", "Donger Flip", "ヽ༼ຈل͜ຈ༽ﾉ︵┻━┻"));
		registerTrack(track("tableflip-enraged", "Enraged Flip", "(ﾉಠдಠ)ﾉ︵┻━┻"));
		registerTrack(track("tableflip-riot", "Rioting Flip", "(┛◉Д◉)┛彡┻━┻"));
		registerTrack(track("magician", "Magician", "(ﾉ◕ヮ◕)ﾉ*:・ﾟ✧"));
		registerTrack(track("teddy-bear", "Teddy Bear", "ʕ•ᴥ•ʔ"));
		registerTrack(track("disgust", "Disgust", "ಠ_ಠ"));
		registerTrack(track("old-man", "Old Man", "໒( •̀ ╭ ͟ʖ╮ •́ )७"));
		registerTrack(track("jake", "Jake", "(❍ᴥ❍ʋ)"));
		registerTrack(track("finn", "Finn", "| (• ◡•)|"));
		registerTrack(track("finn-and-jake", "Finn 'n Jake", "| (• ◡•)| (❍ᴥ❍ʋ)"));
		registerTrack(track("boxer", "Boxer", "(ง'̀-'́)ง"));
		registerTrack(track0("zoidberg", "Zoidberg", "Why not?", "(\\/) (°,,°) (\\/)"));
		registerTrack(track("crying", "Crying", "(ಥ﹏ಥ)"));
//		registerTrack(track("unknown", "Unknown", "(◕‿◕✿)"));
		registerTrack(track("fireball", "Fireball", "༼つಠ益ಠ༽つ ─=≡ΣO))"));
		registerTrack(track("stardust", "Stardust", "(つ◕౪◕)つ━☆ﾟ.*･｡ﾟ"));
		registerTrack(track("magic-missile", "Magic Missile", "( °-°)シ ミ★ ミ☆"));
		registerTrack(track("blow-a-kiss", "Blowing a Kiss", "♡(´ε｀ )"));
		registerTrack(track("pewpewpew", "PEWPEWPEW", "(☞^o^)☞"));
//		registerTrack(track("pewpew", "pew pew", "(☞ﾟヮﾟ)☞"));
		registerTrack(track("cool-guy", "Cool Guy", "(⌐■_■)"));
//		registerTrack(track("unknown", "Unknown", "(ง •̀ω•́)ง✧"));
//		registerTrack(track("unknown", "Unknown", "(╯⊙ ⊱ ⊙╰ )"));
		registerTrack(track("party-time", "Party Time", "♪~ ᕕ(ᐛ)ᕗ"));
		registerTrack(track1("whats-a-liter", "What's a Liter?", "also wats a leader"));

		registerTrack(animatedTrack("deal-with-it", "Deal With It", "(⌐■_■)", "( •_•);( •_•)>⌐■-■;(⌐■_■);( •_•)>⌐■-■", 5));
		registerTrack(animatedTrack("this-guy", "This Guy", "(☞ﾟヮﾟ)☞", "(☞ﾟヮﾟ)☞;☜(ﾟヮﾟ☜)", 5));
		registerTrack(animatedTrack("lalala", "La La La", "♪┏(・o･) ┛", "♪┏(・o･) ┛;♪┗ ( ･o･) ┓♪;┏ ( ･o･) ┛♪;┗ (･o･ ) ┓♪;┏(･o･)┛♪", 1));
		registerTrack(animatedTrack("gotta-go", "Gotta Go", "┬┴┬┴┤(･_├┬┴┬┴", "┬┴┬┴┤(･_├┬┴┬┴;┬┴┬┴┤ (･├┬┴┬┴;┬┴┬┴┤  (├┬┴┬┴;┬┴┬┴┤  (･├┬┴┬┴", 5));
//		registerTrack(animatedTrack("unknown", "Unknown", "（o°▽°)o", "（o°▽°)o;(o_△_)o;(o°▽°)o", 5));
		registerTrack(animatedTrack("rolling-around", "Rolling Around", "(ﾟ‐ﾟ)", "(.-.);(:I );(ﾟ‐ﾟ);( I:)", 4));
//		registerTrack(animatedTrack("unknown", "Unknown", "('ω')", "('ω');( ε: );(.ω.);( :3 );('ω');( ε: );(.ω.);( :3 )", 5));
//		registerTrack(animatedTrack("whee", "Whee", "(ﾟーﾟ)", "(ﾟーﾟ);( ﾟー);(　ﾟ);(　　);(ﾟ　);(ーﾟ );(ﾟーﾟ)", 1));
//		registerTrack(animatedTrack("lets-spin", "Let's Spin", "I Say, Let's Spin", "(・ω・)", "(　・ω);(　・);(　);(・　);(ω・　);(・ω・)", 1));
//		registerTrack(animatedTrack("unknown", "Unknown", "(^∀^)", "(^∀^);( ^∀);(　^);(　　);(^　);(∀^ );(^∀^)", 5));
		registerTrack(animatedTrack("whaaat", "Whaaaaaat?", "(°o°)", "(°o°);(°o。);(。o。);(。o°);(°o°);(°o。);(。o。);(。o°)", 5));
//		registerTrack(animatedTrack("spinning", "Spinning", "(ﾟ◇ﾟ)", "(ﾟ◇ﾟ);( ﾟ◇);(　ﾟ);(　　);(ﾟ　);(◇ﾟ );(ﾟ◇", 5));
//		registerTrack(animatedTrack("unknown", "Unknown", "(･∇･)", "(･∇･);( ･∇);(　･);(　　);(･　);(∇･ );(･∇･)", 5));
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.ADMIN.setPermission(Perm.HAPPY_GARY, true, true);
		PermissionGroup.LT.setPermission(Perm.LEADER, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.TABLE_FLIP, true, true);
		PermissionGroup.BUILDER.setPermission(Perm.BUILDER, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.BUILDER, true, false);
		PermissionGroup.ADMIN.setPermission(Perm.BUILDER, true, true);
		PermissionGroup.MOD.setPermission(Perm.MOD, true, true);
		PermissionGroup.SRMOD.setPermission(Perm.MOD, true, false);
		PermissionGroup.ADMIN.setPermission(Perm.MOD, true, true);
		PermissionGroup.SRMOD.setPermission(Perm.SR_MOD, true, true);
		PermissionGroup.TRAINEE.setPermission(Perm.TRAINEE, true, true);
		PermissionGroup.MOD.setPermission(Perm.TRAINEE, true, false);
		PermissionGroup.ADMIN.setPermission(Perm.TRAINEE, true, true);

		PermissionGroup.ADMIN.setPermission(Perm.GIVE_TRACK_COMMAND, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.TRACK_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new GiveTrackCommand(this));
	}

	private void registerTrack(Track track)
	{
		if (_trackById.containsKey(track.getId()))
			throw new IllegalArgumentException("Duplicate id: " + track.getId());
		_registeredTracks.put(track.getClass(), track);
		_trackById.put(track.getId(), track);
	}

	public final <T extends Track> T getTrack(Class<T> clazz)
	{
		return clazz.cast(_registeredTracks.get(clazz));
	}

	public final Track getTrackById(String id)
	{
		return _trackById.get(id);
	}

	public final List<Track> getAllTracks()
	{
		return new ArrayList<>(_trackById.values());
	}

	public boolean hasTrack(Player player, Track track)
	{
		return _inventoryManager.Get(player).getItemCount("track." + track.getId()) > 0;
	}

	public void unlockTrack(Player player, Track track)
	{
		unlockTrack(player, track, null);
	}

	public void unlockTrack(Player player, Track track, Consumer<TrackGiveResult> consumer)
	{
		unlockTrack(player.getName(), track.getId(), consumer);
	}

	public void unlockTrack(String player, String track, Consumer<TrackGiveResult> consumer)
	{
		_coreClientManager.getOrLoadClient(player, client ->
		{
			if (client == null)
			{
				if (consumer != null)
					consumer.accept(TrackGiveResult.PLAYER_NOT_FOUND);
				return;
			}

			runAsync(() ->
			{
				Callback<Boolean> successCallback = success ->
				{
					if (success)
					{
						if (consumer != null)
							consumer.accept(TrackGiveResult.SUCCESS);
					}
					else
					{
						if (consumer != null)
							consumer.accept(TrackGiveResult.UNKNOWN_ERROR);
					}
				};

				Player playerObj = Bukkit.getPlayer(client.getUniqueId());

				if (playerObj != null)
				{
					_inventoryManager.addItemToInventory(successCallback, playerObj, "track." + track, 1);
				}
				else
				{
					_inventoryManager.addItemToInventoryForOffline(successCallback, client.getAccountId(), "track." + track, 1);
				}
			});
		});
	}

	public enum TrackGiveResult
	{
		PLAYER_NOT_FOUND,
		UNKNOWN_ERROR,
		SUCCESS
	}

	// Begin helper methods
	private ItemizedTrack animatedTrack(String id, String name, String desc, String frames, int ticks)
	{
		return TrackBuilder.builder(id)
				.withShortName(name)
				.withDescription(desc)
				.setFrames(frames.split(";"))
				.setTicks(ticks)
				.withColor(ChatColor.GOLD)
				.setHideIfUnowned(true)
				.build();
	}

	private ItemizedTrack animatedTrack(String id, String name, String longName, String desc, String frames, int ticks)
	{
		return TrackBuilder.builder(id)
				.withShortName(name)
				.withLongName(longName)
				.withDescription(desc)
				.setFrames(frames.split(";"))
				.setTicks(ticks)
				.withColor(ChatColor.GOLD)
				.setHideIfUnowned(true)
				.build();
	}

	private ItemizedTrack track(String id, String name, String tierName)
	{
		return TrackBuilder.builder(id)
				.withShortName(name)
				.withDescription(tierName)
				.setTierName(tierName)
				.withColor(ChatColor.GOLD)
				.setTierColor(ChatColor.GOLD)
				.setHideIfUnowned(true)
				.build();
	}

	private ItemizedTrack track0(String id, String shortName, String desc, String tierName)
	{
		return TrackBuilder.builder(id)
				.withShortName(shortName)
				.withDescription(desc)
				.setTierName(tierName)
				.withColor(ChatColor.GOLD)
				.setTierColor(ChatColor.GOLD)
				.setHideIfUnowned(true)
				.build();
	}

	private ItemizedTrack track1(String id, String name, String desc)
	{
		return TrackBuilder.builder(id)
				.withShortName(name)
				.withDescription(desc)
				.setTierName(name)
				.withColor(ChatColor.GOLD)
				.setTierColor(ChatColor.GOLD)
				.setHideIfUnowned(true)
				.build();
	}

	private ItemizedTrack track(String id, String shortName, String longName, String tierName)
	{
		return TrackBuilder.builder(id)
				.withShortName(shortName)
				.withLongName(longName)
				.withDescription(tierName)
				.setTierName(tierName)
				.withColor(ChatColor.GOLD)
				.setTierColor(ChatColor.GOLD)
				.setHideIfUnowned(true)
				.build();
	}
}
