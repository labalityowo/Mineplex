package mineplex.game.nano.game.games.quick;

import mineplex.game.nano.game.games.quick.challenges.ChallengeAvoidTNT;
import mineplex.game.nano.game.games.quick.challenges.ChallengeBlockSnake;
import mineplex.game.nano.game.games.quick.challenges.ChallengeCraftItem;
import mineplex.game.nano.game.games.quick.challenges.ChallengeCrouch;
import mineplex.game.nano.game.games.quick.challenges.ChallengeEnchantItem;
import mineplex.game.nano.game.games.quick.challenges.ChallengeFood;
import mineplex.game.nano.game.games.quick.challenges.ChallengeIgniteTNT;
import mineplex.game.nano.game.games.quick.challenges.ChallengeIntoVoid;
import mineplex.game.nano.game.games.quick.challenges.ChallengeMaths;
import mineplex.game.nano.game.games.quick.challenges.ChallengeMilkCow;
import mineplex.game.nano.game.games.quick.challenges.ChallengePickASide;
import mineplex.game.nano.game.games.quick.challenges.ChallengePlatform;
import mineplex.game.nano.game.games.quick.challenges.ChallengePlayMusic;
import mineplex.game.nano.game.games.quick.challenges.ChallengePole;
import mineplex.game.nano.game.games.quick.challenges.ChallengePunchAPig;
import mineplex.game.nano.game.games.quick.challenges.ChallengeRedBlocks;
import mineplex.game.nano.game.games.quick.challenges.ChallengeReverseRunner;
import mineplex.game.nano.game.games.quick.challenges.ChallengeSlimeJump;
import mineplex.game.nano.game.games.quick.challenges.ChallengeSpeedBridge;
import mineplex.game.nano.game.games.quick.challenges.ChallengeSpin;
import mineplex.game.nano.game.games.quick.challenges.ChallengeSpleef;
import mineplex.game.nano.game.games.quick.challenges.ChallengeStandStill;
import mineplex.game.nano.game.games.quick.challenges.ChallengeSumo;
import mineplex.game.nano.game.games.quick.challenges.ChallengeThrowEggs;
import mineplex.game.nano.game.games.quick.challenges.ChallengeZombies;

public enum ChallengeType
{

	STAND_STILL(ChallengeStandStill.class, "Don't Move!"),
	INTO_VOID(ChallengeIntoVoid.class, "Jump into the Void!"),
	SUMO(ChallengeSumo.class, "Knock other players off the platform!"),
	CRAFT_ITEM(ChallengeCraftItem.class, "Craft the item shown!"),
	ENCHANT_ITEM(ChallengeEnchantItem.class, "Enchant a sword!"),
	RED_BLOCKS(ChallengeRedBlocks.class, "Don't fall into the void!"),
	POLE(ChallengePole.class, "Get as high as you can!"),
	CROUCH(ChallengeCrouch.class, "Crouch/Sneak!"),
	ZOMBIES(ChallengeZombies.class, "Survive the Zombie Horde!"),
	SPLEEF(ChallengeSpleef.class, "Spleef other players into the void!"),
	BLOCK_SNAKE(ChallengeBlockSnake.class, "Avoid the Block Snake!"),
	THROW_EGGS(ChallengeThrowEggs.class, "Throw all your eggs!"),
	PLATFORM(ChallengePlatform.class, "Get on the Platform!"),
	AVOID_TNT(ChallengeAvoidTNT.class, "Avoid the falling TNT!"),
	SPEED_BRIDGE(ChallengeSpeedBridge.class, "Bridge over to the other side!"),
	FOOD(ChallengeFood.class, "Eat until you get full hunger!"),
	SLIME_JUMP(ChallengeSlimeJump.class, "Get on the emerald blocks!"),
	PUNCH_A_PIG(ChallengePunchAPig.class, "Punch 5 Pigs!"),
	IGNITE_TNT(ChallengeIgniteTNT.class, "Ignite the TNT!"),
	SPIN(ChallengeSpin.class, "Spin around as fast as you can!"),
	PLAY_MUSIC(ChallengePlayMusic.class, "Play a song on the noteblocks!"),
	MATHS_QUESTION(ChallengeMaths.class, "Get ready! Solve this math equation..."),
	MILK_A_COW(ChallengeMilkCow.class, "Drink the milk from one of the cows!"),
	PICK_A_SIDE(ChallengePickASide.class, "Stand on the side with the least players."),
	REVERSE_RUNNER(ChallengeReverseRunner.class, "Reverse Runner, Avoid the falling blocks!")

	;

	private final Class<? extends Challenge> _challengeClass;
	private final String _description;

	ChallengeType(Class<? extends Challenge> challengeClass, String description)
	{
		_challengeClass = challengeClass;
		_description = description;
	}

	public Class<? extends Challenge> getChallengeClass()
	{
		return _challengeClass;
	}

	public String getDescription()
	{
		return _description;
	}
}
