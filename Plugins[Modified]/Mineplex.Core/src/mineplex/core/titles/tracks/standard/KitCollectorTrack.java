package mineplex.core.titles.tracks.standard;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.achievement.AchievementCategory;
import mineplex.core.donation.DonationManager;
import mineplex.core.game.GameDisplay;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class KitCollectorTrack extends Track
{
	private static final int ACHIEVEMENT_KIT_BONUS = 5;

//	private static final Map<GameDisplay, AchievementCategory>

	private final DonationManager _donationManager = Managers.require(DonationManager.class);

	public KitCollectorTrack()
	{
		super("kit-collector", "Kit Collector", "The Kit Collector tree is unlocked by having kits unlocked");
		getRequirements()
				.addTier(new TrackTier(
						"Kit Collector",
						"Gain 25 Kit Collector Points",
						this::getStat,
						20,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Kit Hoarder",
						"Gain 50 Kit Collector Points",
						this::getStat,
						50,
						new TrackFormat(ChatColor.BLUE, null)
				))
				.addTier(new TrackTier(
						"I Have Too Many Kits",
						"Gain 100 Kit Collector Points",
						this::getStat,
						100,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));
	}

	private int getUnlockedKits(Player player)
	{
		int ownedKits = 0;

		for (String unknownPackage : _donationManager.Get(player).getOwnedUnknownSalesPackages())
		{
			for (GameDisplay gameDisplay : GameDisplay.values())
			{
				if (unknownPackage.startsWith(gameDisplay.getKitGameName() + " "))
				{
					ownedKits++;
					break;
				}
			}
		}

		for (AchievementCategory category : AchievementCategory.values())
		{
			int[] gameIDs = category.GameId;
			if (gameIDs != null)
			{
				for (int id : gameIDs)
				{
					GameDisplay display = GameDisplay.getById(id);
					if (display != null)
					{

					}
				}
			}
		}

		return ownedKits;
	}
}
