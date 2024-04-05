import * as L from "leaflet";
import {Marker, Type} from "./Marker";
import {Polygon} from "../util/Polygon";
import {getOrCreatePane, isset, toCenteredLatLng} from "../util/Util";
import {Polyline} from "../util/Polyline";
import {Point} from "../util/Point";

interface MultiPolygonOptions extends L.PolylineOptions {
    key: string;
    polygons: Polygon[];
    pane: string;
}

export class MultiPolygon extends Marker {
    constructor(type: Type) {
        const data: MultiPolygonOptions = type.data as unknown as MultiPolygonOptions;

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

        super(data.key, L.polygon(MultiPolygon.createPolys(data), options));
    }

    private static createPolys(data: MultiPolygonOptions): L.LatLng[][][] {
        const polys: any[] = [];
        data.polygons.forEach((polygon: Polygon): void => {
            const poly: any[] = [];
            polygon.polylines.forEach((polyline: Polyline): void => {
                const line: any[] = [];
                polyline.points.forEach((point: Point): void => {
                    line.push(toCenteredLatLng(point))
                });
                poly.push(line);
            });
            polys.push(poly);
        });
        return polys;
    }

    public update(raw: unknown[]): void {
        const data: MultiPolygonOptions = raw as unknown as MultiPolygonOptions;
        const polygon: L.Polygon = this.marker as L.Polygon;
        polygon.setLatLngs(MultiPolygon.createPolys(data));
    }
}
