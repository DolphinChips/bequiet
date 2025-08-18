-- vim: et:ts=2:sw=2

create table player
  ( player_id uuid not null default gen_random_uuid() primary key
  , djname    varchar not null
  );
insert into player(djname) values
  ('INSEP');

create table song
  ( song_id integer primary key
  , title   varchar not null
  , artist  varchar not null
  );

create table playstyle(value character(2) not null primary key);
insert into playstyle values
  ('SP'), ('DP');

create table difficulty(value character(1) not null primary key);
insert into difficulty values
  ('B'), ('N'), ('H'), ('A'), ('L');

create table chart
  ( chart_id    serial primary key
  , song_id     integer not null references song(song_id)
  , playstyle   character(2) not null references playstyle(value)
  , difficulty  character(1) not null references difficulty(value)
  -- , notecount   integer not null check(notecount >= 0)
  , unique(song_id, playstyle, difficulty)
  );

create table lamp
  ( lamp_id integer primary key
  , value   varchar not null unique
  , short   varchar unique
  );
insert into lamp values
  (0, 'NO PLAY', NULL),
  (1, 'FAILED', 'F'),
  (2, 'ASSIST CLEAR', 'AC'),
  (3, 'EASY CLEAR', 'EC'),
  (4, 'CLEAR', 'NC'),
  (5, 'HARD CLEAR', 'HC'),
  (6, 'EXHARD CLEAR', 'EXC'),
  (7, 'FULL COMBO', 'FC');

create table rating
  ( chart_id integer not null references chart(chart_id)
  , lamp_id  integer not null references lamp(lamp_id)
  , value    real
  , primary key(chart_id, lamp_id)
  , check(value > 0.0)
  ); 

create table score
  ( score_id  integer primary key
  , player_id uuid not null references player(player_id)
  , chart_id  integer not null references chart(chart_id)
  , lamp_id   integer not null references lamp(lamp_id) default 0
  );

-- TODO: options
-- create table score
--   ( score_id        integer primary key
--   , player_id       uuid not null references player(player_id)
--   , chart_id        integer not null references chart(chart_id)
--   , lamp_id         integer not null references lamp(lamp_id) default 0
--   , pgreat_count    integer not null check(pgreat_count >= 0)
--   , great_count     integer not null check(great_count >= 0)
--   , good_count      integer not null check(good_count >= 0)
--   , bad_count       integer not null check(bad_count >= 0)
--   , poor_count      integer not null check(poor_count >= 0)
--   , cb_count        integer not null check(cb_count >= 0)
--   , fast_count      integer not null check(fast_count >= 0)
--   , slow_count      integer not null check(slow_count >= 0)
--   , miss_count      integer generated always as (bad_count + poor_count) stored
--   );
