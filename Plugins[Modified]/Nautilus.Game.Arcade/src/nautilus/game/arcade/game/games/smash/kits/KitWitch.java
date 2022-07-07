package nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.disguise.disguises.DisguiseWitch;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import nautilus.game.arcade.game.games.smash.perks.SmashKit;
import nautilus.game.arcade.game.games.smash.perks.witch.PerkBatWave;
import nautilus.game.arcade.game.games.smash.perks.witch.PerkWitchPotion;
import nautilus.game.arcade.game.games.smash.perks.witch.SmashWitch;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitWitch extends SmashKit
{

	private static final Perk[] PERKS = {
	  new PerkSmashStats(),
	  new PerkDoubleJump("Double Jump"),
	  new PerkWitchPotion(),
	  new PerkBatWave(),
	  new SmashWitch()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Daze Potion",
		new String[]
		  {
			ChatColor.RESET + "Throw a potion that damages and slows",
			ChatColor.RESET + "anything it splashes onto!",
		  }),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SPADE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Bat Wave",
		new String[]
		  {
			ChatColor.RESET + "Release a wave of bats which give",
			ChatColor.RESET + "damage and knockback to anything they",
			ChatColor.RESET + "collide with.",
		  }),
	  ItemStackFactory.Instance.CreateStack(Material.LEASH, (byte) 0, 1,
		C.cYellow + C.Bold + "Double Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Bat Leash",
		new String[]
		  {
			ChatColor.RESET + "Attach a rope to your wave of bats,",
			ChatColor.RESET + "causing you to be pulled behind them!",
		  }),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Bat Form",
		new String[]
		  {
			ChatColor.RESET + "Transform into a bat that can fly and",
			ChatColor.RESET + "launch powerful sonic blasts at opponents,",
			ChatColor.RESET + "dealing damage and knockback.",
		  }),
	  ItemStackFactory.Instance.CreateStack(Material.DIAMOND_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Sonic Boom",
		new String[]
		  {
			ChatColor.RESET + "Screech loudly to create a sonic boom",
			ChatColor.RESET + "that deals damage and knockback to enemies!",
		  })
	};

	private static final ItemStack[] PLAYER_ARMOR = {
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_LEGGINGS),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
	  null
	};

	public KitWitch(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_WITCH, PERKS, DisguiseWitch.class);
	}
	
	public void giveSmashItems(Player player)
	{
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

		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1]);

		if (Manager.GetGame().GetState() == GameState.Recruit)
			player.getInventory().addItem(PLAYER_ITEMS[2], PLAYER_ITEMS[3]);

		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
}
