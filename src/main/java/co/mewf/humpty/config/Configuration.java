package co.mewf.humpty.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import co.mewf.humpty.spi.PipelineElement;

import com.moandjiezana.toml.Toml;

public class Configuration {

  public static enum Mode {
    PRODUCTION, DEVELOPMENT, EXTERNAL;
  }

  public static class Options {
    
    public static final Options EMPTY = new Options(Collections.emptyMap());

    private final Map<String, Object> options;

    public Options(Map<String, Object> options) {
      this.options = options;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
      return options.containsKey(key) ? (T) options.get(key) : defaultValue;
    }
    
    public <T> Optional<T> get(String key) {
      return Optional.ofNullable(get(key, null));
    }

    public boolean containsKey(String key) {
      return options.containsKey(key);
    }
    
    public Map<String, Object> toMap() {
      return new HashMap<>(options);
    }
  }

  private List<Bundle> bundle;
  private Map<String, Object> options;
  
  public static Configuration load(String tomlPath) {
    if (tomlPath.startsWith("/")) {
      tomlPath = tomlPath.substring(1);
    }
    return new Toml().parse(Thread.currentThread().getContextClassLoader().getResourceAsStream(tomlPath)).to(Configuration.class);
  }

  public List<Bundle> getBundles() {
    return bundle;
  }
  
  @SuppressWarnings("unchecked")
  public Configuration.Options getOptionsFor(PipelineElement pipelineElement) {
    String name = pipelineElement.getName();
    
    if (options == null || !options.containsKey(name)) {
      return Options.EMPTY;
    }
    
    return new Options((Map<String, Object>) options.get(name));
  }
}
