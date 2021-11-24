package org.solar;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.solar.engine.*;
import org.solar.engine.renderer.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;
import static org.solar.engine.ModelLoader.loadModel;
import imgui.ImGui;
import java.io.IOException;

public class testApp extends ApplicationTemplate {

    private Camera m_camera;
    private Shader m_testShader;
    private Transform m_testTransform;
    private VertexArray m_testVertexArray;
	private Texture m_texture;

    @Override
    public void initialise() throws Exception{

		m_camera = new Camera(Window.getWidth(), Window.getHeight());
        Renderer.setCameraRefrence(m_camera);

		m_testShader = new Shader("testTextureShader.glsl");
		m_testShader.createUniform("u_projectionMatrix");
		m_testShader.createUniform("u_modelviewmatrix");
		//`m_testShader.createUniform("u_viewMatrix");
		//m_testShader.createUniform("u_worldMatrix");
		m_testShader.createUniform("texture_sampler");
		m_testShader.createMaterialUniform("material");
		m_testShader.createUniform("specularPower");
		m_testShader.createUniform("ambientLight");
		m_testShader.createPointLightUniform("pointLight");

		m_testShader.bind();
		m_testShader.setUniform("u_projectionMatrix", m_camera.getProjectionMatrix());
		m_testShader.setUniform("texture_sampler", 0);
		m_testShader.unbind();

		m_testTransform	= new Transform();

		Event.addWindowResizeCallback((width, height)-> {
			m_testShader.bind();
			m_testShader.setUniform("u_projectionMatrix", m_camera.getProjectionMatrix());
			m_testShader.unbind();
		});


		m_testVertexArray = ModelLoader.loadModel("assets/cube.obj");
		m_texture = new Texture("assets/cube_texture.png", true);
		float reflectance = 0.0f;
		Material material = new Material(m_texture, reflectance);
		m_testVertexArray.setMaterial(material);
		Model testModel = new Model(m_testVertexArray);
		testModel.setPosition(new Vector3f(0f,0f,-5f));
		testModel.setRotation(new Vector3f(-45f,90+45f,0f));
		testModel.setScale(1f);
		Matrix4f modelViewMatrix = m_testTransform.getModelViewMatrix(testModel, m_testTransform.getM_viewMatrix());
		m_testShader.bind();
		m_testShader.setUniform("u_modelviewmatrix",modelViewMatrix);
		m_testShader.setUniform("material", m_testVertexArray.getMaterial());
		Vector3f lightColor = new Vector3f(0.2f,0.2f,0.2f);
		Vector3f lightPosition = new Vector3f(0,0,4);
		Vector3f ambientLight = new Vector3f(0.3f,0.3f,0.3f);
		PointLight pointLight = new PointLight(lightColor, lightPosition, 0.25f);
		m_testShader.setUniform("ambientLight", ambientLight);
		m_testShader.setUniform("pointLight", pointLight);
		m_testShader.unbind();

	}

	@Override
	public void update() {
		//ImGui.text("Hello world!");
		Renderer.setClearColor(new Vector3f(77f/255f, 200f/255f, 233f/255f));
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // we're not using the stencil buffer now

		m_testShader.bind();
		m_testShader.setUniform("texture_sampler", 0);

		// Activate first texture unit
		m_texture.bind();
		// Bind the texture
        Renderer.render(m_testVertexArray, m_testShader, m_testTransform);

		m_testShader.unbind(); 

        m_camera.update();
			
        //ImGui.text("FPS: " +  (int)(10f/Utils.getDeltaTime()));
        //m_testTransform.debugGui("test Transform");
    }

	@Override
	public void render() {

	}

    @Override
    public void terminate() {
        m_testVertexArray.cleanup();

		m_testShader.cleanup();
	}
}
