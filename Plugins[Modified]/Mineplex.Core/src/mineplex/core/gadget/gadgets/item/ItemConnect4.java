package mineplex.core.gadget.gadgets.item;

import java.time.Month;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.event.GadgetBlockEvent;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ItemConnect4 extends GameItemGadget
{

	private static final long COOLDOWN_USE = TimeUnit.SECONDS.toMillis(20);
	private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(5);

	private static final int ROWS = 6;
	private static final int COLUMNS = 7;

	private final Set<GameBoard> _gameBoards;

	public ItemConnect4(GadgetManager manager)
	{
		super(manager, "Connect 4", new String[]
				{
						C.cGray + "Play Connect 4",
						C.cGray + "with other players!",
						C.blankLine,
						C.cWhite + "Left click the quartz block",
						C.cWhite + "to place a counter in that",
						C.cWhite + "column",
						C.cWhite + "First person to get 4",
						C.cWhite + "in a row"
				}, CostConstants.POWERPLAY_BONUS, Material.STAINED_CLAY, (byte) 4, null);

		setPPCYearMonth(YearMonth.of(2018, Month.MARCH));
		Free = false;
		_gameBoards = new HashSet<>();
	}

	public boolean activatePreprocess(Player player)
	{
		for (GameBoard board : _gameBoards)
		{
			if (board.PlayerA.equals(player) || board.PlayerB.equals(player))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		super.disableCustom(player, message);

		remove(player);
	}

	private void remove(Player player)
	{
		_gameBoards.removeIf(board ->
		{
			if (board.PlayerA.equals(player) || board.PlayerB.equals(player))
			{
				cleanupBoard(board, false);
				return true;
			}

			return false;
		});

		_invites.remove(player.getName());
	}

	@EventHandler
	public void updateTimeout(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOWER)
		{
			return;
		}

		_gameBoards.removeIf(board ->
		{
			if (UtilTime.elapsed(board.StartTime, TIMEOUT))
			{
				cleanupBoard(board, false);
				return true;
			}

			return false;
		});
	}

	@Override
	protected void startGame(Player invitee, Player inviter)
	{
		Location location = inviter.getLocation();
		GameBoard board = new GameBoard(inviter, invitee, location);

		if (!Manager.selectLocation(this, location) || !Manager.selectBlocks(this, board.Blocks) || board.Blocks.stream()
				.anyMatch(block -> block.getType() != Material.AIR))
		{
			String message = F.main(Manager.getName(), "You must find a more open area to play " + F.name(getName()));
			invitee.sendMessage(message);
			inviter.sendMessage(message);
			return;
		}

		board.Blocks.forEach(block -> Manager.getBlockRestore().restore(block));
		board.Ceiling.forEach(block -> MapUtil.QuickChangeBlockAt(block.getLocation(), Material.QUARTZ_BLOCK));
		board.Floor.forEach(block -> MapUtil.QuickChangeBlockAt(block.getLocation(), Material.QUARTZ_BLOCK));

		location.setYaw(0);

		{
			Location teleport = location.clone().add(0, 0, 2);
			teleport.setYaw(180);
			inviter.teleport(teleport);
		}
		{
			Location teleport = location.clone().subtract(0, 0, 2);
			teleport.setYaw(0);
			invitee.teleport(teleport);
		}

		_gameBoards.add(board);
	}

	@EventHandler
	public void blockInteract(PlayerInteractEvent evnet)
	{
		Block block = evnet.getClickedBlock();

		if (block == null)
		{
			return;
		}

		Player player = evnet.getPlayer();

		for (GameBoard board : _gameBoards)
		{
			if (!board.AllowInteract)
			{
				continue;
			}

			boolean playerA = board.PlayerA.equals(player);
			boolean playerB = board.PlayerB.equals(player);
			boolean playerATurn = board.Turn % 2 == 0;
			int column = board.Floor.indexOf(block);

			if (!playerA && !playerB || column == -1)
			{
				continue;
			}
			else if (playerA && !playerATurn || playerB && playerATurn)
			{
				player.sendMessage(F.main(Manager.getName(), "It's not your turn."));
				continue;
			}

			board.AllowInteract = false;

			Manager.runSyncTimer(new BukkitRunnable()
			{
				Block top = block.getRelative(0, ROWS + 1, 0);
				int row = 0;

				@Override
				public void run()
				{
					if (!isActive(board.PlayerA))
					{
						cancel();
						return;
					}

					if (row != 0)
					{
						MapUtil.QuickChangeBlockAt(top.getLocation(), Material.AIR);
					}

					top = top.getRelative(BlockFace.DOWN);
					Location location = top.getLocation().add(0.5, 0.5, 0.5);

					if (top.getType() != Material.AIR)
					{
						if (row == 0)
						{
							board.AllowInteract = true;
							player.sendMessage(F.main("Game", "That column is full."));
							cancel();
							return;
						}

						top = top.getRelative(BlockFace.UP);
						location = top.getLocation().add(0.5, 0.5, 0.5);
						board.OwnedTiles[--row][column] = playerATurn ? 1 : 2;
						location.getWorld().playEffect(location, Effect.STEP_SOUND, playerATurn ? Material.REDSTONE_BLOCK : Material.GOLD_BLOCK);
						cancel();

						if (board.nextTurn())
						{
							return;
						}
					}

					MapUtil.QuickChangeBlockAt(location, Material.STAINED_CLAY, (byte) (playerATurn ? 14 : 4));
					row++;
				}
			}, 0, 4);
		}
	}

	private void cleanupBoard(GameBoard board, boolean remove)
	{
		Recharge.Instance.use(board.PlayerA, getName(), COOLDOWN_USE, true, true);
		board.Blocks.forEach(block ->
		{
			Manager.getBlockRestore().restore(block);
			MapUtil.QuickChangeBlockAt(block.getLocation(), Material.AIR);
		});

		if (remove)
		{
			_gameBoards.remove(board);
		}
	}

	@EventHandler
	public void gadgetBlock(GadgetBlockEvent event)
	{
		for (GameBoard board : _gameBoards)
		{
			event.getBlocks().removeIf(board.Blocks::contains);
		}
	}

	private class GameBoard
	{

		final Player PlayerA, PlayerB;
		final int[][] OwnedTiles;
		final Location TopLeft;
		final List<Block> Blocks, Floor, Ceiling;
		final long StartTime;
		int Turn;
		boolean First = true, AllowInteract;

		GameBoard(Player playerA, Player playerB, Location center)
		{
			PlayerA = playerA;
			PlayerB = playerB;

			OwnedTiles = new int[ROWS][COLUMNS];
			for (int[] i : OwnedTiles)
			{
				Arrays.fill(i, 0);
			}

			double xMod = Math.floor(COLUMNS / 2D), yMod = ROWS + 1;
			TopLeft = center.clone().add(-xMod, ROWS, 0);
			Blocks = UtilBlock.getInBoundingBox(center.clone().add(-xMod, 1, 0), center.clone().add(xMod, yMod, 0), false);
			Floor = UtilBlock.getInBoundingBox(center.clone().add(-xMod, 0, 0), center.clone().add(xMod, 0, 0), false);
			Ceiling = UtilBlock.getInBoundingBox(center.clone().add(-xMod, yMod, 0), center.clone().add(xMod, yMod, 0), false);
			Blocks.addAll(Floor);
			Blocks.addAll(Ceiling);

			StartTime = System.currentTimeMillis();

			if (Math.random() > 0.5)
			{
				Turn = 1;
			}

			nextTurn();
		}

		boolean nextTurn()
		{
			boolean playerATurn = Turn % 2 == 0;

			if (hasDrawn())
			{
				drawGame(PlayerA, PlayerB);
				cleanupBoard(this, true);
				return true;
			}
			else if (hasWon(playerATurn ? 1 : 2))
			{
				Player winner = playerATurn ? PlayerA : PlayerB;
				Player loser = playerATurn ? PlayerB : PlayerA;
				endGame(winner, loser);
				Manager.runSyncLater(() -> cleanupBoard(this, true), 60);
				return true;
			}
			else
			{
				Turn++;
				playerATurn = !playerATurn;
			}

			if (First)
			{
				boolean finalPlayerATurn = playerATurn;
				Manager.runSyncLater(() -> informTurn(finalPlayerATurn), 20);
				First = false;
			}
			else
			{
				informTurn(playerATurn);
			}

			AllowInteract = true;
			return false;
		}

		private void informTurn(boolean playerATurn)
		{
			UtilTextMiddle.display("", C.cYellowB + "Your Turn", 5, 15, 5, playerATurn ? PlayerA : PlayerB);
		}

		boolean hasDrawn()
		{
			for (int[] columns : OwnedTiles)
			{
				for (int column : columns)
				{
					if (column == 0)
					{
						return false;
					}
				}
			}

			return true;
		}

		boolean hasWon(int valueToCheck)
		{
			for (int j = 0; j < COLUMNS - 3; j++)
			{
				for (int i = 0; i < ROWS; i++)
				{
					if (OwnedTiles[i][j] == valueToCheck && OwnedTiles[i][j + 1] == valueToCheck && OwnedTiles[i][j + 2] == valueToCheck && OwnedTiles[i][j + 3] == valueToCheck)
					{
						return true;
					}
				}
			}

			for (int i = 0; i < ROWS - 3; i++)
			{
				for (int j = 0; j < COLUMNS; j++)
				{
					if (OwnedTiles[i][j] == valueToCheck && OwnedTiles[i + 1][j] == valueToCheck && OwnedTiles[i + 2][j] == valueToCheck && OwnedTiles[i + 3][j] == valueToCheck)
					{
						return true;
					}
				}
			}

			for (int i = 3; i < ROWS; i++)
			{
				for (int j = 0; j < COLUMNS - 3; j++)
				{
					if (OwnedTiles[i][j] == valueToCheck && OwnedTiles[i - 1][j + 1] == valueToCheck && OwnedTiles[i - 2][j + 2] == valueToCheck && OwnedTiles[i - 3][j + 3] == valueToCheck)
					{
						return true;
					}
				}
			}

			for (int i = 3; i < ROWS; i++)
			{
				for (int j = 3; j < COLUMNS; j++)
				{
					if (OwnedTiles[i][j] == valueToCheck && OwnedTiles[i - 1][j - 1] == valueToCheck && OwnedTiles[i - 2][j - 2] == valueToCheck && OwnedTiles[i - 3][j - 3] == valueToCheck)
					{
						return true;
					}
				}
			}

			return false;
		}
	}
}
