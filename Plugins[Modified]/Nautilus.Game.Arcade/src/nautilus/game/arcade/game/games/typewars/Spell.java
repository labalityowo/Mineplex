package nautilus.game.arcade.game.games.typewars;

import java.util.ArrayList;
import java.util.HashMap;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.recharge.Recharge;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.GameTeam;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public abstract class Spell
{
	
	private ArrayList<Player> _playerUses;
	
	private ArcadeManager _manager;
	private TypeWars _typeWars;
	private String _name;
	private int _cost;
	private Material _material;
	private Long _recharge;
	private boolean _singleUse;
	private long _updateDelay;
	
	private HashMap<GameTeam, Long> _lastUsed;
	private long _useDelay;
	
	public Spell(ArcadeManager manager, String name, int cost, Material material, Long recharge, int updateDelay, long useDelay, boolean singleUse)
	{
		_manager = manager;
		_name = name;
		_cost = cost;
		_material = material;
		_recharge = recharge;
		_singleUse = singleUse;
		_updateDelay = updateDelay;
		_playerUses = new ArrayList<>();
		_lastUsed = new HashMap<>();
		_updateDelay = useDelay;
	}
	
	public void prepareExecution(final Player player)
	{	
		if(isSingleUse())
		{
			if(_playerUses.contains(player))
			{
				UtilTextMiddle.display("", ChatColor.GRAY + "You can't use this spell anymore.", player);
				return;
			}
		}
		GameTeam team = getManager().GetGame().GetTeam(player);
		if(_lastUsed.containsKey(team))
		{
			if(!UtilTime.elapsed(_lastUsed.get(team), _useDelay))
			{
				UtilTextMiddle.display("", ChatColor.GRAY + "This Spell cant be used at the moment.", player);
				return;
			}
		}
		_lastUsed.put(team, System.currentTimeMillis());
		
		_typeWars = (TypeWars) _manager.GetGame();
		if(_typeWars.getMoneyMap().get(player) < getCost())
		{
			UtilTextMiddle.display("", ChatColor.GRAY + "You dont have enough Money to use that spell.", player);
			return;
		}
		if(!Recharge.Instance.usable(player, getName(), true))
			return;
		
		UtilTextMiddle.display(ChatColor.GREEN + "-$" + getCost(), ChatColor.GRAY + "You used " + F.game(getName()), player);
		final Spell spell = this;
		
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				Location loc = player.getLastTwoTargetBlocks(UtilBlock.blockAirFoliageSet, 80).get(0).getLocation().add(0.5, 0.5, 0.5);
				if(trail() != null)
				{
					int i = 0;
					for (Location location : UtilShapes.getLinesDistancedPoints(player.getEyeLocation().subtract(0, 0.1, 0), loc, 0.6))
					{	
						if(getManager().GetGame().GetTeam(player).GetColor() == ChatColor.RED)
						{
							UtilParticle.PlayParticle(trail(), location, 0, 0, 0, 0, 1, ViewDist.NORMAL, UtilServer.getPlayers());
						}
						else
						{
							UtilParticle.PlayParticle(trail(), location, -1, 1, 1, 1, 0, ViewDist.NORMAL, UtilServer.getPlayers());
						}
						trailAnimation(location, i);
						location.getWorld().playSound(location, sound(), 1, 1);
						i++;
						if(i > 30)
							i = 0;
						try
						{
							Thread.sleep(_updateDelay);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
				if(hit() != null)
					UtilParticle.PlayParticle(hit(), loc, 0, 0, 0, 0, 1, ViewDist.NORMAL, UtilServer.getPlayers());
				
				if(execute(player, loc))
				{
					Recharge.Instance.use(player, getName(), getRecharge(), false, true);
					int money = ((TypeWars) _manager.GetGame()).getMoneyMap().get(player);
					((TypeWars) _manager.GetGame()).getMoneyMap().put(player, money - getCost());
					if(!_playerUses.contains(player))
						_playerUses.add(player);
					
					Bukkit.getPluginManager().callEvent(new ActivateSpellEvent(player, spell));
					return;
				}
				else
				{
					UtilPlayer.message(player, F.main("Game", "Error while using spell."));
					return;
				}
			}
			
		}).start();
		
	}
	
	public ParticleType trail()
	{
		return ParticleType.RED_DUST;
	}
	
	public ParticleType hit()
	{
		return ParticleType.EXPLODE;
	}
	
	public Sound sound()
	{
		return Sound.CLICK;
	}

	public boolean hasUsed(Player player)
	{
		return _playerUses.contains(player);
	}
	
	public void trailAnimation(Location location, int frame) {}
	
	public abstract boolean execute(Player player, Location location);
	
	public ArcadeManager getManager()
	{
		return _manager;
	}
	
	public TypeWars getTypeWars()
	{
		return _typeWars;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getCost()
	{
		return _cost;
	}
	
	public Material getMaterial()
	{
		return _material;
	}
	
	public Long getRecharge()
	{
		return _recharge;
	}
	
	public boolean isSingleUse()
	{
		return _singleUse;
	}
	
}
