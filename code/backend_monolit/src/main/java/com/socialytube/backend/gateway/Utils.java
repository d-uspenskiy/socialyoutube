package com.socialytube.backend.gateway;

import com.socialytube.backend.common.Tools.JsonStringWriter;
import com.socialytube.backend.gateway.api.Data.Thumbnail;

public class Utils {
  public static JsonStringWriter thumbnail(JsonStringWriter w, Thumbnail t) {
    return t == null ? w : w.num("w", t.width).num("h", t.height).str("u", t.url);
  }
}