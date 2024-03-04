package test.rotation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;

public class ExampleScreen extends SceneScreen {
    private SceneAsset sceneAsset;
    private Scene scene;
    Matrix4 transform = new Matrix4();
    static Vector3 debugRotationTarget = new Vector3(Vector3.Z);

    private FirstPersonCameraController cameraController;

    public ExampleScreen(){
        // create scene
        sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/Alien Slime.gltf"));
        scene = new Scene(sceneAsset.scene);
        sceneManager = new SceneManager();
        sceneManager.addScene(scene);
        transform = scene.modelInstance.transform;

        // setup camera (The BoomBox model is very small so you may need to adapt camera settings for your scene)
        camera = new PerspectiveCamera(20f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        float d = .02f;
        camera.near = d / 1000f;
        camera.far = 200;
        sceneManager.setCamera(camera);
        camera.position.set(0,0.5f, 4f);

        cameraController = new FirstPersonCameraController(camera);
        Gdx.input.setInputProcessor(cameraController);

        setupEnv();
    }

    @Override
    public void render(float deltaTime) {
        super.render(deltaTime);
        cameraController.update();

        // render
        Gdx.gl.glClearColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, Color.BLACK.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        sceneManager.update(deltaTime);

        rotateToDirection(debugRotationTarget);
        scene.modelInstance.transform.set(transform);
        sceneManager.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        sceneAsset.dispose();
    }

    @Override
    public void resize(int width, int height) {
        sceneManager.updateViewport(width, height);
    }

    boolean rotateToDirection(Vector3 target) {
        Vector3 targetPosition = new Vector3(target);
        Vector3 targetDirection = new Vector3();
        Vector3 currentPosition = new Vector3();
        transform.getTranslation(currentPosition);
        targetDirection.set(targetPosition).sub(currentPosition).nor();
        Vector3 facing = getDirection();

        float angle = getAngleBetweenVectors(facing, targetDirection);
        float myRotation = transform.getRotation(new Quaternion()).getAngleAround(Vector3.Y);

        float rotationSpeed = 5f;

        if (myRotation < angle)
            transform.rotate(Vector3.Y, Math.min(rotationSpeed, Math.abs(myRotation - angle)));
        else if (myRotation > angle)
            transform.rotate(Vector3.Y, -Math.min(rotationSpeed, Math.abs(myRotation - angle)));

        System.out.println("Angle between" + currentPosition + " and " + target + ":" + (int) angle + ",my rotation: " + (int) myRotation);
        return Math.abs(myRotation - angle) <= rotationSpeed;
    }

    public Vector3 getDirection() {
        return (new Vector3((Vector3.Z)).rot(transform).nor());
    }

    public static float getAngleBetweenVectors(Vector3 v1, Vector3 v2) {
        //float dot = MathUtils.clamp(v1.dot(v2), -1f, 1f);
        //return (float) Math.toDegrees(Math.acos(dot));
        //  return (float) Math.acos(dot);
          float angle = (float) Math.toDegrees(Math.atan2(v2.y - v1.y, v2.x - v1.x));

         if(angle < 0){
             angle += 360;
         }
        // if (angle == 0) angle++;
         return angle;
    }

    @Override
    protected void processInput(float deltaTime) {
        super.processInput(deltaTime);
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            debugRotationTarget = new Vector3(-1, 0,0);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            debugRotationTarget = new Vector3(1, 0,0);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            debugRotationTarget = new Vector3(0, 0,1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            debugRotationTarget = new Vector3(0, 0,-1);
        }
    }
}
