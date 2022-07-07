package mineplex.core.gadget.gadgets.arrowtrail;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.particles.ColoredParticle;
import mineplex.core.common.util.particles.DustSpellColor;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.ArrowEffectGadget;

public class ArrowTrailHalloween extends ArrowEffectGadget
{

	private static Map<Arrow, Color> _arrowColors = new HashMap<>();

	public ArrowTrailHalloween(GadgetManager manager)
	{
		super(manager, "Halloween Arrows", UtilText.splitLineToArray(C.cGray + "Don't listen to the critics; orange and black is in all year.", LineFormat.LORE),
				-9, Material.PUMPKIN, (byte) 0);
	}

	@Override
	public void doTrail(Arrow arrow)
	{
		if (_arrowColors.containsKey(arrow))
		{
			Color nextColor = _arrowColors.get(arrow);
			nextColor = (nextColor == Color.ORANGE) ? Color.BLACK : Color.ORANGE;
			ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST, new DustSpellColor(nextColor), arrow.getLocation());
			coloredParticle.display(15);
			_arrowColors.put(arrow, nextColor);
		}
		else
		{
			Color nextColor = Color.ORANGE;
			_arrowColors.put(arrow, nextColor);
			ColoredParticle coloredParticle = new ColoredParticle(UtilParticle.ParticleType.RED_DUST, new DustSpellColor(nextColor), arrow.getLocation());
			coloredParticle.display(2);
		}
	}

	@Override
	public void doHitEffect(Arrow arrow)
	{
		if (_arrowColors.containsKey(arrow))
		{
			_arrowColors.remove(arrow);
		}
	}

}
