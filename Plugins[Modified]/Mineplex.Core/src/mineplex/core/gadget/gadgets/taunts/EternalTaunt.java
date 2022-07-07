package mineplex.core.gadget.gadgets.taunts;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseSkeleton;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.TauntGadget;
import mineplex.core.game.GameDisplay;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;

public class EternalTaunt extends TauntGadget
{

	private static final int COOLDOWN = 30000, PVP_COOLDOWN = 10000;

	private final Map<UUID, DisguiseBase> _disguises = new HashMap<>();

	public EternalTaunt(GadgetManager manager)
	{
		super(manager, "Eternal Taunt", UtilText.splitLinesToArray(new String[]{C.cGray + "Although the Eternal has been around forever, he waited too long for a worthy opponent and he turned to bones.",
				"",
				C.cWhite + "Use /taunt in game to show how long you've been waiting.",
				C.cRed + "Cannot be used while in PvP!"}, LineFormat.LORE),
				-15, Material.WATCH, (byte) 0);

		setCanPlayWithPvp(false);
		setPvpCooldown(PVP_COOLDOWN);
		setShouldPlay(true);
		setEventType(UpdateType.FAST);
		addDisabledGames(GameDisplay.Smash, GameDisplay.SmashTeams, GameDisplay.SmashDomination, GameDisplay.SmashTraining);
	}

	@Override
	public boolean onStart(Player player)
	{
		if (!Recharge.Instance.use(player, getName(), COOLDOWN, true, false, "Cosmetics"))
		{
			return false;
		}

		UtilFirework.playFirework(player.getLocation(), FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(Color.fromRGB(255, 175, 175)).withFade(Color.RED).build());

		Bukkit.broadcastMessage(F.main("Taunt", F.name(player.getName()) + " waited so long they turned to bones."));

		if (!Manager.getDisguiseManager().isDisguised(player))
		{
			DisguiseSkeleton disguiseSkeleton = new DisguiseSkeleton(player);
			disguiseSkeleton.setName(player.getName());
			disguiseSkeleton.setCustomNameVisible(true);
			disguiseSkeleton.showArmor();
			Manager.getDisguiseManager().disguise(disguiseSkeleton);
			_disguises.put(player.getUniqueId(), disguiseSkeleton);
		}

		return true;
	}

	@Override
	public void onPlay(Player player)
	{
		int ticks = getPlayerTicks(player);
		Item clock = UtilItem.dropItem(new ItemStack(Material.WATCH), player.getLocation().add(0.5, 1.5, 0.5), false, false, 60, false);

		Vector vel = new Vector(Math.sin(ticks * 9/5d), 0, Math.cos(ticks * 9/5d));
		UtilAction.velocity(clock, vel, Math.abs(Math.sin(ticks * 12/3000d)), false, 0, 0.2 + Math.abs(Math.cos(ticks * 12/3000d))*0.6, 1, false);

		player.playSound(player.getLocation(), Sound.CLICK, 1f, ticks % 2 == 0 ? 1 : 0.5F);

		if (ticks >= 15)
		{
			finish(player);
		}
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

	@EventHandler
	public void titanOwner(PlayerJoinEvent event)
	{
		if (Manager.getClientManager().Get(event.getPlayer()).hasPermission(GadgetManager.Perm.ETERNAL_TAUNT))
		{
			Manager.getDonationManager().Get(event.getPlayer()).addOwnedUnknownSalesPackage(getName());
		}
	}
}