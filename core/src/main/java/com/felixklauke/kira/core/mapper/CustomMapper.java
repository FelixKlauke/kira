package com.felixklauke.kira.core.mapper;

import com.felixklauke.kira.core.exception.KiraModelException;
import com.felixklauke.kira.core.exception.KiraModelInstantiationException;
import com.felixklauke.kira.core.io.KiraReader;
import com.felixklauke.kira.core.io.KiraWriter;
import com.felixklauke.kira.core.io.KiraMapReader;
import com.felixklauke.kira.core.io.KiraMapWriter;
import com.felixklauke.kira.core.meta.ModelMeta;
import com.felixklauke.kira.core.meta.ModelMetaRepository;
import com.felixklauke.kira.core.meta.ModelProperty;

import java.lang.reflect.Type;
import java.util.*;

/**
 * A custom mapper that can map arbitrary objects by working through their properties.
 *
 * @param <ModelType> The generic type of the mode.
 */
public class CustomMapper<ModelType> implements Mapper<ModelType> {

  /**
   * The class of the model.
   */
  private final Class<ModelType> modelClass;

  /**
   * The manager of the model meta information.
   */
  private final ModelMetaRepository metaManager;

  /**
   * The manager of all available mappers.
   */
  private final MapperRegistry mapperRegistry;

  public CustomMapper(Class<ModelType> modelClass, ModelMetaRepository metaManager, MapperRegistry mapperRegistry) {
    this.modelClass = modelClass;
    this.metaManager = metaManager;
    this.mapperRegistry = mapperRegistry;
  }

  @Override
  public Class<ModelType> getModelClass() {

    return modelClass;
  }

  @Override
  public void write(KiraWriter kiraWriter, String propertyName, ModelType model) throws KiraModelException {

    // Get meta
    Class<ModelType> modelClass = getModelClass();
    ModelMeta meta = metaManager.getMeta(modelClass);

    Map<String, Object> data = new HashMap<>();

    Collection<ModelProperty> properties = meta.getProperties();

    // Loop through properties
    for (ModelProperty<?> property : properties) {

      // Read the meta values
      Class<?> propertyType = property.getType();
      String localPropertyName = property.getName();
      Object propertyValue = property.getValue(model);

      if (propertyValue == null) {
        continue;
      }

      KiraWriter propertyWriter = new KiraMapWriter(data);

      // Obtain corresponding mapper
      Mapper mapper = mapperRegistry.getMapper(propertyType);
      mapper.write(propertyWriter, localPropertyName, propertyValue);
    }

    kiraWriter.writeValue(propertyName, data);
  }

  @Override
  public ModelType read(KiraReader reader, String propertyName, Type genericType) throws KiraModelException {

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
    } catch (InstantiationException e) {
      throw new KiraModelInstantiationException("Couldn't create model instance. Make sure there is a non-argument constructor available.", e);
    } catch (IllegalAccessException e) {
      throw new KiraModelException("Couldn't create model instance.", e);
    }

    // Read properties
    Collection<ModelProperty> properties = meta.getProperties();

    for (ModelProperty property : properties) {

      KiraReader propertyReader = new KiraMapReader(data);

      Class propertyType = property.getType();
      Type localGenericType = property.getGenericType();
      String localPropertyName = property.getName();

      Mapper mapper = mapperRegistry.getMapper(propertyType);
      Object read = mapper.read(propertyReader, localPropertyName, localGenericType);

      property.set(model, read);
    }

    return model;
  }
}
