function historyRequest() {
    fetch(`/api${window.location.pathname}/history`).then(response => {
        if (response.ok) {
            response.json().then( history => {
                fillTable(history)
                return history
            })
        } else {
            response.json().then( em => {
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
    const userId = window.location.pathname.substring(1)
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
    }

    table.appendChild(tr)
}

document.addEventListener("DOMContentLoaded", _ => {
    document.getElementById("logout-button").onclick = _ => {
        document.cookie = "auth-cookie=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;"
        window.location.href = "/sign"
    }

    document.getElementById("new-game-button").onclick = _ => {
        window.location.href = `/game-creation${window.location.pathname}`
    }

    historyRequest()
})