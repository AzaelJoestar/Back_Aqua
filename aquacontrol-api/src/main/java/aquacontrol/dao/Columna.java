package aquacontrol.dao;

/** Una columna editable: nombre en la BD, nombre en el JSON y tipo. */
public record Columna(String bd, String json, Tipo tipo) {}
