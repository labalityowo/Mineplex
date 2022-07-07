package nautilus.game.arcade.game.games.wizards.spells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import nautilus.game.arcade.game.games.wizards.Spell;
import nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SpellNapalm extends Spell implements SpellClick
{
	private HashMap<Material, Material> _glazedBlocks = new HashMap<Material, Material>();

	public SpellNapalm()
	{
		_glazedBlocks.put(Material.STONE, Material.COBBLESTONE);
		_glazedBlocks.put(Material.GRASS, Material.DIRT);
		_glazedBlocks.put(Material.FENCE, Material.NETHER_FENCE);
		_glazedBlocks.put(Material.WOOD_STAIRS, Material.NETHER_BRICK_STAIRS);
		_glazedBlocks.put(Material.SMOOTH_STAIRS, Material.NETHER_BRICK_STAIRS);
		_glazedBlocks.put(Material.SAND, Material.GLASS);
		_glazedBlocks.put(Material.SMOOTH_BRICK, Material.NETHER_BRICK);
		_glazedBlocks.put(Material.LOG, Material.NETHERRACK);
		_glazedBlocks.put(Material.LOG_2, Material.NETHERRACK);
		_glazedBlocks.put(Material.SMOOTH_BRICK, Material.COBBLESTONE);
		_glazedBlocks.put(Material.CLAY, Material.STAINED_CLAY);
	}

	@Override
	public void castSpell(final Player player)
	{
		final int length = 5 + (10 * getSpellLevel(player));

		final Vector vector = player.getLocation().getDirection().normalize().multiply(0.15);

		final Location playerLoc = player.getLocation().add(0, 2, 0);
		final Location napalmLoc = playerLoc.clone().add(playerLoc.getDirection().normalize().multiply(2));

		new BukkitRunnable()
		{

			ArrayList<Block> litOnFire = new ArrayList<Block>();
			HashMap<Block, Double> tempIgnore = new HashMap<Block, Double>();
			double blocksTravelled;
			double size = 1;
			double lastTick;

			public void run()
			{
				Random r = new Random();
				napalmLoc.add(vector);

				if (!UtilBlock.airFoliage(napalmLoc.getBlock()))
				{
					cancel();
					return;
				}

				for (int b = 0; b < size * 20; b++)
				{

					float x = r.nextFloat();
					float y = r.nextFloat();
					float z = r.nextFloat();

					while (Math.sqrt((x * x) + (y * y) + (z * z)) >= 1)
					{
						x = r.nextFloat();
						y = r.nextFloat();
						z = r.nextFloat();
					}

					UtilParticle.PlayParticle(ParticleType.RED_DUST,

							napalmLoc.clone().add(

									(size * (x - 0.5)) / 5,

									(size * (y - 0.5)) / 5,

									(size * (z - 0.5)) / 5),

									-0.3F,

									0.35F + (r.nextFloat() / 8),

									0.1F, 1, 0,
									ViewDist.LONG, UtilServer.getPlayers());
				}

				if (lastTick % 3 == 0)
				{
					for (Entity entity : napalmLoc.getWorld().getEntities())
					{
						if (!UtilPlayer.isSpectator(entity))
						{
							double heat = (size * 1.1) - entity.getLocation().distance(napalmLoc);

							if (heat > 0)
							{
								if (lastTick % 10 == 0 && heat > 0.2)
								{
									if (entity instanceof LivingEntity)
									{
										Wizards.getArcadeManager()
										.GetDamage()
										.NewDamageEvent((LivingEntity) entity, player, null, DamageCause.FIRE,
												heat / 1.5, false, true, true, "Napalm", "Napalm");
									}
									else
									{
										entity.remove();
										continue;
									}
								}

								if (entity instanceof LivingEntity && !UtilPlayer.isSpectator(entity)
										&& entity.getFireTicks() < heat * 40)
								{
									entity.setFireTicks((int) (heat * 40));
								}
							}
						}
					}

					int bSize = (int) Math.ceil(size * 0.75);

					for (int y = -bSize; y <= bSize; y++)
					{
						if (napalmLoc.getBlockY() + y < 256 && napalmLoc.getBlockY() + y > 0)
						{
							for (int x = -bSize; x <= bSize; x++)
							{
								for (int z = -bSize; z <= bSize; z++)
								{
									Block block = napalmLoc.clone().add(x, y, z).getBlock();

									if (litOnFire.contains(block))
									{
										continue;
									}

									if (UtilMath.offset(block.getLocation().add(0.5, 0.5, 0.5), playerLoc) < 2.5)
									{
										continue;
									}

									double heat = bSize - UtilMath.offset(block.getLocation().add(0.5, 0.5, 0.5), napalmLoc);

									if (tempIgnore.containsKey(block))
									{
										if (tempIgnore.remove(block) > heat)
										{
											litOnFire.add(block);
											continue;
										}
									}

									if (heat > 0)
									{
										if (block.getType() != Material.AIR)
										{
											float strength = net.minecraft.server.v1_8_R3.Block.getById(block.getTypeId()).a(
													(net.minecraft.server.v1_8_R3.Entity) null) * 0.7F;

											if (strength <= heat)
											{
												block.setType(Material.AIR);

												block.getWorld().playSound(block.getLocation(), Sound.FIZZ, 1.3F,
														0.6F + ((new Random().nextFloat() - 0.5F) / 3F));
											}
											else if (0.2 <= heat)
											{
												if (_glazedBlocks.containsKey(block.getType()))
												{
													block.setType(_glazedBlocks.get(block.getType()));

													if (block.getType() == Material.STAINED_CLAY)
													{
														block.setData((byte) 8);
													}

													block.getWorld().playSound(block.getLocation(), Sound.FIZZ, 1.3F,
															0.6F + ((new Random().nextFloat() - 0.5F) / 3F));
												}
											}
											else if (strength * 2 > size)
											{
												tempIgnore.put(block, heat);
												continue;
											}
										}

										if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
										{
											if (heat > 1)
											{
												block.setType(Material.AIR);

												block.getWorld().playSound(block.getLocation(), Sound.FIZZ, 1.3F, 0);

												litOnFire.add(block);
											}
										}
										else if (block.getType() == Material.AIR)
										{
											if (UtilMath.random.nextBoolean())
											{
												for (int a = 0; a < 6; a++)
												{
													Block b = block.getRelative(BlockFace.values()[a]);

													if (b.getType() != Material.AIR)
													{
														block.setType(Material.FIRE);
														block.getWorld().playSound(block.getLocation(), Sound.DIG_WOOL, 1.3F,
																0.6F + ((new Random().nextFloat() - 0.5F) / 3F));

														break;
													}
												}
											}

											litOnFire.add(block);
										}
									}
								}
							}
						}
					}

					size = Math.min(8, size + 0.06);
				}

				blocksTravelled += 0.15;

				if (lastTick++ % 8 == 0)
				{
					napalmLoc.getWorld().playSound(napalmLoc, Sound.CAT_HISS, Math.min(0.8F + (float) (size * 0.09F), 1.8f), 0F);
				}

				if (blocksTravelled >= length)
				{
					cancel();
				}
			}
		}.runTaskTimer(Wizards.getArcadeManager().getPlugin(), 0, 1);

		charge(player);
	}
}
