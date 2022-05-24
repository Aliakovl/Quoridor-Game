function getGameHistory(userId, gameId, f) {
    let queryApi = `/api/${userId}/game/history?gameId=${gameId}`
    fetch(queryApi).then(response => {
        if (response.ok) {
            response.json().then(gameHistory => {
                f(gameHistory)
            })
        } else {
            response.json().then( em => {
                alert(em.errorMessage)
            })
        }
    })
}

function createSession(gameId, cb) {
    fetch(`${window.origin}/ws/create/${gameId}`, {
        method: 'POST'
    }).then(response => {
        if (response.ok) {
            response.json().then( sessionId => {
                cb(sessionId.sessionId)
            })
        } else {
            response.json().then( em => {
                alert("Can not make session")
            })
        }
    })
}

function createHistoryPage(gameHistory, node) {
    node.innerHTML = `<div id="placeholder"></div>
                      <div id="table-place"></div>
                      <div id="players-place"></div>`
    let toMain = document.createElement("button")
    toMain.innerText = "Exit history"
    toMain.onclick = _ => {
        window.location.reload()
    }

    document.getElementById("placeholder").appendChild(toMain)

    const game = gameState(gameHistory)
    renderGame(game.getElem(), () => {}, () => {})

    let backButton = document.createElement("button")
    backButton.innerText = "<-"
    backButton.disabled = !game.canBackward()
    backButton.onclick = _ => {
        game.back()
        backButton.disabled = !game.canBackward()
        forwardButton.disabled = !game.canForward()
        renderGame(game.getElem(), () => {}, () => {})
    }

    let forwardButton = document.createElement("button")
    forwardButton.innerText = "->"
    forwardButton.disabled = !game.canForward()
    forwardButton.onclick = _ => {
        game.forward()
        backButton.disabled = !game.canBackward()
        forwardButton.disabled = !game.canForward()
        renderGame(game.getElem(), () => {}, () => {})
    }

    let startFromThisState = document.createElement("button")
    startFromThisState.innerText = "Start new game from this place"
    startFromThisState.onclick = _ => {
        let gameId = game.getElem().id
        createSession(gameId, sessionId => {
            window.location.href = `${window.origin}/game-session/${sessionId}`
        })
    }

    document.getElementById("placeholder").appendChild(document.createElement("br"))
    document.getElementById("placeholder").appendChild(backButton)
    document.getElementById("placeholder").appendChild(forwardButton)
    document.getElementById("placeholder").appendChild(document.createElement("br"))
    document.getElementById("placeholder").appendChild(startFromThisState)
}

function gameState(gameHistory) {
    let index = 0;

    this.getElem = () => {
        return gameHistory[index]
    }

    this.back = () => {
        if (this.canBackward()) {
            index -= 1
        }
    }

    this.canBackward = () => {
        return index > 0
    }

    this.forward = () => {
        if (this.canForward()) {
            index += 1
        }
    }

    this.canForward = () => {
        return index < gameHistory.length - 1
    }

    return this
}