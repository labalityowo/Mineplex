package mineplex.core.gadget.gadgets.morph.moba;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.events.EntityVelocityChangeEvent;
import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTime;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilGameProfile;

public class MorphDana extends MorphGadget
{

	private static final ItemStack ACTIVE_ITEM = new ItemBuilder(Material.NETHER_STAR)
			.setTitle(C.cGreenB + "Dana's Rally")
			.addLore("You leap up into the air, and upon landing", "you plant a banner!")
			.build();
	private static final int ACTIVE_SLOT = 2;

	private final Map<Player, Long> _dashingPlayers = new HashMap<>();
	private final Set<RallyData> _data = new HashSet<>();

	public MorphDana(GadgetManager manager)
	{
		super(manager, "Dana Morph", UtilText.splitLinesToArray(new String[]{
				C.cGray + "Get pranked chest opener.",
				"",
				C.cGreen + "Click" + C.cWhite + " your " + C.cYellow + "Nether Star" + C.cWhite + " to",
				C.cWhite + "deploy your " + C.cYellow + "Rally" + C.cWhite + ".",
				"",
				C.cGreen + "Sneak" + C.cWhite + " to use " + C.cYellow + "Knock up" + C.cWhite + "."
		}, LineFormat.LORE), -20, Material.GLASS, (byte) 0);
		setDisplayItem(SkinData.DANA.getSkull());
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		GameProfile gameProfile = UtilGameProfile.getGameProfile(player);
		gameProfile.getProperties().clear();
		gameProfile.getProperties().put("textures", SkinData.DANA.getProperty());

		DisguisePlayer disguisePlayer = new DisguisePlayer(player, gameProfile);
		disguisePlayer.showInTabList(true, 0);
		UtilMorph.disguise(player, disguisePlayer, Manager);

		player.getInventory().setItem(ACTIVE_SLOT, ACTIVE_ITEM);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);

		UtilMorph.undisguise(player, Manager.getDisguiseManager());

		player.getInventory().setItem(ACTIVE_SLOT, null);
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

		if (!Manager.selectLocation(this, player.getLocation()))
		{
			Manager.informNoUse(player);
			return;
		}

		if (!Recharge.Instance.use(player, "Rally", 30000, false, true, "Cosmetics"))
		{
			return;
		}

		Vector vector = player.getLocation().getDirection();

		vector.setY(1.5);

		UtilAction.velocity(player, vector);
		_data.add(new RallyData(player));
	}

	@EventHandler
	public void updateLand(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		Iterator<RallyData> iterator = _data.iterator();

		while (iterator.hasNext())
		{
			RallyData data = iterator.next();

			if (data.Landed && UtilTime.elapsed(data.LandTime, 7000))
			{
				iterator.remove();
			}
			else if (!data.Landed && UtilTime.elapsed(data.LaunchTime, 1000) && UtilEnt.isGrounded(data.Owner))
			{
				data.LandTime = System.currentTimeMillis();
				data.Landed = true;
				Location location = data.Owner.getLocation();
				data.Banner = location;

				Block block = location.getBlock();

				Manager.getBlockRestore().add(block, Material.STANDING_BANNER.getId(), (byte) 0, 7500);

				boolean red = UtilMath.random.nextBoolean();
				byte colorData = (byte) (red ? 14 : 11);
				Banner banner = (Banner) block.getState();
				banner.setBaseColor(red ? DyeColor.RED : DyeColor.BLUE);
				banner.addPattern(getPattern(red));
				banner.update();

				Collection<Block> blocks = UtilBlock.getBlocksInRadius(banner.getLocation(), 5);
				Manager.selectBlocks(this, blocks);

				for (Block nearby : blocks)
				{
					if (UtilBlock.airFoliage(nearby) || !UtilBlock.airFoliage(nearby.getRelative(BlockFace.UP)))
					{
						continue;
					}

					Manager.getBlockRestore().add(nearby, Material.STAINED_CLAY.getId(), colorData, (long) (7000 + (Math.random() * 500)));
					if (Math.random() > 0.9)
					{
						nearby.getWorld().playEffect(nearby.getLocation(), Effect.STEP_SOUND, Material.STAINED_CLAY, colorData);
					}
				}
			}
		}
	}

	@EventHandler
	public void updateParticles(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (RallyData data : _data)
		{
			if (!data.Landed)
			{
				continue;
			}

			Location banner = data.Banner;

			for (int i = 0; i < 5; i++)
			{
				double x = 5 * Math.sin(data.ParticleTheta);
				double z = 5 * Math.cos(data.ParticleTheta);

				banner.add(x, 0.25, z);

				UtilParticle.PlayParticleToAll(ParticleType.HAPPY_VILLAGER, banner, 0, 0, 0, 0.1F, 1, ViewDist.NORMAL);

				banner.subtract(x, 0.25, z);

				data.ParticleTheta += Math.PI / 100;
			}
		}
	}

	@EventHandler
	public void velocityChange(EntityVelocityChangeEvent event)
	{
		for (RallyData data : _data)
		{
			if (!data.Landed && data.Owner.equals(event.getEntity()))
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void itemSpawn(ItemSpawnEvent event)
	{
		if (event.getEntity().getItemStack().getType() == Material.BANNER)
		{
			event.setCancelled(true);
		}
	}

	private Pattern getPattern(boolean red)
	{
		return red ? new Pattern(DyeColor.WHITE, PatternType.CROSS) : new Pattern(DyeColor.WHITE, PatternType.CIRCLE_MIDDLE);
	}

	private class RallyData
	{

		Player Owner;
		Location Banner;
		boolean Landed;
		long LaunchTime;
		long LandTime;
		double ParticleTheta;

		RallyData(Player owner)
		{
			Owner = owner;
			LaunchTime = System.currentTimeMillis();
		}
	}

	@EventHandler
	public void playerSneak(PlayerToggleSneakEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(event.getPlayer()) || !event.isSneaking() || !Recharge.Instance.use(player, "Knock up", 8000, true, false, "Cosmetics"))
		{
			return;
		}

		_dashingPlayers.put(player, System.currentTimeMillis());
	}

	@EventHandler
	public void updateDash(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_dashingPlayers.keySet().removeIf(player ->
		{
			long start = _dashingPlayers.get(player);

			if (UtilTime.elapsed(start, 600))
			{
				UtilAction.zeroVelocity(player);
				return true;
			}

			Location location = player.getLocation();

			for (Player nearby : UtilPlayer.getNearby(player.getLocation(), 2))
			{
				if (player.equals(nearby))
				{
					continue;
				}

				if (Manager.selectEntity(this, nearby) && Recharge.Instance.use(nearby, "Knock up " + player.getUniqueId(), 1000, false, false, "Cosmetics"))
				{
					nearby.getWorld().playSound(nearby.getLocation(), Sound.IRONGOLEM_HIT, 1, 0.5F);
					UtilAction.velocity(nearby, new Vector(Math.random() / 2 - 0.25, 1, Math.random() / 2 - 0.25));
				}
			}

			UtilAction.velocity(player, location.getDirection().setY(0));
			UtilParticle.PlayParticle(ParticleType.CLOUD, player.getLocation().add(0, 1, 0), 0.5F, 0.5F, 0.5f, 0.1F, 10, ViewDist.LONG);
			return false;
		});
	}

}
