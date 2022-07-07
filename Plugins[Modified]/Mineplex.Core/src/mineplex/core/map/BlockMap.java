package mineplex.core.map;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;

import javax.imageio.ImageIO;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilServer;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.EntityItemFrame;
import net.minecraft.server.v1_8_R3.EnumDirection;
import net.minecraft.server.v1_8_R3.PersistentCollection;
import net.minecraft.server.v1_8_R3.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class BlockMap implements Listener
{

    private HashMap<ItemFrame, MapView> _maps = new HashMap<ItemFrame, MapView>();

    public BlockMap(MiniPlugin plugin, String imageUrl, Block corner1, Block corner2)
    {
        this(plugin, imageUrl, corner1, corner2, null);
    }

    public BlockMap(MiniPlugin plugin, String imageUrl, Block corner1, Block corner2, BlockFace facingDirection)
    {

        try
        {
            // Make sure the world doesn't change the mapcount information
            PersistentCollection collection = ((CraftWorld) corner1.getWorld()).getHandle().worldMaps;
            Field f = collection.getClass().getDeclaredField("b");
            f.setAccessible(true);
            f.set(collection, null);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        plugin.registerEvents(this);

        // Clear out the area to make room for the picture
        {
            int bX = Math.min(corner1.getX(), corner2.getX());
            int bY = Math.min(corner1.getY(), corner2.getY());
            int bZ = Math.min(corner1.getZ(), corner2.getZ());
            int tX = Math.max(corner1.getX(), corner2.getX());
            int tY = Math.max(corner1.getY(), corner2.getY());
            int tZ = Math.max(corner1.getZ(), corner2.getZ());

            for (int x = bX; x <= tX; x++)
            {
                for (int y = bY; y <= tY; y++)
                {
                    for (int z = bZ; z <= tZ; z++)
                    {
                        Block b = corner1.getWorld().getBlockAt(x, y, z);

                        if (!UtilBlock.airFoliage(b))
                        {
                            b.setType(Material.AIR);
                        }
                    }
                }
            }

            // Remove offending hanging pictures.
            for (Hanging hanging : corner1.getWorld().getEntitiesByClass(Hanging.class))
            {
                Location loc = hanging.getLocation();

                if (loc.getX() >= bX && loc.getX() <= tX)
                {
                    if (loc.getY() >= bY && loc.getY() <= tY)
                    {
                        if (loc.getZ() >= bZ && loc.getZ() <= tZ)
                        {
                            hanging.remove();
                        }
                    }
                }
            }
        }

        if (facingDirection == null)
        {
            facingDirection = getImageFace(corner1, corner2);
        }

        // Get the very corner of the image
        Block corner = new Location(

        corner1.getWorld(),

        Math.min(corner1.getX(), corner2.getX()),

        Math.min(corner1.getY(), corner2.getY()),

        Math.min(corner1.getZ(), corner2.getZ()))

        .getBlock();

        // Find the dimensions of the image width
        int x = corner2.getX() - corner1.getX();
        int z = corner2.getZ() - corner1.getZ();

        // Turn it into the single largest number. If there is a 0, we are going to have a problem.
        int width = Math.max(Math.abs(x), Math.abs(z)) + 1;

        // Get the image height
        int height = Math.abs(corner1.getY() - corner2.getY()) + 1;

        corner = corner.getRelative(facingDirection.getOppositeFace());

        // Load image
        BufferedImage image = loadImage(imageUrl);

        if (image == null)
        {
            throw new IllegalArgumentException("Cannot load image at '" + imageUrl + "'");
        }

        // Resize image to fit into the dimensions
        image = toBufferedImage(image.getScaledInstance(width * 128, height * 128, Image.SCALE_SMOOTH), image.getType());

        // Find out the direction to cutup the picture
        boolean reversed = facingDirection.getModZ() > 0 || facingDirection.getModX() < 0;

        // Cut image into 128 pixels images
        BufferedImage[] imgs = cutIntoPieces(image, width, height, !reversed);

        drawImage(imgs, corner, Math.abs(x), height - 1, Math.abs(z), facingDirection);
    }

    private BufferedImage[] cutIntoPieces(BufferedImage image, int width, int height, boolean reversed)
    {
        BufferedImage[] pieces = new BufferedImage[width * height]; // Image array to hold image chunks
        int count = 0;

        for (int x1 = 0; x1 < width; x1++)
        {
            int x = reversed ? (width - 1) - x1 : x1;

            for (int y = 0; y < height; y++)
            {
                // Initialize the image array with image chunks
                pieces[count] = new BufferedImage(128, 128, image.getType());

                // draws the image chunk
                Graphics2D gr = pieces[count++].createGraphics();

                gr.drawImage(image, 0, 0, 128, 128, x * 128, y * 128, (x * 128) + 128, (y * 128) + 128, null);

                gr.dispose();
            }
        }

        return pieces;
    }

    private void drawImage(BufferedImage[] images, Block cornerBlock, int xSize, int ySize, int zSize, BlockFace direction)
    {
        int count = 0;

        // Turn each subimage into a map file
        for (int x = 0; x <= xSize; x++)
        {
            for (int z = 0; z <= zSize; z++)
            {
                for (int y = ySize; y >= 0; y--)
                {
                    // Create a itemframe and set the map inside
                    Block b = cornerBlock.getRelative(x, y, z);

                    setItemFrame(b, direction, images[count++]);
                }
            }
        }
    }

    /**
     * Get the direction the image needs to face
     */
    private BlockFace getImageFace(Block c1, Block c2)
    {
        // Get the center block between the two locations
        Block b = c1.getWorld().getBlockAt(

        Math.min(c1.getX(), c2.getX()) + ((c1.getX() + c2.getX()) / 2),

        Math.min(c1.getY(), c2.getY()) + ((c1.getY() + c2.getY()) / 2),

        Math.min(c1.getZ(), c2.getZ()) + ((c1.getZ() + c2.getZ()) / 2));

        // Using the block as a indicator on how the rest of the blocks look, lets scan the four directions to see what blocks are
        // solid.

        boolean xOk = c1.getX() == c2.getX();
        boolean zOk = c1.getZ() == c2.getZ();

        for (int x = -1; x <= 1; x++)
        {
            for (int z = -1; z <= 1; z++)
            {
                if (x != z && (x == 0 || z == 0))
                {
                    Block b1 = b.getRelative(x, 0, z);

                    if (UtilBlock.solid(b1))
                    {
                        BlockFace face = b1.getFace(b);

                        if ((face.getModX() != 0 && xOk) || (face.getModZ() != 0 && zOk))
                        {
                            return face;
                        }
                    }
                }
            }
        }

        return xOk ? BlockFace.EAST : BlockFace.NORTH;
    }

    private MapView getMap(World world, BufferedImage image)
    {
        MapView map = Bukkit.createMap(world);

        for (MapRenderer r : map.getRenderers())
        {
            map.removeRenderer(r);
        }

        map.addRenderer(new ImageMapRenderer(image));

        return map;
    }

    private ItemStack getMapItem(MapView map)
    {
        ItemStack item = new ItemStack(Material.MAP);

        item.setDurability(map.getId());

        return item;
    }

    private BufferedImage loadImage(String file)
    {
        File f = new File(file);
        BufferedImage image = null;

        try
        {
            if (!f.exists())
            {
                image = ImageIO.read(URI.create(file).toURL().openStream());
            }
            else
            {
                image = ImageIO.read(f);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return image;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        for (MapView map : _maps.values())
        {
            event.getPlayer().sendMap(map);
        }
    }

    public void remove()
    {
        HandlerList.unregisterAll(this);

        for (ItemFrame itemFrame : _maps.keySet())
        {
            itemFrame.remove();
        }
    }

    private void setItemFrame(Block block, BlockFace face, BufferedImage image)
    {
        if (!UtilBlock.solid(block))
        {
            block.setType(Material.QUARTZ_BLOCK);
        }

        ItemFrame itemFrame = spawnItemFrame(block, face);

        MapView map = getMap(block.getWorld(), image);

        _maps.put(itemFrame, map);

        ItemStack mapItem = getMapItem(map);

        itemFrame.setItem(mapItem);

        for (Player player : UtilServer.getPlayers())
        {
            player.sendMap(map);
        }
    }

    private ItemFrame spawnItemFrame(Block block, BlockFace bf)
    {
        EnumDirection dir;
        switch (bf)
        {
        default:
        case SOUTH:
            dir = EnumDirection.SOUTH;
            break;
        case WEST:
            dir = EnumDirection.WEST;
            break;
        case NORTH:
            dir = EnumDirection.NORTH;
            break;
        case EAST:
            dir = EnumDirection.EAST;
            break;
        }

        WorldServer world = ((CraftWorld) block.getWorld()).getHandle();

        EntityItemFrame entity = new EntityItemFrame(world, new BlockPosition(block.getX(), block.getY(), block.getZ()), dir);

        entity.setDirection(dir);

        world.addEntity(entity);

        return (ItemFrame) entity.getBukkitEntity();
    }

    private BufferedImage toBufferedImage(Image img, int imageType)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), imageType);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }
}
