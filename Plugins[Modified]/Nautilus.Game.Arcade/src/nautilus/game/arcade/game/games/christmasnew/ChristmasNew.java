package nautilus.game.arcade.game.games.christmasnew;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlockText;
import mineplex.core.common.util.UtilBlockText.TextAlign;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.gadget.gadgets.particle.christmas.ParticlePumpkinShield;
import mineplex.core.gadget.types.Gadget;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.award.CCIIPublicTrack;
import mineplex.core.treasure.types.TreasureType;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.games.christmas.ChristmasCommon;
import nautilus.game.arcade.game.games.christmas.kits.KitPlayer;
import nautilus.game.arcade.game.games.christmasnew.section.five.Section5;
import nautilus.game.arcade.game.games.christmasnew.section.four.Section4;
import nautilus.game.arcade.game.games.christmasnew.section.one.Section1;
import nautilus.game.arcade.game.games.christmasnew.section.six.Section6;
import nautilus.game.arcade.game.games.christmasnew.section.three.Section3;
import nautilus.game.arcade.game.games.christmasnew.section.two.Section2;
import nautilus.game.arcade.game.modules.gamesummary.GameSummaryModule;
import nautilus.game.arcade.kit.Kit;

public class ChristmasNew extends ChristmasCommon
{

	private static final String[] DESCRIPTION =
			{
					"Someone has stolen all of the Christmas presents.",
					"Help Santa Claus get them back and",
					"find out who stole the presents."
			};
	private static final FireworkEffect FIREWORK_EFFECT = FireworkEffect.builder()
			.with(Type.BALL_LARGE)
			.withColor(Color.LIME, Color.RED)
			.withFade(Color.WHITE)
			.withFlicker()
			.build();

	private final Gadget _gadgetReward;
	private final Track _titleReward;
	private final List<Player> _playersToReward;

	private final Comparator<Location> _locationComparator = (o1, o2) ->
	{
		double o1Dist = UtilMath.offsetSquared(o1, _sleighSpawn);
		double o2Dist = UtilMath.offsetSquared(o2, _sleighSpawn);

		if (o1Dist == o2Dist)
		{
			return 0;
		}

		return o1Dist > o2Dist ? 1 : -1;
	};

	private List<Location> _winSpawns;
	private Location _winText;

	public ChristmasNew(ArcadeManager manager)
	{
		super(manager, GameType.ChristmasNew, new Kit[]
				{
					new KitPlayer(manager)
				}, DESCRIPTION);
		
		AnticheatDisabled = true;
		StrictAntiHack = true;
		WorldTimeSet = 4000;
		WinEffectEnabled = false;

		_gadgetReward = manager.getCosmeticManager().getGadgetManager().getGadget(ParticlePumpkinShield.class);
		_titleReward = manager.getTrackManager().getTrack(CCIIPublicTrack.class);
		_playersToReward = new ArrayList<>();

		getModule(GameSummaryModule.class)
				.addComponent(new ChristmasSummaryComponent(_playersToReward::contains));
	}

	// Take the parse at the purple bridge
	// /parse600 129 88 41 137 165 14 110 179 21 22 153 103 15 121
	@Override
	public void ParseData()
	{
		super.ParseData();

		List<Location> presents = WorldData.GetDataLocs("LIME");
		presents.sort(_locationComparator);

		List<Location> targets = WorldData.GetDataLocs("PINK");
		targets.sort(_locationComparator);

		_sections.add(new Section1(this, targets.remove(0), presents.remove(0), presents.remove(0)));
		_sections.add(new Section2(this, targets.remove(0), presents.remove(0), presents.remove(0)));
		_sections.add(new Section3(this, targets.remove(0), presents.remove(0)));
		_sections.add(new Section4(this, targets.remove(0)));
		_sections.add(new Section5(this, targets.remove(0), presents.remove(0), presents.remove(0)));
		_sections.add(new Section6(this, targets.remove(0), presents.remove(0)));

		_winSpawns = WorldData.GetCustomLocs("WIN SPAWN");
		_winText = WorldData.GetCustomLocs("WIN TEXT").get(0);
	}

	@EventHandler
	public void gameStart(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		Manager.runSyncLater(() -> sendSantaMessage("Thank you for coming. Someone has stolen all of the Christmas presents. I need your help to get them back.", ChristmasNewAudio.SANTA_INTRO), 40);
	}

	@EventHandler
	public void gameLive(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		sendSantaMessage("Follow me, let's find out who’s behind this.", ChristmasNewAudio.SANTA_FOLLOW_ME_2);
	}

	@EventHandler
	public void gameEnd(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End || WinnerTeam == null || !WinnerTeam.equals(getPlayersTeam()))
		{
			return;
		}

		Location location = WorldData.GetCustomLocs("WIN TEXT").get(0);
		BlockFace face = BlockFace.WEST;
		int blockId = Material.STAINED_CLAY.getId();
		TextAlign align = TextAlign.CENTER;

		UtilBlockText.MakeText("Merry Christmas", location, face, blockId, (byte) 14, align);
		UtilBlockText.MakeText("from", location.subtract(0, 6, 0), face, blockId, (byte) 5, align);
		UtilBlockText.MakeText("Mineplex", location.subtract(0, 6, 0), face, blockId, (byte) 4, align);

		for (Player player : UtilServer.getPlayersCollection())
		{
			player.teleport(UtilAlg.Random(_winSpawns));
			player.getInventory().clear();
		}
	}

	@EventHandler
	public void gameEndUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || GetState() != GameState.End || WinnerTeam == null)
		{
			return;
		}

		UtilFirework.playFirework(UtilAlg.getRandomLocation(_winText, 20, 1, 20), FIREWORK_EFFECT);
	}

	@Override
	public void endGame(boolean victory, String customLine)
	{
		if (victory)
		{
			_playersToReward.addAll(GetPlayers(true));
			_playersToReward.removeIf(_gadgetReward::ownsGadget);

			_playersToReward.forEach(player ->
			{
				Manager.GetDonation().purchaseUnknownSalesPackage(player, _gadgetReward.getName(), GlobalCurrency.GEM, 0, true, null);
				Manager.getTrackManager().unlockTrack(player, _titleReward);
				Manager.getInventoryManager().addItemToInventory(player, TreasureType.GINGERBREAD.getItemName(), 1);
			});
		}

		super.endGame(victory, customLine);
	}
}
