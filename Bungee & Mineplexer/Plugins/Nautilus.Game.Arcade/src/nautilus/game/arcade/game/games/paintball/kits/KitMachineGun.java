package nautilus.game.arcade.game.games.paintball.kits;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilInv;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.paintball.kits.perks.PerkPaintballMachineGun;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

public class KitMachineGun extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkPaintballMachineGun()
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.DIAMOND_BARDING, (byte) 0, 1, F.item("Paintball Machine Gun"))
			};

	private static final ItemStack[] PLAYER_ARMOR =
			{
					new ItemBuilder(Material.LEATHER_BOOTS).setUnbreakable(true).setColor(Color.WHITE).build(),
					new ItemBuilder(Material.LEATHER_LEGGINGS).setUnbreakable(true).setColor(Color.WHITE).build(),
					new ItemBuilder(Material.LEATHER_CHESTPLATE).setUnbreakable(true).setColor(Color.WHITE).build(),
					new ItemBuilder(Material.LEATHER_HELMET).setUnbreakable(true).setColor(Color.WHITE).build(),
			};

	public KitMachineGun(ArcadeManager manager)
	{
		super(manager, GameKit.PAINTBALL_MACHINE_GUN, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		UtilInv.insert(player, PLAYER_ITEMS[0]);

		ItemStack potion = new ItemStack(Material.POTION, 3, (short) 16429); // 16422
		PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
		potionMeta.setDisplayName(F.item("Water Bomb"));
		potion.setItemMeta(potionMeta);
		UtilInv.insert(player, potion);

		player.getInventory().setArmorContents(PLAYER_ARMOR);
	}
}
