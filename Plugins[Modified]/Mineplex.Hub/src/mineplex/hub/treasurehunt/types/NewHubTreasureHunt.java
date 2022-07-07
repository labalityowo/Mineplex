package mineplex.hub.treasurehunt.types;

import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.hologram.Hologram;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackManager.TrackGiveResult;
import mineplex.core.titles.tracks.award.NewHub2018Track;
import mineplex.core.treasure.types.TreasureType;
import mineplex.hub.treasurehunt.TreasureHunt;
import mineplex.hub.treasurehunt.TreasureHuntManager;

public class NewHubTreasureHunt extends TreasureHunt
{

	private static final String SKIN = "Pffft";
	private final Track _track;

	public NewHubTreasureHunt(TreasureHuntManager manager, Map<Block, Integer> treasure)
	{
		super(manager, treasure);

		_track = manager.getTrackManager().getTrack(NewHub2018Track.class);
	}

	@Override
	public void createTreasure(Block block, int id)
	{
		if (id == 1)
		{
			new Hologram(_manager.getHologramManager(), block.getLocation().add(0.5, 0.5, 0.5), C.cYellow + C.Scramble + "!" + C.cGoldB + " Treasure Hunt " + C.cYellow + C.Scramble + "!")
					.start();
		}

		block.setType(Material.SKULL);
		block.setData((byte) 1);
		Skull skull = (Skull) block.getState();
		skull.setSkullType(SkullType.PLAYER);
		skull.setOwner(SKIN);
		skull.update();
	}

	@Override
	public void onTreasureFind(Player player, Block block, Set<Integer> found)
	{
		player.playSound(player.getLocation(), Sound.VILLAGER_YES, 1, 1);
		UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, block.getLocation().add(0.5, 0.5, 0.5), 0.2F, 0.2F, 0.2F, 0, 8, ViewDist.NORMAL, player);

		if (found.size() == getTreasure().size())
		{
			_manager.getTrackManager().unlockTrack(player, _track, result ->
			{
				if (result == TrackGiveResult.SUCCESS)
				{
					player.sendMessage(F.main(_manager.getName(), "You found all of the treasure and received the " + F.name(_track.getLongName()) + " Title Track and an " + TreasureType.OMEGA.getName() + C.cGray + "!"));
				}
				else
				{
					player.sendMessage(F.main(_manager.getName(), "Failed to give you the title track."));
				}

				_manager.getInventoryManager().addItemToInventory(null, player, TreasureType.OMEGA.getItemName(), 1);
			});
		}
		else if (found.size() == 1)
		{
			player.sendMessage(F.main(_manager.getName(), "Find all the " + F.name("Globes") + " around the hub to get an exclusive " + F.name("Title Track") + " and " + TreasureType.OMEGA.getName() + C.cGray + "!"));
		}
	}
}
