CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TRIGGER IF EXISTS create_watch_event ON watch_list;
DROP TRIGGER IF EXISTS drop_watch_event ON watch_list;
DROP TRIGGER IF EXISTS create_like_event ON like_list;
DROP TRIGGER IF EXISTS drop_like_event ON like_list;
DROP TRIGGER IF EXISTS create_comment_event ON comment_list;
DROP TRIGGER IF EXISTS drop_comment_event ON comment_list;
DROP TRIGGER IF EXISTS create_subscribe_event ON subscription;
DROP TRIGGER IF EXISTS drop_subscribe_event ON subscription;

DROP FUNCTION IF EXISTS insert_watch;
DROP FUNCTION IF EXISTS force_get_channel;
DROP FUNCTION IF EXISTS force_get_video;
DROP FUNCTION IF EXISTS force_get_video_stub;
DROP FUNCTION IF EXISTS set_like_marker;
DROP FUNCTION IF EXISTS create_watch_event CASCADE;
DROP FUNCTION IF EXISTS drop_watch_event CASCADE;
DROP FUNCTION IF EXISTS create_like_event CASCADE;
DROP FUNCTION IF EXISTS drop_like_event CASCADE;
DROP FUNCTION IF EXISTS create_comment_event CASCADE;
DROP FUNCTION IF EXISTS drop_comment_event CASCADE;
DROP FUNCTION IF EXISTS create_subscribe_event CASCADE;
DROP FUNCTION IF EXISTS drop_subscribe_event CASCADE;

DROP VIEW IF EXISTS tube_video_helper;
DROP VIEW IF EXISTS video_helper;
DROP VIEW IF EXISTS tube_event_helper;
DROP VIEW IF EXISTS video_event_helper;
DROP VIEW IF EXISTS event_source_data;

DROP TABLE IF EXISTS tube_event;
DROP TABLE IF EXISTS update_events;
DROP TABLE IF EXISTS native_subscription;
DROP TABLE IF EXISTS subscription;
DROP TABLE IF EXISTS like_marker;
DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS tube_video;
DROP TABLE IF EXISTS watch_list;
DROP TABLE IF EXISTS comment_list;
DROP TABLE IF EXISTS like_list;
DROP TABLE IF EXISTS video;
DROP TABLE IF EXISTS channel;
DROP TABLE IF EXISTS official_tube;
DROP TABLE IF EXISTS tube;
DROP TABLE IF EXISTS account;

DROP TYPE IF EXISTS nsub_state;
DROP TYPE IF EXISTS event_source;

CREATE TYPE nsub_state AS ENUM ('requested', 'rejected', 'accepted');
CREATE TYPE event_source AS ENUM ('comment', 'like', 'subscribe', 'watch');

CREATE TABLE IF NOT EXISTS account(
  id SERIAL PRIMARY KEY,
  email VARCHAR(128) UNIQUE NOT NULL,
  public_id UUID UNIQUE NOT NULL DEFAULT UUID_GENERATE_V4(),
  extra JSONB,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);

CREATE TABLE IF NOT EXISTS tube(
  id SERIAL PRIMARY KEY,
  account_id INT REFERENCES account(id) NOT NULL,
  external_id VARCHAR(64) UNIQUE NOT NULL,
  title VARCHAR(64),
  public_id UUID UNIQUE NOT NULL DEFAULT UUID_GENERATE_V4(),
  extra JSONB,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(account_id, external_id));

CREATE INDEX IF NOT EXISTS tube_title ON tube(title);

CREATE TABLE IF NOT EXISTS official_tube(
  tube_id INT UNIQUE REFERENCES tube(id) PRIMARY KEY,
  external_id VARCHAR(64) UNIQUE NOT NULL);

CREATE TABLE IF NOT EXISTS channel(
  id SERIAL PRIMARY KEY,
  external_id VARCHAR(64) UNIQUE NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  extra JSONB);

CREATE TABLE IF NOT EXISTS video(
  id SERIAL PRIMARY KEY,
  channel_id INT REFERENCES channel(id),
  external_id VARCHAR(64) UNIQUE NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  extra JSONB);

CREATE TABLE IF NOT EXISTS watch_list(
  id BIGSERIAL PRIMARY KEY,
  tube_id INT REFERENCES tube(id) NOT NULL,
  video_id INT REFERENCES video(id) NOT NULL,
  position_hash VARCHAR(32) NOT NULL,
  position INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(tube_id, position_hash));

CREATE TABLE IF NOT EXISTS like_list(
  id BIGSERIAL PRIMARY KEY,
  tube_id INT REFERENCES tube(id) NOT NULL,
  video_id INT REFERENCES video(id) NOT NULL,
  position_hash VARCHAR(32) NOT NULL,
  position INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(tube_id, video_id),
  UNIQUE(tube_id, position_hash));

CREATE TABLE IF NOT EXISTS comment_list(
  id BIGSERIAL PRIMARY KEY,
  tube_id INT REFERENCES tube(id) NOT NULL,
  video_id INT REFERENCES video(id) NOT NULL,
  external_id VARCHAR(64) NOT NULL UNIQUE,
  extra JSONB NOT NULL,
  position_hash VARCHAR(32) NOT NULL,
  position INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(tube_id, position_hash));

CREATE TABLE IF NOT EXISTS tube_video(
  id BIGSERIAL PRIMARY KEY,
  tube_id INT REFERENCES tube(id) NOT NULL,
  video_id INT REFERENCES video(id) NOT NULL,
  position_hash VARCHAR(32) NOT NULL,
  position SERIAL UNIQUE NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(tube_id, video_id),
  UNIQUE(tube_id, position_hash));

CREATE INDEX tube_video_created_at ON tube_video(created_at);

CREATE TABLE IF NOT EXISTS comment(
  id SERIAL PRIMARY KEY,
  tube_id INT REFERENCES tube(id) NOT NULL,
  video_id INT REFERENCES video(id) NOT NULL,
  external_id VARCHAR(64) NOT NULL,
  body VARCHAR NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(tube_id, external_id));

CREATE TABLE IF NOT EXISTS like_marker(
  id SERIAL PRIMARY KEY,
  tube_id INT REFERENCES tube(id) NOT NULL,
  video_id INT REFERENCES video(id) NOt NULL,
  value BOOL NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(tube_id, video_id));

CREATE INDEX ON like_marker(created_at);

CREATE TABLE IF NOT EXISTS subscription(
  id SERIAL PRIMARY KEY,
  tube_id INT REFERENCES tube(id) NOT NULL,
  channel_id INT REFERENCES channel(id) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(tube_id, channel_id));

CREATE TABLE IF NOT EXISTS native_subscription(
  id SERIAL PRIMARY KEY,
  master_tube_id INT REFERENCES tube(id) NOT NULL,
  follower_tube_id INT REFERENCES tube(id) NOT NULL,
  public_id UUID UNIQUE NOT NULL DEFAULT UUID_GENERATE_V4(),
  state nsub_state NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(master_tube_id, follower_tube_id));

CREATE TABLE IF NOT EXISTS update_events(
  tube_id INT REFERENCES tube(id) PRIMARY KEY,
  version BIGINT NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  extra JSONB,
  UNIQUE(tube_id, version));

CREATE TABLE IF NOT EXISTS tube_event(
  source event_source NOT NULL,
  source_id INT NOT NULL,
  tube_id INT REFERENCES tube(id) NOT NULL,
  channel_id INT REFERENCES channel(id),
  video_id INT REFERENCES video(id),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(source, source_id),
  UNIQUE(source, tube_id, channel_id, video_id));

CREATE INDEX ON tube_event(source);

CREATE OR REPLACE VIEW video_helper AS
  SELECT c.id AS cid,
         c.external_id AS c_external_id, 
         c.extra AS c_extra, 
         v.id AS vid, 
         v.external_id AS v_external_id, 
         v.extra AS v_extra
    FROM video AS v LEFT JOIN channel AS c ON (c.id = v.channel_id);

CREATE OR REPLACE VIEW tube_video_helper AS
  SELECT v.c_external_id,
         v.c_extra,
         v.vid,
         v.cid,
         v.v_external_id,
         v.v_extra,
         t.account_id,
         t.id AS tid,
         position,
         tv.created_at,
         t.public_id AS t_public_id, 
         t.title AS t_title
    FROM tube AS t INNER JOIN tube_video AS tv ON (t.id = tv.tube_id)
                   INNER JOIN video_helper AS v ON (v.vid = tv.video_id);

CREATE OR REPLACE VIEW event_source_data AS
  SELECT 'comment'::event_source AS source, id AS source_id, extra FROM comment_list;

CREATE OR REPLACE VIEW tube_event_helper AS
  SELECT e.source,
         e.created_at,
         t.id AS tid,
         t.title AS t_title,
         t.public_id AS t_public_id,
         v.id AS vid,
         v.external_id AS v_external_id,
         v.extra AS v_extra,
         c.id AS cid,
         c.external_id AS c_external_id,
         c.extra AS c_extra,
         s.extra AS s_extra
    FROM tube_event AS e INNER JOIN tube AS t ON (e.tube_id = t.id)
                         LEFT JOIN video AS v ON (e.video_id = v.id)
                         LEFT JOIN channel AS c ON (e.channel_id = c.id)
                         LEFT JOIN event_source_data AS s ON (e.source = s.source AND e.source_id = s.source_id);

CREATE OR REPLACE VIEW video_event_helper AS
  SELECT e.source,
         e.created_at,
         v.external_id AS v_external_id,
         t.id AS tid,
         t.title AS t_title,
         t.public_id AS t_public_id,
         s.extra AS s_extra
    FROM tube_event AS e INNER JOIN tube AS t ON (e.tube_id = t.id)
                         INNER JOIN video AS v ON (e.video_id = v.id)
                         LEFT JOIN event_source_data AS s ON (e.source = s.source AND e.source_id = s.source_id)
    WHERE e.source in ('watch', 'like', 'comment');

CREATE OR REPLACE FUNCTION force_get_channel(ch_ext_id VARCHAR(64), extra JSON) RETURNS INT AS '
DECLARE res INT;
BEGIN
  SELECT id FROM channel WHERE external_id = ch_ext_id INTO res;
  IF (res IS NULL) THEN
    INSERT INTO channel(external_id, extra) VALUES(ch_ext_id, extra) RETURNING id INTO res;
  END IF;
  RETURN res;
END;'
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION force_get_video(ch_id INT, v_ext_id VARCHAR(64), extra JSON) RETURNS INT AS '
DECLARE res INT;
BEGIN
  SELECT id FROM video WHERE external_id = v_ext_id AND channel_id IS NOT NULL INTO res;
  IF (res IS NULL) THEN
    INSERT INTO video(channel_id, external_id, extra) VALUES(ch_id, v_ext_id, extra) ON CONFLICT(external_id) DO UPDATE SET channel_id = EXCLUDED.channel_id, extra = EXCLUDED.extra, created_at = EXCLUDED.created_at RETURNING id INTO res;
  END IF;
  RETURN res;
END;'
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION force_get_video_stub(v_ext_id VARCHAR(64)) RETURNS INT AS '
DECLARE res INT;
BEGIN
  SELECT id FROM video WHERE external_id = v_ext_id INTO res;
  IF (res IS NULL) THEN
    INSERT INTO video(external_id) VALUES(v_ext_id) RETURNING id INTO res;
  END IF;
  RETURN res;
END;'
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION set_like_marker(t_id INT, v_id INT, val BOOL) RETURNS INT AS '
DECLARE res INT;
BEGIN
  SELECT id FROM like_marker WHERE tube_id = t_id AND video_id = v_id AND value = val INTO res;
  IF (res IS NULL) THEN
    INSERT INTO like_marker(tube_id, video_id, value) VALUES(t_id, v_id, val) ON CONFLICT (tube_id, video_id) DO UPDATE SET value = EXCLUDED.value, created_at = EXCLUDED.created_at RETURNING id INTO res;
  END IF;
  RETURN res;
END;'
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION drop_watch_event() RETURNS trigger AS '
BEGIN
  DELETE FROM tube_event WHERE source=''watch'' AND source_id=OLD.id;
  RETURN OLD;
END;'
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION create_watch_event() RETURNS trigger AS '
DECLARE ch_id INT;
BEGIN
  SHOW user_vars.channel_id INTO ch_id;
  INSERT INTO tube_event(source, source_id, tube_id, channel_id, video_id) VALUES(''watch'', NEW.id, NEW.tube_id, ch_id, NEW.video_id) ON CONFLICT(source, tube_id, channel_id, video_id) DO UPDATE SET created_at = EXCLUDED.created_at;
  RETURN NEW;
END;'
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION drop_like_event() RETURNS trigger AS '
BEGIN
  DELETE FROM tube_event WHERE source=''like'' AND source_id=OLD.id;
  RETURN OLD;
END;'
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION create_like_event() RETURNS trigger AS '
DECLARE ch_id INT;
BEGIN
  SHOW user_vars.channel_id INTO ch_id;
  INSERT INTO tube_event(source, source_id, tube_id, channel_id, video_id) VALUES(''like'', NEW.id, NEW.tube_id, ch_id, NEW.video_id) ON CONFLICT(source, tube_id, channel_id, video_id) DO UPDATE SET created_at = EXCLUDED.created_at;
  RETURN NEW;
END;'
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION drop_comment_event() RETURNS trigger AS '
BEGIN
  DELETE FROM tube_event WHERE source=''comment'' AND source_id=OLD.id;
  RETURN OLD;
END;'
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION create_comment_event() RETURNS trigger AS '
BEGIN
  INSERT INTO tube_event(source, source_id, tube_id, video_id) VALUES(''comment'', NEW.id, NEW.tube_id, NEW.video_id) ON CONFLICT(source, tube_id, channel_id, video_id) DO UPDATE SET created_at = EXCLUDED.created_at;
  RETURN NEW;
END;'
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION drop_subscribe_event() RETURNS trigger AS '
BEGIN
  DELETE FROM tube_event WHERE source=''subscribe'' AND source_id=OLD.id;
  RETURN OLD;
END;'
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION create_subscribe_event() RETURNS trigger AS '
BEGIN
  INSERT INTO tube_event(source, source_id, tube_id, channel_id) VALUES(''subscribe'', NEW.id, NEW.tube_id, NEW.channel_id) ON CONFLICT(source, tube_id, channel_id, video_id) DO UPDATE SET created_at = EXCLUDED.created_at;
  RETURN NEW;
END;'
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION insert_watch(tid INT, cid INT, vid INT, pos INT, pos_hash VARCHAR(32)) RETURNS INT AS '
DECLARE res INT;
BEGIN
  PERFORM set_config(''user_vars.channel_id'', cid::text, true);
  INSERT INTO watch_list(tube_id, video_id, position, position_hash) VALUES(tid, vid, pos, pos_hash) ON CONFLICT(tube_id, position_hash) DO NOTHING RETURNING id INTO res;
  RETURN res;
END;'
LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION insert_like(tid INT, cid INT, vid INT, pos INT, pos_hash VARCHAR(32)) RETURNS INT AS '
DECLARE res INT;
BEGIN
  PERFORM set_config(''user_vars.channel_id'', cid::text, true);
  INSERT INTO like_list(tube_id, video_id, position, position_hash) VALUES(tid, vid, pos, pos_hash) ON CONFLICT(tube_id, position_hash) DO NOTHING RETURNING id INTO res;
  RETURN res;
END;'
LANGUAGE PLPGSQL;

CREATE TRIGGER create_watch_event
  BEFORE INSERT ON watch_list
  FOR EACH ROW EXECUTE PROCEDURE create_watch_event();

CREATE TRIGGER drop_watch_event
  BEFORE DELETE ON watch_list
  FOR EACH ROW EXECUTE PROCEDURE drop_watch_event();

CREATE TRIGGER create_like_event
  BEFORE INSERT ON like_list
  FOR EACH ROW EXECUTE PROCEDURE create_like_event();

CREATE TRIGGER drop_like_event
  BEFORE DELETE ON like_list
  FOR EACH ROW EXECUTE PROCEDURE drop_like_event();

CREATE TRIGGER create_comment_event
  BEFORE INSERT ON comment_list
  FOR EACH ROW EXECUTE PROCEDURE create_comment_event();

CREATE TRIGGER drop_comment_event
  BEFORE DELETE ON comment_list
  FOR EACH ROW EXECUTE PROCEDURE drop_comment_event();

CREATE TRIGGER create_subscribe_event
  BEFORE INSERT ON subscription
  FOR EACH ROW EXECUTE PROCEDURE create_subscribe_event();

CREATE TRIGGER drop_subscribe_event
  BEFORE DELETE ON subscription
  FOR EACH ROW EXECUTE PROCEDURE drop_subscribe_event();
