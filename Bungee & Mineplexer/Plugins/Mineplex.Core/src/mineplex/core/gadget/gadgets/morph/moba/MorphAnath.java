package mineplex.core.gadget.gadgets.morph.moba;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.particles.effects.LineParticle;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilGameProfile;

public class MorphAnath extends MorphGadget
{

	private static final ItemStack ACTIVE_ITEM = new ItemBuilder(Material.FIREBALL)
			.setTitle(C.cGreenB + "Anath's Burn Beam")
			.addLore("Clicking this fires out a huge wave of fiery particles!")
			.setUnbreakable(true)
			.build();
	private static final int ACTIVE_SLOT = 2;

	private final Map<Player, Long> _dashingPlayers = new HashMap<>();

	public MorphAnath(GadgetManager manager)
	{
		super(manager, "Anath Morph", UtilText.splitLinesToArray(new String[]{
				C.cGray + "BURN BEAM!!!!",
				"",
				C.cGreen + "Click" + C.cWhite + " your " + C.cYellow + "Fireball" + C.cWhite + " to",
				C.cWhite + "fire out a huge wave of",
				C.cWhite + "fiery particles.",
				"",
				C.cGreen + "Sneak" + C.cWhite + " to use " + C.cYellow + "Flame Dash" + C.cWhite + "."
		}, LineFormat.LORE), -20, Material.GLASS, (byte) 0);
		setDisplayItem(SkinData.ANATH.getSkull());
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		GameProfile gameProfile = UtilGameProfile.getGameProfile(player);
		gameProfile.getProperties().clear();
		gameProfile.getProperties().put("textures", SkinData.ANATH.getProperty());

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

		if (!Recharge.Instance.use(player, "Burn Beam", 8000,true, true, "Cosmetics"))
		{
			return;
		}

		Vector direction = player.getLocation().getDirection().setY(0);

		player.getWorld().playSound(player.getLocation(), Sound.BLAZE_BREATH, 2, 0.5F);

		LineParticle particle = new LineParticle(player.getLocation().add(direction), direction, 0.2, 9, ParticleType.LAVA, UtilServer.getPlayers());
		particle.setIgnoreAllBlocks(true);

		Manager.runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				for (int i = 0; i < 3; i++)
				{
					if (particle.update())
					{
						cancel();
						return;
					}
					else
					{
						UtilParticle.PlayParticleToAll(ParticleType.FLAME, particle.getLastLocation().clone().add(0, 5, 0), 0.4F, 5, 0.4F, 0.05F, 30, ViewDist.LONG);
					}
				}

				if (Math.random() < 0.1)
				{
					particle.getLastLocation().getWorld().playSound(particle.getLastLocation(), Sound.GHAST_FIREBALL, 1, 0.5F);
				}
			}
		}, 0, 1);
	}

	@EventHandler
	public void playerSneak(PlayerToggleSneakEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(event.getPlayer()) || !event.isSneaking() || !Recharge.Instance.use(player, "Flame Dash", 8000, true, false, "Cosmetics"))
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

			UtilAction.velocity(player, player.getLocation().getDirection());

			Block block = player.getLocation().getBlock();

			while (!UtilBlock.solid(block) && block.getLocation().getBlockY() > 10)
			{
				block = block.getRelative(BlockFace.DOWN);
			}

			if (!Manager.selectBlocks(this, block))
			{
				return false;
			}

			Block fBlock = block;
			Manager.runSyncLater(() -> Manager.getBlockRestore().add(fBlock.getRelative(BlockFace.UP), Material.FIRE.getId(), (byte) 0, 5000), 10);
			return false;
		});
	}
}
