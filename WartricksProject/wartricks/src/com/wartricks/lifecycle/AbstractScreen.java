
package com.wartricks.lifecycle;

import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class AbstractScreen implements Screen {
    protected final BoardGame game;

    protected final World world;

    protected final OrthographicCamera camera;

    public AbstractScreen(BoardGame game, World world) {
        this.game = game;
        this.world = world;
        camera = new OrthographicCamera();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        camera.update();
        world.setDelta(delta);
        world.process();
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void resize(int width, int height) {
        BoardGame.WINDOW_WIDTH = width;
        BoardGame.WINDOW_HEIGHT = height;
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void dispose() {
    }
}
