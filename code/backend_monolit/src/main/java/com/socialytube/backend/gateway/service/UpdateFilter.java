package com.socialytube.backend.gateway.service;

import com.socialytube.backend.common.Tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Service;

@Service
public class UpdateFilter implements IUpdateFilter {
  private static final Logger LOG = LoggerFactory.getLogger(UpdateFilter.class);

  @Autowired
  private JdbcOperations jdbcOperations_;

  @Override
  public boolean filtered(int tubeId, long hash, long version, Marker marker) {
    Object[] update_info = Tools.fetchOptional(jdbcOperations_, String.format("SELECT version, (extra->>'%s')::BIGINT FROM update_events WHERE tube_id = ? AND version >= ?", marker), tubeId, version);
    boolean version_match = false;
    if (update_info != null) {
      Long cur_version = (Long)update_info[0];
      version_match = cur_version == version;
      Long cur_hash = (Long)update_info[1];
      if (version_match && cur_hash != null && cur_hash == hash) {
        LOG.info("update locked {} {}", marker.value, hash);
        // TODO: revert to true
        return false;
      }
    }
    jdbcOperations_.update(String.format("INSERT INTO update_events(tube_id, version, extra) VALUES(?, ?, ?::JSON) ON CONFLICT(tube_id) DO UPDATE SET version = EXCLUDED.version, extra = %s", version_match ? "update_events.extra || EXCLUDED.extra" : "EXCLUDED.extra"), tubeId, version, Tools.jsonObjectWriter().num(marker.value, hash).result());
    return false;
  }
}