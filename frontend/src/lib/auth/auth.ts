import {browser} from "$app/environment";

export function saveToken(accessToken: string): void {
    sessionStorage.setItem("AccessToken", accessToken);
}

export function getToken() {
    return browser && sessionStorage.getItem("AccessToken") || undefined;
}

export function deleteToken() {
    sessionStorage.removeItem("AccessToken");
}

export function directHome() {
    if (browser) {
        window.location.replace('/');
    }
}

export function getTokenUnsafe() {
    const token = getToken();
    if (token === undefined) {
        throw new Error("No access token");
    }
    return token;
}

export type Claim = {
    userId: string,
    username: string
}

export function getUser(accessToken: string): Claim {
    return JSON.parse(atob(accessToken.split(".")[1]));
}