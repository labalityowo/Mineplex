package mineplex.core.gadget.gadgets.morph;

import java.time.Month;
import java.time.YearMonth;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilText;
import mineplex.core.disguise.disguises.DisguiseWitch;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.types.MorphGadget;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class MorphWitch extends MorphGadget
{

	public MorphWitch(GadgetManager manager)
	{
		super(manager, "Witch Morph", UtilText.splitLinesToArray(new String[]{
				C.cWhite + "Press sneak to summon your trusty bat and start brewing"
		}, LineFormat.LORE),
				-14, Material.GLASS, (byte) 0);

		setDisplayItem(SkinData.WITCH.getSkull());
		setPPCYearMonth(YearMonth.of(2016, Month.OCTOBER));
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		this.applyArmor(player, message);
		DisguiseWitch disguiseWitch = new DisguiseWitch(player);
		UtilMorph.disguise(player, disguiseWitch, Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		this.removeArmor(player);
		WitchEffectManager witchEffectManager = WitchEffectManager.getManager(player);
		if (witchEffectManager != null)
		{
			witchEffectManager.stop();
		}
		UtilMorph.undisguise(player, Manager.getDisguiseManager());
	}

	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event)
	{
		WitchEffectManager witchEffectManager = WitchEffectManager.getManager(event.getPlayer());

		if (!Recharge.Instance.usable(event.getPlayer(), "Witch Cauldron", true))
		{
			return;
		}

		if (!event.isSneaking())
			return;

		if (witchEffectManager != null)
		{
			if (witchEffectManager.hasStarted())
				return;
		}

		if (!isActive(event.getPlayer()))
			return;

		if (!event.getPlayer().isOnGround())
		{
			UtilPlayer.message(event.getPlayer(), F.main("Witch Morph", "You can't place the cauldron there!"));
			return;
		}

		// Checks if there is a block under the cauldron
		Location cauldronLocation = event.getPlayer().getLocation().clone().add(event.getPlayer().getLocation().getDirection());
		cauldronLocation.add(0, 1, 0);
		if(cauldronLocation.getBlock().getType() != Material.AIR)
		{
			UtilPlayer.message(event.getPlayer(), F.main("Witch Morph", "You can't place the cauldron there!"));
			return;
		}

		// Checks if the player is close to a cactus
		for (int x = -3; x < 3; x++)
		{
			for (int z = -3; z < 3; z++)
			{
				Location possibleCactus = cauldronLocation.clone().add(x, 0, z);
				if (possibleCactus.getBlock().getType() == Material.CACTUS)
				{
					UtilPlayer.message(event.getPlayer(), F.main("Witch Morph", "You can't place the cauldron there!"));
					return;
				}
			}
		}

		if (!Manager.selectLocation(this, cauldronLocation))
		{
			Manager.informNoUse(event.getPlayer());
			return;
		}

		if (!Recharge.Instance.use(event.getPlayer(), "Witch Cauldron", 15000, true, false, "Cosmetics"))
			return;

		WitchEffectManager newManager = new WitchEffectManager(event.getPlayer(), cauldronLocation);
		newManager.start();
	}

	@EventHandler
	public void onUpdate(UpdateEvent updateEvent)
	{
		if (updateEvent.getType() != UpdateType.TICK)
			return;

		long currentTime = System.currentTimeMillis();
		long delay = 5000;
		for (Player player : WitchEffectManager.getPlayers())
		{
			WitchEffectManager witchEffectManager = WitchEffectManager.getManager(player);
			if (witchEffectManager != null)
			{
				if (witchEffectManager.hasStarted())
				{
					if (currentTime - witchEffectManager.getStarted() >= delay)
					{
						witchEffectManager.stop();
					}
				}
			}
		}
	}

}
