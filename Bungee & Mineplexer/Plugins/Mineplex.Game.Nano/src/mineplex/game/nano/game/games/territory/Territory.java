package mineplex.game.nano.game.games.territory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.disguise.disguises.DisguiseSlime;
import mineplex.core.recharge.Recharge;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.GameType;
import mineplex.game.nano.game.ScoredSoloGame;
import mineplex.game.nano.game.event.PlayerGameRespawnEvent;

public class Territory extends ScoredSoloGame
{

	private static final String RECHARGE_KEY = "Inform Move";

	private final BlockFace[] _connectingFaces =
			{
					BlockFace.NORTH,
					BlockFace.SOUTH,
					BlockFace.EAST,
					BlockFace.WEST,
					BlockFace.NORTH_EAST,
					BlockFace.NORTH_WEST,
					BlockFace.SOUTH_EAST,
					BlockFace.SOUTH_WEST
			};
	private final Map<Player, Material> _materials;
	private Material _default;
	private int _materialIndex;

	public Territory(NanoManager manager)
	{
		super(manager, GameType.TERRITORY, new String[]
				{
						C.cYellow + "Walk over" + C.Reset + " a block to claim it!",
						"You can only claim blocks that are " + C.cRed + "Next To" + C.Reset + " yours",
						"Try to " + C.cGreen + "Cut Off" + C.Reset + " other players.",
						C.cYellow + "Most blocks claimed" + C.Reset + " wins!"
				});

		_materials = new HashMap<>();
		_materialIndex = 1;

		_damageComponent.setPvp(false);
		_damageComponent.setFall(false);

		_endComponent.setTimeout(TimeUnit.MINUTES.toMillis(1));
	}

	@Override
	protected void parseData()
	{
		Block block = _mineplexWorld.getIronLocation("RED").getBlock().getRelative(BlockFace.DOWN);

		_default = block.getType();
		block.setType(Material.AIR);
	}

	@Override
	public void disable()
	{
		_materials.clear();
	}

	@EventHandler
	public void respawn(PlayerGameRespawnEvent event)
	{
		Player player = event.getPlayer();
		DisguiseSlime disguise = new DisguiseSlime(player);

		disguise.setName(event.getTeam().getChatColour() + player.getName());
		disguise.setCustomNameVisible(true);

		getManager().getDisguiseManager().disguise(disguise);

		Material material = null;

		while (material == null || !UtilBlock.fullSolid(material.getId()) || material.equals(_default))
		{
			material = Material.getMaterial(_materialIndex++);
		}

		_materials.put(player, material);

		player.getInventory().setItem(8, new ItemStack(material));

		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		block.setType(material);

		for (BlockFace face : UtilBlock.horizontals)
		{
			block.getRelative(face).setType(material);
		}

		incrementScore(player, 5);
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 254, false, false));
	}

	@EventHandler
	public void playerMove(PlayerMoveEvent event)
	{
		if (!isLive())
		{
			return;
		}

		Player player = event.getPlayer();

		if (UtilPlayer.isSpectator(player))
		{
			return;
		}

		Location from = event.getFrom(), to = event.getTo();

		if (to.getY() > from.getY())
		{
			if (Recharge.Instance.use(player, RECHARGE_KEY, 500, false, false))
			{
				UtilTextMiddle.display(null, C.cRedB + "No Jumping!", 0, 20, 10, player);
			}

			event.setTo(from);
			return;
		}

		if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
		{
			return;
		}

		Material material = _materials.get(player);

		if (material == null)
		{
			return;
		}

		boolean allow = true;
		Block toBlock = event.getTo().getBlock().getRelative(BlockFace.DOWN);

		if (toBlock.getType() == _default)
		{
			for (BlockFace face : _connectingFaces)
			{
				Block next = toBlock.getRelative(face);

				if (next.getType() == material)
				{
					toBlock.setType(material);
					incrementScore(player, 1);
					player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1, 1);
					return;
				}
			}

			allow = false;
		}
		else if (toBlock.getType() != material)
		{
			allow = false;
		}

		if (!allow)
		{
			event.setTo(event.getFrom());

			if (Recharge.Instance.use(player, RECHARGE_KEY, 500, false, false))
			{
				UtilTextMiddle.display(null, C.cRed + "You can only claim blocks next to your own!", 0, 20, 10, player);
				player.sendMessage(F.main(getManager().getName(), "You can only claim blocks that are next to yours!"));
			}
		}
	}
}
