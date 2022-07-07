package mineplex.core.energy;

import java.util.HashMap;

public class ClientEnergy
{
	public double Energy;
	public long LastEnergy;
	
	public HashMap<String, Integer> MaxEnergyMods = new HashMap<String, Integer>();
	public HashMap<String, Integer> SwingEnergyMods = new HashMap<String, Integer>();
	
	public int EnergyBonus()
	{
		int bonus = 0;
		
		for (int i : MaxEnergyMods.values())
			bonus += i;
		
		return bonus;
	}
	
	public int SwingEnergy()
	{
		int mod = 0;
		
		for (int i : SwingEnergyMods.values())
			mod += i;
		
		return Math.max(0, 4 + mod);
	}
}
