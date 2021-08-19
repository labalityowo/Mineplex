package mineplex.core.gadget.gadgets.taunts;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.TauntGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.particleeffects.TextEffect;
import mineplex.core.recharge.Recharge;

public class EmojiTaunt extends TauntGadget
{

	private static final long COOLDOWN = TimeUnit.SECONDS.toMillis(30);
	private static final int TICKS = 40;
	private static final String[] EMOJIS =
			{
					":)",
					";)",
					";o",
					"<3"
			};
	private static final FireworkEffect FIREWORK_EFFECT = FireworkEffect.builder()
			.with(Type.STAR)
			.withColor(Color.RED)
			.withFade(Color.WHITE)
			.withFlicker()
			.build();

	public EmojiTaunt(GadgetManager manager)
	{
		super(manager, "Emoji Taunt", new String[]
						{
								C.cGray + "Winky face ;p",
								C.blankLine,
								C.cWhite + "Use /taunt in game to use this taunt.",
								C.cGreen + "Can be used while in PvP!"
						}, CostConstants.NO_LORE, Material.CAKE, (byte) 0);

		setCanPlayWithPvp(true);
	}

	@Override
	public boolean onStart(Player player)
	{
		if (!Recharge.Instance.use(player, getName(), COOLDOWN, true, false, "Cosmetics"))
		{
			return false;
		}

		Location location = player.getLocation().add(0, 5, 0);
		String emoji = UtilMath.randomElement(EMOJIS);

		Bukkit.broadcastMessage(F.main("Taunt", F.name(player.getName()) + " : " + F.name(emoji)));

		UtilFirework.playFirework(location, FIREWORK_EFFECT);
		new TextEffect(TICKS, emoji, location, false, false, ParticleType.FLAME)
				.start();

		Manager.runSyncLater(() -> finish(player), TICKS);
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