package org.solar.engine;

import java.io.IOException;

public abstract class ApplicationTemplate {
    public abstract void initialise() throws Exception;
    public abstract void update();
    public abstract void terminate();
    public abstract void render();
}
