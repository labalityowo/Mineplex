package mineplex.core.gadget.gadgets.kitselector;

import java.awt.Color;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.KitSelectorGadget;

public class RainbowDanceKitSelector extends KitSelectorGadget
{

	private Color[] _rainbowColors = new Color[]
			{
					new Color(148, 0, 211),
					new Color(75, 0, 130),
					new Color(0, 0, 255),
					new Color(0, 255, 0),
					new Color(255, 255, 0),
					new Color(255, 127, 0),
					new Color(255, 0, 0)
			};
	private int _colorCount = 0;

	public RainbowDanceKitSelector(GadgetManager manager)
	{
		super(manager, "Rainbow Dance", UtilText.splitLinesToArray(new String[]{C.cGray + "At the end of this Rainbow is the kit of your dreams."}, LineFormat.LORE),
				0, Material.WOOL, (byte) 6);
	}

	@Override
	public void playParticle(Entity entity, Player playTo)
	{
		int tick = entity.getTicksLived();

		float x = (float) (Math.sin(tick / 7d) * 1f);
		float z = (float) (Math.cos(tick / 7d) * 1f);
		float y = (float) (Math.cos(tick / 17d) * 1f + 1f);

		ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST, new DustSpellColor(getNextColor()), entity.getLocation().add(x, y, z));
		coloredParticle.display(UtilParticle.ViewDist.NORMAL, playTo);
	}

	private Color getNextColor()
	{
		Color color = _rainbowColors[_colorCount];
		_colorCount++;
		if (_colorCount == _rainbowColors.length)
			_colorCount = 0;
		return color;
	}

}
