import * as L from "leaflet";
import {Marker, Type} from "./Marker";
import {Polyline} from "../util/Polyline";
import {getOrCreatePane, isset, toCenteredLatLng} from "../util/Util";
import {Point} from "../util/Point";

interface PolygonOptions extends L.PolylineOptions {
    key: string;
    polylines: Polyline[];
    pane: string;
}

export class Polygon extends Marker {
    constructor(type: Type) {
        const data: PolygonOptions = type.data as unknown as PolygonOptions;

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

        super(data.key, L.polygon(Polygon.createPoly(data), options));
    }

    private static createPoly(data: PolygonOptions): L.LatLng[][] {
        const poly: any[] = [];
        data.polylines.forEach((polyline: Polyline): void => {
            const line: any[] = [];
            polyline.points.forEach((point: Point): void => {
                line.push(toCenteredLatLng(point));
            });
            poly.push(line);
        });
        return poly;
    }

    public update(raw: unknown[]): void {
        const data: PolygonOptions = raw as unknown as PolygonOptions;
        const polygon: L.Polygon = this.marker as L.Polygon;
        polygon.setLatLngs(Polygon.createPoly(data));
    }
}
