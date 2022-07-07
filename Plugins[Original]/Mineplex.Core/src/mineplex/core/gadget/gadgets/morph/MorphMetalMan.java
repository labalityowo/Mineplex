package mineplex.core.gadget.gadgets.morph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTime;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.GadgetSelectLocationEvent;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.particleeffects.MetalManEffect;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.utils.UtilGameProfile;

public class MorphMetalMan extends MorphGadget
{

	private Map<UUID, Integer> _playerColors = new HashMap<>();
	private Map<Player, Long> _flying = new HashMap<>();

	private static final int FLY_TIME = 15;

	public MorphMetalMan(GadgetManager manager)
	{
		super(manager, "Metal Man Morph", UtilText.splitLinesToArray(new String[]{
						C.cGray + "This powerful suit forged of metal makes the wearer strong enough to even battle the gods",
						"",
						C.cWhite + "Left-click to shoot laser beam",
						C.cWhite + "Sneak to hover"
		}, LineFormat.LORE),
				-14, Material.IRON_INGOT, (byte) 0);
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		GameProfile gameProfile = UtilGameProfile.getGameProfile(player);
		gameProfile.getProperties().clear();
		gameProfile.getProperties().put("textures", SkinData.METAL_MAN.getProperty());

		DisguisePlayer disguisePlayer = new DisguisePlayer(player, gameProfile);
		disguisePlayer.showInTabList(true, 0);
		UtilMorph.disguise(player, disguisePlayer, Manager);

		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1, true, false));
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);

		UtilMorph.undisguise(player, Manager.getDisguiseManager());

		if (_playerColors.containsKey(player.getUniqueId()))
		{
			_playerColors.remove(player.getUniqueId());
		}

		if (_flying.containsKey(player))
		{
			_flying.remove(player);
		}

		player.setFlying(false);
		player.setAllowFlight(false);

		player.removePotionEffect(PotionEffectType.SLOW);
	}

	@EventHandler
	public void onPlayerClick(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(player))
			return;

		if (!UtilEvent.isAction(event, UtilEvent.ActionType.L))
			return;

		if (player.getItemInHand().getType() != Material.AIR)
			return;

		if (!Recharge.Instance.use(player, "Metal Man Missile", 5000, true, false, "Cosmetics"))
			return;

		// Creates colored laser
		HashSet<Material> ignore = new HashSet<Material>();
		ignore.add(Material.AIR);
		Location loc = player.getTargetBlock(ignore, 64).getLocation().add(0.5, 0.5, 0.5);

		if (!Manager.selectLocation(this, loc))
		{
			Manager.informNoUse(player);
			return;
		}

		// Creates the particle beam
		int color = 0;
		if (_playerColors.containsKey(player.getUniqueId()))
		{
			color = _playerColors.get(player.getUniqueId());
		}
		MetalManEffect metalManEffect = new MetalManEffect(player.getEyeLocation(), loc, color, this, player);
		increaseColor(player.getUniqueId());
		metalManEffect.start();
	}

	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event)
	{
		if (!isActive(event.getPlayer()))
			return;

		if (!event.isSneaking())
			return;

		if (_flying.containsKey(event.getPlayer()))
			return;

		Player player = event.getPlayer();

		if (!Recharge.Instance.use(player, "Metal Man Fly", 45000, true, false, "Cosmetics"))
			return;

		UtilAction.velocity(player, new Vector(0, 1, 0));
		player.teleport(player.getLocation().add(0, 1, 0));
		player.playSound(player.getLocation(), Sound.WITHER_SHOOT, 1, 1);
		_flying.put(player, System.currentTimeMillis());
	}

	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<Map.Entry<Player, Long>> iterator = _flying.entrySet().iterator();
		while (iterator.hasNext())
		{
			Map.Entry<Player, Long> entry = iterator.next();
			Player player = entry.getKey();
			if (!isActive(player))
			{
				iterator.remove();
				continue;
			}
			long started = entry.getValue();
			if (UtilTime.elapsed(started, 15000))
			{
				player.setFlying(false);
				player.setAllowFlight(false);
				iterator.remove();
				continue;
			}
			player.setAllowFlight(true);
			player.setFlying(true);
			int filledBars = (int) ((started + 15000 - System.currentTimeMillis()) / 1000);
			UtilTextBottom.displayProgress("Flying", "", 15, filledBars, player);
			UtilParticle.PlayParticle(UtilParticle.ParticleType.FLAME, player.getLocation(), 0, 0, 0, 0, 5, UtilParticle.ViewDist.NORMAL);
		}
	}

	public void increaseColor(UUID uuid)
	{
		if (_playerColors.containsKey(uuid))
		{
			int color = _playerColors.get(uuid);
			if (color == 0)
				color = 1;
			else
				color = 0;
			_playerColors.put(uuid, color);
		}
		else
		{
			_playerColors.put(uuid, 1);
		}
	}

}
