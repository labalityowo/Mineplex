package mineplex.game.clans.clans;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.preferences.Preference;
import mineplex.core.preferences.PreferencesManager;
import mineplex.game.clans.clans.event.ClanTipEvent;
import mineplex.game.clans.clans.event.PlayerClaimTerritoryEvent;
import mineplex.game.clans.clans.event.PlayerEnterTerritoryEvent;
import mineplex.game.clans.clans.event.PlayerUnClaimTerritoryEvent;
import net.minecraft.server.v1_8_R3.EnumDirection;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;

public class ClanTips extends MiniPlugin
{
	private PreferencesManager _preferences;
	private ClansManager _clans;
	
	public ClanTips(final JavaPlugin plugin, final ClansManager clans, final PreferencesManager preferences)
	{
		super("Clans Tips", plugin);
		
		_clans = clans;
		_preferences = preferences;
	}
	
	@EventHandler
	public void onEnterTerritory(final PlayerEnterTerritoryEvent event)
	{
		if (event.getLastTerritory().equals(event.getNewTerritory()))
		{
			return;
		}
		
		final Player player = event.getPlayer();
		final String newTerritory = event.getNewTerritory();
		
		if (_preferences.get(player).isActive(Preference.CLAN_TIPS))
		{
			if (newTerritory.equals("Fields"))
			{
				displayTip(TipType.ENTER_FIELDS, player);
			}
			else if (newTerritory.equals("Shop") && event.isSafe())
			{
				displayTip(TipType.ENTER_SHOP, player);
			}
			else if (newTerritory.equals("Borderlands"))
			{
				displayTip(TipType.ENTER_BORDERLANDS, player);
			}
			else if (newTerritory.equals("Spawn"))
			{
				displayTip(TipType.ENTER_SPAWN, player);
			}
		}
	}
	
	@EventHandler
	public void onClaimTerritory(final PlayerClaimTerritoryEvent event)
	{
		if (event.getClan().getClaimCount() == 0)
		{
			displayTip(TipType.FIRST_CLAIM_SETHOME, event.getClaimer());
			
			// Place New
			boolean bedPlaced = UtilBlock.placeBed(event.getClaimer().getLocation(), BlockFace.valueOf(EnumDirection.fromAngle(event.getClaimer().getLocation().getYaw()).name()), true, true);
			
			if (!bedPlaced)
			{
				UtilPlayer.message(event.getClaimer(), F.main("Clans", "This is not a suitable place for a bed."));
				return;
			}
			
			// Cleanup old
			if (event.getClan().getHome() != null)
			{
				System.out.println("<-old bed cleanup-> <--> " + UtilBlock.deleteBed(event.getClan().getHome()));
			}
			
			// Task
			_clans.getClanDataAccess().setHome(event.getClan(), event.getClaimer().getLocation(), event.getClaimer().getName());
			
			return;
		}
		displayTip(TipType.CLAIM, event.getClaimer());
	}
	
	@EventHandler
	public void onUnClaimTerritory(final PlayerUnClaimTerritoryEvent event)
	{
		displayTip(TipType.UNCLAIM, event.getUnClaimer());
	}
	
	public void displayTip(TipType tip, Player player)
	{
		if (tip == null)
		{
			return;
		}
		
		if (player == null)
		{
			return;
		}
		
		if (!_preferences.get(player).isActive(Preference.CLAN_TIPS) && !tip._ignorePreferences)
		{
			return;
		}
		
		ClanTipEvent event = new ClanTipEvent(tip, player);
		
		UtilServer.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
		{
			return;
		}
		
		UtilPlayer.message(player, "   ");
		UtilPlayer.message(player, tip._messages);
		
		if (!tip._ignorePreferences)
		{
			UtilPlayer.message(player, C.cGray + "(You can disable these Clans Tips in the " + F.elem("/prefs") + " menu.)");
		}
		
		UtilPlayer.message(player, "   ");
	}
	
	public static enum TipType
	{
		UNCLAIM(
				new String[] {
						C.cDAquaB + "You unclaimed some Clan Territory!",
						C.cAqua + "When territory is unclaimed, it cannot be reclaimed by anyone for 30 minutes."
					}),
		
		CLAIM(
				new String[] {
						C.cDAquaB + "You claimed some Clan Territory!",
						C.cAqua + "Clan Territory is an area of the map that only your Clan is allowed to edit! This means that you can build a base and stash your loot safely inside.",
						C.cAqua + "Each territory is a 16x16 area, which extends from bedrock to the sky!", C.cGreen + "The borders are marked with glowstone."
					}),
		
		ENTER_FIELDS(
				new String[] {
						C.cDRedB + "Fields",
						C.cRed + "Fields is a very lucrative area, filled with ores that periodically respawn over time. This is a great place to get a large amount of resources for your clan; however, be aware of other clans who may also be after the riches buried within the fields."
					}),
		
		ENTER_SHOP(
				new String[] {
						C.cDGreenB + "Shops",
						C.cGreen + "Shops is a safe area where combat is disabled between players! Use this safety to purchase food, building blocks, armor, weapons, and other valuable resources. Be careful when leaving though, others may be hiding just outside the gates, eager to steal your recent purchases."
					}),
		
		ENTER_BORDERLANDS(
				new String[] {
						C.cDRedB + "Borderlands",
						C.cRed + "The Borderlands are the very outer reaches of the map, out here you can not edit the terrain. Be careful as very powerful boss monsters will periodically spawn out here! Don't try to fight them alone! If you do manage to slay one of these powerful beasts, you'll be handsomely rewarded with powerful gear or legendary weapons."
					}),
		
		ENTER_SPAWN(
				new String[] {
						C.cDGreenB + "Spawn",
						C.cGreen + "Spawn is a Safe Zone where you spawn after dying. No one can attack you here, and you cannot attack anyone else. If you have set your Clan Home, you are able to teleport to it from Spawns."
					}),
		
		FIRST_CLAIM_SETHOME(
				new String[] {
						C.cDAquaB + "First Claim",
						C.cAqua + "Congratulations on your first Clan Claim.",
						C.cAqua + "We have automatically placed your Clan Home where you are currently standing.",
						C.cAqua + "In the future, to set your Clan Home elsewhere, use the " + F.elem("/c sethome") + " command.",
						C.cAqua + "You can return to your Clan Home by using " + F.elem("/c home") + C.cAqua + ", but remember, you must be in a Safe Spawn location to do this.",
						C.cAqua + "When you die and respawn, you are also given an option to spawn at your Clan Home."
					}),

		DOMINANCE_RIP(
				new String[] {
						C.cDAquaB + "Lost War Point",
	                    C.cAqua + "You were killed by another Clan and they have gained 1 War Point against you.",
	                    C.cAqua + "If your War Points with them reaches -25, they will get to besiege your Territory, giving them access to cannon it for 30 minutes."
					}),

		DOMINANCE_NOOICE(
				new String[] {
						C.cDAquaB + "Gained War Point",
                        C.cAqua + "You killed someone in another Clan and you have gained 1 War Point against them.",
                        C.cAqua + "If your War Points with them reaches +25, you will get to besiege their Territory, giving you access to cannon it for 30 minutes."
					}),

		ENERGY(
				new String[] {
						C.cDAquaB + "Energy",
						C.cAqua + "To top up your energy, go to the Shop and buy some in the Energy Shop.",
						C.cAqua + "To find the Shop, look at your map (use " + C.cYellow + "/map" + C.cAqua + " if you don't have one) and go to either the top-most highlighted area, or the bottom-most highlighted area.",
					}),
		
		NPC_RIPPARONI(
				new String[] {
						C.cDAqua + "Logout NPC",
						C.cAqua + "When you quit Clans, a copy of you will remain in the game for 30 seconds. Other players are able to kill this copy and take your items. Make sure you quit somewhere safe!",
					}),
		
		SETHOME(
				new String[] {
						C.cDAqua + "Clan Home",
						C.cAqua + "Your Clan Home is a bed in your Territory which you can teleport to from the Spawn Islands. However, you are unable to teleport to your Clan Home if the bed is broken, blocks have been placed above the bed, or enemies are in your Territory."
					}),
		MOUNT_CANNON(
				new String[] {
						C.cDAqua + "Meownon",
						C.cAqua + "Congratulations on your new purchase! You are now in possesion, of the one, and only, Meownon 3000.24! " + C.Italics + "we are not responsible for any injuries caused by the meownon, or any related products. stay safe kids."
					}),
		ENTER_NETHER(
				new String[] {
						C.cDRedB + "The Nether",
						C.cRed + "The Nether is a dangerous place full of powerful mobs and hazardous terrain! However, the mobs here drop powerful Ancient Runes, which can be used to strengthen your gear, making your clan even more fearsome than before!"
					}),
		;
		
		// this is only LinkedList because UtilPlayer.message wants it
		private LinkedList<String> _messages;
		private boolean _ignorePreferences;
		
		TipType(String[] messages)
		{
			this(messages, false);
		}
		
		TipType(String[] messages, boolean ignorePreferences)
		{
			_messages = new LinkedList<>();
			
			for (String message : messages)
			{
				_messages.add(message);
			}
			
			_ignorePreferences = ignorePreferences;
		}
	}
	
}
