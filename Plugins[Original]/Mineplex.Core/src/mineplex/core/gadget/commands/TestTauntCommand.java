package mineplex.core.gadget.commands;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.GadgetManager.Perm;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.gadget.types.TauntGadget;

public class TestTauntCommand extends CommandBase<GadgetManager>
{

	public TestTauntCommand(GadgetManager plugin)
	{
		super(plugin, Perm.TEST_TAUNT_COMMAND, "testtaunt");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 1)
		{
			caller.sendMessage(F.main(Plugin.getName(), "/" + _aliasUsed + " <name>"));
			return;
		}

		String name = Arrays.stream(args).collect(Collectors.joining(" "));
		TauntGadget gadget = (TauntGadget) Plugin.getGadget(name, GadgetType.TAUNT);

		if (gadget == null)
		{
			caller.sendMessage(F.main(Plugin.getName(), "There is no taunt named " + F.name(name) + "."));
			return;
		}

		if (!gadget.isActive(caller))
		{
			gadget.enable(caller);
		}

		gadget.start(caller);
	}
}
