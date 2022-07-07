package nautilus.game.arcade.game.games.smash.perks;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.disguises.DisguiseInsentient;
import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

public abstract class SmashKit extends Kit
{

	private final Class<? extends DisguiseInsentient> _clazz;
	private String[] _description;

	public SmashKit(ArcadeManager manager, GameKit gameKit, Perk[] perks, Class<? extends DisguiseInsentient> clazz)
	{
		super(manager, gameKit, perks);

		_clazz = clazz;
	}

	public boolean isSmashActive(Player player)
	{
		for (Perk perk : GetPerks())
		{
			if (!(perk instanceof SmashUltimate))
			{
				continue;
			}

			SmashUltimate ultimate = (SmashUltimate) perk;

			if (ultimate.isUsingUltimate(player))
			{
				return true;
			}
		}

		return false;
	}
	
	public void disguise(Player player)
	{
		disguise(player, _clazz);
	}
	
	public void disguise(Player player, Class<? extends DisguiseInsentient> clazz)
	{
		if (clazz == null)
		{
			return;
		}
		
		DisguiseManager disguiseManager = Manager.GetDisguise();
				
		try
		{
			DisguiseInsentient disguise = clazz.getConstructor(Entity.class).newInstance(player);
			GameTeam gameTeam = Manager.GetGame().GetTeam(player);
			
			if (gameTeam != null)
			{
				disguise.setName(gameTeam.GetColor() + player.getName());
			}
			else
			{
				disguise.setName(player.getName());
			}

			disguise.setCustomNameVisible(true);
			disguiseManager.disguise(disguise);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public String[] GetDesc()
	{
		if (_description == null)
		{
			List<String> description = new ArrayList<>();

			description.add(C.cGreen + "Kit - " + C.cWhiteB + GetName());

			for (Perk perk : GetPerks())
			{
				if (!perk.IsVisible())
				{
					continue;
				}

				for (String line : perk.GetDesc())
				{
					description.add(C.cGray + "  " + line);
				}
			}

			_description = description.toArray(new String[description.size()]);
		}

		return _description;
	}

}
