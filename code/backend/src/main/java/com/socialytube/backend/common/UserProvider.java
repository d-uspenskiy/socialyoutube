package com.socialytube.backend.common;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class UserProvider implements IUserProvider {
  private static final Logger LOG = LoggerFactory.getLogger(UserProvider.class);

  @Autowired
  private JdbcOperations jdbcOperations_;

  public static class NewUserInfo {
  }

  public interface NewUserInfoProvider {
    NewUserInfo info();
  }

  private static class ShortUserInfo {
    public String email;
    public int dbid;
  }

  private static class ShortUserInfoMapper implements RowMapper<ShortUserInfo> {
    @Override
    public ShortUserInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
      ShortUserInfo info = new ShortUserInfo();
      info.dbid = rs.getInt(1);
      info.email = rs.getString(2);
      return info;
    }
  }

  private static final ShortUserInfoMapper USER_INFO_MAPPER = new ShortUserInfoMapper();

  @Override
  public User getUser(GoogleIdToken.Payload tokenPayload) {
    String email = tokenPayload.getEmail();
    try {
     ShortUserInfo info = jdbcOperations_.queryForObject("SELECT id, email FROM account WHERE email=?",
          new Object[] { email }, USER_INFO_MAPPER);
      return new User(info.email, info.dbid);
    } catch (DataAccessException e) {
      LOG.info("User not found {}", email);
    }
    Integer dbid = jdbcOperations_.queryForObject("INSERT INTO account(email) VALUES(?) RETURNING ID",
        new Object[] { email }, Integer.class);
    return new User(email, dbid);
  }

  @Override
  public int resolveTubeId(User user, Info info) {
    Pair<String, Integer> latestTube = user.getCachedData().latestTube;
    String extarnalChannelId = info.channelId();
    if (latestTube == null || !extarnalChannelId.equals(latestTube.getKey())) {
      Integer tube_id = Tools.fetchOptional(jdbcOperations_, Integer.class, "SELECT id FROM tube WHERE account_id = ? AND external_id = ?", user.getDbid(), extarnalChannelId);
      if (tube_id == null) {
        tube_id = Tools.fetch(jdbcOperations_, Integer.class, "INSERT INTO tube(account_id, external_id, title, extra) VALUES(?, ?, ?, ?::JSON) RETURNING ID", user.getDbid(), extarnalChannelId, info.title(), info.extraJson());
      }
      latestTube = new ImmutablePair<String,Integer>(extarnalChannelId, tube_id);
      user.getCachedData().latestTube = latestTube;
    }
    return latestTube.getValue();
  }
}