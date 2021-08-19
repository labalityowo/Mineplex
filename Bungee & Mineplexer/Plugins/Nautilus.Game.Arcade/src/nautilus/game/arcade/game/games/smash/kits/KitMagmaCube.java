package nautilus.game.arcade.game.games.smash.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilInv;
import mineplex.core.disguise.disguises.DisguiseMagmaCube;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.games.smash.perks.PerkSmashStats;
import nautilus.game.arcade.game.games.smash.perks.SmashKit;
import nautilus.game.arcade.game.games.smash.perks.magmacube.PerkFlameDash;
import nautilus.game.arcade.game.games.smash.perks.magmacube.PerkMagmaBlast;
import nautilus.game.arcade.game.games.smash.perks.magmacube.PerkMagmaBoost;
import nautilus.game.arcade.game.games.smash.perks.magmacube.SmashMagmacube;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitMagmaCube extends SmashKit
{

	private static final Perk[] PERKS = {
	  new PerkSmashStats(),
	  new PerkDoubleJump("Double Jump"),
	  new PerkMagmaBoost(),
	  new PerkMagmaBlast(),
	  new PerkFlameDash(),
	  new SmashMagmacube()
	};

	private static final ItemStack[] PLAYER_ITEMS = {
	  ItemStackFactory.Instance.CreateStack(Material.IRON_AXE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Magma Blast",
		new String[]
		  {
			ChatColor.RESET + "Release a powerful ball of magma which explodes",
			ChatColor.RESET + "on impact, dealing damage and knockback.",
			ChatColor.RESET + "",
			ChatColor.RESET + "You receive strong knockback when you shoot it.",
			ChatColor.RESET + "Use this knockback to get back onto the map!",
		  }),
	  ItemStackFactory.Instance.CreateStack(Material.IRON_SPADE, (byte) 0, 1,
		C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Flame Dash",
		new String[]
		  {
			ChatColor.RESET + "Disappear in flames, and fly horizontally",
			ChatColor.RESET + "in the direction you are looking. You explode",
			ChatColor.RESET + "when you re-appear, dealing damage to enemies.",
			ChatColor.RESET + "",
			ChatColor.RESET + "Damage increases with distance travelled.",
			ChatColor.RESET + "",
			ChatColor.RESET + "Right-Click again to end Flame Dash early.",
		  }),
	  ItemStackFactory.Instance.CreateStack(Material.BLAZE_POWDER, (byte) 0, 1,
		C.cYellow + C.Bold + "Passive" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Fuel the Fire",
		 new String[]
		 {
			ChatColor.RESET + "Each kill increases your size, damage,",
			ChatColor.RESET + "armor and decreases your knockback taken.",
			ChatColor.RESET + "Resets on death.",
		 }),
	  ItemStackFactory.Instance.CreateStack(Material.NETHER_STAR, (byte) 0, 1,
		C.cYellow + C.Bold + "Smash Crystal" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Meteor Shower",
		new String[]
		  {
			ChatColor.RESET + "Summon a deadly meteor shower that will rain",
			ChatColor.RESET + "down on your target location, causing extreme",
			ChatColor.RESET + "damage and knockback to enemies!",
		  })
	};

	private static final ItemStack[] PLAYER_ARMOR = {
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_BOOTS),
	  null,
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_CHESTPLATE),
	  ItemStackFactory.Instance.CreateStack(Material.CHAINMAIL_HELMET),
	};

	public KitMagmaCube(ArcadeManager manager)
	{
		super(manager, GameKit.SSM_MAGMA_CUBE, PERKS, DisguiseMagmaCube.class);
	}

	@Override
	public void GiveItems(Player player)
	{
		disguise(player);

		DisguiseMagmaCube disguise = (DisguiseMagmaCube) Manager.GetDisguise().getActiveDisguise(player);

		disguise.SetSize(1);

		UtilInv.Clear(player);

		player.getInventory().addItem(PLAYER_ITEMS[0], PLAYER_ITEMS[1]);

		if (Manager.GetGame().GetState() == GameState.Recruit)
			player.getInventory().addItem(PLAYER_ITEMS[2], PLAYER_ITEMS[3]);

		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}

	@EventHandler
	public void fireResistance(UpdateEvent event)
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

			player.setFireTicks(0);
		}
	}
}
