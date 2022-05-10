CREATE TYPE side as ENUM (
    'north',
    'south',
    'west',
    'east'
);

CREATE TYPE orientation as ENUM (
    'horizontal',
    'vertical'
);

CREATE TABLE "user" (
    id UUID PRIMARY KEY,
    login varchar(32) NOT NULL UNIQUE
);

CREATE TABLE game (
    id UUID PRIMARY KEY
);

CREATE TABLE player (
    game_id UUID REFERENCES game,
    user_id UUID REFERENCES "user",
    target side NOT NULL,
    PRIMARY KEY (game_id, user_id),
    UNIQUE (game_id, target)
);

CREATE TABLE game_state (
    id UUID PRIMARY KEY,
    game_id UUID REFERENCES game,
    previous_state UUID REFERENCES game_state,
    active_player UUID REFERENCES "user",
    FOREIGN KEY (game_id, active_player) REFERENCES player(game_id, user_id)
);

CREATE TABLE pawn_position (
    game_state_id UUID REFERENCES game_state,
    user_id UUID REFERENCES "user",
    wallsAmount INT NOT NULL,
    "row" smallint NOT NULL CHECK ("row" BETWEEN 0 AND 8),
    "column" smallint NOT NULL CHECK ("column" BETWEEN 0 AND 8),
    PRIMARY KEY (game_state_id, user_id)
);

CREATE TABLE wall_position (
    game_state_id UUID REFERENCES game_state,
    orient orientation NOT NULL,
    "row" smallint NOT NULL CHECK ("row" BETWEEN 0 AND 7),
    "column" smallint NOT NULL CHECK ("column" BETWEEN 0 AND 7)
);