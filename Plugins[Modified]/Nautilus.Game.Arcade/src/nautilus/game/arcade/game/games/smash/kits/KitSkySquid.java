package nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.disguise.disguises.DisguiseSquid;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import nautilus.game.arcade.game.games.smash.perks.SmashKit;
import nautilus.game.arcade.game.games.smash.perks.squid.PerkFishFlurry;
import nautilus.game.arcade.game.games.smash.perks.squid.PerkInkBlast;
import nautilus.game.arcade.game.games.smash.perks.squid.PerkSuperSquid;
import nautilus.game.arcade.game.games.smash.perks.squid.SmashSquid;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitSkySquid extends SmashKit
{
	
	private static final Perk[] PERKS = {
	  new PerkSmashStats(),
	  new PerkDoubleJump("Double Jump"),
	  new PerkSuperSquid(),
	  new PerkInkBlast(),
	  new PerkFishFlurry(),
	  new SmashSquid()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Ink Shotgun",
		new String[]
		  {
			ChatColor.RESET + "Blasts 7 ink pellets out at high velocity.",
			ChatColor.RESET + "They explode upon hitting something, dealing",
			ChatColor.RESET + "damage and knockback.",
		  }),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD, (byte) 0, 1,
		C.cYellow + C.Bold + "Hold/Release Block" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Super Squid",
		new String[]{
		  ChatColor.RESET + "You become invulnerable and fly through",
		  ChatColor.RESET + "the sky in the direction you are looking.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SPADE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Fish Flurry",
		new String[]{
		  ChatColor.RESET + "Target a location to create a geyser.",
		  ChatColor.RESET + "After a few seconds, the geyser will explode",
		  ChatColor.RESET + "with all sorts of marine life which will",
		  ChatColor.RESET + "damage nearby opponents.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Storm Squid",
		new String[]{
		  ChatColor.RESET + "Gain permanent flight, as well as the ability",
		  ChatColor.RESET + "to strike lightning at your target location",
		  ChatColor.RESET + "after a short delay.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.DIAMOND_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Lightning Strike",
		new String[]{
		  ChatColor.RESET + "Strikes lightning at target location after",
		  ChatColor.RESET + "a short delay. Deals large damage and knockback.",
		})
	};

	private static final ItemStack[] PLAYER_ARMOR = {
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
	  null
	};


	public KitSkySquid(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_SQUID, PERKS, DisguiseSquid.class);
	}

	public void giveSmashItems(Player player)
	{
		player.getInventory().remove(Material.IRON_SWORD);
		player.getInventory().remove(Material.IRON_AXE);
		player.getInventory().remove(Material.IRON_SPADE);

		player.getInventory().addItem(PLAYER_ITEMS[4]);

		UtilInv.Update(player);
	}

	@Override
	public void GiveItems(Player player)
	{
		disguise(player);
		
		UtilInv.Clear(player);

		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1], PLAYER_ITEMS[2]);

		if (Manager.GetGame().GetState() == GameState.Recruit)
			player.getInventory().addItem(PLAYER_ITEMS[3]);

		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
}
