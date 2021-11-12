package org.solar.engine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.nio.charset.*;

public class Utils {
    public final static String ABS_PROJECT_PATH        = "src/main/resources/shaders/";
    public final static char    VERTEX_SHADER_IDX       = 0;
    public final static char    FRAGMENT_SHADER_IDX     = 1;
    private final static String VERTEX_SHADER_TOKEN     = "#vertexShader";
    private final static String FRAGMENT_SHADER_TOKEN   = "#fragmentShader";

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    private static void print(Object o){
        System.out.println(o.toString());
    }

    public static void LOG_SUCCESS(Object o) {System.out.println(ANSI_GREEN + o.toString() + ANSI_RESET);}
    public static void LOG_ERROR(Object o) {System.out.println(ANSI_RED + o.toString() + ANSI_RESET);}
    public static void LOG_WARNING(Object o) {System.out.println(ANSI_YELLOW + o.toString() + ANSI_RESET);}
    public static void LOG_INFO(Object o) {System.out.println(ANSI_BLUE + o.toString() + ANSI_RESET);}
    public static void LOG(Object o) {System.out.println(ANSI_BLUE + o.toString() + ANSI_RESET);}

    private static long m_startDeltaTime = 0;

    private static float m_deltaTime  = 0;

    public static void updateDeltaTime() {
        long time = System.nanoTime();
        m_deltaTime = ((float)(time - m_startDeltaTime)) / 100000000f;
        m_startDeltaTime = time;
    }

    public static float getDeltaTime() {return m_deltaTime;}

    //Returning content of the text file as String
    public static String getShaderStringFromFile(String shaderName) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader br = new BufferedReader( new FileReader( shaderName ) );
        br.lines()
            .forEach(line -> stringBuffer.append(line).append("\n"));
        br.close();
        return stringBuffer.toString();
    }
    //This function takes a text file and splits it into String array after each token
    public static String[] getTwoShaderStringsFromFile(String shaderName) throws IOException{
        boolean foundVertexShader = false;
        boolean foundFragmentShader = false;
        String[] result = {"",""};
        List<String> lines = Files.readAllLines( Paths.get( shaderName ), StandardCharsets.UTF_8 );
        for ( String line : lines ) {

            if (line.contains(VERTEX_SHADER_TOKEN)) {
                foundVertexShader = true;
                foundFragmentShader = false;
                continue;
            }

            else if ( line.contains(FRAGMENT_SHADER_TOKEN) ) {
                foundVertexShader = false;
                foundFragmentShader = true;
                continue;
            }

            if ( foundVertexShader ) {
                result[VERTEX_SHADER_IDX] += (line + "\n");
            } else if ( foundFragmentShader ) {
                result[FRAGMENT_SHADER_IDX] += (line + "\n");
            }
        }
        return result;
    }
}