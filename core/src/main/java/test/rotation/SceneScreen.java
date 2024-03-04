package test.rotation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

public abstract class SceneScreen extends BaseScreen{
    protected Cubemap diffuseCubemap;
    protected Cubemap environmentCubemap;
    protected Cubemap specularCubemap;
    protected Texture brdfLUT;
    protected DirectionalShadowLight light;
    protected float camPitch = Settings.CAMERA_START_PITCH;
    protected float distanceFromPlayer = 55f;
    protected float angleAroundPlayer = 0f;
    public Vector3 currentCameraPosition = null;

    public SceneScreen(){
        setupCamera();
    }

    protected void setupCamera() {
        camera = new PerspectiveCamera(50f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 10f;
        camera.far = 600f;
    }

    Vector3 getCurrentPos() {
        if (currentCameraPosition==null)
            currentCameraPosition = new Vector3(0,0,0);
        Vector3 currentPosition = currentCameraPosition;
        if (!Gdx.input.isKeyPressed(Input.Keys.TAB)) {
            Vector3 direction = new Vector3();
            direction.rotate(Vector3.Y, angleAroundPlayer);
            currentPosition.add(direction);
        }
        return currentPosition;
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        updateCamera();
    }

    @Override
    public void dispose() {
        super.dispose();
        environmentCubemap.dispose();
        diffuseCubemap.dispose();
        specularCubemap.dispose();
        brdfLUT.dispose();
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        float zoomLevel = amountY * Settings.CAMERA_ZOOM_LEVEL_FACTOR;
        distanceFromPlayer += zoomLevel;
        if (distanceFromPlayer < Settings.CAMERA_MIN_DISTANCE_FROM_PLAYER)
            distanceFromPlayer = Settings.CAMERA_MIN_DISTANCE_FROM_PLAYER;
        if (distanceFromPlayer > Settings.CAMERA_MAX_DISTANCE_FROM_PLAYER)
            distanceFromPlayer = Settings.CAMERA_MAX_DISTANCE_FROM_PLAYER;
        return false;
    }

    private void calculateCameraPosition(Vector3 currentPosition, float horDistance, float vertDistance) {
        float offsetX = (float) (horDistance * Math.sin(Math.toRadians(angleAroundPlayer)));
        float offsetZ = (float) (horDistance * Math.cos(Math.toRadians(angleAroundPlayer)));
        camera.position.x = currentPosition.x - offsetX;
        camera.position.z = currentPosition.z - offsetZ;
        camera.position.y = currentPosition.y + vertDistance;
    }

    private void calculateAngleAroundPlayer() {
        if (Gdx.input.isKeyPressed(Input.Keys.TAB)) {
            float angleChange = Gdx.input.getDeltaX() * Settings.CAMERA_ANGLE_AROUND_PLAYER_FACTOR;
            angleAroundPlayer -= angleChange;
        }
    }

    private void calculatePitch() {
        if (Gdx.input.isKeyPressed(Input.Keys.TAB)) {
            float pitchChange = -Gdx.input.getDeltaY() * Settings.CAMERA_PITCH_FACTOR;
            camPitch -= pitchChange;

            if (camPitch < Settings.CAMERA_MIN_PITCH)
                camPitch = Settings.CAMERA_MIN_PITCH;
            else if (camPitch > Settings.CAMERA_MAX_PITCH)
                camPitch = Settings.CAMERA_MAX_PITCH;
        }
    }

    private float calculateVerticalDistance(float distanceFromPlayer) {
        return (float) (distanceFromPlayer * Math.sin(Math.toRadians(camPitch)));
    }

    private float calculateHorizontalDistance(float distanceFromPlayer) {
        return (float) (distanceFromPlayer * Math.cos(Math.toRadians(camPitch)));
    }

    private void updateCamera() {
        float horDistance = calculateHorizontalDistance(distanceFromPlayer);
        float vertDistance = calculateVerticalDistance(distanceFromPlayer);
        calculatePitch();
        calculateAngleAroundPlayer();
        calculateCameraPosition(getCurrentPos(), horDistance, vertDistance);
        camera.up.set(Vector3.Y);
        camera.lookAt(getCurrentPos());
        camera.update();
    }

    protected void setupEnv() {
        light = new DirectionalShadowLight(4000, 4000, 512, 512, 1f, 50f);
        light.setBounds(new BoundingBox(new Vector3(0, 0, 0), new Vector3(512, 50, 512)));
        light.intensity = 1.6f;
        light.color.set(1f, 1f, 0f, 1f);
        light.direction.set(10, -50, -5).nor();
        light.updateColor();
        sceneManager.environment.add(light);
        brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
        IBLBuilder iblBuilder = IBLBuilder.createOutdoor(light);
        diffuseCubemap = iblBuilder.buildIrradianceMap(256);
        specularCubemap = iblBuilder.buildRadianceMap(10);
        sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
        sceneManager.environment.set(new PBRFloatAttribute(Attribute.getAttributeType("ShadowBias"), 0.01f));
        iblBuilder.dispose();
        sceneManager.setCamera(camera);
        sceneManager.setAmbientLight(0.03f);
    }

    protected void processInput(float deltaTime) {
        if (Gdx.input.isKeyPressed(Input.Keys.EQUALS)) {
            float zoomLevel = -1f * Settings.CAMERA_ZOOM_LEVEL_FACTOR;
            distanceFromPlayer += zoomLevel;
            if (distanceFromPlayer < Settings.CAMERA_MIN_DISTANCE_FROM_PLAYER)
                distanceFromPlayer = Settings.CAMERA_MIN_DISTANCE_FROM_PLAYER;
            if (distanceFromPlayer > Settings.CAMERA_MAX_DISTANCE_FROM_PLAYER)
                distanceFromPlayer = Settings.CAMERA_MAX_DISTANCE_FROM_PLAYER;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) {
            float zoomLevel = Settings.CAMERA_ZOOM_LEVEL_FACTOR;
            distanceFromPlayer += zoomLevel;
            if (distanceFromPlayer < Settings.CAMERA_MIN_DISTANCE_FROM_PLAYER)
                distanceFromPlayer = Settings.CAMERA_MIN_DISTANCE_FROM_PLAYER;
            if (distanceFromPlayer > Settings.CAMERA_MAX_DISTANCE_FROM_PLAYER)
                distanceFromPlayer = Settings.CAMERA_MAX_DISTANCE_FROM_PLAYER;
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
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

}
