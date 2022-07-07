package mineplex.core.texttutorial;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.Managers;
import mineplex.core.MiniPlugin;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.Callback;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.donation.DonationManager;
import mineplex.core.task.TaskManager;
import mineplex.core.texttutorial.tutorial.Tutorial;
import mineplex.core.texttutorial.tutorial.TutorialData;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.visibility.VisibilityManager;

public class TextTutorialManager extends MiniPlugin
{
	private DonationManager _donationManager;
	private TaskManager _taskManager;

	private HashSet<Tutorial> _tutorials;

	public TextTutorialManager(JavaPlugin plugin, DonationManager donationManager, TaskManager taskManager)
	{
		super("Text Tutorial", plugin);

		_donationManager = donationManager;
		_taskManager = taskManager;
		
		_tutorials = new HashSet<>();
	}

	public void addTutorial(Tutorial tutorial)
	{
		_tutorials.add(tutorial);
	}
	
	@EventHandler
	public void startTutorial(PlayerInteractEntityEvent event)
	{
		if (isInTutorial(event.getPlayer()))
			return;

		if (!(event.getRightClicked() instanceof LivingEntity))
			return;

		VisibilityManager vm = Managers.require(VisibilityManager.class);
		LivingEntity ent = (LivingEntity)event.getRightClicked();

		String name = ent.getCustomName();
		if (name == null)
			return;

		for (Tutorial tut : _tutorials)
		{
			if (name.contains(tut.getName()))
			{
				UtilPlayer.message(event.getPlayer(), F.main("Tutorial", "You started " + F.elem(tut.getName()) + "."));
				tut.startTutorial(event.getPlayer());
				
				for (Player other : Bukkit.getOnlinePlayers())
				{
					vm.hidePlayer(other, event.getPlayer(), "Core Text Tutorial");
				}

				((CraftPlayer) event.getPlayer()).getHandle().spectating = true;
				
				return;
			}
		}
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		
		for (final Tutorial tut : _tutorials)
		{
			Iterator<TutorialData> iterator = tut.getTutorialDatas().iterator();
			while (iterator.hasNext())
			{
				TutorialData data = iterator.next();
				final Player player = data.getPlayer();

				//Check if Phase Completed
				if (data.tick())
				{
					//Next Phase
					if (data.getPhaseStep() < tut.getPhaseSize())
					{
						data.setNextPhase(tut.getPhase(data.getPhaseStep()));
					}
					//End Tutorial
					else
					{
						iterator.remove();
						tut.stopTutorial(player);
						
						//Inform
						UtilPlayer.message(player, F.main("Tutorial", "You completed " + F.elem(tut.getName()) + "."));

						for (Player other : Bukkit.getOnlinePlayers())
						{
							vm.showPlayer(other, player, "Core Text Tutorial");
						}

						((CraftPlayer) player).getHandle().spectating = false;

						//Gems
						if (tut.getGemReward() > 0)
						{
							if (!_taskManager.hasCompletedTask(player, tut.getTaskId()))
							{
								_taskManager.completedTask(new Callback<Boolean>()
								{
									public void run(Boolean completed)
									{
										_donationManager.rewardCurrency(GlobalCurrency.GEM, player, "Tutorial " + tut.getName(), tut.getGemReward(), success ->
										{
											if (completed)
											{
												UtilPlayer.message(player, F.main("Tutorial", "You received " + F.elem(C.cGreen + tut.getGemReward() + " Gems") + "."));

												//Sound
												player.playSound(player.getLocation(), Sound.LEVEL_UP, 2f, 1.5f);
											}
										});
									}
								}, player, tut.getTaskId());
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void hidePlayer(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		VisibilityManager vm = Managers.require(VisibilityManager.class);
		
		for (Player other : Bukkit.getOnlinePlayers())
		{
			if (isInTutorial(other))
			{
				vm.hidePlayer(player, other, "Core Text Tutorial");
			}
		}
	}
	
	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		for (Tutorial tut : _tutorials)
		{
			tut.stopTutorial(event.getPlayer());
		}
	}

	@EventHandler
	public void cancelInteract(PlayerInteractEvent event)
	{
		if (isInTutorial(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}

	public boolean isInTutorial(Player player)
	{
		for (Tutorial tutorial : _tutorials)
		{
			if (tutorial.isInTutorial(player))
			{
				return true;
			}
		}

		return false;
	}
}