package nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilInv;
import mineplex.core.disguise.disguises.DisguiseHorse;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import nautilus.game.arcade.game.games.smash.perks.SmashKit;
import nautilus.game.arcade.game.games.smash.perks.skeletalhorse.PerkBoneRush;
import nautilus.game.arcade.game.games.smash.perks.skeletalhorse.PerkDeadlyBones;
import nautilus.game.arcade.game.games.smash.perks.skeletalhorse.PerkHorseKick;
import nautilus.game.arcade.game.games.smash.perks.skeletalhorse.SmashSkeletalHorse;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitSkeletalHorse extends SmashKit
{

	private static final Perk[] PERKS = {
	  new PerkSmashStats(),
	  new PerkDoubleJump("Double Jump"),
	  new PerkHorseKick(),
	  new PerkBoneRush(),
	  new PerkDeadlyBones(),
	  new SmashSkeletalHorse()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Bone Kick",
		new String[]{
		  ChatColor.RESET + "Stand on your hind legs and maul enemies",
		  ChatColor.RESET + "in front of you with your front legs, ",
		  ChatColor.RESET + "dealing damage and large knockback.",

		}),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SPADE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Bone Rush",
		new String[]{
		  ChatColor.RESET + "Charge forth in a deadly wave of bones.",
		  ChatColor.RESET + "Bones deal small damage and knockback.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "Holding Crouch will prevent you from",
		  ChatColor.RESET + "moving forward with the bones.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.BONE, (byte) 0, 1,
		C.cYellow + C.Bold + "Passive" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Deadly Bones",
		new String[]{
		  ChatColor.RESET + "Whenever you take damage, you drop a bone",
		  ChatColor.RESET + "which will explode after a few seconds",
		  ChatColor.RESET + "dealing damage and knockback to enemies."
		}),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Bone Storm",
		new String[]{
		  ChatColor.RESET + "Charge forth in a mighty bone storm.",
		  ChatColor.RESET + "Bones deal damage and knockback.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "Holding Crouch will prevent you from",
		  ChatColor.RESET + "moving forward with the bones.",
		})
	};

	private static final ItemStack[] PLAYER_ARMOR = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_BOOTS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_HELMET),
	};

	public KitSkeletalHorse(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_SKELETON_HORSE, PERKS, DisguiseHorse.class);
	}
	
	@Override
	public void GiveItems(Player player)
	{
		disguise(player);
		
		DisguiseHorse disguise = (DisguiseHorse) Manager.GetDisguise().getActiveDisguise(player);
		
		disguise.setType(Variant.SKELETON_HORSE);
		
		UtilInv.Clear(player);

		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1]);

		if (Manager.GetGame().GetState() == GameState.Recruit)
			player.getInventory().addItem(PLAYER_ITEMS[2], PLAYER_ITEMS[3]);

		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
}
