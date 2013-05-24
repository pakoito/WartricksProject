
package com.wartricks.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.wartricks.components.Movement;
import com.wartricks.components.Path;
import com.wartricks.components.Player;
import com.wartricks.components.Position;
import com.wartricks.custom.Pair;
import com.wartricks.utils.EntityFactory;
import com.wartricks.utils.MapTools;

public class PlayerInputSystem extends EntityProcessingSystem implements InputProcessor {
    @Mapper
    ComponentMapper<Position> pm;

    @Mapper
    ComponentMapper<Path> ptm;

    private OrthographicCamera camera;

    private Vector3 mouseVector;

    private boolean moving;

    private Pair moveTarget;

    @SuppressWarnings("unchecked")
    public PlayerInputSystem(OrthographicCamera camera) {
        super(Aspect.getAspectForAll(Player.class));
        this.camera = camera;
        moving = false;
    }

    @Override
    protected void initialize() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    protected void process(Entity e) {
        mouseVector = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseVector);
        if (moving) {
            moving = false;
            final Movement movement = new Movement(11, 14, moveTarget.x, moveTarget.y);
            ptm.get(e).path.add(movement);
            e.changedInWorld();
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // Get the hex cell being clicked
        final Pair coords = MapTools.window2world(Gdx.input.getX(), Gdx.input.getY(), camera);
        moving = true;
        moveTarget = coords;
        if (button == 1) {
            camera.zoom = 1;
        }
        EntityFactory.createClick(world, coords.x, coords.y, 0.4f, 4f, 0.15f).addToWorld();
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // final Vector3 delta = new Vector3(-camera.zoom * Gdx.input.getDeltaX(), camera.zoom
        // * Gdx.input.getDeltaY(), 0);
        // camera.translate(delta);
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        if (((camera.zoom > 0.2f) || (amount == 1)) && ((camera.zoom < 8) || (amount == -1))) {
            camera.zoom += amount * 0.1;
        }
        return false;
    }
}
