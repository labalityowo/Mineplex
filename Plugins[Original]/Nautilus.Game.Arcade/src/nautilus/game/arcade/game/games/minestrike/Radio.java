package nautilus.game.arcade.game.games.minestrike;

import mineplex.core.common.util.UtilMath;

import org.bukkit.Sound;

public enum Radio
{
	BOMB_PLANT(new Sound[] {Sound.WOLF_PANT}),
	BOMB_DEFUSE(new Sound[] {Sound.WOLF_SHAKE}),
	CT_WIN(new Sound[] {Sound.WOLF_WHINE}),
	T_WIN(new Sound[] {Sound.ZOMBIE_DEATH}),
	
	CT_GRENADE_HE(new Sound[] {Sound.SPIDER_IDLE}),
	CT_GRENADE_FLASH(new Sound[] {Sound.ZOMBIE_METAL}),
	CT_GRENADE_SMOKE(new Sound[] {Sound.WOLF_GROWL}),
	CT_GRENADE_FIRE(new Sound[] {Sound.WOLF_HOWL}),
	
	T_GRENADE_HE(new Sound[] {Sound.WITHER_HURT}),
	T_GRENADE_FLASH(new Sound[] {Sound.WOLF_BARK}),
	T_GRENADE_SMOKE(new Sound[] {Sound.VILLAGER_IDLE}),
	T_GRENADE_FIRE(new Sound[] {Sound.WITHER_IDLE}),
	
	CT_START(new Sound[] {Sound.VILLAGER_HIT}),
	T_START(new Sound[] {Sound.VILLAGER_HAGGLE}),
	
	T_BOMB_PLANT(new Sound[] {Sound.ZOMBIE_REMEDY}),
	T_BOMB_DROP(new Sound[] {Sound.ZOMBIE_INFECT}),
	;
	
	private Sound[] _sounds;
	
	Radio(Sound[] sounds)
	{
		_sounds = sounds;
	}
	
	public Sound getSound()
	{
		if (_sounds.length == 1)
			return _sounds[0];
		
		return _sounds[UtilMath.r(_sounds.length - 1)];
	}
}
