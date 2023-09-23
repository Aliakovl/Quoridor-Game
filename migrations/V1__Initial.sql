CREATE TYPE side as ENUM
(
    'north',
    'south',
    'west',
    'east'
);

CREATE TYPE orientation as ENUM
(
    'horizontal',
    'vertical'
);

CREATE TABLE userdata
(
    user_id     UUID PRIMARY KEY,
    username    VARCHAR(64) NOT NULL UNIQUE,
    user_secret BYTEA       NOT NULL
);

CREATE TABLE game
(
    game_id UUID PRIMARY KEY,
    creator UUID NOT NULL REFERENCES userdata
);

CREATE TABLE player
(
    game_id UUID NOT NULL REFERENCES game,
    user_id UUID NOT NULL REFERENCES userdata,
    target  side NOT NULL,
    PRIMARY KEY (game_id, user_id),
    UNIQUE (game_id, target)
);

CREATE TABLE game_state
(
    game_id       UUID    NOT NULL,
    step          INTEGER NOT NULL CHECK ( step >= 0 ),
    active_player UUID    NOT NULL,
    PRIMARY KEY (step, game_id),
    FOREIGN KEY (game_id, active_player) REFERENCES player (game_id, user_id)
);

CREATE TABLE pawn_position
(
    game_id      UUID     NOT NULL,
    step         INTEGER  NOT NULL,
    user_id      UUID     NOT NULL REFERENCES userdata,
    walls_amount SMALLINT NOT NULL,
    "row"        SMALLINT NOT NULL CHECK ("row" BETWEEN 0 AND 8),
    "column"     SMALLINT NOT NULL CHECK ("column" BETWEEN 0 AND 8),
    PRIMARY KEY (game_id, step, user_id),
    FOREIGN KEY (game_id, step) REFERENCES game_state (game_id, step)
);

CREATE TABLE wall_position
(
    game_id  UUID        NOT NULL,
    step     INTEGER     NOT NULL,
    orient   orientation NOT NULL,
    "row"    SMALLINT    NOT NULL CHECK ("row" BETWEEN 0 AND 7),
    "column" SMALLINT    NOT NULL CHECK ("column" BETWEEN 0 AND 7),
    FOREIGN KEY (game_id, step) REFERENCES game_state (game_id, step)
);

CREATE TABLE winner
(
    game_id UUID NOT NULL PRIMARY KEY REFERENCES game,
    user_id UUID NOT NULL REFERENCES userdata,
    FOREIGN KEY (game_id, user_id) REFERENCES player (game_id, user_id)
);
