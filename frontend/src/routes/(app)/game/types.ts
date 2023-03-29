import type {Side} from "$lib/api/types";

type QuoridorStatus = 'empty' | 'wall' | 'forbidden'

export function pawnColour(target: Side) {
    switch (target) {
        case "north":
            return "#0000FF"
        case "south":
            return "#FF0000"
        case "east":
            return "#00FF00"
        case "west":
            return "#FFFF00"
    }
}