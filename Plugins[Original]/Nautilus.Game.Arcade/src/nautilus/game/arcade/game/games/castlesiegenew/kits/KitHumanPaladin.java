package nautilus.game.arcade.game.games.castlesiegenew.kits;

import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.castlesiegenew.perks.PerkPaladinBoost;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkIronSkin;

public class KitHumanPaladin extends KitCastleSiege
{

	private static final int EIGHT_TICKS = 8 * 20;
	private static final Perk[] PERKS =
			{
					new PerkIronSkin(0.1, true),
					new PerkPaladinBoost(TimeUnit.SECONDS.toMillis(28), new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, EIGHT_TICKS, 0, false, false))
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.STONE_SWORD),
					ItemStackFactory.Instance.CreateStack(Material.BOW),
					ItemStackFactory.Instance.CreateStack(Material.ARROW, 48),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP)
			};

	private static final ItemStack[] ARMOR =
			{
					ItemStackFactory.Instance.CreateStack(Material.GOLD_BOOTS),
					ItemStackFactory.Instance.CreateStack(Material.GOLD_LEGGINGS),
					ItemStackFactory.Instance.CreateStack(Material.GOLD_CHESTPLATE),
					ItemStackFactory.Instance.CreateStack(Material.GOLD_HELMET)
			};

	public static final ItemStack IN_HAND = new ItemStack(Material.STONE_SWORD);

	public KitHumanPaladin(ArcadeManager manager)
	{
		super(manager, GameKit.CASTLE_SIEGE_HUMAN_PALADIN, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
		player.getInventory().setArmorContents(ARMOR);
	}
}
