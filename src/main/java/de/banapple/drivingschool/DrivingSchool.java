package de.banapple.drivingschool;

import com.jme3.app.*;
import com.jme3.asset.*;
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
        settings.setResolution(1280,960);
        
        DrivingSchool app = new DrivingSchool();
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start(); // start the game
    }

    @Override
    public void simpleInitApp() {

        Box b = new Box(1, 1, 1); // create cube shape
        Geometry geom = new Geometry("Box", b); // create cube geometry from the
                                                // shape
        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md"); // create a simple
                                                      // material
        mat.setColor("Color", ColorRGBA.Blue); // set color of material to blue
        geom.setMaterial(mat); // set the cube's material
        rootNode.attachChild(geom); // make the cube appear in the scene
        
        createWorld(rootNode, assetManager);
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
//        floorGeometry.addControl(new RigidBodyControl(0));
        rootNode.attachChild(floorGeometry);
//        space.add(floorGeometry);
//
//        // movable boxes
//        for (int i = 0; i < 12; i++) {
//            Box box = new Box(0.25f, 0.25f, 0.25f);
//            Geometry boxGeometry = new Geometry("Box", box);
//            boxGeometry.setMaterial(material);
//            boxGeometry.setLocalTranslation(i, 5, -3);
//            // RigidBodyControl automatically uses box collision shapes when
//            // attached to single geometry with box mesh
//            boxGeometry.addControl(new RigidBodyControl(2));
//            rootNode.attachChild(boxGeometry);
//            space.add(boxGeometry);
//        }
//
//        // immovable sphere with mesh collision shape
//        Sphere sphere = new Sphere(8, 8, 1);
//        Geometry sphereGeometry = new Geometry("Sphere", sphere);
//        sphereGeometry.setMaterial(material);
//        sphereGeometry.setLocalTranslation(4, -4, 2);
//        sphereGeometry.addControl(new RigidBodyControl(new MeshCollisionShape(sphere), 0));
//        rootNode.attachChild(sphereGeometry);
//        space.add(sphereGeometry);

    }
}
