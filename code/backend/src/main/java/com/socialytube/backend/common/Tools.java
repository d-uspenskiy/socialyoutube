package com.socialytube.backend.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.core.context.SecurityContextHolder;

public class Tools {
  public static JSONArray safeGetJSONArray(JSONObject src, String name) {
    return src.has(name) ? src.getJSONArray(name) : null;
  }

  public static JSONObject safeGetJSONObject(JSONObject src, String name) {
    return src.has(name) ? src.getJSONObject(name) : null;
  }

  public static <T> T fetch(JdbcOperations op, Class<T> tp, String query, Object... args) {
    return op.queryForObject(query, args, tp);
  }

  public static <T> Object[] fetch(JdbcOperations op, String query, Object... args) {
    return op.queryForObject(query, args, Tools::arrayMapper);
  }

  public static <T> T fetchOptional(JdbcOperations op, Class<T> tp, String query, Object... args) {
    return getFirstOptional(op.queryForList(query, tp, args));
  }

  public static <T> T fetchWithDefault(JdbcOperations op, T def, String query, Object... args) {
    return getFirstDefault(op.queryForList(query, (Class<T>)def.getClass(), args), def);
  }

  public static Object[] fetchOptional(JdbcOperations op, String query, Object... args) {
    return getFirstOptional(op.query(query, Tools::arrayMapper, args));
  }

  private static <T> T getFirstOptional(List<T> list) {
    if (list.isEmpty()) {
      return null;
    } else if (list.size() == 1) {
      return list.get(0);
    }
    throw new IncorrectResultSizeDataAccessException(1, list.size());
  }

  private static <T> T getFirstDefault(List<T> list, T def) {
    if (list.isEmpty()) {
      return def;
    } else if (list.size() == 1) {
      return list.get(0);
    }
    throw new IncorrectResultSizeDataAccessException(1, list.size());
  }

  private static Object[] arrayMapper(ResultSet rs, Integer rowNum) throws SQLException {
    int count = rs.getMetaData().getColumnCount();
    Object[] result = new Object[count];
    for (int i = 0; i < count; ++i) {
      result[i] = rs.getObject(i + 1);
    }
    return result;
  }

  public static class JsonStringWriter {
    private StringBuilder str_ = new StringBuilder();
    private List<Boolean> nesting_ = new ArrayList<>();
    private boolean first_ = true;

    public JsonStringWriter str(String name, String value) {
      header(name);
      return rawStr(value);
    }

    public JsonStringWriter num(String name, long value) {
      header(name);
      str_.append(value);
      return this;
    }

    public JsonStringWriter str(String value) {
      checkFirst();
      return rawStr(value);
    }

    public JsonStringWriter num(long value) {
      checkFirst();
      str_.append(value);
      return this;
    }

    public JsonStringWriter obj() {
      checkFirst();
      return open(false);
    }

    public JsonStringWriter array() {
      checkFirst();
      return open(true);
    }

    public JsonStringWriter obj(String name) {
      header(name);
      return open(false);
    }

    public JsonStringWriter array(String name) {
      header(name);
      return open(true);
    }

    public JsonStringWriter close() {
      int last = nesting_.size() - 1;
      str_.append(nesting_.get(last) ? ']' : '}');
      nesting_.remove(last);
      return this;
    }

    public String result() {
      while(!nesting_.isEmpty()) {
        close();
      }
      String v = str_.toString();
      return v;
    }

    public JsonStringWriter clear() {
      str_.delete(0, str_.length());
      first_ = true;
      return this;
    }

    private void header(String name) {
      checkFirst();
      str_.append('"').append(name).append("\":");
    }

    private JsonStringWriter open(boolean array) {
      str_.append(array ? '[' : '{');
      nesting_.add(array);
      first_ = true;
      return this;
    }

    private void checkFirst() {
      if (first_) {
        first_ = false;
      } else {
        str_.append(',');
      }
    }

    private JsonStringWriter rawStr(String str) {
      //TODO: check '\n' escaping
      str_.append('"').append(str.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"").replaceAll("\n", "\\\\n")).append('"');
      return this;
    }
  }

  public static JsonStringWriter jsonArrayWriter() {
    return new JsonStringWriter().array();
  }

  public static JsonStringWriter jsonObjectWriter() {
    return new JsonStringWriter().obj();
  }

  public static User getCurrentUser() { 
    return (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}