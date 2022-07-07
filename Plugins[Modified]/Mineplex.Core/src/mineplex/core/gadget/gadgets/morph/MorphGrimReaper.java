package mineplex.core.gadget.gadgets.morph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

import mineplex.core.common.shape.ShapeWings;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTime;
import mineplex.core.disguise.disguises.DisguiseSkeleton;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.SoulManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MorphGrimReaper extends MorphGadget
{

	private final Map<Player, Long> _flying = new HashMap<>();
	private final List<Player> _flyReady = new ArrayList<>();

	private final SoulManager _soulManager;
	private ItemStack _hoe;

	private static final int FLY_DELAY = 15000;

	private final ShapeWings _wings = new ShapeWings(UtilParticle.ParticleType.RED_DUST.particleName, new Vector(0.2,0.2,0.2), 1, 0, false, ShapeWings.DEFAULT_ROTATION, ShapeWings.SMALL_ANGEL_WING_PATTERN);
	private final ShapeWings _wingsEdge = new ShapeWings(UtilParticle.ParticleType.RED_DUST.particleName, new Vector(0.1,0.1,0.1), 1, 0, true, ShapeWings.DEFAULT_ROTATION, ShapeWings.SMALL_ANGEL_WING_PATTERN);

	public MorphGrimReaper(GadgetManager manager)
	{
		super(manager, "Grim Reaper Morph", UtilText.splitLinesToArray(new String[]{
						C.cGray + "The Grim Reaper is the collector of souls.",
						"",
						C.cWhite + "Right Click a player with Grim Reaper Scythe to steal their soul"
				}, LineFormat.LORE), -9,
				Material.WOOD_HOE, (byte) 0);
		_soulManager = manager.getSoulManager();
		createHoe();
	}

	private void createHoe()
	{
		ItemStack hoe = new ItemStack(Material.WOOD_HOE);
		ItemMeta meta = hoe.getItemMeta();
		meta.setDisplayName(C.cGreen + "Grim Reaper Scythe");
		meta.setLore(UtilText.splitLines(new String[]{C.cWhite + "Right Click a player to steal their soul!",
				C.cWhite + "Cooldown of 10 seconds."}, LineFormat.LORE));
		hoe.setItemMeta(meta);
		_hoe = hoe;
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		this.applyArmor(player, message);

		ItemStack blackChest = new ItemStack(Material.LEATHER_CHESTPLATE),
				blackPants = new ItemStack(Material.LEATHER_LEGGINGS), blackBoots = new ItemStack(Material.LEATHER_BOOTS);
		LeatherArmorMeta chestMeta = (LeatherArmorMeta) blackChest.getItemMeta(),
				pantsMeta = (LeatherArmorMeta) blackPants.getItemMeta(), bootsMeta = (LeatherArmorMeta) blackBoots.getItemMeta();
		chestMeta.setColor(Color.BLACK);
		pantsMeta.setColor(Color.BLACK);
		bootsMeta.setColor(Color.BLACK);
		blackChest.setItemMeta(chestMeta);
		blackPants.setItemMeta(pantsMeta);
		blackBoots.setItemMeta(bootsMeta);

		player.getInventory().setItem(2, _hoe);
		DisguiseSkeleton skeleton = new DisguiseSkeleton(player);
		skeleton.SetSkeletonType(Skeleton.SkeletonType.WITHER);
		skeleton.setChestplate(blackChest);
		skeleton.setLeggings(blackPants);
		skeleton.setBoots(blackBoots);
		UtilMorph.disguise(player, skeleton, Manager.getDisguiseManager());
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		this.removeArmor(player);
		player.setFlying(false);
		player.setAllowFlight(false);
		_flying.remove(player);
		_flyReady.remove(player);
		_soulManager.resetSouls(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());
		player.getInventory().setItem(2, new ItemStack(Material.AIR));
	}

	@EventHandler
	public void stealSoul(PlayerInteractEntityEvent event)
	{
		if (event.getRightClicked().getType() != EntityType.PLAYER)
			return;

		if (_flyReady.contains(event.getPlayer()))
			return;

		Player player = event.getPlayer();

		if (!player.getItemInHand().equals(_hoe))
			return;

		Player clicked = (Player) event.getRightClicked();
		boolean stolen = _soulManager.stealSoul(player, clicked);
		if (stolen)
		{
			player.playSound(player.getLocation(), Sound.WITHER_DEATH, 1f, 1f);
			UtilTextBottom.displayProgress("Grim Reaper Fly", _soulManager.checkSouls(player) + "/20 souls", 20, _soulManager.checkSouls(player), player);
			if (_soulManager.checkSouls(player) == 20)
			{
				_flyReady.add(player);
				UtilTextBottom.displayProgress("Grim Reaper Fly", 100, "Sneak to fly!", player);
			}
		}
	}

	@EventHandler
	public void sneak(PlayerToggleSneakEvent event)
	{
		if (!isActive(event.getPlayer()))
			return;

		if (!event.isSneaking())
			return;

		Player player = event.getPlayer();

		if (_soulManager.checkSouls(player) == 20)
		{
			setFlying(player, true, true);
			_flying.put(player, System.currentTimeMillis());
			_soulManager.resetSouls(player);
		}
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		_soulManager.giveSoul();
		List<Player> stopFlying = new ArrayList<>();
		for (Player player : _flying.keySet())
		{
			if (UtilTime.elapsed(_flying.get(player), FLY_DELAY))
			{
				stopFlying.add(player);
				setFlying(player, false, event.getType()==UpdateType.FAST);
			}
			else
			{
				setFlying(player, true, event.getType()==UpdateType.FAST);
			}
		}

		stopFlying.forEach(_flying::remove);

		if (event.getType() != UpdateType.SEC)
			return;

		for (Player player : _flyReady)
		{
			UtilTextBottom.displayProgress("Grim Reaper Fly", 100, "Sneak to fly!", player);
		}
	}

	private void setFlying(Player player, boolean flying, boolean isFast)
	{
		if (flying && isActive(player))
		{
			if (UtilPlayer.isSpectator(player))
				return;

			player.setAllowFlight(true);
			player.setFlying(true);

			_flyReady.remove(player);

			if (UtilEnt.isGrounded(player))
				UtilAction.velocity(player, new Vector(0,1,0));

			Location loc = player.getLocation().add(0, 1.2, 0).add(player.getLocation().getDirection().multiply(-0.2));
			if (isFast)
			{
				_wings.display(loc);
				_wingsEdge.display(loc);
			}
		}
		else
		{
			player.setAllowFlight(false);
		}
	}

	/**
	 * Removes the blindness effect for that player when they leave
	 * @param event
	 */
	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		_soulManager.giveSoul(event.getPlayer());
	}

}
