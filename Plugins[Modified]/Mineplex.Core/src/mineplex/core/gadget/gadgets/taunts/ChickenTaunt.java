package mineplex.core.gadget.gadgets.taunts;

import java.time.Month;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.mojang.authlib.GameProfile;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.types.TauntGadget;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.game.GameDisplay;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;

public class ChickenTaunt extends TauntGadget
{

	private static final int COOLDOWN = 30000, PVP_COOLDOWN = 10000;
	private static final String CHICKEN_SKIN = "MHF_Chicken";

	private final Map<UUID, DisguiseBase> _disguises = new HashMap<>();

	private GameProfile _cachedProfile;

	public ChickenTaunt(GadgetManager manager)
	{
		super(manager, "Chicken Taunt", new String[]
				{
						C.cGray + "Baw Baw Bawk! It seems that",
						C.cGray + "everyone is too afraid to fight you",
						C.cGray + "what a shame.",
						"",
						C.cWhite + "Use /taunt in game to use this taunt.",
						C.cRed + "Cannot be used while in PvP!"
				}, CostConstants.POWERPLAY_BONUS, Material.GLASS, (byte) 0);

		setDisplayItem(new ItemBuilder(Material.SKULL_ITEM, (byte) 3)
				.setPlayerHead(CHICKEN_SKIN)
				.build());
		setPPCYearMonth(YearMonth.of(2018, Month.JUNE));
		setCanPlayWithPvp(false);
		setPvpCooldown(PVP_COOLDOWN);
		setShouldPlay(true);
		setEventType(UpdateType.FASTER);
		addDisabledGames(GameDisplay.Smash, GameDisplay.SmashTeams, GameDisplay.SmashDomination, GameDisplay.SmashTraining);
	}

	@Override
	public boolean onStart(Player player)
	{
		if (!Recharge.Instance.use(player, getName(), COOLDOWN, true, false, "Cosmetics"))
		{
			return false;
		}

		Location location = player.getLocation().add(0, 1, 0);

		UtilParticle.PlayParticleToAll(ParticleType.CLOUD, location, 1, 0.9F, 1, 0.2F, 30, ViewDist.NORMAL);
		player.getWorld().playSound(location, Sound.CHICKEN_IDLE, 3, (float) (0.1 + Math.random()));
		Bukkit.broadcastMessage(F.main("Taunt", F.name(player.getName()) + " thinks that everyone is too much of a " + F.elem("Chicken") + " to fight them!"));

		if (!Manager.getDisguiseManager().isDisguised(player))
		{
			DisguisePlayer disguise;

			if (_cachedProfile == null)
			{
				disguise = new DisguisePlayer(player, player.getName(), CHICKEN_SKIN);
				disguise.showInTabList(true, 0);
				disguise.initialize(() ->
				{
					_cachedProfile = disguise.getProfile();
					Manager.getDisguiseManager().disguise(disguise);
					_disguises.put(player.getUniqueId(), disguise);
				});
			}
			else
			{
				disguise = new DisguisePlayer(player, _cachedProfile);
				disguise.showInTabList(true, 0);
				Manager.getDisguiseManager().disguise(disguise);
				_disguises.put(player.getUniqueId(), disguise);
			}
		}

		return true;
	}

	@Override
	public void onPlay(Player player)
	{
		Location location = player.getLocation().add((Math.random() - 0.5) * 2, Math.random(), (Math.random() - 0.5) * 2);
		int ticks = getPlayerTicks(player);

		if (ticks % 8 == 0)
		{
			player.getWorld().playSound(location, Sound.CHICKEN_IDLE, 1, (float) (0.1 + Math.random()));
		}

		player.getWorld().playSound(location, Sound.CHICKEN_EGG_POP, 1, (float) (0.1 + Math.random()));
		UtilParticle.PlayParticleToAll(ParticleType.CLOUD, location, 0.3F, 0.3F, 0.3F, 0.05F, 5, ViewDist.NORMAL);
		UtilItem.dropItem(new ItemStack(Material.EGG), location, true, false, 50, false);

		if (ticks >= 24)
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
}