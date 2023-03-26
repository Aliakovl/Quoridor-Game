export {signIn, signUp, refresh, signOut}

async function signUp(username: string, password: string) {
    const response = await fetch("/auth/sign-up", {
        method: 'POST',
        body: JSON.stringify({"username": username, "password": password})
    })
    if (!response.ok) {
        throw new Error("signUp failed")
    }
    return await response.text()
}

async function signIn(username: string, password: string) {
    const response = await fetch("/auth/sign-in", {
        method: 'POST',
        body: JSON.stringify({"username": username, "password": password})
    })
    if (!response.ok) {
        throw new Error("signIn failed")
    }
    return await response.text()
}

async function refresh() {
    const response = await fetch("/auth/refresh", {
        method: 'POST'
    })
    if (!response.ok) {
        throw new Error("refresh failed")
    }
    return await response.text()
}

async function signOut() {
    const response = await fetch("/auth/sign-out", {
        method: 'POST'
    })
    if (!response.ok) {
        throw new Error("signOut failed")
    }
}
