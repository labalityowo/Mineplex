package mineplex.core.gadget.gadgets.doublejump;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.banner.CountryFlag;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.DoubleJumpEffectGadget;

public class DoubleJumpFreedom extends DoubleJumpEffectGadget
{

	public DoubleJumpFreedom(GadgetManager manager)
	{
		super(manager, "Leap of Freedom", UtilText.splitLineToArray(UtilText.colorWords("FREEEEEEEEEEEDOM!",
				ChatColor.RED, ChatColor.WHITE, ChatColor.BLUE), LineFormat.LORE), -8, Material.WOOL,
				(byte) 0);
		setDisplayItem(CountryFlag.USA.getBanner());
	}

	@Override
	public void doEffect(Player player)
	{
		DustSpellColor red = new DustSpellColor(Color.RED), blue = new DustSpellColor(Color.BLUE), white = new DustSpellColor(Color.WHITE);
		Location location = player.getLocation().add(0, 1, 0);

		for (int i = 0; i < 10; i++)
		{
			playParticleAt(location, red);
			playParticleAt(location, blue);
			playParticleAt(location, white);
		}
	}

	private void playParticleAt(Location location, DustSpellColor colour)
	{
		double x = random(2), y = random(1), z = random(2);

		new ColoredParticle(ParticleType.RED_DUST, colour, location.add(x, y, z))
				.display();

		location.subtract(x, y, z);
	}

	private double random(int x)
	{
		return (Math.random() - 0.5) * x;
	}
}
