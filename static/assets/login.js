function postLogin(login, method) {
    fetch("/api/" + method, {
        method: 'POST',
        body: JSON.stringify({"login": login})
    }).then(response => {
        if (response.ok) {
            response.json().then( user => {
                window.localStorage.setItem("lagin", user.login)
                window.localStorage.setItem("user_id", user.id)
                window.location.href = `/account`
            })
        } else {
            response.json().then( em => {
                alert(em.errorMessage)
            })
        }
    }).catch(reason => {
        reason.json().then(er => {
            alert(er.toString())
        })
    })
}

document.addEventListener("DOMContentLoaded", _ => {
    document.getElementById("login-form").addEventListener("submit", (event) => {
        event.preventDefault()
    })

    document.getElementById("submit-login").onclick = _ => {
        let login = document.getElementById("login-field").value
        postLogin(login, "login")
    }

    document.getElementById("submit-register").onclick = _ => {
        let login = document.getElementById("login-field").value
        postLogin(login, "register")
    }
})