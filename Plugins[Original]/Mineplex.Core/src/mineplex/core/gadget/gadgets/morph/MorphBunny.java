package mineplex.core.gadget.gadgets.morph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseRabbit;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MorphBunny extends MorphAbilityGadget
{

	private static final long EGG_COOLDOWN = TimeUnit.SECONDS.toMillis(30);
	private static final int SHARD_COST = 500, SHARD_REWARD = 450;
	private static final float CHARGE_PER_TICK = 0.05F;
	private static final int ITEM_LIFE_TICKS = 24000;
	private static final ItemStack ACTIVE_ITEM = new ItemBuilder(Material.RABBIT_HIDE)
			.setTitle(C.cGreen + "Switch Style")
			.addLore("Clicking this will cycle through", "all possible rabbit styles.")
			.build();

	private final Set<Player> _jumpCharge = new HashSet<>();
	private final Map<Item, UUID> _eggs = new HashMap<>();

	public MorphBunny(GadgetManager manager)
	{
		super(manager, "Easter Bunny Morph", UtilText.splitLinesToArray(new String[]
				{
						C.cGray + "Happy Easter!",
						C.blankLine,
						"#" + C.cWhite + "Charge Crouch to use Super Jump",
						"#" + C.cWhite + "Left Click to use Hide Easter Egg",
						C.blankLine,
						"#" + C.cRed + C.Bold + "WARNING: " + ChatColor.RESET + "Hide Easter Egg uses " + SHARD_COST + " Shards",
						C.blankLine,
						C.cBlue + "Purchasable from mineplex.com/shop during Easter 2018."
				}, LineFormat.LORE), CostConstants.NO_LORE, Material.MONSTER_EGG, UtilEnt.getEntityEggData(EntityType.RABBIT), ACTIVE_ITEM, "Switch Style", TimeUnit.SECONDS.toMillis(1));
	}

	@Override
	public void enableCustom(final Player player, boolean message)
	{
		applyArmor(player, message);

		DisguiseRabbit disguise = new DisguiseRabbit(player);
		UtilMorph.disguise(player, disguise, Manager);

		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 1, false, false));
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		_jumpCharge.remove(player);
		removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());

		player.removePotionEffect(PotionEffectType.SPEED);
		player.removePotionEffect(PotionEffectType.JUMP);
	}

	@Override
	public void onAbilityActivate(Player player)
	{
		DisguiseRabbit disguise = (DisguiseRabbit) Manager.getDisguiseManager().getActiveDisguise(player);

		if (disguise != null)
		{
			Rabbit.Type newType = Rabbit.Type.values()[(disguise.getType().ordinal() + 1) % Rabbit.Type.values().length];

			if (newType == Rabbit.Type.THE_KILLER_BUNNY)
			{
				newType = Rabbit.Type.BROWN;
			}

			disguise.setType(newType);
			Manager.getDisguiseManager().updateDisguise(disguise);

			String name = UtilText.capitalise(newType.toString().replace("_", " "));
			player.sendMessage(F.main(Manager.getName(), "You are now " + (name.startsWith("The") ? "" : "a ") + F.name(name) + " Rabbit."));
		}
	}

	@EventHandler
	public void jumpTrigger(PlayerToggleSneakEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(player))
		{
			return;
		}

		//Start
		if (!player.isSneaking())
		{
			if (UtilEnt.isGrounded(player))
			{
				_jumpCharge.add(player);
			}
		}
		//Jump
		else if (_jumpCharge.remove(player))
		{
			UtilAction.velocity(player, player.getExp() * 4, 1, 10, true);

			player.setExp(0);
			player.getWorld().playSound(player.getLocation(), Sound.CAT_HIT, 0.75f, 2f);
		}
	}

	@EventHandler
	public void jumpBoost(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_jumpCharge.removeIf(player ->
		{
			if (!player.isValid() || !player.isOnline() || !player.isSneaking())
			{
				return true;
			}

			player.setExp(Math.min(0.999F, player.getExp() + CHARGE_PER_TICK));
			player.playSound(player.getLocation(), Sound.FIZZ, 0.25f + player.getExp() * 0.5f, player.getExp());
			return false;
		});
	}

	@EventHandler
	public void eggHide(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!UtilEvent.isAction(event, ActionType.L) || !isActive(player))
		{
			return;
		}

		if (Manager.getDonationManager().Get(player).getBalance(GlobalCurrency.TREASURE_SHARD) < SHARD_COST)
		{
			player.sendMessage(F.main("Gadget", "You do not have enough Shards."));
			return;
		}

		if (!Recharge.Instance.use(player, "Hide Egg", EGG_COOLDOWN, true, false))
		{
			return;
		}

		Location location = player.getEyeLocation();
		ItemStack eggStack = ItemStackFactory.Instance.CreateStack(getDisplayMaterial(), getDisplayData(), 1, "Hidden Egg" + System.currentTimeMillis());

		Item egg = player.getWorld().dropItem(location.add(location.getDirection()), eggStack);
		egg.setPickupDelay(40);
		UtilEnt.addFlag(egg, UtilEnt.FLAG_NO_REMOVE);
		UtilAction.velocity(egg, location.getDirection(), 0.2, false, 0, 0.2, 1, false);

		Manager.getDonationManager().rewardCurrency(GlobalCurrency.TREASURE_SHARD, player, getName() + " Egg Hide", -SHARD_COST);
		_eggs.put(egg, player.getUniqueId());

		Bukkit.broadcastMessage(C.cYellowB + player.getName() + C.cWhiteB + " hid an " + C.cYellowB + "Easter Egg" + C.cWhiteB + " worth " + C.cYellowB + "450 Shards");
		player.getWorld().playSound(player.getLocation(), Sound.CAT_HIT, 1.5f, 1.5f);
	}

	@EventHandler
	public void eggPickup(PlayerPickupItemEvent event)
	{
		Player player = event.getPlayer();
		Item item = event.getItem();
		
		if (!_eggs.containsKey(item))
		{
			return;
		}

		if (!player.getUniqueId().equals(_eggs.get(item)))
		{
			_eggs.remove(item);

			event.setCancelled(true);
			item.remove();

			Manager.getDonationManager().rewardCurrency(GlobalCurrency.TREASURE_SHARD, player, getName() + " Egg Pickup", SHARD_REWARD);

			Location location = player.getLocation();
			location.getWorld().playSound(location, Sound.ORB_PICKUP, 1, 0.75F);
			location.getWorld().playSound(location, Sound.ORB_PICKUP, 1, 1.25F);

			UtilFirework.playFirework(item.getLocation(), Type.BURST, Color.YELLOW, true, true);
			Bukkit.broadcastMessage(C.cGoldB + player.getName() + C.cWhiteB + " found an " + C.cGoldB + "Easter Egg" + C.cWhiteB + "! " + _eggs.size() + " Eggs left!");
		}
	}

	@EventHandler
	public void eggClean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		_eggs.keySet().removeIf(item ->
		{
			if (!item.isValid() || item.getTicksLived() > ITEM_LIFE_TICKS)
			{
				item.remove();
				Bukkit.broadcastMessage(C.cWhiteB + "No one found an " + C.cGoldB + "Easter Egg" + C.cWhiteB + "! " + _eggs.size() + " Eggs left!");
				return true;
			}

			UtilParticle.PlayParticleToAll(ParticleType.SPELL, item.getLocation().add(0, 0.1, 0), 0.1F, 0.1F, 0.1F, 0, 1, ViewDist.NORMAL);
			return false;
		});
	}
}
