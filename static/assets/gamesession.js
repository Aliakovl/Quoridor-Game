function getGameGySessionId(sessionId, cb) {
    const userId = window.localStorage.getItem("user_id")
    fetch(`${window.origin}/ws/game/${sessionId}/${userId}`)
        .then(response => {
            if (response.ok) {
                response.json().then(game => {
                    cb(game)
                })
            } else {
                response.json().then(em => {
                    alert(em.errorMessage)
                }).catch(() => {
                    window.location.href = window.location.origin
                })
            }
        })
}

function onPawnPlace(pawnPlace, ws) {
    pawnPlace.onclick = _ => {
        const userId = window.localStorage.getItem("user_id")
        const userMove =
            {
                "id": userId,
                "move": {
                    "pawnPosition": {
                        "row": pawnPlace.getAttribute("row"),
                        "column": pawnPlace.getAttribute("column")
                    }
                }
            }
        ws.send(JSON.stringify(userMove))
    }
}

function onWallPlace(wallsPlace, ws) {

    wallsPlace.onclick = _ => {
        const userId = window.localStorage.getItem("user_id")
        const userMove =
            {
                "id": userId,
                "move": {
                    "wallPosition": {
                        "orientation": wallsPlace.getAttribute("orientation"),
                        "row": wallsPlace.getAttribute("row"),
                        "column": wallsPlace.getAttribute("column")
                    }
                }
            }
        ws.send(JSON.stringify(userMove))
    }
}

document.addEventListener("DOMContentLoaded", _ => {
    document.getElementById("account-button").onclick = _ => {
        window.location.href = "/account"
    }

    document.getElementById("logout-button").onclick = _ => {
        document.cookie = "auth-cookie=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;"
        window.location.href = "/"
    }

    let sessionId = window.localStorage.getItem("sessionId")

    let uri = `ws://${window.location.origin.split("://")[1]}/ws/session/${sessionId}`

    let webSocket = new WebSocket(uri)

    webSocket.onmessage = event => {
        const gameOrException = JSON.parse(event.data.toString())

        if (gameOrException.hasOwnProperty("errorMessage")) {
            let li = document.createElement('li')
            li.innerText = gameOrException.errorMessage
            const ul = document.getElementById("errors")
            ul.prepend(li)
        } else {
            renderGame(gameOrException, pp => onPawnPlace(pp, webSocket), pw => onWallPlace(pw, webSocket))
        }
    }

    getGameGySessionId(sessionId, game => {
        renderGame(game, pp => onPawnPlace(pp, webSocket), pw => onWallPlace(pw, webSocket))
    })

})