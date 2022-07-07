package mineplex.core.map;

import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.MaterialMapColor;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.WorldMap;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multisets;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class ChunkMapRenderer extends MapRenderer
{
	private final WorldMap worldmap;
	private byte[] colors = new byte[128 * 128 * 128];

	public ChunkMapRenderer(WorldMap worldMap)
	{
		super(false);
		this.worldmap = worldMap;
	}

	public void setupMap(World world)
	{
		/*int i = 1 << worldmap.scale;
		int j = worldmap.centerX;
		int k = worldmap.centerZ;
		int l = MathHelper.floor(0) / i + 64;
		int i1 = MathHelper.floor(0) / i + 64;
		int j1 = 128 / i;

		for (int k1 = l - j1 + 1; k1 < l + j1; k1++)
		{
			int l1 = 255;
			int i2 = 0;
			double d0 = 0.0D;

			for (int j2 = i1 - j1 - 1; j2 < i1 + j1; j2++)
			{
				if ((k1 >= 0) && (j2 >= -1) && (k1 < 128) && (j2 < 128))
				{
					int k2 = k1 - l;
					int l2 = j2 - i1;
					boolean flag = k2 * k2 + l2 * l2 > (j1 - 2) * (j1 - 2);
					int i3 = (j / i + k1 - 64) * i;
					int j3 = (k / i + j2 - 64) * i;
					HashMultiset hashmultiset = HashMultiset.create();
					Chunk chunk = ((CraftWorld) world).getHandle().getChunkAtWorldCoords(new BlockPosition(i3, 0, j3));

					if (!chunk.isEmpty())
					{
						int k3 = i3 & 0xF;
						int l3 = j3 & 0xF;
						int i4 = 0;
						double d1 = 0.0D;

						for (int j4 = 0; j4 < i; j4++)
						{
							for (int k4 = 0; k4 < i; k4++)
							{
								int l4 = chunk.b(j4 + k3, k4 + l3) + 1;
								Block block = Blocks.AIR;
								int i5 = 0;

								if (l4 > 1)
								{
									do
									{
										l4--;
										block = chunk.getType(j4 + k3, l4, k4 + l3);
										i5 = chunk.getData(j4 + k3, l4, k4 + l3);
									}
									while ((block.f(i5) == MaterialMapColor.b) && (l4 > 0));

									if ((l4 > 0) && (block.getMaterial().isLiquid()))
									{
										int j5 = l4 - 1;
										Block block1;
										do
										{
											block1 = chunk.getType(j4 + k3, j5--, k4 + l3);
											i4++;
										}
										while ((j5 > 0) && (block1.getMaterial().isLiquid()));
									}
								}

								d1 += l4 / (i * i);
								hashmultiset.add(block.f(i5));
							}
						}

						i4 /= i * i;
						double d2 = (d1 - d0) * 4.0D / (i + 4) + ((k1 + j2 & 0x1) - 0.5D) * 0.4D;
						byte b0 = 1;

						if (d2 > 0.6D)
						{
							b0 = 2;
						}

						if (d2 < -0.6D)
						{
							b0 = 0;
						}

						MaterialMapColor materialmapcolor = (MaterialMapColor) Iterables.getFirst(
								Multisets.copyHighestCountFirst(hashmultiset), MaterialMapColor.b);

						if (materialmapcolor == MaterialMapColor.n)
						{
							d2 = i4 * 0.1D + (k1 + j2 & 0x1) * 0.2D;
							b0 = 1;
							if (d2 < 0.5D)
							{
								b0 = 2;
							}

							if (d2 > 0.9D)
							{
								b0 = 0;
							}
						}

						d0 = d1;
						if ((j2 >= 0) && (k2 * k2 + l2 * l2 < j1 * j1) && ((!flag) || ((k1 + j2 & 0x1) != 0)))
						{
							byte b1 = colors[(k1 + j2 * 128)];
							byte b2 = (byte) (materialmapcolor.M * 4 + b0);

							if (b1 != b2)
							{
								if (l1 > j2)
								{
									l1 = j2;
								}

								if (i2 < j2)
								{
									i2 = j2;
								}

								colors[(k1 + j2 * 128)] = b2;
							}
						}
					}
				}
			}

			if (l1 <= i2)
				worldmap.flagDirty(k1, l1, i2);
		}*/
	}

	@Override
	public void render(MapView view, MapCanvas canvas, Player player)
	{
		int scale = 1 << worldmap.scale;
		byte i = 30;

		HashMap<Entry, Byte> map = new HashMap<Entry, Byte>();

		for (int mapX = 0; mapX < 128; mapX++)
		{
			for (int mapZ = 0; mapZ < 128; mapZ++)
			{
				byte color = (byte) 0;// worldmap.colors[(z * 128 + x)];

				int bX = (worldmap.centerX + ((mapX - 64) * scale)) & 0x000F;
				int bZ = (worldmap.centerZ + ((mapZ - 64) * scale)) & 0x000F;

				if (bX == 0 || bX == 15 || bZ == 0 || bZ == 15)
				{
					Entry<Integer, Integer> entry = new HashMap.SimpleEntry((mapX - bX) / 16, (mapZ - bZ) / 16);

					if (!map.containsKey(entry))
					{
						map.put(entry, i++);
					}

					color = map.get(entry);
				}
				else
				{
					color = colors[(mapZ * 128 + mapX)];
				}

				canvas.setPixel(mapX, mapZ, color);
			}
		}
	}
}