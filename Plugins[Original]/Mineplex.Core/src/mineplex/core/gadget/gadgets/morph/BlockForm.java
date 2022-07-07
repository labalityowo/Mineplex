package mineplex.core.gadget.gadgets.morph;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.IBlockData;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.disguises.DisguiseBlock;
import mineplex.core.disguise.disguises.DisguiseSlime;
import mineplex.core.itemstack.ItemStackFactory;

public class BlockForm
{
	private MorphBlock _host;
	private Player _player;

	private Material _blockMat;
	private int _blockData;

	private Block _block;
	private Location _loc;

	private Slime _fallingBlock;
	private ArmorStand _fallingBlockBase;

	private DisguiseBlock _disguiseBlock;
	private DisguiseSlime _disguiseBlockBase;
	private DisguiseSlime _hiddenDisguise;

	private EntityPlayer _entityPlayer;
	private Entity _nmsFallingBlockBase;

	public BlockForm(MorphBlock host, Player player, Material blockMat, int blockData)
	{
		_host = host;
		_player = player;

		_blockMat = blockMat;
		_blockData = blockData;

		_loc = player.getLocation();

		_entityPlayer = ((CraftPlayer) player).getHandle();

		_hiddenDisguise = new DisguiseSlime(player);
		_hiddenDisguise.setInvisible(true);

		_host.Manager.getDisguiseManager().disguise(_hiddenDisguise);

		this._fallingBlock = _loc.getWorld().spawn(_loc, Slime.class);
		this._fallingBlock.setSize(0);
		this._fallingBlock.setRemoveWhenFarAway(false);
		UtilEnt.vegetate(this._fallingBlock, true);
		UtilEnt.ghost(this._fallingBlock, true, true);

		this._fallingBlockBase = (ArmorStand) new EntityArmorStand(((CraftWorld) this._loc.getWorld()).getHandle(), this._loc.getX(), this._loc.getY(), this._loc.getZ()).getBukkitEntity();
		this._fallingBlockBase.setGravity(false);
		this._fallingBlockBase.setVisible(false);
		this._fallingBlockBase.setRemoveWhenFarAway(false);
		this._fallingBlockBase.setPassenger(this._fallingBlock);

		UtilEnt.addFlag(this._fallingBlock, UtilEnt.FLAG_NO_REMOVE);
		UtilEnt.addFlag(this._fallingBlock, MorphBlock.FLAG_BLOCK_MORPH_COMPONENT);
		UtilEnt.addFlag(this._fallingBlockBase, UtilEnt.FLAG_NO_REMOVE);
		UtilEnt.addFlag(this._fallingBlockBase, MorphBlock.FLAG_BLOCK_MORPH_COMPONENT);

		_nmsFallingBlockBase = ((CraftEntity) _fallingBlockBase).getHandle();
		_disguiseBlockBase = new DisguiseSlime(_fallingBlockBase);
		_disguiseBlockBase.SetSize(0);
		_disguiseBlockBase.setInvisible(true);
		_host.Manager.getDisguiseManager().disguise(_disguiseBlockBase);

		reset();
	}

	private void createFallingBlock()
	{
		removeFallingBlock();

		_disguiseBlock = new DisguiseBlock(_fallingBlock, _blockMat.getId(), _blockData);
		_disguiseBlock.setHideIfNotDisguised(true);
		_host.Manager.getDisguiseManager().disguise(_disguiseBlock);

		_fallingBlockBase.setPassenger(_fallingBlock);
	}

	private void removeFallingBlock()
	{
		if (_disguiseBlock != null)
		{
			_host.Manager.getDisguiseManager().undisguise(_disguiseBlock);
			_disguiseBlock = null;
			_fallingBlockBase.setPassenger(null);
		}
	}

	private void reset()
	{
		removeSolidBlock();
		createFallingBlock();
		// Inform

		String name = ItemStackFactory.Instance.GetName(_blockMat, (byte) _blockData, false);

		if (!name.contains("Block"))
		{
			name = name + " Block";
		}

		UtilPlayer.message(_player, F.main("Morph", "You are now " + F.vowelAN(name) + " " + F.elem(name) + "!"));

		// Sound
		_player.playSound(_player.getLocation(), Sound.ZOMBIE_UNFECT, 2f, 2f);
	}

	public void remove()
	{
		removeSolidBlock();
		removeFallingBlock();

		_host.Manager.getDisguiseManager().undisguise(_hiddenDisguise);
		_host.Manager.getDisguiseManager().undisguise(_disguiseBlockBase);
		_fallingBlockBase.remove();
		_fallingBlockBase = null;
		_fallingBlock.remove();
		_fallingBlock = null;
		_nmsFallingBlockBase = null;
	}

	public void update()
	{
		// Not a Block
		if (_block == null)
		{
			if (_fallingBlockBase.getPassenger() != _fallingBlock)
				_fallingBlockBase.setPassenger(_fallingBlock);

			if (!_nmsFallingBlockBase.getBukkitEntity().getWorld().equals(_player.getWorld()))
				_nmsFallingBlockBase.getBukkitEntity().teleport(_player);
			else
			{
				_nmsFallingBlockBase.locX = _entityPlayer.locX;
				_nmsFallingBlockBase.locY = _entityPlayer.locY;
				_nmsFallingBlockBase.locZ = _entityPlayer.locZ;
				_nmsFallingBlockBase.motX = _entityPlayer.motX;
				_nmsFallingBlockBase.motY = _entityPlayer.motY;
				_nmsFallingBlockBase.motZ = _entityPlayer.motZ;
				_nmsFallingBlockBase.velocityChanged = true;
			}

			// Moved
			if (!_loc.getBlock().equals(_player.getLocation().getBlock()))
			{
				_player.setExp(0);
				_loc = _player.getLocation();
			}
			// Unmoved
			else
			{
				double hideBoost = 0.025;

				_player.setExp((float) Math.min(0.999f, _player.getExp() + hideBoost));

				// Set Block
				if (_player.getExp() >= 0.999f)
				{
					Block block = _player.getLocation().getBlock();

					// Not Able
					if (block.getType() != Material.AIR || !UtilBlock.solid(block.getRelative(BlockFace.DOWN)) || !_host.Manager.selectBlocks(_host, block))
					{
						UtilPlayer.message(_player, F.main("Morph", "You cannot become a Solid Block here."));
						_player.setExp(0f);
						return;
					}

					// Set Block
					_block = block;

					// Effect
					_player.playEffect(_block.getLocation(), Effect.STEP_SOUND, _blockMat);

					removeFallingBlock();

					// Display
					for (Player other : UtilServer.getPlayers())
					{
						other.sendBlockChange(_block.getLocation(), _blockMat, (byte) _blockData);
					}

					// Sound
					_player.playSound(_block.getLocation(), Sound.NOTE_PLING, 1f, 2f);
				}
			}
		}
		// Is a Block
		else
		{
			// Moved
			if (!_loc.getBlock().equals(_player.getLocation().getBlock()))
			{
				removeSolidBlock();
				createFallingBlock();
			}
		}
	}

	private void removeSolidBlock()
	{
		if (_block != null)
		{
			Location location = _block.getLocation();
			_block = null;

			for (Player other : UtilServer.getPlayers())
			{
				other.sendBlockChange(location, 0, (byte) 0);
			}

			_player.setExp(0f);

			// Inform
			_player.playSound(_player.getLocation(), Sound.NOTE_PLING, 1f, 0.5f);
		}
	}

	public void setType(Block block)
	{
		if (block == null)
			return;

		if (block.getType() == Material.AIR)
			return;

		if (_blockMat == block.getType() && _blockData == block.getData())
			return;

		_blockMat = block.getType();
		_blockData = block.getData();

		reset();
	}

	public Block getBlock()
	{
		return _block;
	}

	public IBlockData getBlockData()
	{
		return CraftMagicNumbers.getBlock(_blockMat).fromLegacyData(_blockData);
	}

	public Player getPlayer()
	{
		return this._player;
	}
}
