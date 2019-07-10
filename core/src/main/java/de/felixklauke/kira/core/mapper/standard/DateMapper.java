package de.felixklauke.kira.core.mapper.standard;

import de.felixklauke.kira.core.io.KiraReader;
import de.felixklauke.kira.core.io.KiraWriter;
import de.felixklauke.kira.core.mapper.Mapper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateMapper implements Mapper<Date> {

  private static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat();

  @Override
  public Class<Date> getModelClass() {
    return Date.class;
  }

  @Override
  public Date read(KiraReader reader, String propertyName) {

    String date = reader.readValue(propertyName);
    try {
      return DEFAULT_DATE_FORMAT.parse(date);
    } catch (ParseException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public void write(KiraWriter kiraWriter, String propertyName, Date model) {

    String format = DEFAULT_DATE_FORMAT.format(model);
    kiraWriter.writeValue(propertyName, format);
  }
}
