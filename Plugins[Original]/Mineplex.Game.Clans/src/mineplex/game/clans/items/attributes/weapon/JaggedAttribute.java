package mineplex.game.clans.items.attributes.weapon;

import mineplex.game.clans.items.attributes.AttackAttribute;
import mineplex.game.clans.items.attributes.AttributeType;
import mineplex.game.clans.items.generation.ValueDistribution;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class JaggedAttribute extends AttackAttribute {
    private static ValueDistribution attackGen = generateDistribution(2, 4);

    public JaggedAttribute() {
        super(AttributeType.PREFIX, attackGen.generateIntValue());
    }

    @Override
    public String getDisplayName() {
        return "Jagged";
    }

    @Override
    public String getDescription() {
        return String.format("Every %d attacks mini-stuns enemies", getAttackLimit());
    }

    @Override
    public void triggerAttack(Entity attacker, Entity defender) {
        if (isTeammate(attacker, defender)) return;
        defender.setVelocity(new Vector(0, 0, 0));
        if (defender instanceof LivingEntity)
            ((LivingEntity) defender).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 1, false, false));
    }

}
