
package com.wartricks.utils;

import bsh.EvalError;

import com.artemis.Entity;
import com.artemis.World;
import com.artemis.managers.GroupManager;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.wartricks.components.ColorAnimation;
import com.wartricks.components.Cooldown;
import com.wartricks.components.Cost;
import com.wartricks.components.EnergyBar;
import com.wartricks.components.EnergyRegen;
import com.wartricks.components.Expires;
import com.wartricks.components.Health;
import com.wartricks.components.Initiative;
import com.wartricks.components.Label;
import com.wartricks.components.MapPosition;
import com.wartricks.components.ScriptExecutable;
import com.wartricks.components.Owner;
import com.wartricks.components.Position;
import com.wartricks.components.Range;
import com.wartricks.components.ScaleAnimation;
import com.wartricks.components.SkillSet;
import com.wartricks.components.Sprite;
import com.wartricks.components.Velocity;
import com.wartricks.custom.WartricksInterpreter;
import com.wartricks.logic.GameMap;
import com.wartricks.utils.Constants.Players;

public class EntityFactory {
    public static Array<Integer> loadEntities(Array<String> scriptNames, World world) {
        final Array<Integer> entityIds = new Array<Integer>();
        final WartricksInterpreter interp = new WartricksInterpreter();
        for (final String script : scriptNames) {
            final FileHandle scriptFile = Gdx.files.internal("scripts/skills/" + script + ".bsh");
            if (scriptFile.exists()) {
                try {
                    interp.eval(scriptFile.readString());
                    interp.set("world", world);
                    interp.set("name", script);
                    entityIds.add((Integer)interp.eval("create()"));
                } catch (final EvalError error) {
                    error.printStackTrace();
                }
            }
        }
        return entityIds;
    }

    public static Entity createPlayer(World world, GameMap map, Players owner, int maxEnergy) {
        final Entity e = world.createEntity();
        e.addComponent(new EnergyBar(maxEnergy));
        e.addComponent(new Owner(owner));
        world.getManager(TagManager.class).register(owner.toString(), e);
        world.getManager(GroupManager.class).add(e, Constants.Groups.PLAYER);
        e.addToWorld();
        return e;
    }

    public static Entity createCreature(World world, GameMap map, String sprite, Players owner,
            Color uiColor, int x, int y, float maxHealth, int energyRegen, Array<String> skillSet) {
        final Entity e = world.createEntity();
        e.addComponent(new MapPosition(x, y));
        e.addComponent(new Sprite(sprite, Sprite.Layer.ACTORS_3));
        e.addComponent(new Health(maxHealth));
        e.addComponent(new EnergyRegen(energyRegen));
        e.addComponent(new SkillSet(loadEntities(skillSet, world)));
        e.addComponent(new Owner(owner));
        world.getManager(TagManager.class).register(sprite, e);
        world.getManager(GroupManager.class).add(e, Constants.Groups.CREATURE);
        final String ownerGroup = (owner == Players.ONE) ? Constants.Groups.PLAYER_ONE_CREATURE
                : Constants.Groups.PLAYER_TWO_CREATURE;
        world.getManager(GroupManager.class).add(e, ownerGroup);
        map.addEntity(e.getId(), x, y);
        final Entity player = world.getManager(TagManager.class).getEntity(owner.toString());
        final EnergyBar energyBar = world.getMapper(EnergyBar.class).get(player);
        energyBar.setMaxEnergyModifier(energyBar.getMaxEnergyModifier() + energyRegen);
        e.addToWorld();
        return e;
    }

    public static Entity createSkill(World world, String name, int baseCost, int minRange,
            int maxRange, int baseInitiative, int cooldown) {
        final Entity e = world.createEntity();
        e.addComponent(new Cooldown(cooldown));
        e.addComponent(new Range(minRange, maxRange));
        e.addComponent(new Cost(baseCost));
        e.addComponent(new Initiative(baseInitiative));
        e.addComponent(new ScriptExecutable(name));
        world.getManager(TagManager.class).register(name, e);
        world.getManager(GroupManager.class).add(e, Constants.Groups.PLAYER_SKILL);
        e.addToWorld();
        return e;
    }

    // public static Entity createAction(World world, Players owner, int skill, Pair origin,
    // Pair target) {
    // final Entity e = world.createEntity();
    // e.addComponent(new Action(skill, origin, target));
    // e.addComponent(new Owner(owner));
    // switch (owner) {
    // case ONE:
    // world.getManager(GroupManager.class).add(e, Constants.Groups.PLAYER_ONE_ACTION);
    // break;
    // case TWO:
    // world.getManager(GroupManager.class).add(e, Constants.Groups.PLAYER_TWO_ACTION);
    // break;
    // default:
    // break;
    // }
    // return e;
    // }
    public static Entity createParticle(World world, float x, float y, float delay) {
        final Entity e = world.createEntity();
        final Position position = new Position();
        position.x = x;
        position.y = y;
        e.addComponent(position);
        final Sprite sprite = new Sprite();
        sprite.name = "troll";
        sprite.scaleX = sprite.scaleY = MathUtils.random(0.3f, 0.6f);
        sprite.r = 1;
        sprite.g = 216 / 255f;
        sprite.b = 0;
        sprite.a = 0.5f;
        sprite.layer = Sprite.Layer.PARTICLES;
        e.addComponent(sprite);
        final Velocity velocity = new Velocity(MathUtils.random(-400, 400), MathUtils.random(-400,
                400));
        e.addComponent(velocity);
        final Expires expires = new Expires();
        expires.delay = delay;
        e.addComponent(expires);
        final ColorAnimation colorAnimation = new ColorAnimation();
        colorAnimation.alphaAnimate = true;
        colorAnimation.alphaSpeed = -1f;
        colorAnimation.alphaMin = 0f;
        colorAnimation.alphaMax = 1f;
        colorAnimation.repeat = false;
        e.addComponent(colorAnimation);
        return e;
    }

    public static Entity createLabel(World world, String name, String text, int x, int y) {
        final Entity e = world.createEntity();
        final Label label = new Label(text);
        final Position position = new Position(x, y);
        e.addComponent(label);
        e.addComponent(position);
        world.getManager(TagManager.class).register(name, e);
        return e;
    }

    public static Entity createClick(World world, int x, int y, float startScale, float speed,
            float expiration) {
        final Entity e = world.createEntity();
        e.addComponent(new MapPosition(x, y));
        final Sprite sprite = new Sprite("kirby", Sprite.Layer.ACTORS_3);
        sprite.r = 1f;
        sprite.g = 1f;
        sprite.b = 1f;
        sprite.a = 0.5f;
        sprite.rotation = 0f;
        sprite.scaleX = startScale;
        sprite.scaleY = startScale;
        e.addComponent(sprite);
        final Expires expires = new Expires(expiration);
        e.addComponent(expires);
        final ScaleAnimation scaleAnimation = new ScaleAnimation(speed);
        e.addComponent(scaleAnimation);
        final ColorAnimation colorAnimation = new ColorAnimation();
        colorAnimation.alphaAnimate = false;
        colorAnimation.alphaSpeed = -1f;
        colorAnimation.alphaMin = 0f;
        colorAnimation.alphaMax = 1f;
        colorAnimation.repeat = false;
        e.addComponent(colorAnimation);
        return e;
    }
}
