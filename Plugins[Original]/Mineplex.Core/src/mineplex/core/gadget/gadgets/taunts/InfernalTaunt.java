package mineplex.core.gadget.gadgets.taunts;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton.SkeletonType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseSkeleton;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.TauntGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.game.GameDisplay;
import mineplex.core.particleeffects.TextEffect;
import mineplex.core.recharge.Recharge;

public class InfernalTaunt extends TauntGadget
{

	private static final int COOLDOWN = 60000, PVP_COOLDOWN = 10000, TICKS = 40;

	private final Map<UUID, DisguiseBase> _disguises = new HashMap<>();

	public InfernalTaunt(GadgetManager manager)
	{
		super(manager, "Infernal Taunt", new String[]
						{
								C.cGray + "Shows everyone your name in",
								C.cGray + "burning text!",
								"",
								C.cWhite + "Use /taunt in game to use this taunt.",
								C.cRed + "Cannot be used while in PvP!"
						}, CostConstants.FOUND_IN_TRICK_OR_TREAT, Material.SKULL_ITEM, (byte) 1);

		setCanPlayWithPvp(false);
		setPvpCooldown(PVP_COOLDOWN);
		addDisabledGames(GameDisplay.Smash, GameDisplay.SmashTeams, GameDisplay.SmashDomination, GameDisplay.SmashTraining);
	}

	@Override
	public boolean onStart(Player player)
	{
		if (!Recharge.Instance.use(player, getName(), COOLDOWN, true, false, "Cosmetics"))
		{
			return false;
		}

		player.getWorld().strikeLightningEffect(player.getLocation());
		Bukkit.broadcastMessage(F.main("Taunt", F.name(player.getName()) + " unleashed the " + F.name("Infernal Horror") + "!"));

		if (!Manager.getDisguiseManager().isDisguised(player))
		{
			DisguiseSkeleton disguiseSkeleton = new DisguiseSkeleton(player);
			disguiseSkeleton.setName(player.getName());
			disguiseSkeleton.setCustomNameVisible(true);
			disguiseSkeleton.showArmor();
			disguiseSkeleton.SetSkeletonType(SkeletonType.WITHER);
			Manager.getDisguiseManager().disguise(disguiseSkeleton);
			_disguises.put(player.getUniqueId(), disguiseSkeleton);
		}

		new TextEffect(TICKS, player.getName(), player.getLocation().add(0, 5, 0), false, false, ParticleType.FLAME)
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
		DisguiseBase disguise = _disguises.remove(player.getUniqueId());

		if (disguise != null)
		{
			Manager.getDisguiseManager().undisguise(disguise);
		}
	}
}