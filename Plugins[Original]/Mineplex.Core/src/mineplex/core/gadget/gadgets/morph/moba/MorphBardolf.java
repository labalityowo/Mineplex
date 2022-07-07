package mineplex.core.gadget.gadgets.morph.moba;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.SpigotUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilGameProfile;

public class MorphBardolf extends MorphGadget
{

	private static final ItemStack ACTIVE_ITEM = new ItemBuilder(Material.BONE)
			.setTitle(C.cGreenB + "Fetch")
			.addLore("Clicking this throws a stick to play fetch", "with your wolves")
			.setUnbreakable(true)
			.build();
	private static final int ACTIVE_SLOT = 2;
	private static final int WOLVES_TO_SPAWN = 5;
	private static final int MIN_DIST_SQUARED = 2;
	private static final int MAX_DIST_SQUARED = 400;

	private final Map<Player, Set<Wolf>> _wolves = new HashMap<>();
	private final Map<Player, Item> _fetch = new HashMap<>();
	private final Map<Player, Wolf> _wolfWithStick = new HashMap<>();

	public MorphBardolf(GadgetManager manager)
	{
		super(manager, "Bardolf Morph", UtilText.splitLinesToArray(new String[]{
				C.cGray + "Play fetch with 5 puppers of your very own!",
				"",
				C.cGreen + "Click" + C.cWhite + " your " + C.cYellow + "Bone" + C.cWhite + " to",
				C.cWhite + "play " + C.cYellow + "Fetch" + C.cWhite + " with your wolves!"
		}, LineFormat.LORE), -20, Material.GLASS, (byte) 0);
		setDisplayItem(SkinData.BARDOLF.getSkull());
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		GameProfile gameProfile = UtilGameProfile.getGameProfile(player);
		gameProfile.getProperties().clear();
		gameProfile.getProperties().put("textures", SkinData.BARDOLF.getProperty());

		DisguisePlayer disguisePlayer = new DisguisePlayer(player, gameProfile);
		disguisePlayer.showInTabList(true, 0);
		UtilMorph.disguise(player, disguisePlayer, Manager);

		Manager.getPetManager().getCreatureModule().SetForce(true);
		Set<Wolf> wolves = new HashSet<>(WOLVES_TO_SPAWN);
		Location location = player.getLocation();

		for (int i = 0; i < WOLVES_TO_SPAWN; i++)
		{
			Wolf wolf = player.getWorld().spawn(UtilAlg.getRandomLocation(location, 1, 0, 1), Wolf.class);

			wolf.setCollarColor(DyeColor.RED);
			SpigotUtil.setOldOwner_RemoveMeWhenSpigotFixesThis(wolf, player);
			wolf.setOwner(player);
			UtilEnt.vegetate(wolf);
			wolf.setTamed(true);
			wolves.add(wolf);
		}

		_wolves.put(player, wolves);
		Manager.getPetManager().getCreatureModule().SetForce(false);

		player.getInventory().setItem(ACTIVE_SLOT, ACTIVE_ITEM);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);

		UtilMorph.undisguise(player, Manager.getDisguiseManager());

		clearWolves(player);

		player.getInventory().setItem(ACTIVE_SLOT, null);
	}

	@EventHandler
	public void updateWolves(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_wolves.forEach((player, wolves) ->
		{
			Item item = _fetch.get(player);

			wolves.forEach(wolf ->
			{
				double ownerOffset = UtilMath.offsetSquared(player, wolf);

				if (wolf.getLocation().getY() <= 20)
				{
					wolf.setFallDistance(0);
					wolf.teleport(player);
					return;
				}

				if (item != null)
				{
					UtilEnt.CreatureMoveFast(wolf, item.getLocation().add(0, 1, 0), 1.5F);

					if (UtilMath.offsetSquared(wolf, item) < MIN_DIST_SQUARED)
					{
						item.remove();
						_fetch.remove(player);
						_wolfWithStick.put(player, wolf);
					}
				}
				else if (ownerOffset > MAX_DIST_SQUARED)
				{
					wolf.teleport(player);
				}
				else if (ownerOffset > MIN_DIST_SQUARED)
				{
					UtilEnt.CreatureMoveFast(wolf, player.getLocation(), 1.5F);
				}
				else if (_wolfWithStick.containsValue(wolf))
				{
					_wolfWithStick.remove(player);
					UtilParticle.PlayParticleToAll(ParticleType.HEART, wolf.getLocation().add(0, 0.8, 0), 0.5F, 0.5F, 0.5F, 0.01F, 5, ViewDist.SHORT);
					player.getInventory().setItem(ACTIVE_SLOT, ACTIVE_ITEM);
					player.sendMessage(F.main(getName(), "Your " + F.name("Wolf") +" gave you the " + F.skill("Bone") + " back."));
				}
			});
		});
	}

	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.PHYSICAL)
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = event.getItem();

		if (!isActive(player) || itemStack == null || !itemStack.equals(ACTIVE_ITEM))
		{
			return;
		}

		event.setCancelled(true);

		if (_fetch.containsKey(player) || !Recharge.Instance.use(player, "Fetch", 10000, true, true, "Cosmetics"))
		{
			return;
		}

		Vector direction = player.getLocation().getDirection();
		Item item = player.getWorld().dropItem(player.getEyeLocation().add(direction), itemStack);
		item.setVelocity(direction.multiply(1.3));
		_fetch.put(player, item);

		player.setItemInHand(null);
		player.playSound(player.getLocation(), Sound.WOLF_BARK, 1, 1);
		player.sendMessage(F.main(getName(), "You threw a " + F.skill("Bone") + "."));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void pickupItem(PlayerPickupItemEvent event)
	{
		Player player = event.getPlayer();
		Item item = _fetch.get(player);

		if (item != null && item.equals(event.getItem()))
		{
			event.setCancelled(false);
			_fetch.remove(player);
		}
	}

	@EventHandler
	public void updateInvalidItems(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		_fetch.keySet().removeIf(player ->
		{
			Item item = _fetch.get(player);

			if (item.isDead() || !item.isValid() || item.getTicksLived() > 200)
			{
				player.getInventory().setItem(ACTIVE_SLOT, ACTIVE_ITEM);
				item.remove();
				return true;
			}

			return false;
		});
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		clearWolves(event.getPlayer());
	}

	private void clearWolves(Player player)
	{
		Set<Wolf> wolves = _wolves.remove(player);

		if (wolves != null)
		{
			wolves.forEach(Entity::remove);
			wolves.clear();
		}

		Item item = _fetch.remove(player);

		if (item != null)
		{
			item.remove();
		}

		_wolfWithStick.remove(player);
	}
}
