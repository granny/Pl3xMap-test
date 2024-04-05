import * as L from "leaflet";
import {Pl3xMap} from "../Pl3xMap";
import {ControlBox} from "./ControlBox";
import {toPoint} from "../util/Util";
import Pl3xMapLeafletMap from "../map/Pl3xMapLeafletMap";

export class CoordsControl extends ControlBox {
    declare _map: Pl3xMapLeafletMap;
    private _dom: HTMLDivElement = L.DomUtil.create('div');

    constructor(pl3xmap: Pl3xMap, position: string) {
        super(pl3xmap, position);
    }

    private _x: number = 0;

    get x(): number {
        return this._x;
    }

    set x(x: number) {
        this._x = x;
    }

    private _y?: number;

    get y(): number | undefined {
        return this._y;
    }

    set y(y: number | undefined) {
        this._y = y;
    }

    private _z: number = 0;

    get z(): number {
        return this._z;
    }

    set z(z: number) {
        this._z = z;
    }

    onAdd(map: Pl3xMapLeafletMap): HTMLDivElement {
        this._dom = L.DomUtil.create('div', 'leaflet-control leaflet-control-panel leaflet-control-coordinates');
        this._dom.dataset.label = this._pl3xmap.settings!.lang.coords.label;
        map.addEventListener('mousemove', this.onEvent);
        this.update(map, [0, 0]);
        return this._dom;
    }

    onRemove(map: Pl3xMapLeafletMap): void {
        map.removeEventListener('mousemove', this.onEvent);
    }

    private onEvent = (event: L.LeafletMouseEvent) => {
        return this.update(this._map, toPoint(event.latlng));
    }

    private update(map: Pl3xMapLeafletMap, point: L.PointTuple): void {
        this.x = Math.round(point[0]) - 1;
        this.z = Math.round(point[1]) - 1;
        this._pl3xmap.controlManager.blockInfoControl?.update(map);
        this._dom.innerHTML = this._pl3xmap.settings!.lang.coords.value
            .replace(/<x>/g, this.x.toString().padStart(6, ' '))
            .replace(/<y>/g, (this.y?.toString() ?? '???').padStart(2, ' ').padEnd(3, ' '))
            .replace(/<z>/g, this.z.toString().padEnd(6, ' '));
    }
}
