import {browser} from "$app/environment"

export function saveToken(accessToken: string): void {
    sessionStorage.setItem("AccessToken", accessToken)
}

export function getToken() {
    return browser && sessionStorage.getItem("AccessToken") || undefined
}

export function getTokenUnsafe() {
    const token = getToken()
    if (token === undefined) {
        throw new Error("No access token")
    }
    return token
}
