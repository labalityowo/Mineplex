package nautilus.game.arcade.game.games.typewars.kits;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.typewars.Spell;
import nautilus.game.arcade.game.games.typewars.spells.SpellFirebomb;
import nautilus.game.arcade.game.games.typewars.spells.SpellKillEverything;
import nautilus.game.arcade.game.games.typewars.spells.SpellSniper;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDummy;

public class KitTyper extends KitTypeWarsBase
{

	private static final Perk[] PERKS =
			{
					new PerkDummy("Fire Bomb", new String[]{"Kills small and medium sized enemies."}),
					new PerkDummy("Sniper spell", new String[]{"Shoot a minion and kill it"}),
					new PerkDummy("Zombie Smash", new String[]{"Kill all enemy minions. One use."})
			};

	public KitTyper(ArcadeManager manager)
	{
		super(manager, GameKit.TYPE_WARS_TYPER, PERKS,
				new Spell[]
						{
								new SpellFirebomb(manager), new SpellSniper(manager), new SpellKillEverything(manager)
						});
	}

}
