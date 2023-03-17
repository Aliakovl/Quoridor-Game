function getUser(userLogin, cb) {
    fetch(`${window.origin}/api/user/${userLogin}`)
        .then(response => {
            if (response.ok) {
                response.json().then(user => {
                    cb(user)
                })
            } else {
                response.json().then(em => {
                    alert(em.errorMessage)
                })
            }
        })
}

function joinUser(user, cb) {
    const gameId = window.localStorage.getItem("game_id")
    fetch(`${window.origin}/api/${user.id}/join-game?gameId=${gameId}`, {
        method: 'POST'
    }).then(response => {
        if (response.ok) {
            response.json().then(protoGame => {
                cb(protoGame)
            })
        } else {
            response.json().then(em => {
                alert(em.errorMessage)
            })
        }
    })
}

function onAddPlayer() {
    const userLogin = document.getElementById("user-login").value
    getUser(userLogin, user => {
        joinUser(user, () => {
            let list = document.getElementById("added-players")
            let li = document.createElement('li')
            li.innerText = user.login
            list.appendChild(li)
        })
    })
}

function startGame(cb) {
    let gameId = window.localStorage.getItem("game_id")
    let user_id = window.localStorage.getItem("user_id")
    fetch(`${window.origin}/api/${user_id}/start-game?gameId=${gameId}`, {
        method: 'POST'
    }).then(response => {
        if (response.ok) {
            response.json().then(game => {
                cb(game)
            })
        } else {
            response.json().then(em => {
                alert(em.errorMessage)
            })
        }
    })
}

function createSession(game, cb) {
    fetch(`${window.origin}/ws/create/${game.id}`, {
        method: 'POST'
    }).then(response => {
        if (response.ok) {
            response.json().then(sessionId => {
                cb(sessionId.sessionId)
            })
        } else {
            response.json().then(em => {
                alert("Can not make session")
            })
        }
    })
}

function onStartGame() {
    startGame(game => {
        createSession(game, sessionId => {
            window.localStorage.setItem("sessionId", sessionId)
            window.location.href = `${window.origin}/game-session`
        })
    })
}

function createGame(cb) {
    let id = window.localStorage.getItem("user_id")
    fetch(`${window.origin}/api/${id}/create-game`, {
        method: 'POST'
    }).then(response => {
        if (response.ok) {
            response.json().then(protoGame => {
                cb(protoGame)
            })
        } else {
            response.json().then(em => {
                alert(em.errorMessage)
            })
        }
    })
}

function storeProtoGame(protoGame) {
    window.localStorage.setItem("game_id", protoGame.id.toString())
    let list = document.getElementById("added-players")
    let creator = protoGame.players.creator.login
    let li = document.createElement('li')
    li.innerText = creator
    list.appendChild(li)
}

document.addEventListener("DOMContentLoaded", _ => {
    document.getElementById("create-game-form").addEventListener("submit", (event) => {
        event.preventDefault()
    })

    document.getElementById("account-button").onclick = _ => {
        window.location.href = "/account"
    }

    document.getElementById("logout-button").onclick = _ => {
        document.cookie = "auth-cookie=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;"
        window.location.href = "/"
    }

    createGame(storeProtoGame)

    document.getElementById("add-player").addEventListener("click", event => {
        onAddPlayer()
    })

    document.getElementById("start-game").addEventListener("click", event => {
        onStartGame()
    })
})
