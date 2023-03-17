function historyRequest(userId) {
    fetch(`/api/${userId}/history`).then(response => {
        if (response.ok) {
            response.json().then(history => {
                fillTable(history)
            })
        } else {
            response.json().then(em => {
                alert(em.errorMessage)
            })
        }
    })
}

function fillTable(history) {
    let table = document.getElementById("user-history-table")
    history.forEach(gameView => appendGameView(table, gameView))
}

function printWinner(winner) {
    if (winner == null) {
        return ``
    } else {
        return `<p>Winner: ${winner.login}</p>`
    }
}

function viewGameHistory(game_id) {
    const userId = window.localStorage.getItem("user_id")
    getGameHistory(userId, game_id, gh => createHistoryPage(gh, document.getElementById("user-space")))
}

function appendGameView(table, gameView) {
    const htmlString =
        `<tr>
            <td class="user-history">
                <div>
                    <p>Players: ${gameView.players.map(x => x.login).join(", ")}</p>
                    <p>${printWinner(gameView.winner)}</p>
                    <button class="view-history-button" type="button" value=${gameView.id}>View game history</button>
                </div>
            </td>
        </tr>`
    const tr = document.createElement("tr")
    tr.innerHTML = htmlString

    const button = tr.getElementsByClassName("view-history-button")[0]
    button.onclick = _ => {
        viewGameHistory(button.value)
        document.getElementById("new-game-button").remove()
    }

    table.appendChild(tr)
}

function currentSessionRequest(userId) {
    fetch(`/ws/current-sessions/${userId}`).then(response => {
        if (response.ok) {
            response.json().then(sessionList => {
                fillSessions(sessionList)
            })
        } else {
            response.json().then(em => {
                alert(em.errorMessage)
            })
        }
    })
}

function fillSessions(sessionList) {
    let table = document.getElementById("current-sessions")
    sessionList.forEach(session => appendSession(table, session))
}

function appendSession(table, session) {
    const htmlString =
        `<tr>
            <td class="user-history">
                <div>
                    <p>${session}</p>
                    <button class="enter-game-session" type="button" value=${session}>Enter game session</button>
                </div>
            </td>
        </tr>`
    const tr = document.createElement("tr")
    tr.innerHTML = htmlString

    const button = tr.getElementsByClassName("enter-game-session")[0]
    button.onclick = _ => {
        window.localStorage.setItem("sessionId", button.value)
        window.location.href = `${window.origin}/game-session`
    }

    table.appendChild(tr)
}

document.addEventListener("DOMContentLoaded", _ => {
    document.getElementById("logout-button").onclick = _ => {
        document.cookie = "auth-cookie=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;"
        window.location.href = "/"
    }

    document.getElementById("new-game-button").onclick = _ => {
        window.location.href = `/game-creation`
    }

    const userId = window.localStorage.getItem("user_id")

    currentSessionRequest(userId)
    historyRequest(userId)
})
