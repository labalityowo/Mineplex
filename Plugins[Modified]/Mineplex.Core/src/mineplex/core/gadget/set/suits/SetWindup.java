package mineplex.core.gadget.set.suits;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.NoteColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.outfit.windup.OutfitWindupBoots;
import mineplex.core.gadget.gadgets.outfit.windup.OutfitWindupChestplate;
import mineplex.core.gadget.gadgets.outfit.windup.OutfitWindupHelmet;
import mineplex.core.gadget.gadgets.outfit.windup.OutfitWindupLeggings;
import mineplex.core.gadget.types.GadgetSet;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.noteblock.NBSReader;
import mineplex.core.noteblock.NoteSong;
import mineplex.core.noteblock.SingleRunNotePlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class SetWindup extends GadgetSet
{

	public static final String NAME = "Windup";
	public static final Color COLOUR = Color.BLACK;
	private static final float CHARGE_GAINED = 0.002F, CHARGE_LOST = 0.01F;
	private static final int EFFECT_MILLIS = (int) TimeUnit.SECONDS.toMillis(63), EFFECT_TICKS = EFFECT_MILLIS / 50;
	private static final long COOLDOWN = TimeUnit.SECONDS.toMillis(10);
	private static final ChatColor[] COLOURS =
			{
					ChatColor.RED,
					ChatColor.GOLD,
					ChatColor.YELLOW,
					ChatColor.GREEN,
					ChatColor.AQUA,
					ChatColor.DARK_AQUA,
					ChatColor.LIGHT_PURPLE,
					ChatColor.WHITE
			};
	private static final String SONG_FILE = ".." + File.separator + ".." + File.separator + "update" + File.separator + "songs" + File.separator + "windupsuit.nbs";

	private final Map<Player, WindupData> _charge;
	private NoteSong _song;

	public SetWindup(GadgetManager manager)
	{
		super(manager, NAME, "The suit begins to charge up.",
				manager.getGadget(OutfitWindupHelmet.class),
				manager.getGadget(OutfitWindupChestplate.class),
				manager.getGadget(OutfitWindupLeggings.class),
				manager.getGadget(OutfitWindupBoots.class)
		);

		_charge = new HashMap<>();

		try
		{
			_song = NBSReader.loadSong(SONG_FILE);
		}
		catch (FileNotFoundException e)
		{
		}
	}

	@Override
	public void customEnable(Player player, boolean message)
	{
		super.customEnable(player, message);

		_charge.put(player, new WindupData());
	}

	@Override
	public void customDisable(Player player)
	{
		super.customDisable(player);

		WindupData data = _charge.remove(player);

		if (data != null)
		{
			if (data.NotePlayer != null)
			{
				data.NotePlayer.end();
			}

			player.removePotionEffect(PotionEffectType.SPEED);
			player.removePotionEffect(PotionEffectType.JUMP);
			UtilPlayer.removeWorldBorder(player);
		}
	}

	@EventHandler
	public void updateWind(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_charge.forEach((player, data) ->
		{
			if (!isActive(player) || !Recharge.Instance.usable(player, getName()))
			{
				return;
			}

			float charge = data.Charge;
			int ticks = player.getTicksLived();

			// Effect is active
			if (data.EffectStart > 0)
			{
				// Effect is over
				if (UtilTime.elapsed(data.EffectStart, EFFECT_MILLIS))
				{
					Location location = player.getLocation().add(0, 1, 0);
					location.setYaw(0);
					location.add(location.getDirection());

					player.getWorld().playSound(location, Sound.ENDERMAN_TELEPORT, 1, 1);
					player.getWorld().playSound(location, Sound.FIZZ, 1, 1);
					player.getWorld().playSound(location, Sound.BAT_TAKEOFF, 1, 1);
					UtilParticle.PlayParticleToAll(ParticleType.CLOUD, location, 1, 0.5F, 1, 0, 30, ViewDist.NORMAL);
					UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, location, 1, 0.5F, 1, 0, 30, ViewDist.NORMAL);
					data.reset();
					player.setExp(data.Charge);
					setArmour(player, COLOUR);
					Recharge.Instance.useForce(player, getName(), COOLDOWN, true);
				}
				else
				{
					Color color = Color.fromRGB(UtilMath.r(255), UtilMath.r(255), UtilMath.r(255));

					if (ticks % 6 == 0)
					{
						setArmour(player, color);
						UtilTextBottom.display(UtilMath.randomElement(COLOURS) + C.Bold + UtilTime.convertString(data.EffectStart + EFFECT_MILLIS - System.currentTimeMillis(), 0, UtilTime.TimeUnit.FIT), player);
					}

					new ColoredParticle(ParticleType.NOTE, new NoteColor(color), player.getLocation().add(0, 0.5, 0))
							.display();
				}
			}
			// Effect is charged
			else if (charge >= 1)
			{
				Location location = player.getLocation();

				data.EffectStart = System.currentTimeMillis();
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, EFFECT_TICKS, 12, false, false));
				player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, EFFECT_TICKS, 6, false, false));
				UtilPlayer.removeWorldBorder(player);

				for (int i = 0; i < 2; i++)
				{
					UtilFirework.spawnRandomFirework(location.add(0, 1, 0));
				}

				if (_song != null)
				{
					data.NotePlayer = new SingleRunNotePlayer(_song, player).start();
				}
			}
			// Effect is charging
			else
			{
				if (Manager.isMoving(player))
				{
					charge += CHARGE_GAINED;

					if (player.isSprinting())
					{
						charge += CHARGE_GAINED;
					}
				}
				else
				{
					charge -= CHARGE_LOST;
				}

				charge = Math.max(0, charge);
				data.Charge = charge;
				charge = Math.min(0.999F, charge);

				if (charge == 0)
				{
					UtilPlayer.removeWorldBorder(player);
					return;
				}

				if (ticks % 10 == 0)
				{
					data.Current = data.Current.setRed((int) (255 * charge));
					setArmour(player, data.Current);
					UtilPlayer.sendRedScreen(player, (int) (6000 + 5000 * charge));
				}

				if (ticks % (int) (4F / charge) == 0)
				{
					player.playSound(player.getLocation(), Sound.WOOD_CLICK, 1, charge / 0.5F + 0.1F);
				}

				String colour = C.cRed;

				if (charge > 0.99)
				{
					colour = C.cGreenB;
				}
				else if (charge > 0.75)
				{
					colour = C.cGreen;
				}
				else if (charge > 0.5)
				{
					colour = C.cYellow;
				}
				else if (charge > 0.25)
				{
					colour = C.cGold;
				}

				UtilTextBottom.displayProgress(colour + NAME, charge, colour + (int) Math.ceil(charge * 100) + "%", player);
				player.setExp(charge);
			}
		});
	}

	private void setArmour(Player player, Color color)
	{
		player.getInventory().setArmorContents
				(
						new ItemStack[]
								{
										new ItemBuilder(Material.LEATHER_BOOTS)
												.setColor(color)
												.build(),
										new ItemBuilder(Material.LEATHER_LEGGINGS)
												.setColor(color)
												.build(),
										new ItemBuilder(Material.LEATHER_CHESTPLATE)
												.setColor(color)
												.build(),
										new ItemBuilder(Material.LEATHER_HELMET)
												.setColor(color)
												.build()
								}
				);
	}

	private static class WindupData
	{

		float Charge;
		long EffectStart;
		Color Current;
		SingleRunNotePlayer NotePlayer;

		WindupData()
		{
			reset();
		}

		void reset()
		{
			Charge = 0;
			EffectStart = 0;
			Current = COLOUR;
		}

	}

}
