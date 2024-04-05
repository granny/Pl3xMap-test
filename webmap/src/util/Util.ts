import * as L from "leaflet";
import {Pl3xMap} from "../Pl3xMap";
import {Point} from "./Point";
import Pl3xMapLeafletMap from "../map/Pl3xMapLeafletMap";

export function createSVGIcon(icon: string): DocumentFragment {
    const template = L.DomUtil.create('template');
    template.innerHTML = `<svg class="svg-icon"><use href="#icon--${icon}"></use></svg>`;
    return template.content;
}

export function getJSON(url: string): Promise<any> {
    return fetch(url, {
        headers: {
            "Content-Disposition": "inline"
        }
    }).then(async (res: Response): Promise<any> => {
        if (res.ok) {
            return await res.json();
        }
    });
}

export function getBytes(url: string): Promise<ArrayBuffer | undefined> {
    return fetch(url, {
        headers: {
            "Content-Disposition": "inline"
        }
    }).then(async (res: Response): Promise<ArrayBuffer | undefined> => {
        if (res.ok) {
            return await res.arrayBuffer();
        }
    });
}

export function getUrlParam<T>(query: string, def: T): T {
    return new URLSearchParams(window.location.search).get(query) as unknown as T ?? def;
}

/**
 * Center marker points on block centers
 *
 * @param point marker point
 * @returns block centered latlng
 */
export function toCenteredLatLng(point: Point): L.LatLng {
    return toLatLng([point.x + 0.5, point.z + 0.5]);
}

export function toLatLng(point: L.PointTuple): L.LatLng {
    return L.latLng(pixelsToMeters(point[1]), pixelsToMeters(point[0]));
}

export function toLatLngBounds(point1: Point, point2: Point): L.LatLngBounds {
    return L.latLngBounds(toCenteredLatLng(point1), toCenteredLatLng(point2));
}

export function toPoint(latlng: L.LatLng): L.PointTuple {
    return [metersToPixels(latlng.lng), metersToPixels(latlng.lat)];
}

export function pixelsToMeters(num: number): number {
    return num * getScale();
}

export function metersToPixels(num: number): number {
    return num / getScale();
}

export function getScale(): number {
    const map: Pl3xMapLeafletMap = Pl3xMap.instance.map;
    return 1 / Math.pow(2, map.getMaxZoomOut());
}

export function isset(obj: unknown): boolean {
    return obj !== null && typeof obj !== 'undefined';
}

export function getOrCreatePane(name: string): HTMLElement {
    const map: Pl3xMapLeafletMap = Pl3xMap.instance.map;
    let pane: HTMLElement | undefined = map.getPane(name);
    if (pane == null) {
        pane = map.createPane(name);
    }
    return pane;
}

export function insertCss(css: string, layer: string): void {
    document.head.insertAdjacentHTML('beforeend', `<style id="${layer}">${css}</style>`);
}

export function removeCss(layer: string): void {
    document.getElementById(layer)?.remove();
}

export function fireCustomEvent<T>(event: keyof (WindowEventMap), detail: T): void {
    window.dispatchEvent(new CustomEvent(event, {detail}));
}

const navigationKeys: Set<string> = new Set<string>([
    'ArrowUp',
    'ArrowDown',
    'ArrowLeft',
    'ArrowRight',
    'Home',
    'End'
]);

/**
 * Helper method for handling keyboard based selection, along with focus navigation within a set of HTML elements.
 *
 * The given {@link KeyboardEvent} will be checked for common navigation keys (currently arrow keys + home/end)
 * and the appropriate {@link HTMLElement} in the provided array of elements will be focused. No focus changes will occur
 * if none of the provided elements are currently focused
 *
 * The event will also be checked for an enter key press, and a click event will be simulated on the target element. The
 * element does not need to be in the provided element array for this to occur
 *
 * @param {KeyboardEvent} e The event to handle
 * @param {HTMLElement[]} elements The elements to consider for focusing
 */
export const handleKeyboardEvent = (e: KeyboardEvent, elements: HTMLElement[]): void => {
    if (!e.target) {
        return;
    }

    if (navigationKeys.has(e.key)) {
        const position: number = elements.indexOf(e.target as HTMLElement);

        if (position < 0) {
            return;
        }

        let newPosition: number = position;

        switch (e.key) {
            case 'ArrowUp':
            case 'ArrowLeft':
                newPosition = position - 1;
                break;

            case 'ArrowDown':
            case 'ArrowRight':
                newPosition = position + 1;
                break;

            case 'Home':
                newPosition = 0;
                break;

            case 'End':
                newPosition = elements.length - 1;
                break;
        }

        if (newPosition < 0) {
            newPosition = elements.length - 1;
        } else if (newPosition >= elements.length) {
            newPosition = 0;
        }

        (elements[newPosition] as HTMLElement).focus();
        e.preventDefault();
    } else if (e.key === 'Enter' && e.target) {
        const mouseEvent: MouseEvent = new MouseEvent('click', {
            ctrlKey: e.ctrlKey,
            shiftKey: e.shiftKey,
            metaKey: e.metaKey,
            altKey: e.altKey,
            bubbles: true,
        });

        e.target.dispatchEvent(mouseEvent);
        e.preventDefault();
    }
}

export function getLangName(prefix: string, name: string): string {
    if (name.indexOf(':') !== -1) {
        const split = name.split(":");
        const a = prefix + '.' + split[0] + '.' + name.split(split[0] + ":")[1];
        const result = Pl3xMap.instance.langPalette.get(a);
        if (result != null) {
            return result
        }
    }
    if (name.indexOf(':') !== -1) {
        name = name.split(':')[1]             // split out the namespace
            .split(".").pop()!                // everything after the last period
            .replace(/_+/g, ' ')              // replace underscores with spaces
            .replace(/\w\S*/g, (w: string) => // capitalize first letter of every word
                w.charAt(0).toUpperCase() + w.substring(1)
            )
    }
    return name;
}
