package nautilus.game.arcade.game.games.wizards.spells;

import java.util.ArrayList;
import java.util.UUID;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilShapes;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.wizards.Spell;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClickBlock;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

public class SpellRumble extends Spell implements SpellClickBlock, SpellClick
{

	final private BlockFace[] _radial =
		{
				BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH,
				BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST
		};

	public void castSpell(Player player)
	{
		Block block = player.getLocation().add(0, -1, 0).getBlock();

		if (!UtilBlock.solid(block))
		{
			block = block.getRelative(BlockFace.DOWN);
		}

		castSpell(player, block);
	}

	@EventHandler
	public void onDamage(CustomDamageEvent event)
	{
		if (event.GetReason() != null && event.GetReason().equals("Rumble"))
		{
			event.AddKnockback("Rumble", 1);
		}
	}

	@Override
	public void castSpell(final Player player, final Block target)
	{

		if (UtilBlock.solid(target))
		{

			final BlockFace moveDirection = _radial[Math.round(player.getEyeLocation().getYaw() / 45f) & 0x7];
			final int spellLevel = getSpellLevel(player);
			final int damage = 4 + (spellLevel * 2);
			final int maxDist = 10 * spellLevel;

			playBlockEffect(target);

			new BukkitRunnable()
			{
				private Block _currentBlock = target;
				private int _distTravelled = 0;
				private ArrayList<Integer> _effected = new ArrayList<Integer>();
				private ArrayList<Block> _previousBlocks = new ArrayList<Block>();

				private void endRun()
				{
					ArrayList<Block> bs = new ArrayList<Block>();

					BlockFace[] faces = UtilShapes.getSideBlockFaces(moveDirection);

					bs.add(_currentBlock);

					for (int i = 1; i <= Math.min(4, Math.floor(_distTravelled / (8D - spellLevel))) + 1; i++)
					{
						for (int a = 0; a < faces.length; a++)
						{
							Block b = _currentBlock.getRelative(faces[a], i);

							if (UtilBlock.solid(b))
							{
								bs.add(b);
							}
						}
					}

					for (Block block : bs)
					{

						ArrayList<Block> toExplode = new ArrayList<Block>();

						toExplode.add(block);

						for (BlockFace face : new BlockFace[]
							{
									BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.UP,
									BlockFace.DOWN
							})
						{
							if (UtilMath.random.nextBoolean())
							{
								Block b = block.getRelative(face);

								if (b.getType() != Material.AIR && b.getType() != Material.BEDROCK)
								{
									if (!toExplode.contains(b))
									{
										toExplode.add(b);
										b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, b.getTypeId());
									}
								}
							}
						}

						Wizards.getArcadeManager().GetExplosion()
								.BlockExplosion(toExplode, block.getLocation().add(0.5, 0, 0.5), false);

						for (LivingEntity entity : block.getWorld().getEntitiesByClass(LivingEntity.class))
						{
							if (!UtilPlayer.isSpectator(entity))
							{
								if (entity instanceof Tameable)
								{
									AnimalTamer tamer = ((Tameable) entity).getOwner();

									if (tamer != null && tamer == player)
									{
										continue;
									}
								}

								double dist = 999;

								for (Block b : toExplode)
								{
									double currentDist = b.getLocation().add(0.5, 0.5, 0.5).distance(entity.getLocation());

									if (dist > currentDist)
									{
										dist = currentDist;
									}
								}

								if (dist < 2)
								{
									Wizards.getArcadeManager()
											.GetDamage()
											.NewDamageEvent(entity, player, null, DamageCause.ENTITY_EXPLOSION,
													(1 + (spellLevel / 5D)) * (2 - dist), true, true, false, "Rumble Explosion",
													"Rumble Explosion");
								}
							}
						}
					}

					cancel();
				}

				@Override
				public void run()
				{
					if (!player.isOnline() || !Wizards.IsAlive(player))
					{
						endRun();
						return;
					}

					boolean found = false;

					for (int y : new int[]
						{
								0, 1, -1, -2
						})
					{
						if (_currentBlock.getY() + y <= 0)
						{
							continue;
						}

						Block b = _currentBlock.getRelative(moveDirection).getRelative(0, y, 0);

						if (UtilBlock.solid(b) && !UtilBlock.solid(b.getRelative(0, 1, 0)))
						{
							found = true;
							_currentBlock = b;

							break;
						}
					}

					if (!found)
					{
						endRun();
						return;
					}

					ArrayList<Block> effectedBlocks = new ArrayList<Block>();

					BlockFace[] faces = UtilShapes.getSideBlockFaces(moveDirection);

					effectedBlocks.add(_currentBlock);

					playBlockEffect(_currentBlock);

					int size = (int) (Math.min(4, Math.floor(_distTravelled / (8D - spellLevel))) + 1);

					for (int i = 1; i <= size; i++)
					{
						for (int a = 0; a < faces.length; a++)
						{
							Block b = _currentBlock.getRelative(faces[a], i);

							if (UtilBlock.solid(b))
							{
								effectedBlocks.add(b);
								playBlockEffect(b);
							}
						}
					}

					for (Block b : UtilShapes.getDiagonalBlocks(_currentBlock, moveDirection, size - 2))
					{
						if (UtilBlock.solid(b))
						{
							effectedBlocks.add(b);
							playBlockEffect(b);
						}
					}

					_previousBlocks.addAll(effectedBlocks);

					for (Block b : _previousBlocks)
					{
						for (Entity entity : b.getChunk().getEntities())
						{
							if (entity instanceof LivingEntity && player != entity && !_effected.contains(entity.getEntityId()))
							{

								if (entity instanceof Tameable)
								{
									AnimalTamer tamer = ((Tameable) entity).getOwner();

									if (tamer != null && tamer == player)
									{
										continue;
									}
								}

								Location loc = entity.getLocation();

								if (loc.getBlockX() == b.getX() && loc.getBlockZ() == b.getZ())
								{

									if (entity instanceof Player && !Wizards.IsAlive(entity))
									{
										continue;
									}

									double height = loc.getY() - b.getY();
									if (height >= 0 && height <= 3)
									{
										Wizards.Manager.GetDamage().NewDamageEvent((LivingEntity) entity, player, null,
												DamageCause.CONTACT, damage, true, true, false, "Rumble", "Rumble");

										if (entity instanceof Player)
										{
											// Why does Slowing by 2 apply slowness 1?  The world may never know.
											// As such, we subtract one from the spellLevel and ask no more questions
											Wizards.getArcadeManager()
													.GetCondition()
													.Factory()
													.Slow("Rumble", (LivingEntity) entity, player, 3, spellLevel-1, false, false,
															false, false);
										}
									}

									_effected.add(entity.getEntityId());
								}
							}
						}
					}

					_previousBlocks = effectedBlocks;

					if (_distTravelled++ >= maxDist)
					{
						endRun();
					}
				}
			}.runTaskTimer(Wizards.getArcadeManager().getPlugin(), 5, 1);

			charge(player);
		}
	}

	private void playBlockEffect(Block block)
	{
		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
		Block b = block.getRelative(BlockFace.UP);

		if (UtilBlock.airFoliage(b))
		{
			b.breakNaturally();
		}
		/*

		final int entityId = UtilEnt.getNewEntityId();

		PacketPlayOutSpawnEntity fallingSpawn = new PacketPlayOutSpawnEntity();
		fallingSpawn.a = entityId;
		fallingSpawn.b = (block.getX() * 32) + 16;
		fallingSpawn.c = (block.getY() * 32) + 4;
		fallingSpawn.d = (block.getZ() * 32) + 16;
		fallingSpawn.i = 70;
		fallingSpawn.k = block.getTypeId() | block.getData() << 16;
		fallingSpawn.f = 10000;
		fallingSpawn.uuid = UUID.randomUUID();

		final Collection<? extends Player> players = Bukkit.getOnlinePlayers();

		for (Player player : players)
		{
		UtilPlayer.sendPacket(player, fallingSpawn);
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(Wizards.getArcadeManager().getPlugin(), new Runnable()
		{
		public void run()
		{
		PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(new int[]
		{
		entityId
		});

		for (Player player : players)
		{
		UtilPlayer.sendPacket(player, destroyPacket);
		}
		}
		}, 15);*/
	}
}