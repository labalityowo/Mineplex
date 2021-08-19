package mineplex.core.bonuses.gui;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import mineplex.core.bonuses.BonusManager;
import mineplex.core.bonuses.gui.buttons.CarlSpinButton;
import mineplex.core.bonuses.gui.buttons.ClaimTipsButton;
import mineplex.core.bonuses.gui.buttons.DailyBonusButton;
import mineplex.core.bonuses.gui.buttons.DiscordButton;
import mineplex.core.bonuses.gui.buttons.SocialMediaButton;
import mineplex.core.bonuses.gui.buttons.LevelRewardsButton;
import mineplex.core.bonuses.gui.buttons.PollButton;
import mineplex.core.bonuses.gui.buttons.PowerPlayClubButton;
import mineplex.core.bonuses.gui.buttons.RankBonusButton;
import mineplex.core.bonuses.gui.buttons.VoteButton;
import mineplex.core.bonuses.gui.buttons.YoutubeButton;
import mineplex.core.gui.SimpleGui;
import mineplex.core.thank.ThankManager;
import mineplex.core.treasure.reward.TreasureRewardManager;
import mineplex.core.youtube.YoutubeManager;

public class BonusGui extends SimpleGui
{
	private static final int VOTE_SLOT = 32;
	private static final int RANK_BONUS_SLOT = 10;
	private static final int DAILY_BONUS_SLOT = 12;
	private static final int POLL_SLOT = 28;
	private static final int DISCORD_SLOT = 20;
	private static final int CLAIM_TIPS_SLOT = 30;
	private static final int POWER_PLAY_SLOT = 16;
	private static final int CARL_SPINNER_SLOT = 14;
	private static final int YOUTUBE_SLOT = 24;
	private static final int SOCIAL_MEDIA_SLOT = 34;
	private static final int LEVEL_REWARDS_SLOT = 22;

	private static final int INV_SIZE = 45;

	public BonusGui(Plugin plugin, Player player, BonusManager manager, TreasureRewardManager rewardManager, YoutubeManager youtubeManager, ThankManager thankManager)
	{
		super(plugin, player, player.getName() + "'s Bonuses", INV_SIZE);

		setItem(VOTE_SLOT, new VoteButton(plugin, player, this, manager));
		
		setItem(RANK_BONUS_SLOT, new RankBonusButton(getPlugin(), player, this, manager));
		
		setItem(DAILY_BONUS_SLOT, new DailyBonusButton(getPlugin(), player, this, manager));

		setItem(POLL_SLOT, new PollButton(getPlugin(), player, manager.getPollManager(), manager.getClientManager(), this, manager));

		setItem(SOCIAL_MEDIA_SLOT, new SocialMediaButton(player));

		setItem(YOUTUBE_SLOT, new YoutubeButton(player, youtubeManager));

		setItem(DISCORD_SLOT, new DiscordButton(player));

		setItem(CLAIM_TIPS_SLOT, new ClaimTipsButton(getPlugin(), player, this, manager, thankManager));

		setItem(POWER_PLAY_SLOT, new PowerPlayClubButton(player, manager));

		setItem(CARL_SPINNER_SLOT, new CarlSpinButton(getPlugin(), player, manager, rewardManager));

		setItem(LEVEL_REWARDS_SLOT, new LevelRewardsButton(manager.getLevelingManager(), player));
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
	}
}