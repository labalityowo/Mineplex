package nautilus.game.arcade.game.games.build.gui.page;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.ClickType;

import mineplex.core.account.CoreClientManager;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.donation.DonationManager;
import mineplex.core.shop.item.IButton;
import mineplex.core.shop.item.ShopItem;
import mineplex.core.shop.page.ShopPageBase;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.build.BuildData;
import nautilus.game.arcade.game.games.build.gui.MobShop;

public class MobPage extends ShopPageBase<ArcadeManager, MobShop>
{
	private BuildData _buildData;
	private Entity _entity;


	public MobPage(ArcadeManager plugin, MobShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player, BuildData buildData, Entity entity)
	{
		super(plugin, shop, clientManager, donationManager, "Mob Options", player, 9);

		_buildData = buildData;
		_entity = entity;
		buildPage();
	}

	private String format(String s)
	{
		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
	}

	@Override
	protected void buildPage()
	{
		final String entityName = _entity.getType().getName();
		int buttonSlot = 0;

		// Set Baby
		if (_entity instanceof Ageable)
		{
			final Ageable ageable = ((Ageable) _entity);
			ShopItem item = new ShopItem(Material.BLAZE_ROD, "Make " + (ageable.isAdult() ? "Baby" : "Adult"), null, 0, false, false);

			addButton(buttonSlot, item, new IButton()
			{
				@Override
				public void onClick(Player player, ClickType clickType)
				{
					if (ageable.isAdult())
						ageable.setBaby();
					else
						ageable.setAdult();

					UtilPlayer.message(player, F.main("Game", entityName + " is now " + (ageable.isAdult() ? "an Adult" : "a Baby")));
					buildPage();
				}
			});
			buttonSlot+= 2;
		}

		// Enable/Disable Ghosting
		if (_entity instanceof LivingEntity)
		{
			final LivingEntity livingEntity = ((LivingEntity) _entity);
			final boolean ghost = ((CraftLivingEntity) livingEntity).getHandle().isGhost();

			ShopItem item = new ShopItem(Material.FEATHER, (ghost ? "Allow " : "Disable ") + "Pushing " + entityName, null, 0, false, false);
			addButton(buttonSlot, item, new IButton()
			{
				@Override
				public void onClick(Player player, ClickType clickType)
				{
					((CraftLivingEntity) livingEntity).getHandle().setGhost(!ghost);
					UtilPlayer.message(player, F.main("Game", "Pushing " + (ghost ? "Enabled" : "Disabled") + " for " + entityName));
					buildPage();
				}
			});
			buttonSlot += 2;
		}

		// Increase and Decrease Slime Size
		if (_entity instanceof Slime)
		{
			final Slime slime = ((Slime) _entity);

			ShopItem decreaseSize = new ShopItem(Material.CLAY_BALL, "Decrease Size", null, 0, false, false);
			ShopItem increaseSize = new ShopItem(Material.CLAY, "Increase Size", null, 0, false, false);

			addButton(buttonSlot, decreaseSize, new IButton()
			{
				@Override
				public void onClick(Player player, ClickType clickType)
				{
					if (slime.getSize() <= 1)
					{
						UtilPlayer.message(player, F.main("Game", "Slime is already smallest size"));
					}
					else
					{
						slime.setSize(slime.getSize() - 1);
					}
				}
			});
			buttonSlot += 2;

			addButton(buttonSlot, increaseSize, new IButton()
			{
				@Override
				public void onClick(Player player, ClickType clickType)
				{
					if (slime.getSize() >= 10)
					{
						UtilPlayer.message(player, F.main("Game", "Slime is already largest size"));
					}
					else
					{
						slime.setSize(slime.getSize() + 1);
					}
				}
			});
			buttonSlot += 2;
		}

		// Change Villager Profession
		if (_entity instanceof Villager)
		{
			final Villager villager = ((Villager) _entity);
			ShopItem shopItem = new ShopItem(Material.DIAMOND_PICKAXE, "Change Profession", new String[] { " ", ChatColor.GRAY + "Currently " + format(villager.getProfession().name()) }, 0, false);
			addButton(buttonSlot, shopItem, new IButton()
			{
				@Override
				public void onClick(Player player, ClickType clickType)
				{
					Villager.Profession next =
							Villager.Profession.values()[(villager.getProfession().ordinal() + 1) % Villager.Profession.values().length];
					villager.setProfession(next);
					UtilPlayer.message(player, F.main("Game", "Villager is now a " + format(next.name())));
					buildPage();
				}
			});
			buttonSlot += 2;
		}

		// Entity look at player (Currently Bugged)
		if (_entity instanceof LivingEntity)
		{
			ShopItem lookAtMe = new ShopItem(Material.GHAST_TEAR, "Look at Me", null, 0, false);
			addButton(buttonSlot, lookAtMe, new IButton()
			{
				@Override
				public void onClick(Player player, ClickType clickType)
				{
					final Location newLoc = _entity.getLocation();
					newLoc.setDirection(player.getLocation().getDirection().multiply(-1));

					((CraftLivingEntity) _entity).getHandle().setPositionRotation(newLoc.getX(), newLoc.getY(), newLoc.getZ(), newLoc.getYaw(), newLoc.getPitch());
					((CraftLivingEntity) _entity).getHandle().g(0.05, 0.0, 0.05);

					getPlugin().runSyncLater(new Runnable()
					{
						@Override
						public void run()
						{
							_entity.teleport(newLoc);
						}
					}, 1L);

					UtilPlayer.message(player, F.main("Game", entityName + " is now looking at you"));
				}
			});
			buttonSlot += 2;
		}

		// Delete Entity
		ShopItem deleteButton = new ShopItem(Material.TNT, "Delete " + entityName, null, 0, false);
		addButton(8, deleteButton, new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				_buildData.removeEntity(_entity);
				UtilPlayer.message(player, F.main("Game", "Entity Deleted"));
				player.closeInventory();
			}
		});

	}
}
