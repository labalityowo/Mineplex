package mineplex.core.gadget.gadgets.morph;

import java.time.Month;
import java.time.YearMonth;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseSquid;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.PlayerToggleSwimEvent;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.gadget.types.OutfitGadget;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.projectile.IThrown;
import mineplex.core.projectile.ProjectileUser;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MorphSquid extends MorphGadget implements IThrown
{

	public MorphSquid(GadgetManager manager)
	{
		super(manager, "Squid Morph", UtilText.splitLinesToArray(new String[]{
			C.cGray + "It's more fun to be a squid than to eat one. They're really rubbery.",
				C.blankLine,
				C.cWhite + "Swim to enable Fast Swimming",
				C.cWhite + "Sneak to shoot a fish above you"
		}, LineFormat.LORE),
				-14, Material.INK_SACK, (byte) 0);

		setPPCYearMonth(YearMonth.of(2016, Month.SEPTEMBER));
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);
		DisguiseSquid disguiseSquid = new DisguiseSquid(player);
		UtilMorph.disguise(player, disguiseSquid, Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());

		if (player.getInventory().getBoots() != null && player.getInventory().getBoots().getType() == Material.DIAMOND_BOOTS)
		{
			player.getInventory().setBoots(new ItemStack(Material.AIR));
			player.removePotionEffect(PotionEffectType.SPEED);
		}
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (!isActive(player))
				continue;
			UtilParticle.PlayParticle(UtilParticle.ParticleType.WATER_WAKE, player.getLocation().clone().add(0, .5, 0), 0.01f, 0.01f, 0.01f,
					0.001f, 1, UtilParticle.ViewDist.NORMAL);
		}
	}

	@EventHandler
	public void onToggleSwim(PlayerToggleSwimEvent event)
	{
		if (!isActive(event.getPlayer()))
		{
			return;
		}

		if (event.isSwimming())
		{
			// Removes any costume player could be wearing
			Manager.removeOutfit(event.getPlayer(), OutfitGadget.ArmorSlot.BOOTS);

			// Adds enchanted boot
			ItemStack enchantedBoot = new ItemStack(Material.DIAMOND_BOOTS, 1);
			enchantedBoot.addEnchantment(Enchantment.DEPTH_STRIDER, 3);
			event.getPlayer().getInventory().setBoots(enchantedBoot);

			// Adds swiftness potion
			event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 3, true, true));
		}
		else
		{
			event.getPlayer().getInventory().setBoots(new ItemStack(Material.AIR));
			event.getPlayer().removePotionEffect(PotionEffectType.SPEED);
		}
	}

	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event)
	{

		Player player = event.getPlayer();

		if (!player.isSneaking())
			return;

		if (!isActive(player))
			return;

		if (!Recharge.Instance.use(player, getName(), 1000, false, false, "Cosmetics"))
			return;

		Item item = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()),
				ItemStackFactory.Instance.CreateStack(Material.RAW_FISH));
		UtilAction.velocity(item, player.getLocation().getDirection(),
				0.01, true, -0.3, 1.5, 10, false);

		Manager.getProjectileManager().AddThrow(item, player, this, -1, true, true, true, true,
				null, 1f, 1f, null, null, 0, UpdateType.TICK, 0.5f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		data.getThrown().remove();
	}

	@Override
	public void Idle(ProjectileUser data)
	{
		data.getThrown().remove();
	}

	@Override
	public void Expire(ProjectileUser data)
	{
		data.getThrown().remove();
	}
	
	@Override
	public void ChunkUnload(ProjectileUser data)
	{
		data.getThrown().remove();
	}
}