package test.rotation;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class RotationFix extends Game {
    @Override
    public void create() {
        setScreen(new ExampleScreen());
    }
}
