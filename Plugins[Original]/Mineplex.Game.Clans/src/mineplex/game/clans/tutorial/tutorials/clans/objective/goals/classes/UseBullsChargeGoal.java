package mineplex.game.clans.tutorial.tutorials.clans.objective.goals.classes;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.minecraft.game.classcombat.Class.ClassManager;
import mineplex.minecraft.game.classcombat.Class.ClientClass;
import mineplex.minecraft.game.classcombat.Class.IPvpClass;
import mineplex.minecraft.game.classcombat.Class.event.ClassEquipEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.game.clans.tutorial.objective.ObjectiveGoal;
import mineplex.game.clans.tutorial.tutorials.clans.objective.ClassesObjective;
import mineplex.minecraft.game.classcombat.Skill.event.SkillTriggerEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class UseBullsChargeGoal extends ObjectiveGoal<ClassesObjective> {
	public UseBullsChargeGoal(ClassesObjective objective) {
		super(
				objective,
				"Use Bulls Charge",
				"Right-Click with Axe to use Bulls Charge",
				"One of your default abilities as Knight is Bulls Charge. This ability will make " +
						"you run faster for a short time, and deal extra damage to enemies.",
				null
		);
	}

	@Override
	protected void customStart(Player player) {
		ClientClass client = ClansManager.getInstance().getClassManager().Get(player);

		client.ResetSkills(player);
		client.SetActiveCustomBuild(client.GetGameClass(), client.GetGameClass().getDefaultBuild());
	}

	@Override
	protected void customFinish(Player player) {
	}

	@EventHandler
	public void checkSkill(SkillTriggerEvent event) {
		if (contains(event.GetPlayer())) {
				finish(event.GetPlayer());
		}
	}

}
