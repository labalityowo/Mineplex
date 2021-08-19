package mineplex.core.gadget.gadgets.morph.moba;

import com.mojang.authlib.GameProfile;
import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.particles.effects.LineParticle;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.GadgetSelectLocationEvent;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.utils.UtilGameProfile;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class MorphHattori extends MorphGadget
{

	private static final ItemStack ACTIVE_ITEM = new ItemBuilder(Material.SNOW_BALL)
			.setTitle(C.cGreenB + "Hattori's Snowballs")
			.addLore("Click to fire 3 snowballs, one after another.")
			.build();
	private static final int SNOWBALL_SLOT = 2;

	public MorphHattori(GadgetManager manager)
	{
		super(manager, "Hattori Morph", UtilText.splitLinesToArray(new String[]{
				C.cGray + "Ever wanted to dash around",
				C.cGray + "the hub like a Ninja?",
				C.cGray + "Well now you can!",
				"",
				C.cGreen + "Click" + C.cWhite + " your " + C.cYellow + "Snowball" + C.cWhite + " to fire",
				C.cWhite + "snowballs in the direction you are looking.",
				"",
				C.cGreen + "Sneak" + C.cWhite + " to " + C.cYellow + "Dash" + C.cWhite + "."
		}, LineFormat.LORE), -20, Material.GLASS, (byte) 0);
		setDisplayItem(SkinData.HATTORI.getSkull());
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		GameProfile gameProfile = UtilGameProfile.getGameProfile(player);
		gameProfile.getProperties().clear();
		gameProfile.getProperties().put("textures", SkinData.HATTORI.getProperty());

		DisguisePlayer disguisePlayer = new DisguisePlayer(player, gameProfile);
		disguisePlayer.showInTabList(true, 0);
		UtilMorph.disguise(player, disguisePlayer, Manager);

		player.getInventory().setItem(SNOWBALL_SLOT, ACTIVE_ITEM);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);

		UtilMorph.undisguise(player, Manager.getDisguiseManager());

		player.getInventory().setItem(SNOWBALL_SLOT, null);
	}

	@EventHandler
	public void snowballClick(PlayerInteractEvent event)
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

		if (!Recharge.Instance.use(player, "Snowballs", 1000, false, true, "Cosmetics"))
		{
			return;
		}

		Manager.runSyncTimer(new BukkitRunnable()
		{
			int balls = 0;

			@Override
			public void run()
			{
				player.playSound(player.getLocation(), Sound.LAVA_POP, 1, 1.3F);
				player.launchProjectile(Snowball.class);

				if (++balls == 3)
				{
					cancel();
				}
			}
		}, 0, 4);
	}

	@EventHandler
	public void playerSneak(PlayerToggleSneakEvent event)
	{
		Player player = event.getPlayer();

		if (!isActive(event.getPlayer()) || !event.isSneaking() || !Recharge.Instance.use(player, "Dash", 5000, true, false, "Cosmetics"))
		{
			return;
		}

		Location start = player.getEyeLocation();

		if (!Manager.selectLocation(this, start))
		{
			Manager.informNoUse(player);
			return;
		}

		LineParticle lineParticle = new LineParticle(start, start.getDirection(), 0.8, 15, ParticleType.FIREWORKS_SPARK, UtilServer.getPlayers());

		while (!lineParticle.update())
		{
		}

		Location location = lineParticle.getDestination();
		Color color = UtilMath.random.nextBoolean() ? Color.RED : Color.AQUA;
		FireworkEffect effect = FireworkEffect.builder()
				.with(Type.BALL)
				.withColor(color)
				.withFlicker()
				.build();

		UtilFirework.playFirework(player.getEyeLocation(), effect);

		player.playSound(player.getLocation(), Sound.SHOOT_ARROW, 1, 1);
		player.teleport(location.add(0, 0.5, 0));
		player.playSound(player.getLocation(), Sound.SHOOT_ARROW, 1, 1);

		UtilFirework.playFirework(player.getEyeLocation(), effect);
	}

}
