package nautilus.game.arcade.game.games.sneakyassassins.kits;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.disguise.disguises.DisguiseVillager;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

public abstract class SneakyAssassinKit extends Kit
{

	public static final ItemStack SMOKE_BOMB = ItemStackFactory.Instance.CreateStack(Material.INK_SACK, (byte) 0, 1,
			C.cYellow + C.Bold + "Right-Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Smoke Bomb",
			new String[]{
					ChatColor.RESET + "Throw a Smoke Bomb.",
					ChatColor.RESET + "Everyone within 6 blocks",
					ChatColor.RESET + "gets Blindness for 6 seconds.",
			});

	private static final ItemStack[] PLAYER_ARMOR =
			{
					new ItemStack(Material.LEATHER_BOOTS),
					new ItemStack(Material.LEATHER_LEGGINGS),
					new ItemStack(Material.LEATHER_CHESTPLATE),
					new ItemStack(Material.LEATHER_HELMET)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					new ItemStack(Material.WOOD_SWORD),
					SMOKE_BOMB.clone(),
			};

	SneakyAssassinKit(ArcadeManager manager, GameKit gameKit, Perk... perks)
	{
		super(manager, gameKit, perks);
	}

	@Override
	public void GiveItems(Player player)
	{
		DisguiseVillager disguise = new DisguiseVillager(player);
		disguise.setLockPitch(true);
		Manager.GetDisguise().disguise(disguise);

		player.getInventory().addItem(PLAYER_ITEMS);
		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
}
