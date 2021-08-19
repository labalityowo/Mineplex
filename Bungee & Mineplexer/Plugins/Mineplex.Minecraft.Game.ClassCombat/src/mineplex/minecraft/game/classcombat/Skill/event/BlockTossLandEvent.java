package mineplex.minecraft.game.classcombat.Skill.event;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BlockTossLandEvent extends BlockTossEvent
{
	public FallingBlock Entity;
	
    public BlockTossLandEvent(Block block, FallingBlock fallingBlock)
    {
    	super(block);
    	Entity = fallingBlock;
    }
}
