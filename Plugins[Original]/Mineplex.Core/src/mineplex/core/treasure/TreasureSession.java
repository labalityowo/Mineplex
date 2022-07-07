package mineplex.core.treasure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.common.util.UtilTime;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.reward.Reward;
import mineplex.core.reward.RewardData;
import mineplex.core.reward.rewards.GadgetReward;
import mineplex.core.reward.rewards.TitleReward;
import mineplex.core.reward.rewards.TreasureShardReward;
import mineplex.core.treasure.animation.TreasureAnimation;
import mineplex.core.treasure.animation.TreasureRewardAnimation;
import mineplex.core.treasure.reward.RewardRarity;
import mineplex.core.treasure.types.Treasure;
import mineplex.core.treasure.util.TreasureUtil;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class TreasureSession implements Listener
{

	private static final double CHEST_RADIUS = 3.5;
	private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(1);

	private final Player _player;
	private final TreasureLocation _treasureLocation;
	private final Treasure _treasure;
	private final TreasureAnimation _animation;
	private final List<TreasureRewardAnimation> _rewardAnimations;
	private final List<Reward> _rewards;
	private final List<RewardData> _rewardData;
	private final List<Location> _openedChests;
	private final long _start;

	TreasureSession(Player player, TreasureLocation treasureLocation, Treasure treasure)
	{
		_player = player;
		_treasureLocation = treasureLocation;
		_treasure = treasure;
		_animation = treasure.getAnimations(treasureLocation);
		_rewardAnimations = new ArrayList<>();
		_rewards = Treasure.getRewardManager().getRewards(player, treasure);
		_rewardData = new ArrayList<>(treasure.getRewardsPerChest());
		_openedChests = new ArrayList<>(treasure.getRewardsPerChest());
		_start = System.currentTimeMillis();
	}

	public void register()
	{
		_rewards.forEach(reward -> reward.giveReward(_player, _rewardData::add));
		_animation.setRunning(true);
		UtilServer.RegisterEvents(this);
	}

	@EventHandler
	public void chestInteract(PlayerInteractEvent event)
	{
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
		{
			return;
		}

		Player player = event.getPlayer();
		Location location = event.getClickedBlock().getLocation();

		boolean found = false;

		for (Location chestLocation : _treasureLocation.getChestLocations())
		{
			if (!chestLocation.getBlock().equals(location.getBlock()))
			{
				continue;
			}

			found = true;
			break;
		}

		if (!found)
		{
			return;
		}

		event.setCancelled(true);

		if (!_player.equals(player) || _openedChests.contains(location) || isDone())
		{
			return;
		}

		RewardData rewardData = _rewardData.get(_openedChests.size());
		Reward reward = _rewards.get(_openedChests.size());
		RewardRarity rarity = rewardData.getRarity();
		TreasureRewardAnimation rewardAnimation = TreasureRewardAnimation.getAnimationFor(_treasure, _treasureLocation, location.clone().add(0.5, 1, 0.5), rewardData);

		_openedChests.add(location);

		Material material = event.getClickedBlock().getType();

		if (material == Material.CHEST || material == Material.TRAPPED_CHEST || material == Material.ENDER_CHEST)
		{
			TreasureUtil.playChestOpen(location, true);
		}

		if (rarity.ordinal() >= RewardRarity.RARE.ordinal())
		{
			boolean an = UtilText.startsWithVowel(rewardData.getFriendlyName());

			String message = F.name(player.getName()) + " found " + (an ? "an" : "a") + " " + F.name(rarity.getColor() + rarity.getName()) + " " + F.name(rewardData.getFriendlyName());

			Reward actualReward = reward;
			String type = null;

			if (reward instanceof TreasureShardReward)
			{
				actualReward = ((TreasureShardReward) reward).getOtherReward();
			}

			// Add reward type to chat where possible
			if (actualReward instanceof GadgetReward)
			{
				GadgetType gadgetType = ((GadgetReward) actualReward).getGadget().getGadgetType();

				// Don't set type if the reward contains the first word in the gadget's singular name.
				// This will catch "arrow" from "arrow trail", "death" from "death effect", etc.
				// It's possible this will have some false positives.
				if (rewardData.getFriendlyName().toLowerCase().contains(gadgetType.getSingularType().toLowerCase().split(" ")[0]))
				{
					type = null;
				}
				else
				{
					type = gadgetType.getSingularType();
				}
			}
			else if (actualReward instanceof TitleReward)
			{
				type = "Title";
			}

			if (type != null && !rewardData.getFriendlyName().toLowerCase().contains(type.toLowerCase()))
			{
				message += C.cGray + " (" + C.cAqua + type + C.cGray + ")";
			}

			message += C.cGray + ".";

			Bukkit.broadcastMessage(F.main(_treasureLocation.getManager().getName(),  message));
		}

		if (rewardAnimation != null)
		{
			_rewardAnimations.add(rewardAnimation);
			rewardAnimation.setRunning(true);
		}

		if (isDone())
		{
			UtilServer.runSyncLater(this::cleanup, 5 * 20);
		}
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		if (!event.getPlayer().equals(getPlayer()))
		{
			return;
		}

		cleanup();
	}

	public void cleanup()
	{
		UtilServer.Unregister(this);
		_animation.cleanup();
		_rewardAnimations.forEach(TreasureRewardAnimation::cleanup);
		_treasureLocation.cleanup();
	}

	@EventHandler
	public void updateAnimation(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_rewardAnimations.stream()
				.filter(TreasureAnimation::isRunning)
				.forEach(TreasureAnimation::run);

		if (_animation.isRunning())
		{
			_animation.run();
		}
	}

	@EventHandler
	public void updateEntities(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		pushEntitiesAway();
	}

	void pushEntitiesAway()
	{
		for (LivingEntity entity : UtilEnt.getInRadius(_treasureLocation.getChest(), CHEST_RADIUS).keySet())
		{
			if (entity.equals(_player) || entity instanceof ArmorStand || _animation.getEntities().contains(entity))
			{
				continue;
			}

			UtilAction.velocity(entity, UtilAlg.getTrajectory(_treasureLocation.getChest(), entity.getLocation()).setY(1));
		}
	}

	@EventHandler
	public void playerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();

		if (!player.equals(getPlayer()))
		{
			return;
		}

		if (UtilMath.offset2dSquared(player.getLocation(), _treasureLocation.getChest()) > CHEST_RADIUS * CHEST_RADIUS)
		{
			event.setTo(_treasureLocation.getChest());
		}
	}

	@EventHandler
	public void updateTimeout(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW || !UtilTime.elapsed(_start, TIMEOUT))
		{
			return;
		}

		getPlayer().sendMessage(F.main(_treasureLocation.getManager().getName(), "You took too long opening your chest. I picked your rewards for you:"));

		for (RewardData rewardData : _rewardData)
		{
			getPlayer().sendMessage(F.main(_treasureLocation.getManager().getName(), rewardData.getHeader() + " : " + rewardData.getFriendlyName()));
		}

		cleanup();
	}

	public Player getPlayer()
	{
		return _player;
	}

	public Treasure getTreasure()
	{
		return _treasure;
	}

	public List<Reward> getRewards()
	{
		return _rewards;
	}

	boolean hasFailed()
	{
		return _rewards == null;
	}

	private boolean isDone()
	{
		return _openedChests.size() == _treasure.getRewardsPerChest();
	}
}
