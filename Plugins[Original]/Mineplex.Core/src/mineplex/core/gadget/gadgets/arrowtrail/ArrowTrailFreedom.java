package mineplex.core.gadget.gadgets.arrowtrail;

import java.awt.Color;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;

import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.banner.CountryFlag;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ArrowEffectGadget;
import mineplex.core.particleeffects.BabyFireworkEffect;

public class ArrowTrailFreedom extends ArrowEffectGadget
{

	private Color _color = Color.RED;
	private long _count;

	public ArrowTrailFreedom(GadgetManager manager)
	{
		super(manager, "Shock and Awe", UtilText.splitLineToArray(UtilText.colorWords("Send freedom directly into the faces of your foes.",
				ChatColor.RED, ChatColor.WHITE, ChatColor.BLUE), LineFormat.LORE),
				-8, Material.WOOL,
				(byte) 0);
		setDisplayItem(CountryFlag.USA.getBanner());
	}

	@Override
	public void doTrail(Arrow arrow)
	{
		ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST,
				new DustSpellColor(_color), arrow.getLocation().clone().add(0, .5, 0));
		for (int i = 0; i < 7; i++)
		{
			coloredParticle.setLocation(arrow.getLocation().clone().add(0, .5, 0));
			coloredParticle.display();
		}
		_count++;
		if (_count % 5 == 0)
		{
			if (_color == Color.RED)
				_color = Color.WHITE;
			else if (_color == Color.WHITE)
				_color = Color.BLUE;
			else
				_color = Color.RED;
		}
		if (_count == Long.MAX_VALUE - 1)
			_count = 0;
	}

	@Override
	public void doHitEffect(Arrow arrow)
	{
		BabyFireworkEffect babyFireworkEffect = new BabyFireworkEffect(arrow.getLocation(), _color);
		babyFireworkEffect.start();
	}

}
