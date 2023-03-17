function appendPseudoRow(table, row) {
    let tr = document.createElement("tr")
    for (let pseudoColumn = -1; pseudoColumn < 18; ++pseudoColumn) {
        let td = document.createElement("td")
        let column = Math.floor(pseudoColumn / 2)
        if (pseudoColumn % 2 === 0) {
            td.classList.add("wall-place")
            td.classList.add("horizontal")
            td.setAttribute("row", `${row}`)
            td.setAttribute("column", `${column}`)
            if (row < 0 || row > 7 || pseudoColumn < 0 || pseudoColumn > 16) {
                td.classList.add("empty-place")
            } else {
                td.id = `wh${row}${column}`
                td.setAttribute("orientation", "horizontal")
                if (column < 8) {
                    td.onmouseover = _ => {
                        td.classList.add("pointed-wall")
                        document.getElementById(`i${row}${column}`).classList.add("pointed-wall")
                        document.getElementById(`wh${row}${column + 1}`).classList.add("pointed-wall")
                    }
                    td.onmouseout = _ => {
                        td.classList.remove("pointed-wall")
                        document.getElementById(`i${row}${column}`).classList.remove("pointed-wall")
                        document.getElementById(`wh${row}${column + 1}`).classList.remove("pointed-wall")
                    }
                }
            }
        } else {
            td.classList.add("intersection")
            td.setAttribute("row", `${row}`)
            td.setAttribute("column", `${column}`)
            if (row < 0 || row > 7 || pseudoColumn < 0 || pseudoColumn > 16) {
                td.classList.add("empty-place")
            } else {
                td.id = `i${row}${column}`
            }
        }
        tr.appendChild(td)
    }
    table.appendChild(tr)
}

function appendRow(table, row) {
    let tr = document.createElement("tr")
    for (let pseudoColumn = -1; pseudoColumn < 18; ++pseudoColumn) {
        let column = Math.floor(pseudoColumn / 2)
        let td = document.createElement("td")
        if (pseudoColumn % 2 === 0) {
            td.classList.add("pawn-place")
            td.setAttribute("row", `${row}`)
            td.setAttribute("column", `${column}`)
            td.id = `p${row}${column}`
            td.onmouseover = _ =>
                td.classList.add("pointed-cell")
            td.onmouseout = _ =>
                td.classList.remove("pointed-cell")
        } else {
            td.classList.add("wall-place")
            td.classList.add("vertical")
            if (pseudoColumn < 0 || pseudoColumn > 16) {
                td.classList.add("empty-place")
            } else {
                td.setAttribute("row", `${column}`)
                td.setAttribute("column", `${row}`)
                td.id = `wv${column}${row}`
                td.setAttribute("orientation", "vertical")
                if (row < 8) {
                    td.onmouseover = _ => {
                        td.classList.add("pointed-wall")
                        document.getElementById(`i${row}${column}`).classList.add("pointed-wall")
                        document.getElementById(`wv${column}${row + 1}`).classList.add("pointed-wall")
                    }
                    td.onmouseout = _ => {
                        td.classList.remove("pointed-wall")
                        document.getElementById(`i${row}${column}`).classList.remove("pointed-wall")
                        document.getElementById(`wv${column}${row + 1}`).classList.remove("pointed-wall")
                    }
                }
            }
        }
        tr.appendChild(td)
    }
    table.appendChild(tr)
}

function createGameField(parent) {
    let table = document.createElement("table")
    table.classList.add("game-field")

    appendPseudoRow(table, -1)
    for (let row = 0; row < 9; ++row) {
        appendRow(table, row)
        appendPseudoRow(table, row)
    }

    parent.appendChild(table)
    return table
}

function setWall(gameField, wall) {
    if (wall.orientation === "horizontal") {
        document.getElementById(`wh${wall.row}${wall.column}`).classList.add("placed-wall")
        document.getElementById(`i${wall.row}${wall.column}`).classList.add("placed-wall")
        document.getElementById(`wh${wall.row}${wall.column + 1}`).classList.add("placed-wall")
    } else {
        document.getElementById(`wv${wall.row}${wall.column}`).classList.add("placed-wall")
        document.getElementById(`i${wall.column}${wall.row}`).classList.add("placed-wall")
        document.getElementById(`wv${wall.row}${wall.column + 1}`).classList.add("placed-wall")
    }
}

function sideOrder(side) {
    switch (side) {
        case "north":
            return 0
        case "east":
            return 1
        case "south":
            return 2
        case "west":
            return 3
    }
}

function sideArrow(side) {
    switch (side) {
        case "north":
            return '\u2191'
        case "east":
            return '\u2192'
        case "south":
            return '\u2193'
        case "west":
            return '\u2190'
    }
}

function createPlayersTable(players) {
    let playersList = document.createElement("ul")
    let activePlayer = players.activePlayer
    let allPlayers = Array.from(players.enemies)
    allPlayers.push(activePlayer)
    allPlayers.sort((a, b) => {
        return sideOrder(a.target) - sideOrder(b.target)
    })
    allPlayers.forEach(player => {
        pinPawn(player)
        addPlayerToTable(playersList, player, activePlayer)
    })
    return playersList
}

function addPlayerToTable(playersList, player, activePlayer) {
    let li = document.createElement("li")
    li.classList.add(player.target)
    if (player === activePlayer) {
        li.textContent = `➤ ${player.login}: ${sideArrow(player.target)} █ ${player.wallsAmount}`
    } else {
        li.textContent = `${player.login}: ${sideArrow(player.target)} █ ${player.wallsAmount}`
    }
    playersList.appendChild(li)
}

function pinPawn(player) {
    let row = player.pawnPosition.row
    let column = player.pawnPosition.column
    let td = document.getElementById(`p${row}${column}`)
    td.setAttribute("player-id", player.id)
    td.classList.add(player.target)
}

function renderGame(game, onPawnPlace, onWallPlace) {
    let tablePlace = document.getElementById("table-place")
    let nd = document.getElementById("players-place")

    tablePlace.innerHTML = null
    nd.innerHTML = null

    let gameField = createGameField(tablePlace)

    game.state.walls.forEach(wall => setWall(gameField, wall))

    const pawnPlaces = document.body.getElementsByClassName('pawn-place')

    for (let pp of pawnPlaces) {
        onPawnPlace(pp)
    }

    const wallsPlaces = document.body.getElementsByClassName('wall-place')

    for (let wp of wallsPlaces) {
        if (!wp.classList.contains('empty-place')) {
            onWallPlace(wp)
        }
    }

    let playersTable = createPlayersTable(game.state.players)
    nd.appendChild(playersTable)
}
