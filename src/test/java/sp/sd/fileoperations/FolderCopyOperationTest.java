package sp.sd.fileoperations;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FolderCopyOperationTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testDefaults() {
        String sourceFolderPath = "sourceFolder";
        String destinationFolderPath = "destinationFolder";
        FolderCopyOperation operation = new FolderCopyOperation(sourceFolderPath, destinationFolderPath);

        assertEquals(sourceFolderPath, operation.getSourceFolderPath());
        assertEquals(destinationFolderPath, operation.getDestinationFolderPath());
    }

    @Test
    public void testRunFolderCopyOperation() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("folderCopyTest");

        FilePath workspace = jenkins.jenkins.getWorkspaceFor(project);
        FilePath sourceFolder = new FilePath(workspace, "sourceFolder");
        FilePath destinationFolder = new FilePath(workspace, "destinationFolder");

        sourceFolder.mkdirs();
        FilePath fileInSource = new FilePath(sourceFolder, "file.txt");
        fileInSource.write("Sample content", "UTF-8");

        FolderCopyOperation operation = new FolderCopyOperation("sourceFolder", "destinationFolder");
        project.getBuildersList().add(new FileOperationsBuilder(List.of(operation)));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());

        FilePath copiedFile = new FilePath(destinationFolder, "file.txt");
        assertTrue("The destination folder should have been created", destinationFolder.exists());
        assertTrue("The file should have been copied to the destination folder", copiedFile.exists());
        assertEquals("Sample content", copiedFile.readToString());
    }

    @Test
    public void testRunFolderCopyOperationWithTokens() throws Exception {
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        envVars.put("SOURCE_FOLDER", "sourceFolder");
        envVars.put("DESTINATION_FOLDER", "destinationFolder");
        jenkins.jenkins.getGlobalNodeProperties().add(prop);

        FreeStyleProject project = jenkins.createFreeStyleProject("folderCopyTestWithTokens");

        FilePath workspace = jenkins.jenkins.getWorkspaceFor(project);
        FilePath sourceFolder = new FilePath(workspace, "sourceFolder");
        FilePath destinationFolder = new FilePath(workspace, "destinationFolder");

        sourceFolder.mkdirs();
        FilePath fileInSource = new FilePath(sourceFolder, "file.txt");
        fileInSource.write("Sample content", "UTF-8");

        FolderCopyOperation operation = new FolderCopyOperation("$SOURCE_FOLDER", "$DESTINATION_FOLDER");
        project.getBuildersList().add(new FileOperationsBuilder(List.of(operation)));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());

        FilePath copiedFile = new FilePath(destinationFolder, "file.txt");
        assertTrue("The destination folder should have been created", destinationFolder.exists());
        assertTrue("The file should have been copied to the destination folder", copiedFile.exists());
        assertEquals("Sample content", copiedFile.readToString());
    }
}

