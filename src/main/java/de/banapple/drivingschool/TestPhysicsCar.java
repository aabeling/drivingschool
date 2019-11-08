/*
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.banapple.drivingschool;

import java.util.*;

import com.jme3.app.*;
import com.jme3.bullet.*;
import com.jme3.bullet.collision.shapes.*;
import com.jme3.bullet.control.*;
import com.jme3.input.*;
import com.jme3.input.controls.*;
import com.jme3.light.*;
import com.jme3.material.*;
import com.jme3.math.*;
import com.jme3.scene.*;
import com.jme3.scene.control.CameraControl.*;
import com.jme3.scene.plugins.blender.*;
import com.jme3.scene.shape.*;
import com.jme3.system.*;

public class TestPhysicsCar
        extends SimpleApplication
        implements ActionListener, AnalogListener {

    private BulletAppState bulletAppState;
    private VehicleControl vehicle;
    private final float accelerationForce = 1000.0f;
    private final float brakeForce = 100.0f;
    private float steeringValue = 0;
    private float accelerationValue = 0;
    private Vector3f jumpForce = new Vector3f(0, 3000, 0);

    private Node vehicleNode;

    private CameraNode camNode;

    public static void main(String[] args) {

        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 768);
        settings.setUseJoysticks(true);

        TestPhysicsCar app = new TestPhysicsCar();
        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {

        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        bulletAppState.setDebugEnabled(false);

        AmbientLight light = new AmbientLight();
        light.setColor(ColorRGBA.LightGray);
        rootNode.addLight(light);

        // PhysicsTestHelper.createPhysicsTestWorld(rootNode, assetManager,
        // bulletAppState.getPhysicsSpace());

        // addMeshTestFloor();
        // addBlenderModel();

        addChessBoardFloor(100, 20f, bulletAppState.getPhysicsSpace());

        setupKeys();
        buildPlayer();

        useChaseNode();
    }

    /**
     * Creates a floor build of black and white tiles.
     * 
     * @param count
     *            the number of tiles in each direction
     * @param tileSize
     *            the size of the tiles
     */
    void addChessBoardFloor(
            int count,
            float tileSize,
            PhysicsSpace space) {

        Material blackMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        blackMaterial.setColor("Color", ColorRGBA.Black);
        Material whiteMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        whiteMaterial.setColor("Color", ColorRGBA.White);

        /* offset for the local translation to center the floor */
        float offset = -count * tileSize / 2;

        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {

                Box tile = new Box(tileSize / 2, 0.25f, tileSize / 2);
                Geometry tileGeometry = new Geometry("tile-" + i + "-" + j, tile);
                if ((i + j) % 2 == 0) {
                    tileGeometry.setMaterial(blackMaterial);
                } else {
                    tileGeometry.setMaterial(whiteMaterial);
                }
                tileGeometry.setLocalTranslation(
                        offset + i * tileSize,
                        -5,
                        offset + j * tileSize);
                tileGeometry.addControl(new RigidBodyControl(0));
                rootNode.attachChild(tileGeometry);
                space.add(tileGeometry);
            }
        }
    }

    void addMeshTestFloor() {

        Geometry floor = PhysicsTestHelper.createMeshTestFloor(assetManager, 100.0f, new Vector3f(-50, -1, -50));
        rootNode.attachChild(floor);
        bulletAppState.getPhysicsSpace().add(floor);
    }

    /**
     * Hiermit habe ich versucht, ein blender-model von Preetz darzustellen. Da
     * Preetz über NN liegt, musste das city-Model nach unten verschoben werden,
     * damit das Auto darauf landet. Ansonsten wäre das city-Model oberhalb des
     * von PhysicsTestHelper.createPhysicsTestWorld erstellten Floors gewesen.
     */
    void addBlenderModel() {

        Material mat = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(false);
        mat.setColor("Color", ColorRGBA.Green);
        assetManager.registerLoader(BlenderLoader.class, "blend");
        Spatial city = assetManager.loadModel("preetz.blend");
        city.setMaterial(mat);
        city.setLocalTranslation(-10, -40, 0);
        city.addControl(new RigidBodyControl(0));
        bulletAppState.getPhysicsSpace().add(city);
        rootNode.attachChild(city);
    }

    void useChaseNode() {

        // Disable the default flyby cam
        flyCam.setEnabled(false);
        // create the camera Node
        camNode = new CameraNode("Camera Node", cam);
        // This mode means that camera copies the movements of the target:
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        // Attach the camNode to the target:
        vehicleNode.attachChild(camNode);
        // Move camNode, e.g. behind and above the target:
        setCamPositionChase();
        // Rotate the camNode to look at the target:
        camNode.lookAt(vehicleNode.getLocalTranslation(), Vector3f.UNIT_Y);
    }

    void useChaseCamera() {

        // Disable the default flyby cam
        flyCam.setEnabled(false);
        // Enable a chase cam for this target (typically the player).
        ChaseCamera chaseCam = new ChaseCamera(cam, vehicleNode,
                inputManager);
        chaseCam.setSmoothMotion(false);
    }

    private PhysicsSpace getPhysicsSpace() {
        return bulletAppState.getPhysicsSpace();
    }

    private void setupKeys() {

        Joystick[] joysticks = inputManager.getJoysticks();
        if (joysticks == null) {
            throw new IllegalStateException("Cannot find any joysticks!");
        }
        Arrays.asList(joysticks).forEach(j -> System.out.println(j.getXAxis().getDeadZone()));
        if (joysticks.length != 1) {
            throw new IllegalStateException("more than one joystick");
        }
        Joystick joystick = joysticks[0];

        inputManager.addMapping("Lefts", new JoyAxisTrigger(
                joystick.getJoyId(),
                joystick.getXAxisIndex(), true));
        inputManager.addMapping("Rights", new JoyAxisTrigger(
                joystick.getJoyId(),
                joystick.getXAxisIndex(), false));
        inputManager.addMapping("Ups", new JoyAxisTrigger(
                joystick.getJoyId(),
                joystick.getYAxisIndex(), true));
        inputManager.addMapping("Downs", new JoyAxisTrigger(
                joystick.getJoyId(),
                joystick.getYAxisIndex(), false));

        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));

        inputManager.addMapping("Cam1", new KeyTrigger(KeyInput.KEY_1));
        inputManager.addMapping("Cam2", new KeyTrigger(KeyInput.KEY_2));
        inputManager.addListener(this, "Cam1", "Cam2");

        inputManager.addListener(this, "Lefts");
        inputManager.addListener(this, "Rights");
        inputManager.addListener(this, "Ups");
        inputManager.addListener(this, "Downs");
        inputManager.addListener(this, "Space");
        inputManager.addListener(this, "Reset");
    }

    private void buildPlayer() {
        Material mat = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(false);
        mat.setColor("Color", ColorRGBA.Red);

        // create a compound shape and attach the BoxCollisionShape for the car
        // body at 0,1,0
        // this shifts the effective center of mass of the BoxCollisionShape to
        // 0,-1,0
        CompoundCollisionShape compoundShape = new CompoundCollisionShape();
        BoxCollisionShape box = new BoxCollisionShape(new Vector3f(1.2f, 0.5f, 2.4f));
        compoundShape.addChildShape(box, new Vector3f(0, 1, 0));

        // create vehicle node
        vehicleNode = new Node("vehicleNode");
        vehicle = new VehicleControl(compoundShape, 400);
        vehicleNode.addControl(vehicle);

        // setting suspension values for wheels, this can be a bit tricky
        // see also
        // https://docs.google.com/Doc?docid=0AXVUZ5xw6XpKZGNuZG56a3FfMzU0Z2NyZnF4Zmo&hl=en
        float stiffness = 60.0f;// 200=f1 car
        float compValue = .3f; // (should be lower than damp)
        float dampValue = .4f;
        vehicle.setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness));
        vehicle.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
        vehicle.setSuspensionStiffness(stiffness);
        vehicle.setMaxSuspensionForce(10000.0f);

        // Create four wheels and add them at their locations
        Vector3f wheelDirection = new Vector3f(0, -1, 0); // was 0, -1, 0
        Vector3f wheelAxle = new Vector3f(-1, 0, 0); // was -1, 0, 0
        float radius = 0.5f;
        float restLength = 0.3f;
        float yOff = 0.5f;
        float xOff = 1f;
        float zOff = 2f;

        float width = radius * 0.6f;
        Cylinder wheelMesh = new Cylinder(16, 16, radius, width, true);

        Node node1 = new Node("wheel 1 node");
        Geometry wheels1 = new Geometry("wheel 1", wheelMesh);
        node1.attachChild(wheels1);
        wheels1.rotate(0, FastMath.HALF_PI, 0);
        wheels1.setMaterial(mat);
        vehicle.addWheel(node1, new Vector3f(-xOff, yOff, zOff),
                wheelDirection, wheelAxle, restLength, radius, true);

        Node node2 = new Node("wheel 2 node");
        Geometry wheels2 = new Geometry("wheel 2", wheelMesh);
        node2.attachChild(wheels2);
        wheels2.rotate(0, FastMath.HALF_PI, 0);
        wheels2.setMaterial(mat);
        vehicle.addWheel(node2, new Vector3f(xOff, yOff, zOff),
                wheelDirection, wheelAxle, restLength, radius, true);

        Node node3 = new Node("wheel 3 node");
        Geometry wheels3 = new Geometry("wheel 3", wheelMesh);
        node3.attachChild(wheels3);
        wheels3.rotate(0, FastMath.HALF_PI, 0);
        wheels3.setMaterial(mat);
        vehicle.addWheel(node3, new Vector3f(-xOff, yOff, -zOff),
                wheelDirection, wheelAxle, restLength, radius, false);

        Node node4 = new Node("wheel 4 node");
        Geometry wheels4 = new Geometry("wheel 4", wheelMesh);
        node4.attachChild(wheels4);
        wheels4.rotate(0, FastMath.HALF_PI, 0);
        wheels4.setMaterial(mat);
        vehicle.addWheel(node4, new Vector3f(xOff, yOff, -zOff),
                wheelDirection, wheelAxle, restLength, radius, false);

        vehicleNode.attachChild(node1);
        vehicleNode.attachChild(node2);
        vehicleNode.attachChild(node3);
        vehicleNode.attachChild(node4);

        rootNode.attachChild(vehicleNode);

        /* reduce rolling */
        for (int wheel = 0; wheel < vehicle.getNumWheels(); wheel++) {
            vehicle.setRollInfluence(wheel, 0.3f);
        }

        getPhysicsSpace().add(vehicle);
    }

    @Override
    public void simpleUpdate(float tpf) {

        cam.lookAt(vehicle.getPhysicsLocation(), Vector3f.UNIT_Y);
    }

    public void onAction(String binding, boolean value, float tpf) {

        if (binding.equals("Space")) {
            if (value) {
                vehicle.applyImpulse(jumpForce, Vector3f.ZERO);
            }
        } else if (binding.equals("Reset")) {
            if (value) {
                System.out.println("Reset");
                vehicle.setPhysicsLocation(Vector3f.ZERO);
                vehicle.setPhysicsRotation(new Matrix3f());
                vehicle.setLinearVelocity(Vector3f.ZERO);
                vehicle.setAngularVelocity(Vector3f.ZERO);
                vehicle.resetSuspension();
            }
        } else if ("Cam1".equals(binding) && value) {
            setCamPositionChase();
        } else if ("Cam2".equals(binding) && value) {
            setCamPositionDriver();
        }
    }

    private void setCamPositionDriver() {

        camNode.setLocalTranslation(new Vector3f(0.4f, 1.5f, -0.5f));
    }

    private void setCamPositionChase() {

        camNode.setLocalTranslation(new Vector3f(0, 2, -10));
    }

    @Override
    public void onAnalog(String binding, float value, float tpf) {

        if (binding.equals("Lefts")) {
            steer(+1, value, tpf);
        } else if (binding.equals("Rights")) {
            steer(-1, value, tpf);
        } else if (binding.equals("Ups")) {
            accelerationValue = accelerationForce * value / tpf;
            vehicle.accelerate(accelerationValue);
            vehicle.brake(0.0f);
        } else if (binding.equals("Downs")) {
            vehicle.accelerate(0.0f);
            float brakeValue = brakeForce * value / tpf;
            vehicle.brake(brakeValue);
        } else {
            vehicle.brake(0.0f);
        }

    }
    
    private void steer(int direction, float value, float tpf) {
        
        float relativeValue = value / tpf;
        System.out.println(relativeValue);
        if (relativeValue < 0.1) {
            vehicle.steer(0.0f);
        } else {
            steeringValue = direction * 0.5f * relativeValue;
            vehicle.steer(steeringValue);
        }
    }
}
