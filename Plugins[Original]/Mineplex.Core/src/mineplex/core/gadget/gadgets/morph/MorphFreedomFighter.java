package mineplex.core.gadget.gadgets.morph;

import java.time.Month;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.mojang.authlib.GameProfile;

import mineplex.core.blockrestore.BlockRestore;
import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.banner.CountryFlag;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilGameProfile;

/**
 * Freedom fighter morph, capable of planting flags by crouching.
 */
public class MorphFreedomFighter extends MorphGadget
{
	/** How long it takes to plant a flag */
	private static final long FLAG_DELAY = 3500;

	/** How long between flag plantings */
	private static final long FLAG_COOLDOWN = 25000;

	/** Recharge key for planting flags */
	private static final String RECHARGE_KEY = "Plant Flag";

	/** Design for beacon base */
	private static final int[][] BEACON_BASE = {
			{ 0, -2, 0}, {1, -2,  0}, {0, -2, 1}, {1, -2,  1},
			{-1, -2, 0}, {0, -2, -1}, {-1,-2,-1}, {0, -2,  1},
			{-1, -2, 1}, {1, -2, -1}
	};

	/** Active timers for players planting flags */
	private final Map<UUID, Long> _flagTimers = new HashMap<>();

	/** Active timers for players that have planted flags */
	private final Map<UUID, Long> _flagCooldowns = new HashMap<>();

	public MorphFreedomFighter(GadgetManager manager)
	{
		super(manager, "Freedom Fighter", UtilText.splitLinesToArray(new String[] {
				C.cGray + "Fight for your freedom from tyranny and oppressors!",
				"",
				C.cGreen + "Hold sneak" + C.cWhite + " to plant a flag of freedom!",
		}, LineFormat.LORE), -14, Material.CHAINMAIL_CHESTPLATE, (byte) 0);

		setPPCYearMonth(YearMonth.of(2017, Month.JULY));
	}

	/**
	 * Sets the player's skin.
	 */
	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		GameProfile profile = UtilGameProfile.getGameProfile(player);
		profile.getProperties().clear();
		profile.getProperties().put("textures", SkinData.REVOLUTIONARY.getProperty());

		DisguisePlayer disguisePlayer = new DisguisePlayer(player, profile);
		disguisePlayer.showInTabList(true, 0);
		UtilMorph.disguise(player, disguisePlayer, Manager);
	}

	/**
	 * Restores the player's skin
	 */
	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);
		UtilMorph.undisguise(player, Manager.getDisguiseManager());
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTEST)
		{
			return;
		}

		// Clear cooldown timers
		if (event.getType() == UpdateType.SEC)
		{
			_flagCooldowns.entrySet().removeIf(entry -> entry.getValue() + FLAG_COOLDOWN < System.currentTimeMillis());
		}

		// For all active cosmetics
		for (Player player : getActive())
		{
			UUID uuid = player.getUniqueId();

			// If the player is attempting to place a flag
			if (_flagTimers.containsKey(uuid) && !_flagCooldowns.containsKey(uuid))
			{
				// Mark them as no longer attempting to place if not sneaking
				if (!player.isSneaking())
				{
					_flagTimers.remove(uuid);
					continue;
				}

				// If the players has waiting long enough to place the flag
				if (_flagTimers.get(uuid) + FLAG_DELAY < System.currentTimeMillis())
				{
					if (!Manager.selectLocation(this, player.getLocation()))
					{
						_flagTimers.remove(uuid);
						player.sendMessage(F.main("Morphs", C.cRed + "You must plant your flag on flatter ground."));
					}
					else
					{
						// handle placing the flag
						if (Recharge.Instance.use(player, RECHARGE_KEY, FLAG_COOLDOWN, true, false))
						{
							_flagTimers.remove(uuid);
							_flagCooldowns.put(uuid, System.currentTimeMillis());
							buildStructure(player);
						}
					}
				}
				else
				{
					// Play particles leading up to placing the flag
					int particleCount = (int) ((System.currentTimeMillis() - _flagTimers.get(uuid)) / 40);
					UtilParticle.playParticleFor(player, UtilParticle.ParticleType.FIREWORKS_SPARK,
							UtilMath.gauss(player.getLocation().add(0, 1, 0), 2, 6, 2), null, 0, particleCount, UtilParticle.ViewDist.NORMAL);
				}
			}
			else // if the player is not attempting to or has already placed a flag
			{
				if (player.isSneaking())
				{
					if (_flagCooldowns.containsKey(uuid))
					{
						if (Recharge.Instance.usable(player, RECHARGE_KEY, true))
						{
							_flagCooldowns.remove(uuid);
						}
					}
					else
					{
						_flagTimers.put(uuid, System.currentTimeMillis());
						_flagCooldowns.remove(uuid);
					}
				}
			}
		}
	}

	/**
	 * Builds the structure and beacon by the player.
	 */
	private void buildStructure(Player player)
	{
		World world = player.getWorld();
		BlockRestore restore = Manager.getBlockRestore();
		int r = ThreadLocalRandom.current().nextInt(3);
		byte data = r == 0 ? (byte) 14 : r == 1 ? (byte) 0 : 11;
		Location point = player.getLocation().subtract(0, 0.5, 0);

		while (point.getY() > 1 && !(UtilBlock.fullSolid(point.getBlock()) || UtilBlock.airFoliage(point.getBlock())))
		{
			point.setY(point.getY() - 1);
		}

		Block glass = point.getBlock().getRelative(BlockFace.UP);
		restore.add(glass, Material.STAINED_GLASS.getId(), data, FLAG_COOLDOWN);
		glass = glass.getRelative(BlockFace.UP);
		restore.add(glass, Material.STAINED_GLASS.getId(), data, FLAG_COOLDOWN);

		BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST };
		Block[] blocks = { glass.getRelative(BlockFace.NORTH), glass.getRelative(BlockFace.SOUTH),
				glass.getRelative(BlockFace.WEST), glass.getRelative(BlockFace.EAST) };

		restore.add(glass.getRelative(BlockFace.UP), Material.CARPET.getId(), data, FLAG_COOLDOWN - 50);

		for (int i = 0; i < 4; ++i)
		{
			restore.add(blocks[i], Material.WALL_BANNER.getId(), (byte) i, blocks[i].getTypeId(), blocks[i].getData(), FLAG_COOLDOWN - 100);
		}

		for (int i = 0; i < 4; ++i)
		{
			Banner state = ((Banner) blocks[i].getState());
			org.bukkit.material.Banner stateData = (org.bukkit.material.Banner) state.getData();
			stateData.setFacingDirection(faces[i]);
			CountryFlag flag = i < 2 ? CountryFlag.USA : CountryFlag.CANADA;
			state.setBaseColor(flag.getBaseColor());
			state.setPatterns(flag.getPatterns());
			state.update();
		}

		restore.add(point.getBlock(), Material.PISTON_BASE.getId(), (byte) 0, FLAG_COOLDOWN);

		point.subtract(0, 1, 0);
		restore.add(point.getBlock(), Material.PISTON_BASE.getId(), (byte) 0, FLAG_COOLDOWN);

		restore.add(point.getBlock().getRelative(BlockFace.DOWN), Material.BEACON.getId(), (byte) 0, FLAG_COOLDOWN);

		for (int[] beaconBase : BEACON_BASE)
		{
			restore.add(world.getBlockAt(point.clone().add(beaconBase[0], beaconBase[1], beaconBase[2])),
					Material.IRON_BLOCK.getId(), (byte) 0, FLAG_COOLDOWN);
		}

		UtilParticle.PlayParticleToAll(UtilParticle.ParticleType.HUGE_EXPLOSION, player.getLocation(), null, 0, 1, UtilParticle.ViewDist.NORMAL);
		player.playSound(player.getLocation(), Sound.EXPLODE, 1.0f, 1.0f);
		player.teleport(player.getLocation().add(0, 2.5, 0));
	}
}
