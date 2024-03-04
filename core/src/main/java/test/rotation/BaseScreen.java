package test.rotation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import net.mgsx.gltf.scene3d.scene.SceneManager;

public abstract class BaseScreen extends ScreenAdapter implements InputProcessor {
    public volatile SceneManager sceneManager;
    public PerspectiveCamera camera;
    InputMultiplexer multiplexer;

    public BaseScreen() {
        multiplexer = new InputMultiplexer();
        Gdx.input.setCursorCatched(false);
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        processInput(delta);
    }

    @Override
    public void dispose() {
        sceneManager.dispose();
    }

    protected abstract void processInput(float deltaTime);

    @Override
    public boolean keyDown(int i) {
        return false;
    }

    @Override
    public boolean keyUp(int i) {
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(float v, float v1) {
        return false;
    }

}
