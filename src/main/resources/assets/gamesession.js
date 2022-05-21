function getGameGySessionId(sessionId, cb) {
    let userId = window.localStorage.getItem("user_id")
    fetch(`${window.origin}/ws/game/${sessionId}/${userId}`)
        .then(response => {
            if (response.ok) {
                response.json().then(game => {
                    cb(game)
                })
            } else {
                response.json().then( em => {
                    alert(em.errorMessage)
                }).catch(reason => {
                    alert(reason.toString())
                })
            }
        })
}

document.addEventListener("DOMContentLoaded", _ => {
    document.getElementById("logout-button").onclick = _ => {
        document.cookie = "auth-cookie=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;"
        window.location.href = "/sign"
    }

    let sessionId = window.location.pathname.split("/")[2]

    getGameGySessionId(sessionId, game => {
        renderGame(game)
    })

})