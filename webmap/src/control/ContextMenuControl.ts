import * as L from "leaflet";
import {Pl3xMap} from "../Pl3xMap";

export default class ContextMenuControl extends L.Control {
    private readonly _pl3xmap: Pl3xMap;
    private _dom: HTMLDivElement = L.DomUtil.create('div');


    constructor(pl3xmap: Pl3xMap) {
        super();
        this._pl3xmap = pl3xmap;
        this._pl3xmap.map.on('contextmenu', this._show, this);
        this._pl3xmap.map.on('click', this._hide, this);
    }

    onAdd(): HTMLDivElement {
        this._dom = L.DomUtil.create('div', 'leaflet-control leaflet-control-contextmenu', this._pl3xmap.map.getContainer());
        this._dom.dataset.label = this._pl3xmap.settings!.lang.contextMenu.label;
        return this._dom;
    }

    private _show(event: L.LeafletMouseEvent): void {
        L.DomEvent.stopPropagation(event);
        this._dom.innerHTML = '';
        this._getItems(event).forEach((item) => {
            const menuItem = L.DomUtil.create('div', 'leaflet-control-contextmenu-item', this._dom);
            menuItem.innerHTML = item.label;
            L.DomEvent.on(menuItem, 'click', (e) => {
                L.DomEvent.stopPropagation(e);
                item.callback();
                this._hide();
            });
        });
        this._dom.style.display = 'block';
        this._dom.style.left = event.containerPoint.x + 'px';
        this._dom.style.top = event.containerPoint.y + 'px';
    }

    private _hide(): void {
        this._dom.style.display = 'none';
    }

    private _getItems(e: L.LeafletMouseEvent): Map<string, { label: string, callback: () => void }> {
        const {x, y, z} = this._pl3xmap.controlManager.coordsControl ?? {x: 0, y: 0, z: 0};
        const coords = `(${x}, ${y ?? '???'}, ${z})`;

        const items = new Map();
        items.set('copyCoords', {
            label: `${this._pl3xmap.settings!.lang.contextMenu.copyCoords} ${coords}`,
            callback: () => navigator.clipboard.writeText(coords),
        });
        items.set('centerMap', {
            label: this._pl3xmap.settings!.lang.contextMenu.centerMap,
            callback: () => this._pl3xmap.map.panTo(e.latlng),
        });

        return items;
    }
}