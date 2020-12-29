package alphaComplex.core.gameplay;

import java.util.HashSet;
import java.util.Set;

/**
 * Skill enumerations in TL order (Top-to-bottom, Left-to-Right)
 */
public enum Skill {

    ATHLETICS("Athletics"),
    SCIENCE("Science"),
    BLUFF("Bluff"),
    OPERATE("Operate"),

    GUNS("Guns"),
    PSYCHOLOGY("Psychology"),
    CHARM("Charm"),
    ENGINEER("Engineer"),

    MELEE("Melee"),
    BUREAUCRACY("Bureaucracy"),
    INTIMIDATE("Intimidate"),
    PROGRAM("Program"),

    THROW("Throw"),
    ALPHA_COMPLEX("Alpha Complex"),
    STEALTH("Stealth"),
    DEMOLITIONS("Demolitions");

    private final String name;

    Skill(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Skill getSkillByName(String name) {
        Skill target = null;
        for (Skill skill : values()) {
            if(skill.name.equals(name)){
                target = skill;
                break;
            }
        }
        return target;
    }

    public static Set<String> remainder(Set<String> skip) {
        Set<String> set = new HashSet<>();
        for(Skill skill : values()) {
            if(!skip.contains(skill.name))
                set.add(skill.name);
        }
        return set;
    }
}
