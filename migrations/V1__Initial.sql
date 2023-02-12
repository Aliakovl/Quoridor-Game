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
    login varchar(64) NOT NULL UNIQUE
);

CREATE TABLE game (
    id UUID PRIMARY KEY,
    creator UUID NOT NULL REFERENCES "user"
);

CREATE TABLE player (
    game_id UUID NOT NULL REFERENCES game,
    user_id UUID NOT NULL REFERENCES "user",
    target side NOT NULL,
    PRIMARY KEY (game_id, user_id),
    UNIQUE (game_id, target)
);

CREATE TABLE game_state (
    id UUID PRIMARY KEY,
    game_id UUID NOT NULL,
    previous_state UUID NOT NULL REFERENCES game_state,
    active_player UUID NOT NULL,
    FOREIGN KEY (game_id, active_player) REFERENCES player(game_id, user_id)
);

CREATE TABLE pawn_position (
    game_state_id UUID NOT NULL REFERENCES game_state,
    user_id UUID NOT NULL REFERENCES "user",
    walls_amount INT NOT NULL,
    "row" smallint NOT NULL CHECK ("row" BETWEEN 0 AND 8),
    "column" smallint NOT NULL CHECK ("column" BETWEEN 0 AND 8),
    PRIMARY KEY (game_state_id, user_id)
);

CREATE TABLE wall_position (
    game_state_id UUID NOT NULL REFERENCES game_state,
    orient orientation NOT NULL,
    "row" smallint NOT NULL CHECK ("row" BETWEEN 0 AND 7),
    "column" smallint NOT NULL CHECK ("column" BETWEEN 0 AND 7)
);

CREATE TABLE winner (
    game_id UUID NOT NULL PRIMARY KEY REFERENCES game,
    user_id UUID NOT NULL REFERENCES "user",
    FOREIGN KEY (game_id, user_id) REFERENCES player(game_id, user_id)
);