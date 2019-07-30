package com.felixklauke.kira.core;

import com.felixklauke.kira.core.exception.KiraModelException;
import com.felixklauke.kira.core.io.KiraReader;
import com.felixklauke.kira.core.io.KiraWriter;
import com.felixklauke.kira.core.io.SimpleKiraReader;
import com.felixklauke.kira.core.io.SimpleKiraWriter;
import com.felixklauke.kira.core.mapper.Mapper;
import com.felixklauke.kira.core.mapper.MapperManager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleKira implements Kira {

  /**
   * The name of the pseudo parent map.
   */
  private static final String ROOT_MAP_NAME = "root";

  /**
   * The logger to log general actions.
   */
  private final Logger logger = Logger.getLogger(SimpleKira.class.getSimpleName());

  /**
   * The mapper manager that delivers the right mappers.
   */
  private final MapperManager mapperManager;

  /**
   * Create a new kira instance by the underlying mapper manager.
   *
   * @param mapperManager The mapper manager.
   */
  SimpleKira(MapperManager mapperManager) {
    this.mapperManager = mapperManager;
  }

  @Override
  public <ModelType> Map<String, Object> serialize(ModelType model) {

    // Get mapper
    Class<?> modelClass = model.getClass();
    Mapper<ModelType> mapper = (Mapper<ModelType>) mapperManager.getMapper(modelClass);

    // Construct data and writer
    Map<String, Object> data = new HashMap<>();
    KiraWriter writer = new SimpleKiraWriter(data);

    // Serialize
    try {
      mapper.write(writer, ROOT_MAP_NAME, model);
    } catch (KiraModelException e) {
      logger.log(Level.SEVERE, "Couldn't serialize model.", e);
      return new HashMap<>();
    }

    return (Map<String, Object>) data.get(ROOT_MAP_NAME);
  }

  @Override
  public <ModelType> ModelType deserialize(Map<String, Object> data, Class<ModelType> modelClass) {

    // Construct root map
    Map<String, Object> root = new HashMap<>();
    root.put("root", data);

    // Get mapper
    Mapper<ModelType> mapper = mapperManager.getMapper(modelClass);

    // Construct model and reader
    KiraReader reader = new SimpleKiraReader(root);

    try {
      return mapper.read(reader, ROOT_MAP_NAME, modelClass);
    } catch (KiraModelException e) {
      logger.log(Level.SEVERE, "Couldn't deserialize model.", e
      );
      return null;
    }
  }
}