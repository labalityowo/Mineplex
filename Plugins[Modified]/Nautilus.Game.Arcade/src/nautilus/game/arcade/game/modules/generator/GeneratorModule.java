package nautilus.game.arcade.game.modules.generator;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

import mineplex.core.common.util.F;
import mineplex.core.mission.MissionTrackerType;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.modules.Module;
import nautilus.game.arcade.missions.GameMissionTracker;

public class GeneratorModule extends Module
{

	private final List<Generator> _generators;

	public GeneratorModule()
	{
		_generators = new ArrayList<>();
	}

	@Override
	protected void setup()
	{
		getGame().registerMissions(new GameMissionTracker<Game>(MissionTrackerType.GAME_GENERATOR_COLLECT, getGame())
		{
			@EventHandler
			public void generatorCollect(GeneratorCollectEvent event)
			{
				_manager.incrementProgress(event.getPlayer(), event.getGenerator().getType().getItemStack().getAmount(), _trackerType, getGameType(), null);
			}
		});
	}

	@Override
	public void cleanup()
	{
		_generators.clear();
	}

	public GeneratorModule addGenerator(Generator generator)
	{
		_generators.add(generator);
		return this;
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (!getGame().IsLive())
		{
			return;
		}

		if (event.getType() == UpdateType.FAST)
		{
			getGame().CreatureAllowOverride = true;

			for (Generator generator : _generators)
			{
				generator.checkSpawn();
				generator.checkCollect();
				generator.updateName();
			}

			getGame().CreatureAllowOverride = false;
		}
		else if (event.getType() == UpdateType.TICK)
		{
			for (Generator generator : _generators)
			{
				generator.animateHolder();
			}
		}
	}

	@EventHandler
	public void live(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}

		_generators.forEach(Generator::setLastCollect);
	}

	@EventHandler
	public void blockBreak(BlockBreakEvent event)
	{
		Block block = event.getBlock();

		for (Generator generator : _generators)
		{
			if (generator.getBlock().equals(block))
			{
				event.setCancelled(true);
				event.getPlayer().sendMessage(F.main("Game", "You cannot break this block."));
				return;
			}
		}
	}

	@EventHandler
	public void armourStandManipulate(PlayerArmorStandManipulateEvent event)
	{
		for (Generator generator : _generators)
		{
			if (generator.getHolder() != null && generator.getHolder().equals(event.getRightClicked()))
			{
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void armourStandDamage(CustomDamageEvent event)
	{
		for (Generator generator : _generators)
		{
			if (generator.getHolder() != null && generator.getHolder().equals(event.GetDamageeEntity()))
			{
				event.SetCancelled("Generator Holder");
				return;
			}
		}
	}

	public List<Generator> getGenerators()
	{
		return _generators;
	}
}
