package mineplex.core.gadget.gadgets.taunts;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.TauntGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.particleeffects.TextEffect;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;

public class EasyModeTaunt extends TauntGadget
{

	private static final long COOLDOWN = TimeUnit.SECONDS.toMillis(30);
	private static final String[] TEXT =
			{
					"Is",
					"this",
					"easy",
					"mode?"
			};

	public EasyModeTaunt(GadgetManager manager)
	{
		super(manager, "Easy Mode Taunt", new String[]
						{
								C.cGray + "You think to yourself, this can't be this easy.",
								C.blankLine,
								C.cWhite + "Use /taunt in game to use this taunt.",
								C.cGreen + "Can be used while in PvP!"
						}, CostConstants.NO_LORE, Material.SKULL_ITEM, (byte) 3);

		setCanPlayWithPvp(true);
		setEventType(UpdateType.FASTER);
	}

	@Override
	public boolean onStart(Player player)
	{
		if (!Recharge.Instance.use(player, getName(), COOLDOWN, true, false, "Cosmetics"))
		{
			return false;
		}

		Location location = player.getLocation().add(0, 5, 0);

		Bukkit.broadcastMessage(F.main("Taunt", F.name(player.getName()) + " thought to themselves... " + F.elem("Is this easy mode?")));

		TextEffect effect = new TextEffect(Integer.MAX_VALUE, TEXT[0], location, true, false, ParticleType.FLAME);
		effect.start();

		Manager.runSyncTimer(new BukkitRunnable()
		{
			int index = 0;

			@Override
			public void run()
			{
				if (index == TEXT.length)
				{
					cancel();
					effect.stop();
					finish(player);
					return;
				}

				location.getWorld().playSound(location, Sound.VILLAGER_IDLE, 1, (float) (0.5F + Math.random()));
				effect.setText(TEXT[index++]);
			}
		}, 0, 20);

		return true;
	}

	@Override
	public void onPlay(Player player)
	{
	}

	@Override
	public void onFinish(Player player)
	{
	}
}