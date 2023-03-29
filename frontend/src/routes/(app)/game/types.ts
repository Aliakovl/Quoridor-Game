import type {Side} from "$lib/api/types";

type QuoridorStatus = 'empty' | 'wall' | 'forbidden';

export function pawnColour(target: Side) {
    switch (target) {
        case "north":
            return "#0000FF";
        case "south":
            return "#FF0000";
        case "east":
            return "#00FF00";
        case "west":
            return "#FFFF00";
    }
}

export function rotationAngle(target: Side) {
    switch (target) {
        case "north":
            return 0;
        case "south":
            return 180;
        case "east":
            return 90;
        case "west":
            return 270;
    }
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