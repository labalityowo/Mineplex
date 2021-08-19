package nautilus.game.arcade.game.games.halloween;

public enum HalloweenAudio implements NamedAudio
{
	WAVE_1("halloween.wave1"),
	WAVE_2("halloween.wave2"),
	WAVE_3("halloween.wave3"),
	WAVE_4("halloween.wave4"),
	WAVE_5("halloween.wave5"),
	WAVE_6("halloween.wave6"),
	
	BOSS_SPAWN("halloween.boss_spawn"),
	BOSS_LOSE("halloween.boss_lose"),
	BOSS_WIN("halloween.boss_win"),
	
	BOSS_STAGE_MINION_ATTACK("halloween.boss_minion"),
	BOSS_STAGE_SHIELD_RESTORE("halloween.boss_shield"),
	BOSS_STAGE_FINAL("halloween.boss_final"),
	BOSS_STAGE_FINAL_HALF_DEAD("halloween.boss_final_taunt");
	
	private String _name;
	
	HalloweenAudio(String name)
	{
		_name = name;
	}
	
	public String getAudioPath()
	{
		return _name;
	}
}
