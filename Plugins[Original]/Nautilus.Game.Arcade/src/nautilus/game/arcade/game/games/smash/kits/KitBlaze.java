package nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.disguise.disguises.DisguiseBlaze;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import nautilus.game.arcade.game.games.smash.perks.SmashKit;
import nautilus.game.arcade.game.games.smash.perks.blaze.PerkFirefly;
import nautilus.game.arcade.game.games.smash.perks.blaze.PerkInferno;
import nautilus.game.arcade.game.games.smash.perks.blaze.SmashBlaze;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;
import nautilus.game.arcade.kit.perks.PerkKnockbackFire;
import nautilus.game.arcade.kit.perks.PerkSpeed;

public class KitBlaze extends SmashKit
{
	private static final Perk[] PERKS = {
	  new PerkSmashStats(),
	  new PerkDoubleJump("Double Jump"),
	  new PerkKnockbackFire(),
	  new PerkSpeed(0),
	  new PerkInferno(),
	  new PerkFirefly(),
	  new SmashBlaze()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD, (byte) 0, 1,
		C.cYellow + C.Bold + "Hold Block" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Inferno",
		new String[]{
		  ChatColor.RESET + "Releases a deadly torrent of flames,",
		  ChatColor.RESET + "which ignite and damage opponents.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Firefly",
		new String[]{
		  ChatColor.RESET + "After a short startup time, you fly",
		  ChatColor.RESET + "forward with great power, destroying",
		  ChatColor.RESET + "anyone you touch.",
		  ChatColor.RESET + "",
		  ChatColor.RESET + "If you are hit by a projectile during",
		  ChatColor.RESET + "startup time, the skill is cancelled.",
		}),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Phoenix",
		new String[]{
		  ChatColor.RESET + "Unleash all your fiery power and",
		  ChatColor.RESET + "propel yourself forwards, destroying",
		  ChatColor.RESET + "everything that comes into your path."
		})
	};

	private static final ItemStack[] PLAYER_ARMOR = {
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_HELMET)
	};


	public KitBlaze(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_BLAZE, PERKS, DisguiseBlaze.class);
	}
	
	@Override
	public void GiveItems(Player player)
	{		
		disguise(player);
		
		UtilInv.Clear(player);

		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1]);
		if (Manager.GetGame().GetState() == GameState.Recruit)
		{
			player.getInventory().addItem(PLAYER_ITEMS[2]);
		}

		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}


	@EventHandler
	public void FireItemResist(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}
		
		if (Manager.GetGame() == null)
		{
			return;
		}
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!HasKit(player))
			{
				continue;
			}
			
			Manager.GetCondition().Factory().FireItemImmunity(GetName(), player, player, 1.9, false);
		}
	}
}
