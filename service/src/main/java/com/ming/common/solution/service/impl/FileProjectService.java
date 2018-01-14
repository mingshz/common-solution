package com.ming.common.solution.service.impl;

import com.ming.common.solution.Project;
import com.ming.common.solution.event.NewProjectEvent;
import com.ming.common.solution.service.ProjectService;
import com.ming.common.solution.service.file.FileProject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 以文件方式维护的项目；基于IO的方式 此类项目的互动必须建立在互斥的基础上
 *
 * @author CJ
 */
@Service
public class FileProjectService implements ProjectService {

    private static final Log log = LogFactory.getLog(FileProjectService.class);
    private final Path home;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public FileProjectService(Environment environment) throws IOException {
        // 默认就是在 target/home
        this.home = Paths.get(environment.getProperty("project.home"
                , new File("target/home").getAbsoluteFile().toString()));
        if (!Files.exists(home))
            Files.createDirectories(home);
    }

    public static <T> T workWithLocalRepository(Project project, BiFunction<Git, Path, T> function) {
        FileProject fileProject = (FileProject) project;

        try {
            Path workingTree = Files.createTempDirectory("_C_S_T_G");
            try {
                Git git = Git.cloneRepository()
                        .setCloneAllBranches(true)
                        .setDirectory(workingTree.toFile())
                        .setURI(fileProject.getPath().toUri().toString())
                        .call();
                git.fetch().call();
                return function.apply(git, workingTree);

            } finally {
                FileSystemUtils.deleteRecursively(workingTree.toFile());
            }

        } catch (IOException | GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T workWithRepository(Project project, Function<Repository, T> function) {
        FileProject fileProject = (FileProject) project;
        try {
            try (Repository repository = new FileRepositoryBuilder()
                    .setGitDir(fileProject.getPath().toFile())
                    .setBare()
                    .build()) {
                return function.apply(repository);
            }

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Ref checkoutBranch(Git git, String branch) throws GitAPIException {
        // 看看本地分支在不在；不在就使用远程分支进行创建
        if (git.branchList().call().stream()
                .anyMatch(ref -> ref.getName().substring("refs/heads/".length()).equals(branch))) {
            // 直接checkout
            return git.checkout().setName(branch).call();
        } else {
//            git.branchCreate();
            return git.checkout().setName(branch)
                    .setCreateBranch(true)
                    .setStartPoint("origin/" + branch)
                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                    .call();
        }
    }

    @Override
    public Project byId(String id) {
        try {
            return Files.list(home).filter(isProjectDir())
                    .filter(path -> path.endsWith(id))
                    .map(fromPathToProject())
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("找不到" + id));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Function<Path, Project> fromPathToProject() {
        return path -> new FileProject(path.getName(path.getNameCount() - 1).toString(), path);
    }

    private Predicate<Path> isProjectDir() {
        return path -> Files.isDirectory(path);
    }

    @Override
    public void deleteProject(String id) {
        final Path path = home.resolve(id);
        if (Files.exists(path)) {
            log.info("删除项目" + id);
            try {
                deleteFile(path);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private void deleteFile(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.list(path)
                    .forEach(path1 -> {
                        try {
                            deleteFile(path1);
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    });
            Files.delete(path);
        } else
            Files.deleteIfExists(path);
    }

    @Override
    public Project newProject(String id) {
        if (projectStream().anyMatch(project -> project.getId().equals(id))) {
            throw new IllegalArgumentException(id + "is really existing.");
        }
        try {
            final Path path = home.resolve(id);

            Git.init().setGitDir(path.toFile())
                    .setBare(true)
                    .call();
            final Project project = fromPathToProject().apply(path);
            applicationEventPublisher.publishEvent(new NewProjectEvent(project, null));
            return project;
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Stream<Project> projectStream() {
        try {
            return Files.list(home)
                    .filter(isProjectDir())
                    .map(fromPathToProject());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String[] branches(String id) {
        return workWithRepository(byId(id), repository -> repository.getAllRefs().entrySet()
                .stream()
                .filter(stringRefEntry -> !"HEAD".equals(stringRefEntry.getKey()))
                .filter(stringRefEntry -> stringRefEntry.getKey().startsWith("refs/heads/"))
                .filter(stringRefEntry -> !stringRefEntry.getValue().isPeeled())
                .map(Map.Entry::getKey)
                .map(str -> str.substring("refs/heads/".length()))
                .toArray(String[]::new));
    }

    @Override
    public void newBranch(String id, String fromBranch, String toBranch) {
        workWithLocalRepository(byId(id), ((git, path) -> {
            try {
                git.checkout().setName(fromBranch).call();
                Ref newBranch = git.branchCreate()
                        .setName(toBranch)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .call();
                git.push()
                        .add(newBranch)
                        .call();
            } catch (GitAPIException e) {
                throw new IllegalStateException(e);
            }
            return null;
        }));
    }

    @Override
    public String commitId(String id, String branch) {
        return workWithRepository(byId(id), repository -> {
            try {
                Ref ref = repository.findRef("refs/heads/" + branch);
                return ref.getObjectId().name();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
