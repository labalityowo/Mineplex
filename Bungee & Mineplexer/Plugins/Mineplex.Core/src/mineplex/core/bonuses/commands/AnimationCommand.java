package mineplex.core.bonuses.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import mineplex.core.bonuses.BonusManager;
import mineplex.core.command.CommandBase;
import mineplex.core.reward.RewardType;

public class AnimationCommand extends CommandBase<BonusManager>{

	private BonusManager _plugin; 

	public AnimationCommand(BonusManager plugin)
	{
		super(plugin, BonusManager.Perm.ANIMATION_COMMAND, "animation");
		_plugin = plugin;
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		caller = Bukkit.getPlayer(args[0]);
		//plugin.addPendingExplosion(caller, _plugin.getRewardManager().nextReward(caller, null, false, RewardType.SPINNER_FILLER, true));

		if (args.length >= 2)
		{
			_plugin.addPendingExplosion(caller, args[1]);
		}

	}
}