package mineplex.core.energy;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.MiniClientPlugin;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.energy.event.EnergyEvent;
import mineplex.core.energy.event.EnergyEvent.EnergyChangeReason;

import java.util.UUID;

public class Energy extends MiniClientPlugin<ClientEnergy>
{
	private double _baseEnergy = 180;
	private boolean _enabled = true;

	public Energy(JavaPlugin plugin)
	{
		super("Energy", plugin);
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (!_enabled)
			return;
		
		if (event.getType() != UpdateType.TICK) 
			return;

		for (Player cur : UtilServer.getPlayers())
			UpdateEnergy(cur);
	}

	private void UpdateEnergy(Player cur) 
	{
		if (cur.isDead())
			return;

		//Get Exp Attribs
		double energy = 0.4;	

		//Modify Energy
		EnergyEvent energyEvent = new EnergyEvent(cur, energy, EnergyChangeReason.Recharge);
		_plugin.getServer().getPluginManager().callEvent(energyEvent);

		if (energyEvent.isCancelled())
			return;

		//Update Players Exp
		ModifyEnergy(cur, energyEvent.GetTotalAmount());
	}

	public void ModifyEnergy(Player player, double energy) 
	{
		if (!_enabled)
			return;
		
		ClientEnergy client = Get(player);

		if (energy > 0)
		{
			client.Energy = Math.min(GetMax(player), client.Energy + energy);
		}
		else
		{
			client.Energy = Math.max(0, client.Energy + energy);
		}

		//Record Drain
		if (energy < 0)
		{
			client.LastEnergy = System.currentTimeMillis();
		}

		player.setExp(Math.min(0.999f, ((float)client.Energy/(float)GetMax(player))));		
	}

	public double GetMax(Player player)
	{
		return _baseEnergy + Get(player).EnergyBonus();
	}

	public double GetCurrent(Player player)
	{
		return Get(player).Energy;
	}

	@EventHandler
	public void HandleRespawn(PlayerRespawnEvent event)
	{
		Get(event.getPlayer()).Energy = 0;
	}

	@EventHandler
	public void HandleJoin(PlayerJoinEvent event)
	{
		Get(event.getPlayer()).Energy = 0;
	}

	public boolean Use(Player player, String ability, double amount, boolean use, boolean inform)
	{
		ClientEnergy client = Get(player);

		if (client.Energy < amount)
		{
			if (inform)
				UtilPlayer.message(player, F.main(_moduleName, "You are too exhausted to use " + F.skill(ability) + "."));

			return false;
		}
		else
		{
			if (!use)
				return true;

			ModifyEnergy(player, -amount);

			return true;
		}
	}

	@EventHandler
	public void handleExp(PlayerExpChangeEvent event)
	{
		if (!_enabled)
			return;
		
		event.setAmount(0);
	}

	@Override
	protected ClientEnergy addPlayer(UUID uuid)
	{
		return new ClientEnergy();
	}
	
	public void AddEnergyMaxMod(Player player, String reason, int amount)
	{
		Get(player).MaxEnergyMods.put(reason, amount);
	}
	
	public void RemoveEnergyMaxMod(Player player, String reason)
	{
		Get(player).MaxEnergyMods.remove(reason);
	}

	public void setEnabled(boolean b)
	{
		_enabled = b;
	}
}
