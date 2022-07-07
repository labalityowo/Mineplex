package mineplex.core.gadget.gadgets.morph.moba;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.item.ItemTNT;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilGameProfile;

public class MorphDevon extends MorphGadget
{

	private static final ItemStack BOW = new ItemBuilder(Material.BOW)
			.setTitle(C.cGreenB + "Devon's Bow")
			.addEnchantment(Enchantment.ARROW_INFINITE, 1)
			.addLore("Firing this bow causes " + F.elem("TNT") + " to explode where it lands!")
			.setUnbreakable(true)
			.build();
	private static final int BOW_SLOT = 2;
	private static final ItemStack ARROW = new ItemStack(Material.ARROW);
	private static final int ARROW_SLOT = 9;

	private final Map<Entity, Player> _arrows = new HashMap<>();
	private final ItemTNT _tntGadget;

	public MorphDevon(GadgetManager manager)
	{
		super(manager, "Devon Morph", UtilText.splitLinesToArray(new String[]{
				C.cGray + "You thought this was overpowered in game?",
				C.cGray + "Well now you shoot TNT",
				C.cGray + "at unsuspecting players in the hub!",
				"",
				C.cGreen + "Shoot" + C.cWhite + " your " + C.cYellow + "Bow" + C.cWhite + " to fire",
				C.cYellow + "TNT Infused" + C.cWhite + " arrows which explode on contact."
		}, LineFormat.LORE), -20, Material.GLASS, (byte) 0);
		setDisplayItem(SkinData.DEVON.getSkull());

		_tntGadget = manager.getGadget(ItemTNT.class);
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		GameProfile gameProfile = UtilGameProfile.getGameProfile(player);
		gameProfile.getProperties().clear();
		gameProfile.getProperties().put("textures", SkinData.DEVON.getProperty());

		DisguisePlayer disguisePlayer = new DisguisePlayer(player, gameProfile);
		disguisePlayer.showInTabList(true, 0);
		UtilMorph.disguise(player, disguisePlayer, Manager);

		player.getInventory().setItem(BOW_SLOT, BOW);
		player.getInventory().setItem(ARROW_SLOT, ARROW);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);

		UtilMorph.undisguise(player, Manager.getDisguiseManager());

		player.getInventory().setItem(BOW_SLOT, null);
		player.getInventory().setItem(ARROW_SLOT, null);
	}

	@EventHandler
	public void entityShootBow(EntityShootBowEvent event)
	{
		LivingEntity entity = event.getEntity();

		if (!(entity instanceof Player))
		{
			return;
		}

		Player player = (Player) entity;

		if (!isActive(player))
		{
			return;
		}

		if (!Manager.selectLocation(this, player.getLocation()))
		{
			event.setCancelled(true);
			Manager.informNoUse(player);
			return;
		}

		if (!Recharge.Instance.use(player, "TNT Infusion", 2000, false, true, "Cosmetics"))
		{
			event.setCancelled(true);
			return;
		}

		_arrows.put(event.getProjectile(), player);
	}

	@EventHandler
	public void projectileHit(ProjectileHitEvent event)
	{
		Entity projectile = event.getEntity();
		Player shooter = _arrows.remove(projectile);

		if (shooter == null)
		{
			return;
		}

		_tntGadget.addTNT(shooter, projectile.getLocation(), 0, false);
		projectile.remove();
	}

	@EventHandler
	public void cleanup(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		_arrows.keySet().removeIf(entity ->
		{
			if (!entity.isValid())
			{
				entity.remove();
				return true;
			}

			return false;
		});
	}

}
