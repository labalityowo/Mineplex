package nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.disguise.disguises.DisguiseChicken;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import nautilus.game.arcade.game.games.smash.perks.SmashKit;
import nautilus.game.arcade.game.games.smash.perks.chicken.PerkChickenRocket;
import nautilus.game.arcade.game.games.smash.perks.chicken.PerkEggGun;
import nautilus.game.arcade.game.games.smash.perks.chicken.PerkFlap;
import nautilus.game.arcade.game.games.smash.perks.chicken.SmashChicken;
import nautilus.game.arcade.kit.Perk;

public class KitChicken extends SmashKit
{
	
	private static final Perk[] PERKS = {
	  new PerkSmashStats(),
	  new PerkFlap(),
	  new PerkEggGun(),
	  new PerkChickenRocket(),
	  new SmashChicken()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD, (byte) 0, 1,
		C.cYellow + C.Bold + "Hold Block" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Egg Blaster",
		new String[]{
		  ChatColor.RESET + "Unleash a barrage of your precious eggs.",
		  ChatColor.RESET + "They won't deal any knockback, but they",
		  ChatColor.RESET + "can deal some serious damage.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Chicken Missile",
		new String[]{
		  ChatColor.RESET + "Launch one of your newborn babies.",
		  ChatColor.RESET + "It will fly forwards and explode if it",
		  ChatColor.RESET + "collides with anything, giving large",
		  ChatColor.RESET + "damage and knockback to players.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.FEATHER, (byte) 0, 1,
		C.cYellow + C.Bold + "Passive" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Flap",
		new String[]{
		  ChatColor.RESET + "You are able to use your double jump",
		  ChatColor.RESET + "up to 6 times in a row.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + C.cAqua + "Flap uses Energy (Experience Bar)",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Airial Gunner",
		new String[]{
		  ChatColor.RESET + "Unleash an unlimited barrage of eggs",
		  ChatColor.RESET + "while also gaining permanant flight.",
		})

	};

	private static final ItemStack[] PLAYER_ARMOR = {
	  null,
	  null,
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
	  null
	};


	public KitChicken(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_CHICKEN, PERKS, DisguiseChicken.class);
	}

	@Override
	public void GiveItems(Player player)
	{
		disguise(player);
		
		UtilInv.Clear(player);

		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1]);

		if (Manager.GetGame().GetState() == GameState.Recruit)
		{
			player.getInventory().addItem(PLAYER_ITEMS[2], PLAYER_ITEMS[3]);
		}
		else
		{
			player.setExp(0.999f);
		}

		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
}
