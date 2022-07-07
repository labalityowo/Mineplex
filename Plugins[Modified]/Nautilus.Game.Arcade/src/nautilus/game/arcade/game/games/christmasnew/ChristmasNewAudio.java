package nautilus.game.arcade.game.games.christmasnew;

import mineplex.core.common.util.UtilPlayer.CustomSound;

public enum ChristmasNewAudio implements CustomSound
{

	PK_DEFEATED("pk_defeated"),
	PK_LAUGH("pk_expecting_santa"),
	PK_FALSE_BAN("pk_false_ban"),
	PK_LEARNED("pk_learned_tricks"),
	PK_ANGRY("pk_now_done_it"),
	PK_IT_WAS_ME("pk_yes_it_was_me_nothing_you_can_do_too_late"),

	SANTA_ALMOST_THERE("santa_almost_there"),
	SANTA_MINE("santa_another_one_in_mine_watch_out_monsters"),
	SANTA_CLEAR_PATH("santa_clear_path_through_rocks"),
	SANTA_FOLLOW_ME("santa_follow_me"),
	SANTA_FOLLOW_ME_2("santa_follow_me_find_out_who_behind_this"),
	SANTA_GEN_DESTROYED("santa_generator_destroyed"),
	SANTA_GOOD_JOB("santa_good_job"),
	SANTA_LONGER("santa_great_job_monsters_coming"),
	SANTA_GWEN("santa_gwen_is_you"),
	SANTA_SHIELD("santa_he_has_shield"),
	SANTA_PUMPKIN_CASTLE("santa_jeepers_creepers_pumpkin_kings_castle"),
	SANTA_LETS_GO("santa_lets_go"),
	SANTA_GHASTS("santa_look_ghasts_ghouls"),
	SANTA_TWO_MORE_PRESENTS("santa_look_two_more_presents"),
	SANTA_FAIRIES("santa_move_flame_fairies"),
	SANTA_BRIDGE_OFF("santa_oh_no_magical_bridge_turned_off_defend_generators"),
	SANTA_SPIDERS("santa_spiders_nasty"),
	SANTA_STORM("santa_storm_coming"),
	SANTA_TREES("santa_see_one_in_trees_follow_ornaments"),
	SANTA_STOLE_PRESENTS("santa_so_it_was_you_who_stole_all_of_the_christmas_presents"),
	SANTA_INTRO("santa_thanks_for_coming"),
	SANTA_ICE_MAZE("santa_theres_another_present_hidden_ice_maze"),
	SANTA_VULNERABLE("santa_vulnerable_to_attacks"),
	SANTA_ITS_A_TRAP("santa_watch_out_its_a_trap"),
	SANTA_PURPLE("santa_watch_out_purple_aura"),
	SANTA_PREPARE_FOR_BATTLE("santa_we_will_see_about_that_prepare_for_battle"),
	SANTA_WELL_DONE("santa_well_done"),
	SANTA_YOU_DID_IT("santa_you_did_it"),
	SANTA_BRIDGE_ON("santa_you_did_it_magical_bridge_turned_on")

	;

	private final String _audioPath;

	ChristmasNewAudio(String audioPath)
	{
		_audioPath = audioPath;
	}

	@Override
	public String getAudioPath()
	{
		return _audioPath;
	}
}
