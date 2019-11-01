package de.banapple.drivingschool;

import java.util.*;

import com.jme3.app.*;
import com.jme3.asset.*;
import com.jme3.input.*;
import com.jme3.input.controls.*;
import com.jme3.light.*;
import com.jme3.material.*;
import com.jme3.math.*;
import com.jme3.scene.*;
import com.jme3.scene.shape.*;
import com.jme3.system.*;

/**
 * Sample 1 - how to get started with the most simple JME 3 application. Display
 * a blue 3D cube and view from all sides by moving the mouse and pressing the
 * WASD keys.
 */
public class DrivingSchool extends SimpleApplication {

    public static void main(String[] args) {

        AppSettings settings = new AppSettings(true);
        settings.setResolution(640, 480);
        settings.setUseJoysticks(true);

        DrivingSchool app = new DrivingSchool();
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start(); // start the game
    }

    protected Geometry car;
    private Joystick joystick;
    private Vector3f drivingDirection;
    float speed;
    
    @Override
    public void simpleInitApp() {

        Joystick[] joysticks = inputManager.getJoysticks();
        if (joysticks == null) {
            throw new IllegalStateException("Cannot find any joysticks!");
        }
        Arrays.asList(joysticks).forEach(j -> System.out.println(j));
        if (joysticks.length != 1) {
            throw new IllegalStateException("more than one joystick");
        }
        joystick = joysticks[0];

        /* disable FlyCamAppState with all the key and joystick bindings */
        stateManager.detach(stateManager.getState(FlyCamAppState.class));
        
        Box b = new Box(1, 1, 1); // create cube shape
        car = new Geometry("Car", b); // create cube geometry from the shape
        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md"); // create a simple
                                                      // material
        mat.setColor("Color", ColorRGBA.Blue); // set color of material to blue
        car.setMaterial(mat); // set the cube's material
        rootNode.attachChild(car); // make the cube appear in the scene

        createWorld(rootNode, assetManager);

        initKeys();
        
        drivingDirection = new Vector3f(1, 0, 0);
        speed = 0;
    }

    /**
     * Geborgt von
     * https://github.com/jMonkeyEngine/jmonkeyengine/blob/master/jme3-examples/src/main/java/jme3test/bullet/PhysicsTestHelper.java
     * 
     * @param rootNode
     * @param assetManager
     */
    public void createWorld(Node rootNode, AssetManager assetManager) {

        AmbientLight light = new AmbientLight();
        light.setColor(ColorRGBA.LightGray);
        rootNode.addLight(light);

        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setTexture("ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));

        Box floorBox = new Box(140, 0.25f, 140);
        Geometry floorGeometry = new Geometry("Floor", floorBox);
        floorGeometry.setMaterial(material);
        floorGeometry.setLocalTranslation(0, -5, 0);
        // Plane plane = new Plane();
        // plane.setOriginNormal(new Vector3f(0, 0.25f, 0), Vector3f.UNIT_Y);
        // floorGeometry.addControl(new RigidBodyControl(new
        // PlaneCollisionShape(plane), 0));
        // floorGeometry.addControl(new RigidBodyControl(0));
        rootNode.attachChild(floorGeometry);
        // space.add(floorGeometry);
        //
        // // movable boxes
        // for (int i = 0; i < 12; i++) {
        // Box box = new Box(0.25f, 0.25f, 0.25f);
        // Geometry boxGeometry = new Geometry("Box", box);
        // boxGeometry.setMaterial(material);
        // boxGeometry.setLocalTranslation(i, 5, -3);
        // // RigidBodyControl automatically uses box collision shapes when
        // // attached to single geometry with box mesh
        // boxGeometry.addControl(new RigidBodyControl(2));
        // rootNode.attachChild(boxGeometry);
        // space.add(boxGeometry);
        // }
        //
        // // immovable sphere with mesh collision shape
        // Sphere sphere = new Sphere(8, 8, 1);
        // Geometry sphereGeometry = new Geometry("Sphere", sphere);
        // sphereGeometry.setMaterial(material);
        // sphereGeometry.setLocalTranslation(4, -4, 2);
        // sphereGeometry.addControl(new RigidBodyControl(new
        // MeshCollisionShape(sphere), 0));
        // rootNode.attachChild(sphereGeometry);
        // space.add(sphereGeometry);

    }

    private void initKeys() {

        System.out.println(inputManager);

        inputManager.clearRawInputListeners();

        inputManager.addMapping("Left", new JoyAxisTrigger(
                joystick.getJoyId(),
                joystick.getXAxisIndex(), true));
        inputManager.addMapping("Right", new JoyAxisTrigger(
                joystick.getJoyId(),
                joystick.getXAxisIndex(), false));
        inputManager.addMapping("Forward", new JoyAxisTrigger(
                joystick.getJoyId(),
                joystick.getYAxisIndex(), true));
        inputManager.addMapping("Back", new JoyAxisTrigger(
                joystick.getJoyId(),
                joystick.getYAxisIndex(), false));
        inputManager.addListener(analogListener, "Left", "Right", "Forward", "Back");

    }

    private final AnalogListener analogListener = new AnalogListener() {

        @Override
        public void onAnalog(String name, float value, float tpf) {

            speed = 0;            
            
            if (name.equals("Right")) {
                car.rotate(0, -value, 0);
                drivingDirection = car.getLocalTransform().getRotation().getRotationColumn(2);
            }
            if (name.equals("Left")) {
                car.rotate(0, value, 0);
                drivingDirection = car.getLocalTransform().getRotation().getRotationColumn(2);
            }

            if ("Forward".equals(name)) {
                speed = 1000 * value * tpf;
            }

            if ("Back".equals(name)) {
                speed = -1000 * value * tpf;
            }
            
            System.out.println(name + " " + value + " " + tpf + " speed: " + speed
                    + " drivingDirection: " + drivingDirection);
        }
    };

    @Override
    public void simpleUpdate(float tpf) {

        car.move(drivingDirection.mult(speed));
    }
}
