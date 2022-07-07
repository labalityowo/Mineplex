package nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.disguise.disguises.DisguiseSpider;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import nautilus.game.arcade.game.games.smash.perks.SmashKit;
import nautilus.game.arcade.game.games.smash.perks.spider.PerkNeedler;
import nautilus.game.arcade.game.games.smash.perks.spider.PerkSpiderLeap;
import nautilus.game.arcade.game.games.smash.perks.spider.PerkWebShot;
import nautilus.game.arcade.game.games.smash.perks.spider.SmashSpider;
import nautilus.game.arcade.kit.Perk;

public class KitSpider extends SmashKit
{

	private static final Perk[] PERKS = {
	  new PerkSmashStats(),
	  new PerkSpiderLeap(),
	  new PerkNeedler(),
	  new PerkWebShot(),
	  new SmashSpider()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD, (byte) 0, 1,
		C.cYellow + C.Bold + "Hold/Release Block" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Needler",
		new String[]{
		  ChatColor.RESET + "Quickly spray up to 5 needles from ",
		  ChatColor.RESET + "your mouth, dealing damage and small",
		  ChatColor.RESET + "knockback to opponents.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Spin Web",
		new String[]{
		  ChatColor.RESET + "Spray out webs behind you, launching",
		  ChatColor.RESET + "yourself forwards. Webs will damage",
		  ChatColor.RESET + "opponents and spawn temporary web blocks.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.SPIDER_EYE, (byte) 0, 1,
		C.cYellow + C.Bold + "Double Jump" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Spider Leap",
		new String[]{
		  ChatColor.RESET + "Your double jump is special. It goes",
		  ChatColor.RESET + "exactly in the direction you are looking.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + C.cAqua + "Spider Leap uses Energy (Experience Bar)",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.FERMENTED_SPIDER_EYE, (byte) 0, 1,
		C.cYellow + C.Bold + "Crouch" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Wall Climb",
		new String[]{
		  ChatColor.RESET + "While crouching, you climb up walls.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "Climbing a wall allows you to use",
		  ChatColor.RESET + "Spider Leap one more time.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + C.cAqua + "Wall Climb uses Energy (Experience Bar)",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Spiders Nest",
		new String[]{
		  ChatColor.RESET + "Spawn a nest of webs around you to trap",
		  ChatColor.RESET + "enemy players. Your attacks heal you and",
		  ChatColor.RESET + "permanently increase your health. ",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "Your abilities have a one second recharge.",
		})
	};

	private static final ItemStack[] PLAYER_ARMOR = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_BOOTS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
	  null,
	};

	public KitSpider(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_SPIDER, PERKS, DisguiseSpider.class);
	}

	@Override
	public void GiveItems(Player player)
	{
		disguise(player);
		
		UtilInv.Clear(player);

		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1]);

		if (Manager.GetGame().GetState() == GameState.Recruit)
		{
			player.getInventory().addItem(PLAYER_ITEMS[2], PLAYER_ITEMS[3], PLAYER_ITEMS[4]);
		}
		
		player.getInventory().setArmorContents(PLAYER_ARMOR);
		player.setExp(0.99F);
	}
}
