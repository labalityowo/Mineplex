package mineplex.core.gadget.gadgets.taunts;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.TauntGadget;
import mineplex.core.itemstack.ItemStackFactory;
import mineplex.core.particleeffects.BlowAKissEffect;
import mineplex.core.recharge.Recharge;

public class BlowAKissTaunt extends TauntGadget
{

	private static final int COOLDOWN = 30000, PVP_COOLDOWN = 10000;

	public BlowAKissTaunt(GadgetManager manager)
	{
		super(manager, "Blow A Kiss", UtilText.splitLinesToArray(new String[]{
						C.cWhite + "Use /taunt in game to blow a kiss at your enemies.",
						C.cRed + "Cannot be used while in PvP!"}, LineFormat.LORE),
				-17, Material.GLASS, (byte) 0);
		setDisplayItem(ItemStackFactory.Instance.createCustomPotion(PotionType.INSTANT_HEAL));
		setCanPlayWithPvp(false);
		setPvpCooldown(PVP_COOLDOWN);
		setShouldPlay(false);
	}

	@Override
	public boolean onStart(Player player)
	{
		if (!Recharge.Instance.use(player, getName(), COOLDOWN, true, false, "Cosmetics"))
		{
			return false;
		}

		HashSet<Material> ignore = new HashSet<>();
		ignore.add(Material.AIR);
		Location loc = player.getTargetBlock(ignore, 64).getLocation().add(0.5, 0.5, 0.5);

		BlowAKissEffect blowAKissEffect = new BlowAKissEffect(player, loc);
		blowAKissEffect.start();

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
