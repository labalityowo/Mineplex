package nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.disguise.disguises.DisguiseCow;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import nautilus.game.arcade.game.games.smash.perks.SmashKit;
import nautilus.game.arcade.game.games.smash.perks.cow.PerkCowAngryHerd;
import nautilus.game.arcade.game.games.smash.perks.cow.PerkCowMilkSpiral;
import nautilus.game.arcade.game.games.smash.perks.cow.PerkCowStampede;
import nautilus.game.arcade.game.games.smash.perks.cow.SmashCow;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitCow extends SmashKit
{

	private static final Perk[] PERKS = {
	  new PerkSmashStats(),
	  new PerkDoubleJump("Double Jump"),
	  new PerkCowStampede(), 
	  new PerkCowAngryHerd(),
	  new PerkCowMilkSpiral(),
	  new SmashCow()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Angry Herd",
		new String[]{
		  ChatColor.RESET + "Send forth an angry herd of Cows",
		  ChatColor.RESET + "which deal damage and knockback",
		  ChatColor.RESET + "to opponents. Can hit multiple times."
		}),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SPADE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Milk Spiral",
		new String[]{
		  ChatColor.RESET + "Spray out a spiral of milk, propelling",
		  ChatColor.RESET + "yourself forwards through it. Deals damage",
		  ChatColor.RESET + "to opponents it collides with.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "Crouching cancels propulsion.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.LEATHER, (byte) 0, 1,
		C.cYellow + C.Bold + "Passive" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Stampede",
		new String[]{
		  ChatColor.RESET + "As you sprint, you will slowly",
		  ChatColor.RESET + "build up Speed Levels. You attacks",
		  ChatColor.RESET + "will deal extra damage and knockback",
		  ChatColor.RESET + "while you have Speed.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Mooshroom Madness",
		new String[]{
		  ChatColor.RESET + "Transform into a powerful Mooshroom Cow.",
		  ChatColor.RESET + "This grants you +1 damage on all attacks",
		  ChatColor.RESET + "and abilities, halves ability cooldowns,",
		  ChatColor.RESET + "and increases your health to 15 hearts.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "You remain a Mooshroom for 30 seconds.",
		})
	};

	private static final ItemStack[] PLAYER_ARMOR = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_BOOTS),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_LEGGINGS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_HELMET),
	};


	public KitCow(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_COW, PERKS, DisguiseCow.class);
	}

	@Override
	public void GiveItems(Player player)
	{
		disguise(player);
		
		UtilInv.Clear(player);
		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1]);

		if (Manager.GetGame().GetState() == GameState.Recruit)
			player.getInventory().addItem(PLAYER_ITEMS[2], PLAYER_ITEMS[3]);

		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
}
