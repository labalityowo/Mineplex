package nautilus.game.arcade.game.games.quiver.module;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.updater.UpdateType;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import nautilus.game.arcade.game.games.quiver.QuiverTeamBase;
import nautilus.game.arcade.game.games.quiver.ultimates.UltimatePerk;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

public class ModuleUltimate extends QuiverTeamModule implements Listener
{

	private static final int ULTIMATE_PERCENTAGE_INCREASE_KILL = 5;
	private static final int ULTIMATE_PERCENTAGE_INCREASE_ASSIST = 2;
	
	private Map<UUID, Float> _ultimatePercentage = new HashMap<>();

	private boolean _colouredMessage;
	
	public ModuleUltimate(QuiverTeamBase base)
	{
		super(base);
	}

	@Override
	public void setup()
	{
		getBase().Manager.registerEvents(this);
	}

	@Override
	public void update(UpdateType updateType)
	{
		if (updateType != UpdateType.SEC)
		{
			return;
		}
		
		_colouredMessage = !_colouredMessage;
		
		for (Player player : getBase().GetPlayers(true))
		{
			Kit kit =  getBase().GetKit(player);

			if (kit == null || UtilPlayer.isSpectator(player))
			{
				continue;
			}

			if (!_ultimatePercentage.containsKey(player.getUniqueId()))
			{
				_ultimatePercentage.put(player.getUniqueId(), 0F);
			}

			double percentage = _ultimatePercentage.get(player.getUniqueId());

			if (percentage >= 100)
			{
				UtilTextBottom.display((_colouredMessage ? C.cWhiteB : C.cAquaB) + "ULTIMATE READY (PRESS SNEAK)", player);
				player.setExp(0.999F);
				player.setLevel(100);
				continue;
			}

			String percentageFormatted = new DecimalFormat("0.0").format(percentage);

			UtilTextBottom.displayProgress("Ultimate", percentage / 100, percentageFormatted + "%", player);
			player.setExp((float) percentage / 100);
			player.setLevel((int) percentage);

			for (Perk perk : kit.GetPerks())
			{
				if (perk instanceof UltimatePerk)
				{
					UltimatePerk ultimate = (UltimatePerk) perk;

					if (ultimate.isUsingUltimate(player))
					{
						continue;
					}

					incrementUltimate(player, ultimate.getChargePassive());
				}
			}
		}
	}

	@Override
	public void finish()
	{
		UtilServer.Unregister(this);
		
		for (UUID uuid : _ultimatePercentage.keySet())
		{
			Player player = UtilPlayer.searchExact(uuid);

			for (Perk perk : getBase().GetKit(player).GetPerks())
			{
				if (!(perk instanceof UltimatePerk))
				{
					continue;
				}

				UltimatePerk ultimate = (UltimatePerk) perk;

				if (ultimate.isUsingUltimate(player))
				{
					ultimate.cancel(player);
				}
			}
		}
	}
	
	@EventHandler
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (event.GetEvent().getEntity() == null || event.GetLog().GetKiller() == null)
		{
			return;
		}

		if (!(event.GetEvent().getEntity() instanceof Player))
		{
			return;
		}

		Player player = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());

		if (player == null)
		{
			return;
		}

		incrementUltimate(player, ULTIMATE_PERCENTAGE_INCREASE_KILL);

		for (CombatComponent combatComponent : event.GetLog().GetAttackers())
		{
			if (event.GetLog().GetKiller() != null && combatComponent.equals(event.GetLog().GetKiller()))
			{
				continue;
			}

			if (combatComponent.IsPlayer())
			{
				Player assitedPlayer = UtilPlayer.searchExact(event.GetLog().GetKiller().GetName());

				if (assitedPlayer != null)
				{
					incrementUltimate(assitedPlayer, ULTIMATE_PERCENTAGE_INCREASE_ASSIST);
				}
			}
		}
	}

	public void incrementUltimate(Player player, float percentage)
	{
		Kit kit = getBase().GetKit(player);

		for (Perk perk : kit.GetPerks())
		{
			if (perk instanceof UltimatePerk)
			{
				UltimatePerk ultimate = (UltimatePerk) perk;

				if (ultimate.isUsingUltimate(player))
				{
					return;
				}
			}
		}
		
		_ultimatePercentage.put(player.getUniqueId(), _ultimatePercentage.get(player.getUniqueId()) + percentage);
	}

	public void resetUltimate(Player player, boolean inform)
	{
		if (inform)
		{
			player.sendMessage(F.main("Game", "Your Ultimate charge has been reset!"));
		}
		
		_ultimatePercentage.put(player.getUniqueId(), 0F);
	}
	
	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event)
	{
		if (!getBase().IsLive())
		{
			return;
		}

		Player player = event.getPlayer();
		Kit kit = getBase().GetKit(player);

		if (!event.isSneaking() || kit == null || UtilPlayer.isSpectator(player))
		{
			return;
		}

		if (_ultimatePercentage.get(player.getUniqueId()) < 100)
		{
			return;
		}

		for (Perk perk : kit.GetPerks())
		{
			if (perk instanceof UltimatePerk)
			{
				UltimatePerk ultimate = (UltimatePerk) perk;

				if (ultimate.isUsable(player))
				{
					ultimate.activate(player);
					resetUltimate(player, false);
				}
			}
		}
	}
	
}
