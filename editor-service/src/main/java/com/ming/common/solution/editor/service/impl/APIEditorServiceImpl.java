package com.ming.common.solution.editor.service.impl;

import com.ming.common.solution.Project;
import com.ming.common.solution.editor.model.WatchSession;
import com.ming.common.solution.editor.service.APIEditorService;
import com.ming.common.solution.event.NewProjectEvent;
import com.ming.common.solution.service.ProjectService;
import com.ming.common.solution.service.impl.FileProjectService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ming.common.solution.service.impl.FileProjectService.workWithLocalRepository;

/**
 * @author CJ
 */
@Service
public class APIEditorServiceImpl implements APIEditorService {

    private static final Log log = LogFactory.getLog(APIEditorServiceImpl.class);
    private static final String API_FILE_NAME = "api.yaml";
    private final List<WatchSession> sessionList = Collections.synchronizedList(new ArrayList<>());
    @Autowired
    private ProjectService projectService;

    @Override
    public void onNewProject(NewProjectEvent event) {
        log.debug("New:" + event);
        // 目前仅仅支持FileProject
        workWithLocalRepository(event.getProject(), (git, path) -> {
            try {
                writeAPIFile(path, new ClassPathResource("/defaultApi.yaml").getInputStream());
                git.add().addFilepattern(API_FILE_NAME).call();

                git.commit().setMessage("init").call();

                git.push().call();
            } catch (IOException | GitAPIException ex) {
                throw new IllegalStateException(ex);
            }
            return null;
        });
    }

    private void writeAPIFile(Path path, InputStream data) throws IOException {
//        Path apiFile = Files.createFile(path.resolve(API_FILE_NAME));
        final Path apiFile = path.resolve(API_FILE_NAME);
        if (!Files.exists(apiFile))
            Files.createFile(apiFile);
        try (BufferedWriter writer = Files.newBufferedWriter(apiFile, Charset.forName("UTF-8")
                , StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(StreamUtils.copyToString(data, Charset.forName("UTF-8")));
            writer.flush();
        }
    }

    @Override
    public String readAPI(Project project, String branch) {
        return workWithLocalRepository(project, (git, path) -> {
            try {
                // checkout 到特定分支
                FileProjectService.checkoutBranch(git, branch);
                // 打开那个文件
                git.pull();
                return new String(Files.readAllBytes(path.resolve(API_FILE_NAME)), "UTF-8");
            } catch (IOException | GitAPIException ex) {
                throw new IllegalStateException(ex);
            }
        });
    }

    @Override
    public String writeAPI(Project project, String branch, String api) {
        workWithLocalRepository(project, (git, path) -> {
            try {
                // checkout 到特定分支
                Ref branchRef = FileProjectService.checkoutBranch(git, branch);
                // 写入新的文件
                writeAPIFile(path, new ByteArrayInputStream(api.getBytes("UTF-8")));
                git.add().addFilepattern(API_FILE_NAME).call();
                git.commit()
                        .setMessage("... change the api via Editor.")
                        .call();
                git.push()
                        .add(branchRef)
                        .call();
            } catch (IOException | GitAPIException ex) {
                throw new IllegalStateException(ex);
            }
            return null;
        });
        String id = projectService.commitId(project.getId(), branch);
        try {
            dispatchBranchUpdateEvent(project, branch, id);
        } catch (Throwable ex) {
            log.warn("dispatch branch update event", ex);
        }
        return id;
    }

    private void dispatchBranchUpdateEvent(Project project, String branch, String commitId) {
        sessionList.removeIf(watchSession -> !watchSession.getSession().isOpen());
        sessionList.stream()
                .filter(watchSession -> watchSession.match(project, branch))
                .forEach(watchSession -> watchSession.sendLastCommitId(commitId));
    }

    @Override
    public void watch(WatchSession session) {
        sessionList.add(session);
    }

    @Override
    public void closeWatch(String id) {
        sessionList.removeIf(watchSession -> watchSession.getSession().getId().equalsIgnoreCase(id));
    }
}
