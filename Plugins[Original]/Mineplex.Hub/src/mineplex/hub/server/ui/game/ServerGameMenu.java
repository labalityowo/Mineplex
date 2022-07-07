package mineplex.hub.server.ui.game;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.achievement.Achievement;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.boosters.Booster;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTime;
import mineplex.core.donation.DonationManager;
import mineplex.core.game.GameDisplay;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.personalServer.PersonalServerManager;
import mineplex.core.preferences.Preference;
import mineplex.core.preferences.UserPreferences;
import mineplex.core.shop.page.ShopPageBase;
import mineplex.core.stats.PlayerStats;
import mineplex.core.titles.tracks.Track;
import mineplex.core.treasure.TreasureLocation;
import mineplex.hub.hubgame.HubGameManager;
import mineplex.hub.hubgame.ui.HubGamesPage;
import mineplex.hub.server.ServerManager;

public class ServerGameMenu extends ShopPageBase<ServerManager, QuickShop>
{

	private static final String ARCADE_BOOSTER_GROUP = "Arcade";
	private static final ItemStack PREFERENCES = new ItemBuilder(Material.REDSTONE_COMPARATOR)
			.setTitle(C.cRedB + "Player Settings")
			.addLore(UtilText.splitLineToArray("Want to turn something off? This is the place for it. All player-specific settings can be found here. You can also use the " + C.cWhite + "/prefs" + C.cGray + " command at any time to open this.", LineFormat.LORE))
			.build();
	private static final ItemStack LOBBY = new ItemBuilder(Material.GOLD_BLOCK)
			.setTitle(C.cYellowB + "Lobby Stuff")
			.addLore
					(
							"",
							"Quickly teleport to our lobby games",
							"or parkours with this handy-dandy",
							"teleport tool.",
							"",
							C.cYellow + C.Line + "Click me for more options!"
					)
			.build();
	private static final ItemStack MPS = new ItemBuilder(Material.MELON)
			.setTitle(C.cYellowB + "Mineplex Player Servers")
			.addLore
					(
							"Looking to play something a little different?",
							"Try these player hosted servers that feature",
							"removed games, game voting and more!",
							"",
							C.cGreen + "Left-Click to Join!",
							C.cRed + "Right-Click to Host! " + C.cGray + "[Requires " + PermissionGroup.LEGEND.getDisplay(true, true, true, true) + C.cGray + "]"
					)
			.build();

	ServerGameMenu(ServerManager plugin, QuickShop quickShop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player)
	{
		super(plugin, quickShop, clientManager, donationManager, name, player);

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		UserPreferences preferences = getPlugin().getPreferencesManager().get(getPlayer());
		AchievementManager achievementManager = getPlugin().getAchievementManager();
		PlayerStats stats = achievementManager.getStatsManager().Get(getPlayer());
		long totalAchievements = Arrays.stream(Achievement.values())
				.filter(achievement -> achievementManager.get(stats, achievement).getLevel() >= achievement.getMaxLevel())
				.count();
		GameDisplay bestGame = Arrays.stream(GameDisplay.values())
				.min((o1, o2) -> (int) (stats.getStat(o2.getName() + ".Wins") - stats.getStat(o1.getName() + ".Wins")))
				.orElse(null);
		Track track = getPlugin().getTitles().getActiveTrack(getPlayer());

		addButton(9, PREFERENCES, (player, clickType) -> _plugin.getPreferencesManager().openMenu(player));
		addButton(10, new ItemBuilder(Material.SKULL_ITEM, (byte) 3)
						.setPlayerHead(getPlayer().getName())
						.setTitle(C.cGreenB + getPlayer().getName() + "\'s " + C.cDGreen + "Stats and Achievements")
						.addLore
								(
										"The one stop shop for everything you.",
										"Get a leg up on your next game by finding",
										"what you're best at.",
										"",
										C.cWhite + "Game: " + C.cGray + "►► " + C.cGreen + (bestGame == null ? "None" : bestGame.getLobbyName()),
										C.cWhite + "Title: " + C.cGray + "►► " + C.cGreen + (track == null ? "None" : track.getRequirements().getTier(getPlayer()).getDisplayName()),
										C.cGray + "Total Achievements: " + C.cWhite + totalAchievements,
										"",
										C.cGray + "Total Gems Earned: " + C.cWhite + stats.getStat("Global.GemsEarned"),
										C.cGray + "Games Played: " + C.cWhite + stats.getStat("Global.GamesPlayed"),
										C.cGray + "Time In Game: " + C.cWhite + UtilTime.MakeStr(stats.getStat("Global.TimeInGame") * 1000),
										"",
										C.cYellow + C.Line + "Click me for more info"
								)
						.build(),
				(player, clickType) -> getPlugin().getAchievementManager().openShop(player));
		addButton(11, new ItemBuilder(Material.EMERALD)
						.setTitle(C.cAquaB + "Teleport to Game Area" + C.cGray + " (" + C.cGray + (!preferences.isActive(Preference.AUTO_QUEUE) ? C.cGreen + "ON" : C.cRed + "OFF") + C.cGray + ")")
						.addLore
								(
										"Changing this setting to " + (preferences.isActive(Preference.AUTO_QUEUE) ? C.cGreen + "ON" + C.cGray + " will disable opening" : C.cRed + "OFF" + C.cGray + " will open"),
										"the server selector" + (preferences.isActive(Preference.AUTO_QUEUE) ? ", instead" : " instead of"),
										"teleporting you to the game area."
								)
						.build(),
				(player, clickType) ->
				{
					preferences.toggle(Preference.AUTO_QUEUE);
					getPlugin().getPreferencesManager().save(preferences);
					refresh();
				});
// TODO Add with QM, not easily done now
//		addButton(13, new ItemBuilder(Material.EMERALD_BLOCK)
//						.setTitle(C.cYellowB + "Join Last Played Game")
//						.addLore
//								(
//										"Go straight into the last game",
//										"you played. Waste no time!",
//										"",
//										C.cDAqua + "Last Game Played:",
//										C.cDAqua + "►► " + C.cYellow + "No Clue",
//										"",
//										C.cDRed + "Note: " + C.cGray + "It may take some time depending",
//										"on the amount of players active."
//								)
//						.build(),
//				(player, clickType) ->
//				{
//
//				});

		addButton(15, LOBBY, (player, clickType) ->
		{
			HubGameManager manager = getPlugin().getHubManager().getHubGameManager();
			manager.getShop().openPageForPlayer(player, new HubGamesPage(manager, player));
		});

		addButton(16, new ItemBuilder(Material.ENDER_CHEST)
						.setTitle(C.cPurpleB + "Open A Chest")
						.addLore
								(
										"Have a few shards floating around?",
										"Open a random chest for a chance at some epic loot!",
										"",
										C.cPurple + "►►" + C.cDPurple + " You have " + C.cPurpleB + getPlugin().getTreasureManager().getChestsToOpen(getPlayer()) + C.cDPurple + " chests to open! " + C.cPurple + "◄◄",
										"",
										"Make sure to check out " + C.cYellow + "Mineplex.com/shop" + C.cGray + " for more!"
								)
						.build(),
				(player, clickType) ->
				{
					TreasureLocation location = getPlugin().getTreasureManager().getOpenTreasureLocation();

					if (location == null)
					{
						player.sendMessage(F.main(getPlugin().getTreasureManager().getName(), "Sorry, there isn't an open platform right now."));
						return;
					}

					location.getShop().attemptShopOpen(player);
				});

		addButton(17, MPS,
				(player, clickType) ->
				{
					if (clickType.isLeftClick())
					{
						getPlugin().teleportOrOpen(player, "Mineplex Player Servers", false);
					}
					else
					{
						if (getClientManager().Get(player).hasPermission(PersonalServerManager.Perm.MPS))
						{
							playAcceptSound(player);
							getPlugin().getPersonalServerManager().hostServer(player, player.getName(), false);
						}
						else
						{
							playDenySound(player);
							player.sendMessage(F.main(getPlugin().getPersonalServerManager().getName(), "You need to be " + PermissionGroup.LEGEND.getDisplay(true, false, false, true) + C.cGray + " or higher to create an MPS."));
						}

						player.closeInventory();
					}
				});

		add(29, Material.WOOD, "Master Builders", C.cGreenB + "Master Builders " + C.cGray + "Creative Build", new String[]
				{
						"Wow so pretty! 10/10",
						"Time to show off your real building skills",
						"in this arena-type build off. Don't",
						"fall behind or there might be an explosive result!",
						"",
						C.cWhite + "Perfect for Solo or Parties up to 12."
				}, "Master_Builders");

		add(30, Material.BOOK_AND_QUILL, "Draw My Thing", C.cGreenB + "Draw My Thing " + C.cGray + "Pictionary", new String[]
				{
						"Basically Picasso",
						"Draw your way to victory in this skillful game",
						"that requires you to draw an image and guess the",
						"word!",
						"",
						C.cWhite + "Perfect for Solo or Parties up to 8."
				}, "Draw_My_Thing");
		add(31, Material.LAVA_BUCKET, "Micro Battles", C.cGreenB + "Micro Battles " + C.cGray + "Fast Paced Team PvP", new String[]
				{
						"It fits in your pocket!",
						"A 4v4v4v4 basic pvp battle to see who is left",
						"alive! Don't fall off!",
						"",
						C.cWhite + "Perfect for Solo or Parties up to 4."
				}, ARCADE_BOOSTER_GROUP);
		add(32, Material.NOTE_BLOCK, "Mixed Arcade", C.cGreenB + "Mixed Arcade " + C.cGray + "Multiple Quick Games", new String[]
				{
						"It's a huge party!",
						"Switch between multiple different gamemodes",
						"and play all our different arcade games!",
						"",
						C.cWhite + "Perfect for Solo or Parties up to 16."
				}, ARCADE_BOOSTER_GROUP);
		add(33, Material.WOOL, 3, "Turf Wars", C.cGreenB + "Turf Wars " + C.cGray + "Team PvP", new String[]
				{
						"Fight for your color!",
						"A blue vs red battle to take the most land!",
						"Every kill means more turf for your team. Seize",
						"all the turf and win the game!",
						"",
						C.cWhite + "Perfect for Solo or Parties up to 8."
				}, ARCADE_BOOSTER_GROUP);
		add(38, Material.QUARTZ_BLOCK, "Speed Builders", C.cYellowB + "Speed Builders " + C.cGray + "Building Memory Game", new String[]
				{
						"How good is your memory?",
						"You only have a short time to remember the build",
						"before it's your turn to build it. Pay attention to",
						"every small detail and remember: GWEN is watching!",
						"",
						C.cWhite + "Perfect for Solo or Parties of 8."
				}, "Speed_Builders");
		add(39, Material.GRASS, "Block Hunt", C.cYellowB + "Block Hunt " + C.cGray + "Hide & Seek", new String[]
				{
						"Shhh, they can see you!",
						"A cat and mouse game of your favorite",
						"Minecraft objects and blocks. Can you see them?",
						"Try and hide, or try and seek to win the game!",
						"",
						C.cWhite + "Perfect for Solo or Parties up to 16."
				}, "Block_Hunt");
		add(40, Material.CAKE, 0, "Cake Wars", C.cYellowB + "Cake Wars " + C.cGray + "4 Teams/Duos PvP", new String[]
				{
						"Winner Winner Cake For Dinner",
						"A challenging 4 or 8 team battle to protect your",
						"cake. Don't let your cake fall, and you'll be in",
						"for a treat. Last team standing wins.",
						"",
						C.cWhite + "Perfect for Parties of 2 to 4."
				}, "Cake_Wars");
		add(41, Material.DIAMOND_SWORD, "Survival Games", C.cYellowB + "Survival Games " + C.cGray + "Solo/Team Survival", new String[]
				{
						"Trust no one.",
						"A free for all of pvp madness. Go straight for the",
						"chests, go straight for the kills. Avoid the dragon",
						"at all costs. Just. Win.",
						"",
						C.cWhite + "Perfect for Solo or Parties of 2."
				}, "Survival_Games");
		add(42, Material.FEATHER, "Skywars", C.cYellowB + "Skywars " + C.cGray + "Solo/Team Survival", new String[]
				{
						"It's kinda high up here...",
						"A war in the sky. Spawn on a floating island and",
						"be the last alive. Find chests, mine for resources",
						"and kill everyone you see.",
						"",
						C.cWhite + "Perfect for Solo or Parties of 2."
				}, "Skywars");
		add(47, Material.IRON_PICKAXE, "The Bridges", C.cRedB + "The Bridges " + C.cGray + "Team Game", new String[]
				{
						"Protect your land!",
						"A resource gathering game that after 10 minutes",
						"turns into the last team alive as bridges fall from",
						"the sky and connect the islands together.",
						"",
						C.cWhite + "Perfect for Solo or Parties up to 10."
				}, "Bridges");
		add(48, Material.TNT, "Mine-Strike", C.cRedB + "Mine-Strike " + C.cGray + "2 Team Gun Battles", new String[]
				{
						"Pew Pew Pew",
						"Fight with guns and skill and take your team to the top.",
						"Plant the bomb, earn money to buy better guns, don't die!",
						"",
						C.cWhite + "Perfect for Solo or Parties up to 8."
				}, "MineStrike");
		add(49, Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.CREEPER), "Super Smash Mobs", C.cRedB + "Super Smash Mobs " + C.cGray + "Solo/Team Arena Brawl", new String[]
				{
						"Oink! Baa! Moo!",
						"Fly though the air in this kit based arena brawl with",
						"all of your favorite Minecraft mobs and characters.",
						"Be careful though, you only have so many lives!",
						"",
						C.cWhite + "Perfect for Solo or Parties up to 2."
				}, "Smash_Mobs");
		add(50, Material.GOLD_SWORD, "Champions", C.cRedB + "Champions " + C.cGray + "Team PvP", new String[]
				{
						"Which class are you?",
						"A brawl of unique classes and builds where you",
						"fight over classic objectives with not-so-classic classes.",
						"",
						C.cWhite + "Perfect for Solo or Parties up to 5."
				}, "Champions");
		add(51, Material.IRON_DOOR, "Clans", C.cRedB + "Clans " + C.cGray + "Factions Survival", new String[]
				{
						"Hide. Fight. War. Win.",
						"Fight in this medieval land with Champions classes to",
						"take over as much territory as you can, and besiege",
						"as many Clans as you can before they destroy you.",
						"",
						C.cWhite + "Perfect for Solo or Parties of any size."
				}, null);
		add(13, new ItemBuilder(Material.EGG), "Nano Games", C.cYellow + C.Scramble + "ABC" + C.cGreenB + " Nano Games " + C.cYellow + C.Scramble + "DEF"  + C.cGray + " Beta Game", new String[]
				{
						"Game Over? Straight into the next!",
						"Want to enjoy constant action with no",
						"waiting around? Then Nano Games are perfect",
						"for you. Play over 20 quick games. Once",
						"you're down hit the Cash Out Clock and",
						"claim your rewards.",
						"",
						C.cWhite + "Perfect for Solo or Parties up to 16."
				}, "Nano_Games", true);

		fillRow(27, (byte) 5);
		fillRow(36, (byte) 4);
		fillRow(45, (byte) 14);
	}

	private void add(int slot, Material material, String npcName, String title, String[] lore, String boosterGroup)
	{
		add(slot, material, 0, npcName, title, lore, boosterGroup);
	}

	private void add(int slot, Material material, int data, String npcName, String title, String[] lore, String boosterGroup)
	{
		add(slot, new ItemBuilder(material, 1, (short) data), npcName, title, lore, boosterGroup);
	}

	private void add(int slot, ItemBuilder builder, String npcName, String title, String[] lore, String boosterGroup)
	{
		add(slot, builder, npcName, title, lore, boosterGroup, false);
	}

	private void add(int slot, ItemBuilder builder, String npcName, String title, String[] lore, String boosterGroup, boolean feature)
	{
		if (boosterGroup != null)
		{
			Booster booster = getPlugin().getBoosterManager().getActiveBooster(boosterGroup);

			if (booster != null)
			{
				builder.addLore
						(
								"Amplified by " + C.cGreen + booster.getPlayerName() + C.cWhite + " - " + C.cGreen + booster.getTimeRemainingString(),
								"All players earn " + C.cAqua + "2x Shards"
						);
				builder.setGlow(true);
			}
		}

		if (feature)
		{
			builder.setGlow(true);
		}

		if (title != null)
		{
			builder.setTitle(title);
		}

		builder.addLore("");

		if (lore != null)
		{
			builder.addLore(lore);
		}

		String[] serverTags = getPlugin().getServerTags(npcName);

		if (serverTags != null && serverTags.length > 0)
		{
			int playerCount = 0;

			for (String tag : serverTags)
			{
				playerCount += getPlugin().getGroupTagPlayerCount(tag);
			}

			builder.addLore("", C.cYellow + "►► Join " + C.cGoldB + playerCount + C.cYellow + " other players!");
		}

		builder.setHideInfo(true);

		addButton(slot, builder.build(), (player, clickType) -> getPlugin().teleportOrOpen(player, npcName, true));
	}

	private void fillRow(int start, byte colour)
	{
		ItemStack itemStack = new ItemBuilder(Material.STAINED_GLASS_PANE, colour)
				.setTitle(C.cBlack)
				.build();

		for (int i = start; i < start + 9; i++)
		{
			if (getItem(i) == null)
			{
				addButtonNoAction(i, itemStack);
			}
		}
	}
}
