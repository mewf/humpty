package co.mewf.humpty.tools;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import co.mewf.humpty.Pipeline;
import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.HumptyBootstrap;

import com.moandjiezana.toml.Toml;

public class DigesterTest {
  
  @Rule
  public TemporaryFolder tmpDir = new TemporaryFolder();
  private Path digestTomlPath;
  private Path buildDir;
  
  private final Configuration configuration = Configuration.load("DigesterTest/humpty.toml");
  private final Pipeline pipeline = new HumptyBootstrap(configuration).createPipeline();
  private final Digester digest = new Digester();
  
  @Before
  public void before() {
    digestTomlPath = tmpDir.getRoot().toPath().resolve("humpty-digest.toml");
    buildDir = tmpDir.getRoot().toPath().resolve("build");
  }
  
  @Test
  public void should_provide_bundle_digest() throws Exception {
    digest.processBundles(pipeline, configuration.getBundles(), buildDir, digestTomlPath);
    
    Toml digestToml = new Toml().parse(digestTomlPath.toFile());
    
    assertEquals("app-humpty" + hash(pipeline.process("app.js").getAsset()) + ".js", digestToml.getString("app.js"));
  }
  
  @Test
  public void should_return_null_for_unknown_asset() throws Exception {
    digest.processBundles(pipeline, configuration.getBundles(), buildDir, digestTomlPath);
    
    Toml digestToml = new Toml().parse(digestTomlPath.toFile());
    
    assertNull(digestToml.getString("unknown"));
  }
  
  @Test
  public void should_write_bundles_to_build_directory() throws Exception {
    digest.processBundles(pipeline, configuration.getBundles(), buildDir, digestTomlPath);

    Toml digestToml = new Toml().parse(digestTomlPath.toFile());
    
    List<String> compiledAssets = Files.list(buildDir).map(Path::getFileName).map(Path::toString).sorted().collect(Collectors.toList());
    
    assertThat(compiledAssets, contains(digestToml.getString("app.js"), digestToml.getString("app.js") + ".gz", digestToml.getString("bapp.js"), digestToml.getString("bapp.js") + ".gz"));
  }
  
  @Test
  public void should_always_process_bundles_in_production_mode() throws Exception {
    Pipeline pipeline = mock(Pipeline.class);
    when(pipeline.process("app.js", Configuration.Mode.PRODUCTION)).thenReturn(new Pipeline.Output("", "abc"));
    when(pipeline.process("bapp.js", Configuration.Mode.PRODUCTION)).thenReturn(new Pipeline.Output("", "abc"));
    
    digest.processBundles(pipeline, configuration.getBundles(), buildDir, digestTomlPath);
  }

  private String hash(String asset) throws Exception {
    MessageDigest messageDigest = MessageDigest.getInstance("md5");
    messageDigest.update(asset.getBytes());

    return DatatypeConverter.printHexBinary(messageDigest.digest());
  }
}