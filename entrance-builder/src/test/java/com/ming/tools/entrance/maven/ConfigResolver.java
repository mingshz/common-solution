package com.ming.tools.entrance.maven;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

/**
 * @author CJ
 */
@Component(role = ConfigResolver.class, instantiationStrategy = "singleton")
public class ConfigResolver implements Initializable {
    @Override
    public void initialize() throws InitializationException {

    }
}
