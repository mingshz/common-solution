package com.ming.tools.entrance.maven;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author CJ
 */
public class BuildMojoTest {

    @Rule
    public MojoRule rule = new MojoRule();

    @Test
    @Ignore
    public void testGo() throws Exception {
        BuildMojo mojo = getMojo("project_with_nothing/pom.xml");
        assertThat(mojo.getName())
                .isEqualToIgnoringCase("project_with_nothing-entrance");
        assertThat(mojo.getVersion())
                .isEqualTo("1.0.0-SNAPSHOT");
    }

    private BuildMojo getMojo(String classPath) throws Exception {
        MavenProject pom = rule.readMavenProject(new ClassPathResource(classPath).getFile().getParentFile());
        assertThat(pom)
                .isNotNull();

        // 开始干活
        final BuildMojo mojo = (BuildMojo) rule.lookupMojo("build", pom.getFile());
        if (mojo.getProject() == null)
            mojo.setProject(pom);
        if (mojo.getName() == null)
            mojo.setName("name");
        if (mojo.getVersion() == null)
            mojo.setVersion("1.0");
        if (mojo.getLog() == null)
            mojo.setLog(new SystemStreamLog());
        return mojo;
//        return (BuildMojo) rule.lookupConfiguredMojo(pom, "build");
    }

    @Test
    public void withServers() throws Exception {
        BuildMojo mojo = getMojo("project_with_servers/pom.xml");

        mojo.execute();
        assertThat(mojo.getApiServers())
                .isNotNull()
                .isNotEmpty();
    }

}