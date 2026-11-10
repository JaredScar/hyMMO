package com.jaredscarito.hymmo.data;

import com.jaredscarito.hymmo.skill.Skill;

import java.util.ArrayList;
import java.util.List;

public final class Profile {
    public String id;
    public String name;
    public List<SkillState> skills = new ArrayList<>();

    public SkillState state(Skill skill) {
        if (skills == null) {
            skills = new ArrayList<>();
        }
        for (SkillState state : skills) {
            if (state != null && skill.name().equals(state.skill)) {
                return state;
            }
        }
        SkillState created = new SkillState();
        created.skill = skill.name();
        skills.add(created);
        return created;
    }
}
