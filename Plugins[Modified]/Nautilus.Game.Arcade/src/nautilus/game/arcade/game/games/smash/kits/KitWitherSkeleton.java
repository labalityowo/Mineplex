package nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.disguise.disguises.DisguiseSkeleton;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import nautilus.game.arcade.game.games.smash.perks.SmashKit;
import nautilus.game.arcade.game.games.smash.perks.witherskeleton.PerkWitherImage;
import nautilus.game.arcade.game.games.smash.perks.witherskeleton.PerkWitherSkull;
import nautilus.game.arcade.game.games.smash.perks.witherskeleton.SmashWitherSkeleton;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitWitherSkeleton extends SmashKit
{

	private static final Perk[] PERKS = {
	  new PerkSmashStats(),
	  new PerkDoubleJump("Double Jump"),
	  new PerkWitherSkull(),
	  new PerkWitherImage(),
	  new SmashWitherSkeleton()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD, (byte) 0, 1,
		C.cYellow + C.Bold + "Hold Block" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Guided Wither Skull",
		new String[]{
		  ChatColor.RESET + "Launch a Wither Skull forwards, hold",
		  ChatColor.RESET + "block to guide the missile!"
		}),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Wither Image",
		new String[]{
		  ChatColor.RESET + "Create an exact image of yourself.",
		  ChatColor.RESET + "The copy is launched forwards with",
		  ChatColor.RESET + "high speeds. Lasts 8 seconds.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "Use the skill again to swap positions",
		  ChatColor.RESET + "with your image.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Wither Form",
		new String[]{
		  ChatColor.RESET + "Transform into a legendary Wither that is",
		  ChatColor.RESET + "able to launch wither skulls at opponents,",
		  ChatColor.RESET + "dealing damage and knockback.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.DIAMOND_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Wither Skull",
		new String[]{
		  ChatColor.RESET + "Launch a deadly Wither Skull forwards.",
		})
	};


	private static final ItemStack[] PLAYER_ARMOR = {
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_HELMET)
	};

	public KitWitherSkeleton(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_WITHER_SKELETON, PERKS, DisguiseSkeleton.class);
	}

	public void giveSmashItems(Player player)
	{
		player.getInventory().remove(Material.IRON_SWORD);
		player.getInventory().remove(Material.IRON_AXE);

		player.getInventory().addItem(PLAYER_ITEMS[3]);

		UtilInv.Update(player);
	}

	@Override
	public void GiveItems(Player player)
	{
		disguise(player);
		
		UtilInv.Clear(player);

		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1]);

		if (Manager.GetGame().GetState() == GameState.Recruit)
			player.getInventory().addItem(PLAYER_ITEMS[2]);

		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
	
	@Override
	public void disguise(Player player)
	{		
		//Disguise
		DisguiseSkeleton disguise = new DisguiseSkeleton(player);
		GameTeam gameTeam = Manager.GetGame().GetTeam(player);
		
		if (gameTeam != null)
		{
			disguise.setName(gameTeam.GetColor() + player.getName());
		}
		else
		{
			disguise.setName(player.getName());
		}
		
		disguise.setCustomNameVisible(true);
		disguise.SetSkeletonType(SkeletonType.WITHER);
		disguise.hideArmor();
		Manager.GetDisguise().disguise(disguise);
	}
}
