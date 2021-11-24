package org.solar.engine;

import org.joml.Vector3f;
import org.solar.engine.renderer.VertexArray;

public class Model {

    private VertexArray object = null;
    private Vector3f position = new Vector3f();
    private Vector3f rotation = new Vector3f();
    private float scale = 1.0f;

    public Model(VertexArray object){
        this.object = object;
    }

    public VertexArray getObject() {
        return object;
    }

    public void setObject(VertexArray object) {
        this.object = object;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}
