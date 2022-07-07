package nautilus.game.arcade.game.games.halloween.waves;

import nautilus.game.arcade.game.games.halloween.Halloween;
import nautilus.game.arcade.game.games.halloween.HalloweenAudio;
import nautilus.game.arcade.game.games.halloween.creatures.MobCreeper;
import nautilus.game.arcade.game.games.halloween.creatures.MobGiant;
import nautilus.game.arcade.game.games.halloween.creatures.MobZombie;
import nautilus.game.arcade.game.games.halloween.creatures.PumpkinKing;

public class WaveBoss extends WaveBase
{
	private PumpkinKing _king;
	
	private boolean _canEnd = false;
	
	public WaveBoss(Halloween host) 
	{
		super(host, "The Pumpkin King", 0, host.GetSpawnSet(0), HalloweenAudio.WAVE_6);
	}

	@Override
	public void Spawn(int tick) 
	{
		if (tick == 100)
		{
			_king = new PumpkinKing(Host, Host.WorldData.GetDataLocs("BLACK").get(0));
			Host.AddCreature(_king);
			
			Host.playSound(HalloweenAudio.BOSS_SPAWN);
			
			_canEnd = true;
		}
		
		//Increasing difficulty of mobs
		if (Host.GetCreatures().size() < 20 + (tick/200) && (_king == null || !_king.IsFinal()))
		{
			if (tick % Math.max(5, 15 - tick/400) == 0)
				if (Math.random() > 0.10)
					Host.AddCreature(new MobZombie(Host, Host.GetRandomSpawn()));
				else
					Host.AddCreature(new MobCreeper(Host, Host.GetRandomSpawn()));
		}
		
		//Giant every 2.5 minutes
		if (tick % 3000 == 0 && (_king == null || !_king.IsFinal()))
		{
			Host.AddCreature(new MobGiant(Host, GetSpawn()));
		}
	}
	
	@Override
	public boolean CanEnd() 
	{
		if (_canEnd && (_king == null || !_king.GetEntity().isValid()))
		{
			_king.Host.playSound(HalloweenAudio.BOSS_LOSE);
			return true;
		}
		
		return false;
	}
}
