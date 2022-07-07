package mineplex.core.mission.ui;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import mineplex.core.achievement.leveling.rewards.LevelReward;
import mineplex.core.common.util.C;
import mineplex.core.game.GameDisplay;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.mission.MissionClient;
import mineplex.core.mission.MissionLength;
import mineplex.core.mission.MissionManager;
import mineplex.core.mission.PlayerMission;
import mineplex.core.shop.confirmation.ConfirmationCallback;
import mineplex.core.shop.confirmation.ConfirmationPage;
import mineplex.core.shop.confirmation.ConfirmationProcessor;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.core.stats.PlayerStats;

public class MissionMainPage extends ShopPageBase<MissionManager, MissionShop>
{

	MissionMainPage(MissionManager plugin, MissionShop shop, Player player)
	{
		super(plugin, shop, plugin.getClientManager(), plugin.getDonationManager(), "Missions", player, 27);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		int column = 0;
		MissionClient client = _plugin.Get(getPlayer());
		Set<PlayerMission> missions = client.getMissions();
		PlayerStats stats = _plugin.getStatsManager().Get(getPlayer());

		for (MissionLength length : MissionLength.values())
		{
			ItemBuilder glassBuilder = new ItemBuilder(Material.STAINED_GLASS_PANE, length.getColourData())
					.setTitle(length.getChatColour() + length.getName() + " Missions")
					.addLore("")
					.addLore(length.getResetInfo());

			if (length != MissionLength.EVENT)
			{
				glassBuilder.addLore(
						"",
						C.cDAqua + "You've completed " + C.cAqua + stats.getStat(length.getStatName()) + C.cDAqua + " " + length.getName().toLowerCase() + " missions."
				);
			}

			addButtonNoAction(getSlot(length.ordinal(), 0), glassBuilder.build());
		}

		MissionLength lastLength = null;

		for (PlayerMission<?> mission : missions)
		{
			if (mission.getRewards() == null)
			{
				continue;
			}

			int row = mission.getLength().ordinal();
			int progress = mission.getCurrentProgress();
			int requiredProgress = mission.getRequiredProgress();
			boolean complete = mission.hasRewarded();
			boolean discarded = mission.isDiscarded();
			boolean eventMission = mission.getLength() == MissionLength.EVENT;
			GameDisplay[] games = mission.getGames();

			ItemBuilder builder = new ItemBuilder(Material.PAPER)
					.setTitle((complete ? C.cGreenB : C.cYellowB))
					.addLore("");

			if (games.length == GameDisplay.values().length)
			{
				builder.addLore("Game: " + C.Reset + "Any");
			}
			else if (games.length > 0)
			{
				String lore = "Game: " + C.Reset + games[0].getName();

				if (games.length > 1)
				{
					StringBuilder gameBuilder = new StringBuilder(lore);

					for (int i = 1; i < games.length; i++)
					{
						GameDisplay game = games[i];

						gameBuilder
								.append(C.mBody)
								.append(", ")
								.append(C.Reset)
								.append(game.getName());
					}

					lore = gameBuilder.toString();
				}

				builder.addLore(lore);
			}
			else
			{
				builder.addLore("Lobby Mission");
			}

			String progressString;

			if (complete)
			{
				progressString = C.cGreenB + "Complete";
			}
			else if (discarded)
			{
				progressString = C.cRedB + "Discarded";
			}
			else
			{
				progressString = C.cPurple + (int) (((double) progress / requiredProgress) * 100) + "%" + C.mBody + " - " + C.cPurple + progress + "/" + requiredProgress;
			}

			builder.addLore(
					"Objective: " + C.Reset + mission.getDescription(),
					"Progress: " + progressString,
					"",
					"Rewards:"
			);

			for (LevelReward reward : mission.getRewards())
			{
				builder.addLore(C.mBody + "- " + (complete || discarded ? C.Strike + ChatColor.stripColor(reward.getDescription()) : reward.getDescription()));
			}

			if (complete)
			{
				builder.setTitle(C.cYellowB + mission.getName());
			}
			else if (discarded)
			{
				builder.setTitle(C.cRedB + mission.getName());
				builder.addLore("", C.cRed + "This mission is discarded.");
			}
			else
			{
				builder.setGlow(true);
				builder.setTitle(C.cGreenB + mission.getName());
				builder.addLore("", C.cGreen + "This mission is active." + (eventMission ? "" : " Click to discard."));
			}

			if (lastLength == null || lastLength != mission.getLength())
			{
				lastLength = mission.getLength();
				column = 0;
			}

			addButton(getSlot(row, ++column), builder.build(), (player, clickType) ->
			{
				if (discarded || eventMission)
				{
					playDenySound(player);
				}
				else
				{
					ItemStack displayItem = new ItemBuilder(Material.PAPER)
							.setTitle(C.cRedB + "Discard " + mission.getName())
							.addLore("", "This will discard the mission.", "You will " + C.cRedB + "NOT" + C.mBody + " be able to progress and", "claim the rewards for this mission.", "A new one will replace it " + mission.getLength().getResetWhen() + ".")
							.build();

					getShop().openPageForPlayer(player, new ConfirmationPage<>(player, this, new ConfirmationProcessor()
					{
						@Override
						public void init(Inventory inventory)
						{

						}

						@Override
						public void process(ConfirmationCallback callback)
						{
							getPlugin().discardMission(player, mission);
							callback.resolve("Discarded");
							refresh();
						}
					}, displayItem));
				}
			});
		}

		if (getItem(19) == null)
		{
			addButtonNoAction(19, new ItemBuilder(Material.BARRIER)
					.setTitle(C.cRedB + "No Event Missions :(")
					.addLore("", "There are no event missions", "available right now.")
					.build());
		}
	}

}
