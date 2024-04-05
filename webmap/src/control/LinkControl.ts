import * as L from "leaflet";
import {Pl3xMap} from "../Pl3xMap";
import {ControlBox} from "./ControlBox";
import {createSVGIcon, toPoint} from "../util/Util";
import Pl3xMapLeafletMap from "../map/Pl3xMapLeafletMap";
import '../svg/link.svg';
import {World} from "../world/World";

export class LinkControl extends ControlBox {
    private readonly _dom: HTMLAnchorElement;

    constructor(pl3xmap: Pl3xMap, position: string) {
        super(pl3xmap, position);
        this._dom = L.DomUtil.create('a', 'leaflet-control leaflet-control-button leaflet-control-link');
        this._dom.appendChild(createSVGIcon('link'));
    }

    onAdd(map: Pl3xMapLeafletMap): HTMLAnchorElement {
        map.addEventListener('moveend', this.onEvent);
        map.addEventListener('zoomend', this.onEvent);
        this.update();
        return this._dom;
    }

    onRemove(map: Pl3xMapLeafletMap): void {
        map.removeEventListener('moveend', this.onEvent);
        map.removeEventListener('zoomend', this.onEvent);
    }

    public update(): void {
        const url: string = this.getUrlFromView(this._pl3xmap.worldManager.currentWorld);
        this._dom.href = url;
        this._dom.title = this._pl3xmap.settings?.lang.link.label ?? '';
        window.history.replaceState(null, this._pl3xmap.settings!.lang.title, url);
    }

    public getUrlFromView(world?: World): string {
        const center: L.PointTuple = toPoint(this._pl3xmap.map.getCenter());
        const zoom: number = this._pl3xmap.map.getCurrentZoom();
        const x: number = Math.floor(center[0]);
        const z: number = Math.floor(center[1]);
        let url: string = `?`;
        if (world !== undefined) {
            url += `world=${world.name}&renderer=${world.currentRenderer?.label ?? 'basic'}`;
        }
        return `${url}&zoom=${zoom}&x=${x}&z=${z}`;
    }

    private onEvent = (): void => {
        this.update();
    }
}
