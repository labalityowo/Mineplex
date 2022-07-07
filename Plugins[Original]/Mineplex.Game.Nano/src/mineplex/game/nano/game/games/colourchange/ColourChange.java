package mineplex.game.nano.game.games.colourchange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import mineplex.core.common.util.C;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilColor;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.SoloGame;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;

public class ColourChange extends SoloGame
{

	private final Map<Location, DyeColor> _tileMap;
	private final DyeColor[] _colourPool = new DyeColor[]
			{
					DyeColor.RED,
					DyeColor.YELLOW,
					DyeColor.GREEN,
					DyeColor.BLUE,
					DyeColor.ORANGE,
					DyeColor.LIME,
					DyeColor.LIGHT_BLUE,
					DyeColor.GRAY,
					DyeColor.WHITE,
					DyeColor.PINK,
					DyeColor.PURPLE,
			};
	private final List<DyeColor> _usableColours;
	private final BlockFace[] _faces = new BlockFace[]
			{
					BlockFace.SELF,
					BlockFace.NORTH,
					BlockFace.EAST,
					BlockFace.SOUTH,
					BlockFace.WEST,
					BlockFace.NORTH_WEST,
					BlockFace.NORTH_EAST,
					BlockFace.SOUTH_WEST,
					BlockFace.SOUTH_EAST
			};

	private int _minY;
	private int _rounds;
	private long _roundTime = TimeUnit.SECONDS.toMillis(5), _roundEndTime = TimeUnit.SECONDS.toMillis(3);
	private long _lastRoundStart, _lastRoundEnd;
	private DyeColor _target;
	private ChatColor _targetChat;
	private String _targetName;
	private boolean _roundOver;

	public ColourChange(NanoManager manager)
	{
		super(manager, GameType.COLOUR_CHANGE, new String[]
				{
						C.cYellow + "Stand" + C.Reset + " on the color shown.",
						"All others " + C.cRed + "Disappear" + C.Reset + " after a few seconds.",
						C.cYellow + "Last player" + C.Reset + " standing wins!"
				});

		_usableColours = new ArrayList<>(_colourPool.length);
		_tileMap = new HashMap<>();

		_prepareComponent.setPrepareFreeze(false);

		_damageComponent.setPvp(false);

		_endComponent.setTimeout(TimeUnit.MINUTES.toMillis(2));
	}

	@Override
	protected void parseData()
	{
		for (int i = 0; i < 4; i++)
		{
			_usableColours.add(_colourPool[i]);
		}

		_roundOver = true;

		List<Location> tiles = _mineplexWorld.getIronLocations("RED");
		_minY = tiles.get(0).getBlockY() + 1;

		tiles.forEach(location ->
		{
			_tileMap.put(location, null);
			setTile(location, UtilMath.randomElement(_colourPool));
		});
	}

	@Override
	public void disable()
	{
		_usableColours.clear();
		_tileMap.clear();
	}

	@EventHandler
	public void combatDeath(CombatDeathEvent event)
	{
		event.getPlayersToInform().clear();
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK || !isLive())
		{
			return;
		}

		Player[] players = getAlivePlayers().toArray(new Player[0]);

		if (_roundOver)
		{
			// Round over
			if (UtilTime.elapsed(_lastRoundEnd, _roundEndTime))
			{
				generateFloor();
				_target = randomColour();
				_targetChat = UtilColor.woolDataToChatColor(_target.getWoolData());
				_targetName = _target.toString().replace("_", " ");

				ItemStack itemStack = new ItemBuilder(Material.WOOL, _target.getWoolData())
						.setTitle(_targetChat + C.Bold + _targetName)
						.build();

				for (Player player : players)
				{
					PlayerInventory inventory = player.getInventory();

					for (int i = 0; i < 9; i++)
					{
						inventory.setItem(i, itemStack);
					}
				}

				_rounds++;
				_lastRoundStart = System.currentTimeMillis();
				_roundOver = false;

				if (_roundTime > 1000)
				{
					_roundTime -= 200;
				}

				if (_roundEndTime > 1500)
				{
					_roundEndTime -= 100;
				}

				for (Player player : players)
				{
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
				}

				UtilTextMiddle.display(null, _targetChat + C.Bold + _targetName, 10, 20, 10, players);

				if (_rounds % 2 == 0 && _usableColours.size() < _colourPool.length)
				{
					_usableColours.add(_colourPool[_usableColours.size()]);
				}
			}
			else
			{
				for (Player player : players)
				{
					if (player.getLocation().getBlockY() < _minY)
					{
						UtilFirework.launchFirework(player.getLocation(), FireworkEffect.builder()
								.with(UtilMath.randomElement(Type.values()))
								.withColor(_target.getColor())
								.build(), null, 1);
						_manager.getDamageManager().NewDamageEvent(player, null, null, DamageCause.CUSTOM, 500, false, true, true, getGameType().getName(), null);
					}
				}
			}
		}
		else
		{
			long diff = System.currentTimeMillis() - _lastRoundStart;

			if (diff > _roundTime)
			{
				_tileMap.forEach((location, colour) ->
				{
					if (!_target.equals(colour))
					{
						setTile(location, null);
					}
				});

				for (Player player : players)
				{
					player.playSound(player.getLocation(), Sound.NOTE_SNARE_DRUM, 1, 0.2F);
				}
				UtilTextBottom.display(_targetChat + C.Bold + "SWAP!", players);

				_lastRoundEnd = System.currentTimeMillis();
				_roundOver = true;
			}
			else
			{
				diff = _roundTime - diff;

				UtilTextBottom.displayProgress(_targetChat + _targetName, (double) diff / _roundTime, UtilTime.MakeStr(Math.max(0, diff)), players);
			}
		}
	}

	private void generateFloor()
	{
		_tileMap.entrySet().forEach(entry ->
		{
			DyeColor colour = randomColour();

			setTile(entry.getKey(), colour);
			entry.setValue(colour);
		});
	}

	private void setTile(Location tile, DyeColor colour)
	{
		for (BlockFace face : _faces)
		{
			Location location = tile.clone().add(face.getModX(), 0, face.getModZ());

			if (colour == null)
			{
				MapUtil.QuickChangeBlockAt(location, Material.AIR);
			}
			else
			{
				MapUtil.QuickChangeBlockAt(location, Material.WOOL, colour.getWoolData());
			}
		}
	}

	private DyeColor randomColour()
	{
		return UtilAlg.Random(_usableColours);
	}
}
