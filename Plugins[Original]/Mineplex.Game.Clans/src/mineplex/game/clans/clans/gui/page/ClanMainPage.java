package mineplex.game.clans.clans.gui.page;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilSkull;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.item.ShopItem;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClanRole;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.ClansPlayer;
import mineplex.game.clans.clans.ClansPlayerComparator;
import mineplex.game.clans.clans.gui.ClanIcon;
import mineplex.game.clans.clans.gui.ClanShop;
import mineplex.game.clans.clans.gui.button.ClanCreateButton;
import mineplex.game.clans.clans.gui.button.ClanEnergyButton;
import mineplex.game.clans.clans.gui.button.ClanInviteButton;
import mineplex.game.clans.clans.gui.button.ClanLeaveButton;
import mineplex.game.clans.clans.gui.button.ClanMemeberButton;
import mineplex.game.clans.clans.gui.button.ClanTerritoryButton;
import mineplex.game.clans.clans.gui.button.ClanWhoButton;
import mineplex.game.clans.core.war.ClanWarData;

public class ClanMainPage extends ClanPageBase
{
	private static boolean USE_RESOURCE_ICONS = true;

	public ClanMainPage(ClansManager plugin, ClanShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player)
	{
		super(plugin, shop, clientManager, donationManager, "Manage Clan", player, 54);
		
		buildPage();
	}

	@Override
	public void buildNoClan()
	{
		// Clan Create
		ShopItem clanCreate = new ShopItem(Material.BOOK_AND_QUILL, "Create Clan", new String[] {C.cGray + "To create a clan type", C.cRed + "/c create <ClanName>"}, 1, false, false);
		addButton(21, clanCreate, new ClanCreateButton());

		// Clan Join
		ShopItem clanJoin = new ShopItem(ClanIcon.JOIN.getMaterial(), ClanIcon.JOIN.getData(), "Join Clan", new String[] {}, 1, false, false);
		addButton(23, clanJoin, new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				getShop().openPageForPlayer(getPlayer(), new ClanJoinPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), getPlayer()));
			}
		});
	}

	@Override
	public void buildClan(final ClanInfo clanInfo, ClanRole clanRole)
	{
		// Invites
		{
			String inviteName = "Invites";
			Material inviteMaterial = USE_RESOURCE_ICONS ? ClanIcon.JOIN.getMaterial() : Material.RED_ROSE;
			byte inviteDate = USE_RESOURCE_ICONS ? ClanIcon.JOIN.getData() : 0;
			int size = clanInfo.getSize();
			int maxSize = clanInfo.getMaxSize();
			ArrayList<String> inviteLore = new ArrayList<String>();
			inviteLore.add(" ");
			inviteLore.add(C.Reset + C.cGray + "Clans can have a max size of " + C.cYellow + maxSize + C.cGray + " members");
			inviteLore.add(C.Reset + C.cGray + "You currently have " + C.cYellow + size + C.cGray + " members");
			inviteLore.add(C.Reset + C.cGray + "More members in your clan will allow you to");
			inviteLore.add(C.Reset + C.cGray + "claim more land, but it will also increase");
			inviteLore.add(C.Reset + C.cGray + "your Energy drain per minute.");
			if (clanRole.has(ClanRole.ADMIN))
			{
				inviteLore.add(" ");
				inviteLore.add(C.Reset + C.cYellow + "Left Click " + C.cWhite + "Invite Player");
			}

			ShopItem inviteItem = new ShopItem(inviteMaterial, inviteDate, inviteName, inviteLore.toArray(new String[0]), 1, false, false);
			ClanInviteButton inviteButton = new ClanInviteButton(getShop(), getPlugin(), getDonationManager(), getPlayer(), clanInfo, clanRole);
			addButton(0, inviteItem, inviteButton);
		}

		// Territory
		{
			String territoryName = "Territory";
			Material territoryMaterial = USE_RESOURCE_ICONS ? ClanIcon.TERRITORY.getMaterial() : Material.GRASS;
			byte territoryData = USE_RESOURCE_ICONS ? (byte) ClanIcon.TERRITORY.getData() : 0;
			ArrayList<String> territoryLore = new ArrayList<String>();
			int claims = clanInfo.getClaims();
			int maxClaims = clanInfo.getClaimsMax();
			territoryLore.add(" ");
			territoryLore.add(C.Reset + C.cGray + "Every land claim represents a 16x16 chunk");
			territoryLore.add(C.Reset + C.cGray + "Your clan can claim a maximum of " + C.cYellow + maxClaims + C.cGray + " chunks");
			territoryLore.add(C.Reset + C.cGray + "You currently have " + C.cYellow + claims + C.cGray + " chunk(s) claimed");
			territoryLore.add(C.Reset + C.cGray + "Increase max claims with more clan members");
			territoryLore.add(C.Reset + C.cGray + "Energy cost will increase with more land claimed");
			if (clanRole.has(ClanRole.ADMIN))
			{
				territoryLore.add(" ");
				territoryLore.add(ChatColor.RESET + C.cYellow + "Left Click " + C.cWhite + "Claim Land");
				territoryLore.add(ChatColor.RESET + C.cYellow + "Shift-Left Click " + C.cWhite + "Unclaim Land");
				if (clanRole.has(ClanRole.LEADER))
				{
					territoryLore.add(ChatColor.RESET + C.cYellow + "Shift-Right Click " + C.cWhite + "Unclaim All Land");
				}
			}

			ShopItem territoryItem = new ShopItem(territoryMaterial, territoryData, territoryName, territoryLore.toArray(new String[0]), 1, false, false);
			ClanTerritoryButton territoryButton = new ClanTerritoryButton(getShop(), getPlugin(), getPlayer(), clanInfo, clanRole);
			addButton(2, territoryItem, territoryButton);
		}

		// Energy
		{
			String energyName = "Energy";
			Material energyMaterial = USE_RESOURCE_ICONS ? ClanIcon.ENERGY.getMaterial() : Material.GOLD_BLOCK;
			byte energyData = USE_RESOURCE_ICONS ? ClanIcon.ENERGY.getData() : 0;
			ArrayList<String> energyLore = new ArrayList<String>();
			energyLore.add(" ");
			energyLore.add(C.Reset + C.cGray + "Energy is the currency used to upkeep");
			energyLore.add(C.Reset + C.cGray + "your clan. Energy drains over time and");
			energyLore.add(C.Reset + C.cGray + "you will need to refill it at the NPC in");
			energyLore.add(C.Reset + C.cGray + "the shops. More clan members and more land");
			energyLore.add(C.Reset + C.cGray + "increases the rate energy drains at.");
			energyLore.add(" ");
			energyLore.add(C.Reset + C.cYellow + "Energy " + C.cWhite + clanInfo.getEnergy() + "/" + clanInfo.getEnergyMax());
			if (clanInfo.getEnergyCostPerMinute() > 0)
				energyLore.add(C.Reset + C.cYellow + "Energy Depletes " + C.cWhite + clanInfo.getEnergyLeftString());

			ShopItem energyItem = new ShopItem(energyMaterial, energyData, energyName, energyLore.toArray(new String[0]), 1, false, false);
			ClanEnergyButton energyButton = new ClanEnergyButton(getShop(), getPlugin(), getPlayer(), clanInfo, clanRole);
			addButton(4, energyItem, energyButton);
		}


		// Leave
		{
			String leaveName = "Leave";
			Material leaveMaterial = USE_RESOURCE_ICONS ? ClanIcon.LEAVE.getMaterial() : Material.TNT;
			byte leaveData = USE_RESOURCE_ICONS ? ClanIcon.LEAVE.getData() : 0;
			ArrayList<String> leaveLore = new ArrayList<String>();
			leaveLore.add(" ");
			if (clanRole.has(ClanRole.MEMBER))
			{
				leaveLore.add(ChatColor.RESET + C.cYellow + "Shift-Left Click " + C.cWhite + "Leave Clan");
			}
			if (clanRole.has(ClanRole.LEADER))
			{
				leaveLore.add(ChatColor.RESET + C.cYellow + "Shift-Right Click " + C.cWhite + "Disband Clan");
			}

			ShopItem leaveItem = new ShopItem(leaveMaterial, leaveData, leaveName, leaveLore.toArray(new String[0]), 1, false, false);
			ClanLeaveButton leaveButton = new ClanLeaveButton(getShop(), getPlugin(), getPlayer(), clanInfo, clanRole);
			addButton(6, leaveItem, leaveButton);
		}

		// Commands
		{
			String commandsName = "Commands";
			Material commandsMaterial = USE_RESOURCE_ICONS ? ClanIcon.COMMANDS.getMaterial() : Material.PAPER;
			byte commandsData = USE_RESOURCE_ICONS ? ClanIcon.COMMANDS.getData() : 0;
			ArrayList<String> commandsLore = new ArrayList<String>();
			commandsLore.add(" ");
			commandsLore.add(ChatColor.RESET + C.cYellow + "/c help " + C.cWhite + "Lists Clans Commands");
			commandsLore.add(ChatColor.RESET + C.cYellow + "/c ally <clan> " + C.cWhite + "Request Ally");
			commandsLore.add(ChatColor.RESET + C.cYellow + "/c neutral <clan> " + C.cWhite + "Revoke Ally");
			commandsLore.add(ChatColor.RESET + C.cYellow + "/c sethome " + C.cWhite + "Set Home Bed");
			commandsLore.add(ChatColor.RESET + C.cYellow + "/c home " + C.cWhite + "Teleport to Home Bed");
			commandsLore.add(ChatColor.RESET + C.cYellow + "/map " + C.cWhite + "Give yourself a World Map");
			
			ShopItem commandsItem = new ShopItem(commandsMaterial, commandsData, commandsName, commandsLore.toArray(new String[0]), 1, false, false);
			setItem(8, commandsItem);
		}

		// Players
		{
			int slot = 18;
			for (ClansPlayer clansPlayer : UtilAlg.sortSet(clanInfo.getMembers().values(), new ClansPlayerComparator()))
			{
				addPlayerButton(slot, clansPlayer, clanInfo, clanRole);
				slot++;
			}
		}

		// Allies
		{
			// We need to sort the ally set in the order of players online!
			TreeSet<ClanInfo> allySet = new TreeSet<ClanInfo>(new Comparator<ClanInfo>()
			{
				@Override
				public int compare(ClanInfo o1, ClanInfo o2)
				{
					return o1.getOnlinePlayers().size() - o2.getOnlinePlayers().size();
				}
			});

			for (String allyName : clanInfo.getAllyMap().keySet())
			{
				ClanInfo ally = getPlugin().getClan(allyName);
				if (ally != null)
				{
					allySet.add(ally);
				}
			}

			int slot = 36;
			for (ClanInfo ally : allySet)
			{
				addAllyButton(slot, ally);
				slot++;
			}
		}

		// Wars
		{
			TreeSet<ClanWarData> warSet = new TreeSet<ClanWarData>(new Comparator<ClanWarData>()
			{
				@Override
				public int compare(ClanWarData o1, ClanWarData o2)
				{
					return o1.getPoints(clanInfo.getName()) - o2.getPoints(clanInfo.getName());
				}
			});

			warSet.addAll(clanInfo.getWars());

			Iterator<ClanWarData> descIterator = warSet.descendingIterator();
			Iterator<ClanWarData> ascIterator = warSet.iterator();

			for (int i = 0; i < 4 && descIterator.hasNext(); i++)
			{
				ClanWarData data = descIterator.next();
				if (data.getPoints(clanInfo.getName()) <= 0)
					break;

				int slot = 45 + i;
				addWarButton(slot, clanInfo, data);
			}

			for (int i = 0; i < 4 && ascIterator.hasNext(); i++)
			{
				ClanWarData data = ascIterator.next();
				if (data.getPoints(clanInfo.getName()) >= 0)
					break;

				int slot = 53 - i;
				addWarButton(slot, clanInfo, data);
			}

		}
	}

	private void addWarButton(int slot, ClanInfo clan, final ClanWarData clanWar)
	{
		String enemyName = clanWar.getClanA().equals(clan.getName()) ? clanWar.getClanB() : clanWar.getClanA();
		final ClanInfo enemy = getPlugin().getClan(enemyName);
		if (enemy != null)
		{
			String itemName = enemyName;
			Material material = USE_RESOURCE_ICONS ? ClanIcon.WAR.getMaterial() : Material.DIAMOND_SWORD;
			byte data = USE_RESOURCE_ICONS ? ClanIcon.WAR.getData() : 0;
			int warPoints = clanWar.getPoints(clan.getName());

			ArrayList<String> lore = new ArrayList<String>(5);
			lore.add(" ");
			lore.add(C.Reset + C.cYellow + "War Points " + clan.getFormattedWarPoints(enemy));
			lore.add(" ");
			lore.add(ChatColor.RESET + C.cGray + "Left Click " + C.cWhite + "Clan Info");

			ShopItem shopItem = new ShopItem(material, data, itemName, lore.toArray(new String[0]), 0, false, false);
			addButtonFakeCount(slot, shopItem, new IButton()
			{
				@Override
				public void onClick(Player player, ClickType clickType)
				{
					getShop().openPageForPlayer(player, new ClanWhoPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), player, enemy, true));
				}
			}, warPoints);
		}
		else
		{
			System.err.println("Could not find enemy clan: " + enemyName);
		}
	}

	private void addAllyButton(int slot, final ClanInfo ally)
	{
		String itemName = ally.getName();
		Material material = USE_RESOURCE_ICONS ? ClanIcon.ALLIANCE.getMaterial() : Material.EMERALD_BLOCK;
		byte data = USE_RESOURCE_ICONS ? ClanIcon.ALLIANCE.getData() : 0;
		ArrayList<String> lore = new ArrayList<String>(5);
		lore.add(" ");
		lore.add(ChatColor.RESET + C.cGray + "Left Click " + C.cWhite + "Clan Info");

		ShopItem shopItem = new ShopItem(material, data, itemName, lore.toArray(new String[0]), 1, false, false);
		addButtonFakeCount(slot, shopItem, new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				getShop().openPageForPlayer(player, new ClanWhoPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), player, ally, true));
			}
		}, ally.getOnlinePlayers().size());
	}

	private void addPlayerButton(int slot, ClansPlayer clansPlayer, ClanInfo guiInfo, ClanRole guiRole)
	{
		ClanRole playerRole = clansPlayer.getRole();
		String itemName = (clansPlayer.isOnline() ? C.cGreenB : C.cRedB) + clansPlayer.getPlayerName();
		ArrayList<String> lore = new ArrayList<String>(5);
		lore.add(" ");
		lore.add(C.Reset + C.cYellow + "Role " + C.cWhite + clansPlayer.getRole().getFriendlyName());
		if (clansPlayer.isOnline())
		{
			Player player = UtilPlayer.searchExact(clansPlayer.getUuid());
			if (player != null)
			{
				String loc = UtilWorld.locToStrClean(player.getLocation());
				lore.add(C.Reset + C.cYellow + "Location " + C.cWhite + loc);
				// TODO Save join date?
			}
		}
		// TODO Save last join date

		if (guiRole.has(ClanRole.ADMIN))
		{
			lore.add(" ");
			
			if (!playerRole.has(ClanRole.LEADER) && guiRole.has(playerRole))
			{
				lore.add(ChatColor.RESET + C.cYellow + "Left Click " + C.cWhite + "Promote");
			}
			
			if (!playerRole.has(ClanRole.LEADER) && guiRole.has(playerRole))
			{
				lore.add(ChatColor.RESET + C.cYellow + "Right Click " + C.cWhite + "Demote");
			}
			
			if (!playerRole.has(ClanRole.LEADER))
			{
				lore.add(ChatColor.RESET + C.cYellow + "Shift-Right Click " + C.cWhite + "Kick");
			}
		}

		ItemStack item = UtilSkull.getPlayerHead(clansPlayer.isOnline() ? clansPlayer.getPlayerName() : "", itemName, lore);

		addButton(slot, item, new ClanMemeberButton(getShop(), getPlugin(), getPlayer(), guiInfo, guiRole, this, clansPlayer.getPlayerName()));
	}
}