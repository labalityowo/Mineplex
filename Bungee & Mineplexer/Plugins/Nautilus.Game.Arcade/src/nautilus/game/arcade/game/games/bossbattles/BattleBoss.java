package nautilus.game.arcade.game.games.bossbattles;

import mineplex.minecraft.game.core.boss.broodmother.SpiderBoss;
import mineplex.minecraft.game.core.boss.ironwizard.GolemBoss;
import mineplex.minecraft.game.core.boss.slimeking.SlimeBoss;
import mineplex.minecraft.game.core.boss.snake.SnakeBoss;
import nautilus.game.arcade.game.games.bossbattles.displays.*;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum BattleBoss
{
	GOLEM(GolemBoss.class, IronWizardDisplay.class),

	SNAKE(SnakeBoss.class, SnakeDisplay.class),

	SLIME(SlimeBoss.class, SlimeKingDisplay.class),

	SPIDER(SpiderBoss.class, SpiderDisplay.class);

	private Class _bossClass;
	private Class<? extends BossDisplay> _bossDisplay;

	private BattleBoss(Class bossClass,
			Class<? extends BossDisplay> displayClass)
	{
		_bossClass = bossClass;
		_bossDisplay = displayClass;
	}

	public Class getBoss()
	{
		return _bossClass;
	}

	public Class<? extends BossDisplay> getBossDisplay()
	{
		return _bossDisplay;
	}

}