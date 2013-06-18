
package com.wartricks.logic;

import java.util.Random;

import bsh.EvalError;

import com.artemis.Entity;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.wartricks.components.ActionSequence;
import com.wartricks.components.ActionSequenceOnBeginTurn;
import com.wartricks.components.ActionSequenceOnEndTurn;
import com.wartricks.components.Cooldown;
import com.wartricks.components.Cost;
import com.wartricks.components.EnergyBar;
import com.wartricks.components.EnergyRegen;
import com.wartricks.components.Health;
import com.wartricks.components.Initiative;
import com.wartricks.components.MapPosition;
import com.wartricks.components.OnCast;
import com.wartricks.components.Owner;
import com.wartricks.components.Range;
import com.wartricks.components.SkillSet;
import com.wartricks.components.Sprite;
import com.wartricks.custom.Pair;
import com.wartricks.custom.WartricksInterpreter;
import com.wartricks.utils.Constants;
import com.wartricks.utils.Constants.Players;

public class Api {
    private VersusGame game;

    public Api(VersusGame game) {
        super();
        this.game = game;
    }

    public boolean addEntityToWorld(int entityId) {
        game.world.addEntity(game.world.getEntity(entityId));
        return true;
    }

    public Array<Integer> loadCreatures(Array<String> scriptNames) {
        final Array<Integer> entityIds = new Array<Integer>();
        for (final String script : scriptNames) {
            int entityId = -1;
            final WartricksInterpreter interp = new WartricksInterpreter();
            final FileHandle scriptFile = Gdx.files
                    .internal("scripts/creatures/" + script + ".bsh");
            if (scriptFile.exists()) {
                try {
                    interp.eval(scriptFile.readString());
                    interp.set("name", script);
                    entityId = (Integer)interp.eval("create()");
                } catch (final EvalError error) {
                    error.printStackTrace();
                }
            }
            if (entityId > -1) {
                entityIds.add(entityId);
            }
        }
        return entityIds;
    }

    private Array<Integer> loadSkills(Array<String> skillSet) {
        final Array<Integer> entityIds = new Array<Integer>();
        for (final String script : skillSet) {
            int skillId = -1;
            final WartricksInterpreter interp = new WartricksInterpreter();
            final FileHandle scriptFile = Gdx.files.internal("scripts/skills/" + script + ".bsh");
            if (scriptFile.exists()) {
                try {
                    interp.eval(scriptFile.readString());
                    interp.set("name", script);
                    skillId = (Integer)interp.eval("create()");
                } catch (final EvalError error) {
                    error.printStackTrace();
                }
            }
            if (skillId > -1) {
                final Entity skill = game.world.getEntity(skillId);
                skill.addComponent(new OnCast(script, interp));
                entityIds.add(skillId);
            }
        }
        return entityIds;
    }

    public int createCreature(String name, int maxHealth, int energyRegen, Array<String> skillSet) {
        final Entity creature = game.world.createEntity();
        creature.addComponent(new Sprite(name, Sprite.Layer.ACTORS_3));
        creature.addComponent(new Health(maxHealth));
        creature.addComponent(new EnergyRegen(energyRegen));
        creature.addComponent(new SkillSet(this.loadSkills(skillSet)));
        game.world.getManager(TagManager.class).register(name, creature);
        game.world.getManager(GroupManager.class).add(creature, Constants.Groups.CREATURE);
        creature.addToWorld();
        return creature.getId();
    }

    public int createSkill(String name, int baseCost, int minRange, int maxRange, int initiative,
            int cooldown) {
        final Entity skill = game.world.createEntity();
        skill.addComponent(new Cooldown(cooldown));
        skill.addComponent(new Range(minRange, maxRange));
        skill.addComponent(new Cost(baseCost));
        skill.addComponent(new Initiative(initiative));
        game.world.getManager(TagManager.class).register(name, skill);
        game.world.getManager(GroupManager.class).add(skill, Constants.Groups.PLAYER_SKILL);
        skill.addToWorld();
        return skill.getId();
    }

    public boolean assignCreatureToPlayer(int creatureId, Players owner, int x, int y) {
        final Entity creature = game.world.getEntity(creatureId);
        if (null != creature) {
            final Random rand = new Random((long)game.world.getDelta());
            creature.addComponent(new ActionSequence(new Color(rand.nextFloat(), rand.nextFloat(),
                    rand.nextFloat(), 1)));
            creature.addComponent(new MapPosition(x, y));
            creature.addComponent(new Owner(owner));
            final String ownerGroup = (owner == Players.ONE) ? Constants.Groups.PLAYER_ONE_CREATURE
                    : Constants.Groups.PLAYER_TWO_CREATURE;
            game.world.getManager(GroupManager.class).add(creature, ownerGroup);
            final Entity player = game.world.getManager(TagManager.class).getEntity(
                    owner.toString());
            final EnergyRegen regen = game.world.getMapper(EnergyRegen.class).getSafe(creature);
            final EnergyBar energyBar = game.world.getMapper(EnergyBar.class).get(player);
            energyBar.setMaxEnergyModifier(energyBar.getMaxEnergyModifier()
                    + regen.getEnergyRegenAfterModifiers());
            return true;
        }
        return false;
    }

    public boolean creatureMoveTo(int creatureId, int x, int y) {
        final Entity creature = game.world.getEntity(creatureId);
        if ((null != creature) && !game.map.cellOccupied(x, y)) {
            final MapPosition mapPosition = game.world.getMapper(MapPosition.class).get(creature);
            game.map.moveEntity(creatureId, x, y);
            mapPosition.setPosition(x, y);
            creature.changedInWorld();
            return true;
        }
        return false;
    }

    public boolean creatureMoveBy(int creatureId, int x, int y) {
        final Entity creature = game.world.getEntity(creatureId);
        if (null != creature) {
            final Pair mapPosition = game.world.getMapper(MapPosition.class).get(creature)
                    .getPosition();
            mapPosition.x += x;
            mapPosition.y += y;
            game.map.moveEntity(creatureId, x, y);
            creature.changedInWorld();
            return true;
        }
        return false;
    }

    public boolean creatureHealthTo(int creatureId, float value) {
        final Entity creature = game.world.getEntity(creatureId);
        if (null != creature) {
            final Health health = game.world.getMapper(Health.class).get(creature);
            health.setCurrentHealth(value);
            creature.changedInWorld();
            return true;
        }
        return false;
    }

    public boolean creatureHealthBy(int creatureId, int value) {
        final Entity creature = game.world.getEntity(creatureId);
        if (null != creature) {
            final Health health = game.world.getMapper(Health.class).get(creature);
            health.modifyHealthBy(value);
            creature.changedInWorld();
            return true;
        }
        return false;
    }

    public boolean skillActivateBeginTurn(int skillId, int value) {
        final Entity skill = game.world.getEntity(skillId);
        if (null != skill) {
            skill.addComponent(new ActionSequenceOnBeginTurn());
            return true;
        }
        return false;
    }

    public boolean skillDeactivateBeginTurn(int skillId, int value) {
        final Entity skill = game.world.getEntity(skillId);
        if (null != skill) {
            skill.removeComponent(new ActionSequenceOnBeginTurn());
            return true;
        }
        return false;
    }

    public boolean skillActivateEndTurn(int skillId, int value) {
        final Entity skill = game.world.getEntity(skillId);
        if (null != skill) {
            skill.addComponent(new ActionSequenceOnEndTurn());
            return true;
        }
        return false;
    }

    public boolean skillDeactivateEndTurn(int skillId, int value) {
        final Entity skill = game.world.getEntity(skillId);
        if (null != skill) {
            skill.removeComponent(new ActionSequenceOnEndTurn());
            return true;
        }
        return false;
    }
}
