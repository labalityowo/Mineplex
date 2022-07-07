package mineplex.game.clans.clans.gui.page;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilSkull;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.item.ShopItem;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClanRole;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.ClansPlayer;
import mineplex.game.clans.clans.ClansPlayerComparator;
import mineplex.game.clans.clans.ClansUtility;
import mineplex.game.clans.clans.gui.ClanShop;
import mineplex.game.clans.clans.gui.ClanIcon;

public class ClanWhoPage extends ClanPageBase
{
	private ClanInfo _lookupClan;
	private boolean _showBackButton;

	public ClanWhoPage(ClansManager plugin, ClanShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player, ClanInfo lookupClan, boolean showBackButton)
	{
		super(plugin, shop, clientManager, donationManager, lookupClan.getName(), player, 45);
		_lookupClan = lookupClan;
		_showBackButton = showBackButton;

		buildPage();
	}

	@Override
	public void buildNoClan()
	{
		build(null);
	}

	@Override
	public void buildClan(ClanInfo clanInfo, ClanRole clanRole)
	{
		build(clanInfo);
	}

	private void build(ClanInfo clanInfo)
	{
		ClansUtility.ClanRelation relation = getPlugin().getClanUtility().rel(clanInfo, _lookupClan);

		// Main Clan Info
		{
			int slot = 4;
			ArrayList<String> lore = new ArrayList<String>();
			lore.add(" ");

			// Basic Clan Info
			lore.add(C.Reset + C.cYellow + "Founder " + C.cWhite + _lookupClan.getDesc());
			lore.add(C.Reset + C.cYellow + "Formed " + C.cWhite + UtilTime.convertString(System.currentTimeMillis() - _lookupClan.getDateCreated().getTime(), 1, UtilTime.TimeUnit.FIT) + " ago on " + UtilTime.when(_lookupClan.getDateCreated().getTime()));
			lore.add(C.Reset + C.cYellow + "Members " + C.cWhite + _lookupClan.getOnlinePlayerCount() + "/" + _lookupClan.getMembers().size());
			lore.add(C.Reset + C.cYellow + "Territory " + C.cWhite + _lookupClan.getClaims() + "/" + _lookupClan.getClaimsMax());
			if (clanInfo != null)
			{
				lore.add(C.Reset + C.cYellow + "Your War Points " + C.cWhite + clanInfo.getFormattedWarPoints(_lookupClan));
			}
			lore.add(" ");

			// Energy
			lore.add(C.Reset + C.cYellow + "Energy " + C.cWhite + _lookupClan.getEnergy() + "/" + _lookupClan.getEnergyMax());
			if (_lookupClan.getEnergyCostPerMinute() > 0)
				lore.add(C.Reset + C.cYellow + "Energy Depletes " + C.cWhite + _lookupClan.getEnergyLeftString());

			lore.add(" ");

			// Allies
			if (_lookupClan.getAllyMap().size() > 0)
			{
				String allySorted = "";
				HashSet<String> allyUnsorted = new HashSet<String>();

				for (String allyName : _lookupClan.getAllyMap().keySet())
					allyUnsorted.add(allyName);

				for (String cur : UtilAlg.sortKey(allyUnsorted))
					allySorted += C.cGreen + cur + C.cWhite + ", ";
				
				lore.add(C.Reset + C.cYellow + "Allies");
				lore.add(" " + allySorted);
			}

			// Self Clan Info
			if (_lookupClan.equals(clanInfo))
			{
				String clanHome = _lookupClan.getHome() == null ? "None" : UtilWorld.locToStrClean(_lookupClan.getHome());
				String generator = _lookupClan.getGenerator() == null ? "None" : "Yes";

				lore.add(C.Reset + C.cYellow + "Clan Home " + C.cWhite + clanHome);
				lore.add(C.Reset + C.cYellow + "TNT Generator " + C.cWhite + generator);
			}

			ShopItem shopItem = new ShopItem(ClanIcon.CASTLE.getMaterial(), ClanIcon.CASTLE.getData(), _lookupClan.getName(), lore.toArray(new String[0]), 1, false, false);
			setItem(slot, shopItem);
		}


		// Players
		{
			int slot = 18;
			for (ClansPlayer player : UtilAlg.sortSet(_lookupClan.getMembers().values(), new ClansPlayerComparator()))
			{
				addPlayer(slot, player, relation == ClansUtility.ClanRelation.ALLY || relation == ClansUtility.ClanRelation.ALLY_TRUST || relation == ClansUtility.ClanRelation.SELF);
				slot++;
			}
		}

		if (_showBackButton)
		{
			addBackButton(0);
		}
	}

	private void addPlayer(int slot, ClansPlayer clansPlayer, boolean showLocation)
	{
		String itemName = (clansPlayer.isOnline() ? C.cGreenB : C.cRedB) + clansPlayer.getPlayerName();
		ArrayList<String> lore = new ArrayList<String>(5);
		lore.add(" ");
		lore.add(C.Reset + C.cYellow + "Role " + C.cWhite + clansPlayer.getRole().getFriendlyName());
		if (showLocation && clansPlayer.isOnline())
		{
			Player player = UtilPlayer.searchExact(clansPlayer.getUuid());
			if (player != null)
			{
				String loc = UtilWorld.locToStrClean(player.getLocation());
				lore.add(C.Reset + C.cYellow + "Location " + C.cWhite + loc);
			}
		}

		ItemStack item = UtilSkull.getPlayerHead(clansPlayer.isOnline() ? clansPlayer.getPlayerName() : "", itemName, lore);
		setItem(slot, item);
	}
}
