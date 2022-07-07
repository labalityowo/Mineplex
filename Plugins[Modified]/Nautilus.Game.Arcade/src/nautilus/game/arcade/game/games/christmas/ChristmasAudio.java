package nautilus.game.arcade.game.games.christmas;

public enum ChristmasAudio
{
	ELF1("christmas.elf1"),
	ELF2("christmas.elf2"),
	ELF3("christmas.elf3"),
	ELF4("christmas.elf4"),
	ELF5("christmas.elf5"),
	
	GENERAL_LETS_GO("christmas.general_lets_go"),
	GENERAL_FOLLOW_ME("christmas.general_follow_me"),
	GENERAL_WELL_DONE("christmas.general_well_done"),
	GENERAL_COLLECT_PRESENTS("christmas.general_collect_presents"),
	
	INTRO("christmas.intro"),//Thank you for coming! Someone has stolen all of the Christmas Presents! I need your help to get them back!

	P1_A("christmas.p1_a"),//Follow me, lets find out who's behind this!
	P1_B("christmas.p1_b"),//There's some presents up ahead!
	P1_C("christmas.p1_c"),//LOOK OUT! ITS A TRAP!
	P1_D("christmas.p1_d"),//Clear a path through the rocks, and watch out for the undead!
	
	P2_A("christmas.p2_a"),//Oh no, my magic bridge has been turned off!
	P2_B("christmas.p2_b"),//Turn on all four switches to rebuild it!
	P2_C("christmas.p2_c"),//Great job, 3 switches to go!
	P2_D("christmas.p2_d"),//Well done! Only 2 switches left!
	P2_E("christmas.p2_e"),//Great! Just 1 more switch!
	P2_F("christmas.p2_f"),//Excellent work! The bridge is powering up!
	
	P3_BOSS_INTRO("christmas.p3_boss_intro"),
	P3_SHOOT_HEART("christmas.p3_shoot_heart"),
	P3_USE_SWORD("christmas.p3_use_sword"),
	P3_GET_PRESENTS("christmas.p3_get_presents"),
	P3_JOKE("christmas.p3_joke"),
	
	P4_A("christmas.p4_a"),//That wall of ice is blocking our path!
	P4_B("christmas.p4_b"),//Get those presents while i think of a way through...
	P4_C("christmas.p4_c"),//OH NO! ITS A FROST GIANT! KILL IT!
	
	BANTER_A("christmas.banter_a"),//Santa	What is this?! Whos castle is this!!!
	BANTER_B("christmas.banter_b"),//PKing	I will destroy Christmas! Not even your pathetic friends can save it now!
	BANTER_C("christmas.banter_c"),//Santa	Who are you!? 
	BANTER_D("christmas.banter_d"),//PKing	It is me... THE PUMPKIN KING! 
	BANTER_E("christmas.banter_e"),//PKing	Revenge will be mine! You will all die!
	BANTER_F("christmas.banter_f"),//Santa	My friends beat you before, and they'll do it again!
	BANTER_G("christmas.banter_g"),//Santa	Prepare for battle!
	BANTER_H("christmas.banter_h"),//PKing	More like, PREPARE TO DIE!
	
	STAY_ON_RED("christmas.color_red"),
	STAY_ON_YELLOW("christmas.color_yellow"),
	STAY_ON_GREEN("christmas.color_green"),
	STAY_ON_WHITE("christmas.color_white"),
	
	END_WIN("christmas.end_win"),
	END_WIN2("christmas.end_win2"),
	END_LOSE("christmas.end_lose");
	
	private String _name;
	
	ChristmasAudio(String name)
	{
		_name = name;
	}
	
	public String getName()
	{
		return _name;
	}
}
