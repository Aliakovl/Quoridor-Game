import type {Side} from "$lib/api/types";

type QuoridorStatus = 'empty' | 'wall' | 'forbidden';

export function pawnColour(target: Side) {
    switch (target) {
        case "north":
            return "#3434e6";
        case "south":
            return "#bc1313";
        case "east":
            return "#38ae38";
        case "west":
            return "#c8c826";
    }
}

export function rotationAngle(target: Side) {
    return -90 * sideOrder(target);
}

export function sideOrder(side: Side) {
    switch (side) {
        case "north":
            return 0;
        case "east":
            return 1;
        case "south":
            return 2;
        case "west":
            return 3;
    }
}