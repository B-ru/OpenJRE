package org.solar.engine.renderer;

import static org.lwjgl.opengl.GL20.*;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.solar.engine.Utils;

public class Shader {

    private final int m_programId; //openGL id of the shader program
    private int vertexShaderId; //openGl id of the vertex shader program
    private int fragmentShaderId; //openGl id of the fragment shader program
    private final Map<String, Integer> m_uniforms; //map of uniform names and locations
    private FloatBuffer m_floatBuffer16; //FloatBuffer for loading matrices

    public static final String uniformProjectionMatrixToken = "u_projectionMatrix"; 
    //public static final String uniformViewMatrixToken = "u_viewMatrix";
    //public static final String uniformTransformMatrixToken = "u_worldMatrix";

    public Shader() {
        m_uniforms = new HashMap<>();
        m_programId = glCreateProgram();
        if (m_programId == 0) Utils.LOG_ERROR("System could not create the shader program");
    }

    /**
     * Creates a shader program from a file which stores two shader, each one starting with a proper token (#vertexShader, #fragmentShader).
     * @param bothShadersFileName Name of the file (including suffix) in which both of our shaders are stored
     */
    public Shader(String bothShadersFileName) throws IOException {

        m_uniforms = new HashMap<>();
        m_programId = glCreateProgram();
        if (m_programId == 0) Utils.LOG_ERROR("System could not create the shader program");
        load(bothShadersFileName);
    }

    /**
     * Creates a shader program from two files containing vertex and fragment shaders.
     * @param vertexShaderName Name of the file (including suffix) containing the vertex shader (no token).
     * @param fragmentShaderName Name of the file (including suffix) containing the fragment shader (no token).
     */
    public Shader(String vertexShaderName, String fragmentShaderName) throws IOException {

        m_uniforms = new HashMap<>();
        m_programId = glCreateProgram();
        if (m_programId == 0) Utils.LOG_ERROR("System could not create the shader program");
        load(vertexShaderName, fragmentShaderName);
    }

    /**
     * Sets a value of a uniform in a shader.
     * @param uniformName Name of the uniform to be set.
     * @param value Data to be sent to the gpu.
     */
    public void setUniform(String uniformName, Matrix4f value) throws RuntimeException {

        // Dump the matrix into a float buffer
        if(m_uniforms.containsKey(uniformName)) {
            if (m_floatBuffer16==null)
                m_floatBuffer16 = BufferUtils.createFloatBuffer(16);
            m_floatBuffer16.clear();
            value.get(m_floatBuffer16);
            glUniformMatrix4fv(m_uniforms.get(uniformName), false, m_floatBuffer16);
            m_floatBuffer16.flip();
        } else {
            Utils.LOG_ERROR("Trying to set value of the uniform that does not exist 1: " + uniformName);
        }
    }

    public void setUniform(String uniformName, PointLight pointLight) {
        setUniform(uniformName + ".colour", pointLight.getColor());
        setUniform(uniformName + ".position", pointLight.getPosition());
        setUniform(uniformName + ".intensity", pointLight.getIntensity());
        PointLight.Attenuation att = pointLight.getAttenuation();
        setUniform(uniformName + ".att.constant", att.getConstant());
        setUniform(uniformName + ".att.linear", att.getLinear());
        setUniform(uniformName + ".att.exponent", att.getExponent());
    }

    public void setUniform(String uniformName, Material material) {
        setUniform(uniformName + ".ambient", material.getAmbientColour());
        setUniform(uniformName + ".diffuse", material.getDiffuseColour());
        setUniform(uniformName + ".specular", material.getSpecularColour());
        setUniform(uniformName + ".hasTexture", material.isTextured() ? 1 : 0);
        setUniform(uniformName + ".reflectance", material.getReflectance());
    }

    public void setUniform(String uniformName, Vector4f value) {
        glUniform4f(m_uniforms.get(uniformName), value.x, value.y, value.z, value.w);
    }

    public void setUniform(String uniformName, Vector3f value) {
        glUniform3f(m_uniforms.get(uniformName), value.x, value.y, value.z);
    }

     /**
     * Sets a value of a uniform in a shader.
     * @param uniformName Name of the uniform to be set.
     * @param value Data to be sent to the gpu.
     */
    public void setUniform(String uniformName, int value) {
        if(m_uniforms.containsKey(uniformName)) {
            glUniform1i(m_uniforms.get(uniformName), value);
        } else {
            Utils.LOG_ERROR("Trying to set value of the uniform that does not exist 2: " + uniformName);
        }
    }

    public void setUniform(String uniformName, float value) {
        glUniform1f(m_uniforms.get(uniformName), value);
    }

    private void generateUniforms(String shaderCode) throws RuntimeException {

        for(int index = shaderCode.indexOf("uniform");index >= 0; index = shaderCode.indexOf("uniform", index + 1)) {

            int nameBeginIndex = index;
            int numOfSpaces = 0;
            while (numOfSpaces != 2) {
                if(shaderCode.charAt(nameBeginIndex) == ' ') {
                    numOfSpaces++;
                }
                nameBeginIndex++;
            }
            int nameEndIndex = nameBeginIndex;
            while(shaderCode.charAt(nameEndIndex) != ';') {
                nameEndIndex++;
            }

            String uniformName = shaderCode.substring(nameBeginIndex, nameEndIndex);

            int uniformLocation = glGetUniformLocation(m_programId, uniformName);

            if (uniformLocation < 0) {
                throw new RuntimeException("Could not find uniform (or it is not used): " + uniformName);
            }

            m_uniforms.put(uniformName, uniformLocation);
        }
    }

    public void createUniform(String uniformName) throws Exception {
        int uniformLocation = glGetUniformLocation(m_programId, uniformName);
        if (uniformLocation < 0) {
            throw new Exception("Could not find uniform:" + uniformName);
        }
        m_uniforms.put(uniformName, uniformLocation);
    }

    public void createPointLightUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".colour");
        createUniform(uniformName + ".position");
        createUniform(uniformName + ".intensity");
        createUniform(uniformName + ".att.constant");
        createUniform(uniformName + ".att.linear");
        createUniform(uniformName + ".att.exponent");
    }

    public void createMaterialUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".ambient");
        createUniform(uniformName + ".diffuse");
        createUniform(uniformName + ".specular");
        createUniform(uniformName + ".hasTexture");
        createUniform(uniformName + ".reflectance");
    }
    /**
     * Loads a shader program from a single file. Vertex shader must beging with #vertexShader 
     * and fragment shader needs to begin with #fragmentShader
     * @param bothShadersFileName Name of the file containing shader code. 
     */
    private void load(String bothShadersFileName) {
        
        try {
            String[] shadersContent = multipleShadersFromFile(bothShadersFileName);
            createVertexShader(shadersContent[0]);
            createFragmentShader(shadersContent[1]);
            link();
            //generateUniforms(shadersContent[0]);
            //generateUniforms(shadersContent[1]);
        } catch (Exception e) {
            Utils.LOG_ERROR("Error while loading shaders from path: " + bothShadersFileName + " , " + e.toString());
        }
    }
    //Load and create shaders from two separate files
    public void load(String vertexShaderName, String fragmentShaderName) {

        try {
            String shaderCode = Utils.getFileAsString(vertexShaderName);
            createVertexShader(shaderCode);
            generateUniforms(shaderCode);
        } catch (Exception e) {

            Utils.LOG_ERROR("Error while loading shaders from path: " + vertexShaderName + " , " + e.toString());
        }

        try{
            String shaderCode = Utils.getFileAsString(fragmentShaderName);
            createFragmentShader(shaderCode);
        } catch (Exception e) {
            Utils.LOG_ERROR("Error while loading shaders from path: " + fragmentShaderName + " , " + e.toString());
        }
        link();
    }

    public void createVertexShader(String shaderCode) throws RuntimeException{
        vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderCode) throws RuntimeException {
        fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }

    protected int createShader(String shaderCode, int shaderType){
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new RuntimeException("Error creating shader. Type: " + shaderType);
        }
        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
        }
        glAttachShader(m_programId, shaderId);
        return shaderId;
    }

    public void link() throws RuntimeException {
        glLinkProgram(m_programId);
        if (glGetProgrami(m_programId, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Error linking Shader code: " + glGetProgramInfoLog(m_programId, 1024));
        }
        if (vertexShaderId != 0) {
            glDetachShader(m_programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(m_programId, fragmentShaderId);
        }
        glValidateProgram(m_programId);
        if (glGetProgrami(m_programId, GL_VALIDATE_STATUS) == 0) {
            Utils.LOG_WARNING("Warning validating Shader code: " + glGetProgramInfoLog(m_programId, 1024));
        }
    }

    public void bind() { glUseProgram(m_programId); }
    public void unbind() { glUseProgram(0); }

    public void cleanup() {
        unbind();
        if (m_programId != 0) {
            glDeleteProgram(m_programId);
        }
    }
    
    public final static String SHADERS_FOLDER_PATH = "src/main/resources/shaders/"; // for jar-builds use: "shaders/"
    public final static int VERTEX_SHADER_IDX = 0;
    public final static int FRAGMENT_SHADER_IDX = 1;
    private final static String VERTEX_SHADER_TOKEN = "#vertexShader";
    private final static String FRAGMENT_SHADER_TOKEN = "#fragmentShader";

    //This function takes a text file and splits it into two after each token
    private static String[] multipleShadersFromFile(String shaderName) throws IOException{
        String vertexShaderContent = "";
        String fragmentShaderContent = "";
        String path = SHADERS_FOLDER_PATH + /*"shaders/"*/ shaderName;
        List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);

        boolean foundVertexShader = false;
        boolean foundFragmentShader = false;
        for(int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            //Checking if the line is our token
            if (line.contains(VERTEX_SHADER_TOKEN)) {
                foundVertexShader = true;
                foundFragmentShader = false;
                continue;
            }
            //Checking if the line is our token
            else if (line.contains(FRAGMENT_SHADER_TOKEN)) {
                foundVertexShader = false;
                foundFragmentShader = true;
                continue;
            }
            if (foundVertexShader) {vertexShaderContent += (lines.get(i) + "\n") ;}
            else if (foundFragmentShader) {fragmentShaderContent += (lines.get(i) + "\n");}
        }

        String[] result = new String[2];
        result[VERTEX_SHADER_IDX] = vertexShaderContent;
        result[FRAGMENT_SHADER_IDX] = fragmentShaderContent;
        return result;
    }
}