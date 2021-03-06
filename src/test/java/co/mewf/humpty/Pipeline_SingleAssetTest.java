package co.mewf.humpty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.webjars.WebJarAssetLocator;

import co.mewf.humpty.config.Configuration;
import co.mewf.humpty.config.HumptyBootstrap;
import co.mewf.humpty.spi.listeners.TracerPipelineListener;

public class Pipeline_SingleAssetTest {
  
  private final WebJarAssetLocator locator = new WebJarAssetLocator();
  private final Pipeline pipeline = new HumptyBootstrap(Configuration.load("co/mewf/humpty/should_process_asset_in_bundle.toml")).createPipeline();

  @Test
  public void should_process_asset_within_bundle() throws Exception {
    String result = pipeline.process("asset.js/blocks.coffee").getAsset();
    
    assertEquals(read("blocks.js") + "\n", result);
  }
  
  @Test
  public void should_call_bundle_listeners_for_single_asset() throws Exception {
    pipeline.process("asset.js/blocks.coffee");

    TracerPipelineListener tracer = pipeline.getPipelineListener(TracerPipelineListener.class).get();
    
    assertEquals("asset.js/blocks.coffee", tracer.processedBundleName);
  }
  
  @Test
  public void should_call_asset_listeners() throws Exception {
    pipeline.process("asset.js/blocks.coffee");

    TracerPipelineListener tracer = pipeline.getPipelineListener(TracerPipelineListener.class).get();
    
    assertTrue("Did not call asset listener", tracer.onAssetProcessedCalled);
  }
  
  private String read(String filename) {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(locator.getFullPath(filename))) {
      return IOUtils.toString(is);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
