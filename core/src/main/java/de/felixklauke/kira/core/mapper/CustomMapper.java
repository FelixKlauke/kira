package de.felixklauke.kira.core.mapper;

import de.felixklauke.kira.core.io.KiraReader;
import de.felixklauke.kira.core.io.KiraWriter;
import de.felixklauke.kira.core.io.SimpleKiraReader;
import de.felixklauke.kira.core.io.SimpleKiraWriter;
import de.felixklauke.kira.core.meta.ModelMeta;
import de.felixklauke.kira.core.meta.ModelMetaManager;
import de.felixklauke.kira.core.meta.ModelProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomMapper<ModelType> implements Mapper<ModelType> {

  private final Class<ModelType> modelClass;
  private final ModelMetaManager metaManager;
  private final MapperManager mapperManager;

  public CustomMapper(Class<ModelType> modelClass, ModelMetaManager metaManager, MapperManager mapperManager) {
    this.modelClass = modelClass;
    this.metaManager = metaManager;
    this.mapperManager = mapperManager;
  }

  @Override
  public Class<ModelType> getModelClass() {

    return modelClass;
  }

  @Override
  public void write(KiraWriter kiraWriter, String propertyName, ModelType model) {

    // Get meta
    Class<ModelType> modelClass = getModelClass();
    ModelMeta meta = metaManager.getMeta(modelClass);

    Map<String, Object> data = new HashMap<>();

    List<ModelProperty> properties = meta.getProperties();

    // Loop through properties
    for (ModelProperty<?> property : properties) {

      // Read the meta values
      Class<?> propertyType = property.getType();
      String localPropertyName = property.getName();
      Object propertyValue = property.getValue(model);

      if (propertyValue == null) {
        continue;
      }

      KiraWriter propertyWriter = new SimpleKiraWriter(data);

      // Obtain corresponding mapper
      Mapper mapper = mapperManager.getMapper(propertyType);
      mapper.write(propertyWriter, localPropertyName, propertyValue);
    }

    kiraWriter.writeValue(propertyName, data);
  }

  @Override
  public ModelType read(KiraReader reader, String propertyName) {

    // Read data
    Map<String, Object> data = reader.readValue(propertyName);

    if (data == null) {
      return null;
    }

    // Get model meta
    Class<ModelType> modelClass = getModelClass();
    ModelMeta meta = metaManager.getMeta(modelClass);

    // Create model
    ModelType model;

    try {
      model = modelClass.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
      return null;
    }

    // Read properties
    List<ModelProperty> properties = meta.getProperties();

    for (ModelProperty property : properties) {

      KiraReader propertyReader = new SimpleKiraReader(data);

      Class propertyType = property.getType();
      String localPropertyName = property.getName();

      Mapper mapper = mapperManager.getMapper(propertyType);
      Object read = mapper.read(propertyReader, localPropertyName);

      property.set(model, read);
    }

    return model;
  }
}