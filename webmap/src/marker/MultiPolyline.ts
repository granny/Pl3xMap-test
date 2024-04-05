import * as L from "leaflet";
import {Marker, Type} from "./Marker";
import {Polyline} from "../util/Polyline";
import {getOrCreatePane, isset, toCenteredLatLng} from "../util/Util";
import {Point} from "../util/Point";

interface MultiPolylineOptions extends L.PolylineOptions {
    key: string;
    polylines: Polyline[];
    pane: string;
}

export class MultiPolyline extends Marker {
    constructor(type: Type) {
        const data: MultiPolylineOptions = type.data as unknown as MultiPolylineOptions;

        let options = {
            ...type.options?.properties,
            smoothFactor: 1.0,
            noClip: false,
            bubblingMouseEvents: true,
            interactive: true,
            attribution: undefined
        };

        if (isset(data.pane)) {
            const dom: HTMLElement = getOrCreatePane(data.pane);
            options = {
                ...options,
                pane: dom.className.split("-")[1]
            };
        }

        super(data.key, L.polyline(MultiPolyline.createLines(data), options));
    }

    private static createLines(data: MultiPolylineOptions): L.LatLng[][] {
        const lines: any[] = [];
        data.polylines.forEach((polylines: Polyline): void => {
            const line: any[] = [];
            polylines.points.forEach((point: Point): void => {
                line.push(toCenteredLatLng(point))
            });
            lines.push(line);
        });
        return lines;
    }

    public update(raw: unknown[]): void {
        const data: MultiPolylineOptions = raw as unknown as MultiPolylineOptions;
        const polyline: L.Polyline = this.marker as L.Polyline;
        polyline.setLatLngs(MultiPolyline.createLines(data));
    }
}
