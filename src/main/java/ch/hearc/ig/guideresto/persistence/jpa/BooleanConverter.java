package ch.hearc.ig.guideresto.persistence.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convertisseur JPA pour stocker un booléen sous forme "T" / "F".
 * Suppose une valeur de colonne non nulle lors de la lecture.
 */
@Converter
public class BooleanConverter implements AttributeConverter<Boolean, String> {

  @Override
  public String convertToDatabaseColumn(Boolean value) {
    return Boolean.TRUE.equals(value) ? "T" : "F";
  }

  @Override
  public Boolean convertToEntityAttribute(String s) {
    return s.equalsIgnoreCase("T");
  }
}
