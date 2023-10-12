export {signIn, signUp, refresh, signOut}

async function signUp(username: string, password: string) {
    const response = await fetch("/auth/sign-up", {
        method: 'POST',
        body: JSON.stringify({"username": username, "password": password})
    });
    if (!response.ok) {
        const error = await response.text();
        const message = JSON.parse(error).errorMessage;
        if (message.endsWith("already exists")) {
            throw new Error("Username is taken");
        }
    }
    return await response.text();
}

async function signIn(username: string, password: string) {
    if (username === "") {
        throw new Error("Invalid Username");
    }
    if (password === "") {
        throw new Error("Invalid Password");
    }
    const response = await fetch("/auth/sign-in", {
        method: 'POST',
        body: JSON.stringify({"username": username, "password": password})
    });
    if (!response.ok) {
        const error = await response.text();
        const message = JSON.parse(error).errorMessage;
        if (response.status === 404) {
            throw new Error("Invalid Username");
        }
        if (message === "Invalid password") {
            throw new Error("Invalid Password");
        }
    }
    return await response.text();
}

async function refresh() {
    const response = await fetch("/auth/refresh", {
        method: 'POST'
    });
    if (!response.ok) {
        throw new Error("refresh failed");
    }
    return await response.text();
}

async function signOut() {
    const response = await fetch("/auth/sign-out", {
        method: 'POST'
    });
    if (!response.ok) {
        throw new Error("signOut failed");
    }
}
