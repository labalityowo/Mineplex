package nautilus.game.arcade.game.games.moba.kit;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.gadgets.gamemodifiers.moba.skins.HeroSkinGadget;
import mineplex.core.game.GameDisplay;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.preferences.Preference;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilGameProfile;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.ArcadeManager.Perm;
import nautilus.game.arcade.game.games.moba.Moba;
import nautilus.game.arcade.game.games.moba.MobaRole;
import nautilus.game.arcade.game.games.moba.progression.MobaProgression;
import nautilus.game.arcade.game.games.moba.shop.MobaItem;
import nautilus.game.arcade.game.games.moba.util.MobaConstants;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

public class HeroKit extends Kit
{

	private static final int AMMO_SLOT = 7;

	private final String _name;
	private final MobaRole _role;
	private final SkinData _skin;
	private final int _unlockLevel;

	private ItemStack _ammo;
	private long _giveTime;
	private int _maxAmmo;

	private static final int SHOP_SLOT = 8;
	private static final ItemStack SHOP_ITEM = new ItemBuilder(Material.GOLD_INGOT)
			.setTitle(C.cGold + "Open Gold Upgrades")
			.addLore("Click to open the Gold Upgrades Shop")
			.build();

	private boolean _visible = true;

	public HeroKit(ArcadeManager manager, String name, Perk[] kitPerks, MobaRole role, SkinData skin)
	{
		this(manager, name, kitPerks, role, skin, 0);
	}

	public HeroKit(ArcadeManager manager, String name, Perk[] kitPerks, MobaRole role, SkinData skin, int unlockLevel)
	{
		super(manager, GameKit.NULL_PLAYER, kitPerks);

		_name = name;
		_role = role;
		_maxAmmo = 64;
		_skin = skin;
		_unlockLevel = unlockLevel;
	}

	@Override
	public String GetName()
	{
		return _name;
	}

	public MobaRole getRole()
	{
		return _role;
	}

	public int getUnlockLevel()
	{
		return _unlockLevel;
	}

	public ItemStack getAmmo()
	{
		return _ammo;
	}

	public boolean ownsKit(Player player)
	{
		MobaProgression progression = ((Moba) Manager.GetGame()).getProgression();
		return _unlockLevel == 0 ||
				Manager.GetDonation().Get(player).ownsUnknownSalesPackage(progression.getPackageName(this)) ||
				Manager.getPreferences().get(player).isActive(Preference.UNLOCK_KITS)
		;
	}

	public void setAmmo(ItemStack ammo, long giveTime)
	{
		_ammo = ammo;
		_giveTime = giveTime;
	}

	public void setMaxAmmo(int max)
	{
		_maxAmmo = max;
	}

	public boolean useAmmo(Player player, int amount)
	{
		ItemStack itemStack = player.getInventory().getItem(AMMO_SLOT);

		if (itemStack == null)
		{
			return false;
		}

		int newAmount = itemStack.getAmount() - amount;

		if (itemStack.getAmount() < amount)
		{
			return false;
		}

		if (newAmount == 0)
		{
			player.getInventory().remove(itemStack);
		}
		else
		{
			itemStack.setAmount(newAmount);
		}
		return true;
	}

	@EventHandler
	public void giveAmmo(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST || Manager.GetGame() == null || !Manager.GetGame().IsLive() || _ammo == null)
		{
			return;
		}

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!HasKit(player) || UtilPlayer.isSpectator(player))
			{
				continue;
			}

			long giveTime = _giveTime;

			if (!Recharge.Instance.usable(player, MobaConstants.AMMO))
			{
				continue;
			}

			CooldownCalculateEvent cooldownEvent = new CooldownCalculateEvent(player, MobaConstants.AMMO, giveTime);
			UtilServer.CallEvent(cooldownEvent);

			if (!Recharge.Instance.use(player, MobaConstants.AMMO, cooldownEvent.getCooldown(), false, false))
			{
				continue;
			}

			giveAmmo(player, 1);
		}
	}

	public void giveAmmo(Player player, int amount)
	{
		ItemStack itemStack = player.getInventory().getItem(AMMO_SLOT);

		AmmoGiveEvent event = new AmmoGiveEvent(player, amount, _maxAmmo);
		UtilServer.CallEvent(event);

		if (itemStack == null)
		{
			itemStack = _ammo;
			player.getInventory().setItem(AMMO_SLOT, itemStack);
			return;
		}

		if (itemStack.getAmount() >= event.getMaxAmmo())
		{
			return;
		}

		itemStack.setAmount(itemStack.getAmount() + event.getAmmoToGive());
	}

	@Override
	public void GiveItems(Player player)
	{
		PlayerInventory inventory = player.getInventory();

		// This is important
		inventory.clear();

		// Give standard items
		inventory.setItem(AMMO_SLOT, _ammo);
		inventory.setItem(SHOP_SLOT, SHOP_ITEM);

		Moba game = (Moba) Manager.GetGame();
		List<MobaItem> items = game.getShop().getOwnedItems(player);

		for (MobaItem item : items)
		{
			ItemStack itemstack = item.getItem();

			// Give armour
			if (UtilItem.isHelmet(itemstack))
			{
				inventory.setHelmet(itemstack);
			}
			else if (UtilItem.isChestplate(itemstack))
			{
				inventory.setChestplate(itemstack);
			}
			else if (UtilItem.isLeggings(itemstack))
			{
				inventory.setLeggings(itemstack);
			}
			else if (UtilItem.isBoots(itemstack))
			{
				inventory.setBoots(itemstack);
			}
		}

		// Give all skill related items
		for (Perk perk : GetPerks())
		{
			if (!(perk instanceof HeroSkill))
			{
				continue;
			}

			HeroSkill skill = (HeroSkill) perk;

			if (skill.isOnCooldown(player))
			{
				player.getInventory().setItem(skill.getSlot(), skill.getCooldownItem());
			}
			else
			{
				skill.giveItem(player);
			}
		}
	}

	@Override
	public void GiveItemsCall(Player player)
	{
		super.GiveItemsCall(player);

		disguise(player);
		giveConsumables(player);
	}

	public void giveConsumables(Player player)
	{
		Inventory inventory = player.getInventory();
		Moba game = (Moba) Manager.GetGame();
		List<MobaItem> items = game.getShop().getOwnedItems(player);

		for (MobaItem item : items)
		{
			ItemStack itemStack = item.getItem();

			// Give consumable items
			if (itemStack.getType() == Material.POTION || itemStack.getType() == Material.ENDER_PEARL)
			{
				// Keep moving left from the ammo slot until a free slot is available
				for (int i = AMMO_SLOT - 1; i >= GetPerks().length - 1; i--)
				{
					ItemStack consumable = inventory.getItem(i);

					if (consumable == null)
					{
						inventory.setItem(i, itemStack);
						break;
					}
					else if (consumable.isSimilar(itemStack))
					{
						consumable.setAmount(consumable.getAmount() + 1);
						break;
					}
				}
			}
		}
	}

	public void disguise(Player player)
	{
		HeroSkinGadget skinGadget = (HeroSkinGadget) Manager.getCosmeticManager().getGadgetManager().getGameCosmeticManager()
				.getActiveCosmetic(player,
						GameDisplay.MOBA,
						"Hero Skins",
						gadget -> gadget instanceof HeroSkinGadget &&
								((HeroSkinGadget) gadget).getGadgetData().getHero().equals(GetName())
				);

		disguise(player, skinGadget == null ? _skin : skinGadget.getGadgetData().getSkinData());
	}

	public void disguise(Player player, SkinData skin)
	{
		GameProfile profile = UtilGameProfile.getGameProfile(player);
		profile.getProperties().clear();
		profile.getProperties().put("textures", skin.getProperty());
		DisguisePlayer disguise = new DisguisePlayer(player, profile);
		disguise.showInTabList(true, 0);

		Manager.GetDisguise().disguise(disguise);
	}

	public SkinData getSkinData()
	{
		return _skin;
	}

	public boolean isVisible()
	{
		return _visible;
	}

	public void setVisible(boolean visible)
	{
		_visible = visible;
	}
}
