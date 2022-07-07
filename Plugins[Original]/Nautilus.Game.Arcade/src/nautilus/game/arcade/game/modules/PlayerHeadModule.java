package nautilus.game.arcade.game.modules;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.game.GameTeam;

public class PlayerHeadModule extends Module
{
	private boolean _disableCraftingRegularApples = true;
	private boolean _disableHeadPlace = true;

	@Override
	protected void setup()
	{
		ShapedRecipe headApple2 = new ShapedRecipe(new ItemStack(Material.GOLDEN_APPLE, 1));
		headApple2.shape("GGG", "GHG", "GGG");
		headApple2.setIngredient('G', Material.GOLD_INGOT);
		headApple2.setIngredient('H', new MaterialData(Material.SKULL_ITEM, (byte) 2));
		UtilServer.getServer().addRecipe(headApple2);
	}

	public PlayerHeadModule enableCraftingRegularApples()
	{
		this._disableCraftingRegularApples = false;
		return this;
	}

	public PlayerHeadModule enableHeadPlace()
	{
		this._disableHeadPlace = false;
		return this;
	}

	@EventHandler
	public void disableHeadPlace(BlockPlaceEvent event)
	{
		if ((event.getItemInHand().getType() == Material.SKULL || event.getItemInHand().getType() == Material.SKULL_ITEM) &&
				this._disableHeadPlace)
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void headPickup(PlayerPickupItemEvent event)
	{
		if (!getGame().IsLive())
			return;

		if (event.getItem().getItemStack().getType() == Material.SKULL_ITEM)
		{
			UtilPlayer.message(event.getPlayer(), " ");
			UtilPlayer.message(event.getPlayer(), C.cGreen + C.Bold + "You picked up a Golden Head!");
			UtilPlayer.message(event.getPlayer(), C.cWhite + "Craft a Golden Head Apple with it for ultimate healing.");
			UtilPlayer.message(event.getPlayer(), C.cWhite + "Use the recipe for Golden Apple, but Head replaces Apple.");
			UtilPlayer.message(event.getPlayer(), " ");
		}
	}


	@EventHandler
	public void PlayerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();

		GameTeam team = getGame().GetTeam(player);
		if (team == null)
			return;

		event.getDrops().add(getGoldenHead());
	}

	@EventHandler
	public void eatHeadApple(PlayerItemConsumeEvent event)
	{
		if (event.getItem().getItemMeta().getDisplayName() == null)
			return;

		if (!event.getItem().getItemMeta().getDisplayName().contains("Head"))
			return;

		UtilPlayer.message(event.getPlayer(), "You ate " + event.getItem().getItemMeta().getDisplayName());

		(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0)).apply(event.getPlayer());
		(new PotionEffect(PotionEffectType.REGENERATION, 200, 1)).apply(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void denyGoldApple(PrepareItemCraftEvent event)
	{
		if (event.getRecipe().getResult() == null)
			return;

		Material type = event.getRecipe().getResult().getType();

		if (type != Material.GOLDEN_APPLE)
			return;

		CraftingInventory inv = event.getInventory();

		for (ItemStack item : inv.getMatrix())
			if (item != null && item.getType() != Material.AIR)
				if (item.getType() == Material.GOLD_INGOT)
					return;

		inv.setResult(null);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void craftHeadApple(PrepareItemCraftEvent event)
	{
		if (event.getRecipe().getResult() == null)
			return;

		Material type = event.getRecipe().getResult().getType();

		if (type != Material.GOLDEN_APPLE)
			return;

		CraftingInventory inv = event.getInventory();

		for (ItemStack item : inv.getMatrix())
			if (item != null && item.getType() != Material.AIR)
				if (item.getType() == Material.SKULL_ITEM || item.getType() == Material.SKULL)
				{
					if (item.getItemMeta() == null)
						continue;

					if (item.getItemMeta().getDisplayName() == null)
						continue;

					ItemStack apple = ItemStackFactory.Instance.CreateStack(Material.GOLDEN_APPLE, (byte) 0, 1, item
							.getItemMeta().getDisplayName() + ChatColor.AQUA + " Golden Apple");
					apple.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);

					inv.setResult(apple);
					return;
				}
	}

	public static final ItemStack getGoldenHead()
	{
		return new ItemBuilder(Material.SKULL_ITEM)
				.setData((short) 2)
				.setAmount(1)
				.setTitle(C.cGoldB + "Player Head")
				.build();
	}
}
