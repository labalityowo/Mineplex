package nautilus.game.arcade.game.games.minestrike.items.guns;

import nautilus.game.arcade.game.games.minestrike.GunModule;

public enum GunType
{
	PISTOL("Pistol", 		0.01, 	0.02, 	0.02,	3f,		0.1),
	SHOTGUN("Shotgun", 		0, 		0, 		0,		3f,		0.1),
	SMG("SMG", 				0.02, 	0.04, 	0.06,	3f,		0.3),
	RIFLE("Rifle", 			0.04, 	0.08, 	0.12,	3.5f,	0.3),
	
	SNIPER("Sniper Rifle", 	0.16, 	0.08, 	0.16,	4f,		0.1);
	
	private String _name;
	
	private double _movePenalty;
	private double _sprintPentalty;
	private double _jumpPenalty;
	
	private float _volume;
	
	private double _recoilReductionRate;

	GunType(String name, double move, double sprint, double jump, float volume, double recoilReductionRate)
	{
		_name = name;
		_movePenalty = move;
		_sprintPentalty = sprint;
		_jumpPenalty = jump;
		
		_volume = volume;
		
		_recoilReductionRate = recoilReductionRate;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public double getMovePenalty()
	{
		return _movePenalty * GunModule.MOVE_PENALTY;
	}
	
	public double getSprintPenalty()
	{
		return _sprintPentalty * GunModule.MOVE_PENALTY;
	}
	
	public double getJumpPenalty()
	{
		return _jumpPenalty * GunModule.MOVE_PENALTY;
	}

	public float getVolume()
	{
		return _volume;
	}
	
	public double getRecoilReduction()
	{
		return _recoilReductionRate;
	}
}
