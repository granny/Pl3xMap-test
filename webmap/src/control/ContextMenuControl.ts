import * as L from "leaflet";
import {Pl3xMap} from "../Pl3xMap";
import {ContextMenuCustomHtml, ContextMenuItemType} from "../settings/WorldSettings";

export default class ContextMenuControl extends L.Control {
    private readonly _pl3xmap: Pl3xMap;
    private _dom: HTMLDivElement = L.DomUtil.create('div');
    private _customHtml: ContextMenuCustomHtml;


    constructor(pl3xmap: Pl3xMap) {
        super();
        this._pl3xmap = pl3xmap;
        this._customHtml = pl3xmap.worldManager.currentWorld?.settings.ui.contextMenu.customHtml ?? new ContextMenuCustomHtml();
        if (this._pl3xmap.worldManager.currentWorld?.settings.ui.contextMenu.enabled) {
            this._init();
        }
    }
    
    private _init(): void {
        this._pl3xmap.map.on('contextmenu', this._show, this);
        this._pl3xmap.map.on('click', this._hide, this);
        if (this._customHtml.enabled) {
            const style = L.DomUtil.create('style', 'leaflet-control-contextmenu-custom-style', document.head);
            style.innerHTML = this._customHtml.css;
        }
    }

    onAdd(): HTMLDivElement {
        this._dom = L.DomUtil.create('div', 'leaflet-control leaflet-control-contextmenu');
        this._dom.dataset.label = this._pl3xmap.settings!.lang.contextMenu.label;
        if (this._customHtml.enabled) {
            this._dom.innerHTML = this._customHtml.html;
        }
        return this._dom;
    }

    private _show(event: L.LeafletMouseEvent): void {
        // Ignore right-clicks on controls (https://github.com/JLyne/LiveAtlas/blob/0819cdf2728b49d361f9adfda09ff08311a59337/src/components/map/MapContextMenu.vue#L188-L194)
        if(event.originalEvent.target && (event.originalEvent.target as HTMLElement).closest('.leaflet-control')) {
            return;
        }

        event.originalEvent.stopImmediatePropagation();
        event.originalEvent.preventDefault();

        this._dom.style.visibility = 'visible';

        if (!this._customHtml.enabled) {
            this._dom.innerHTML = '';
            this._getItems(event).forEach((item) => {
                const menuItem = L.DomUtil.create('button', 'leaflet-control-contextmenu-item', this._dom);
                menuItem.innerHTML = item.label;
                L.DomEvent.on(menuItem, 'click', (e) => {
                    L.DomEvent.stopPropagation(e);
                    item.callback();
                    this._hide();
                });
            });
        }

        // Don't position offscreen (https://github.com/JLyne/LiveAtlas/blob/0819cdf2728b49d361f9adfda09ff08311a59337/src/components/map/MapContextMenu.vue#L123-L135)
        const x = Math.min(
                window.innerWidth - this._dom.offsetWidth - 10,
                event.originalEvent.clientX
            ),
            y = Math.min(
                window.innerHeight - this._dom.offsetHeight - 10,
                event.originalEvent.clientY
            );

        this._dom.style.display = 'flex';
        this._dom.style.transform = `translate(${x}px, ${y}px)`;
    }

    private _hide(): void {
        this._dom.style.visibility = 'hidden';
        this._dom.style.left = '-1000';
    }

    private _getItems(e: L.LeafletMouseEvent): Map<string, { label: string, callback: () => void }> {
        const {x, y, z} = this._pl3xmap.controlManager.coordsControl ?? {x: 0, y: 0, z: 0};
        const coords = `(${x}, ${y ?? '???'}, ${z})`;
        const world = this._pl3xmap.worldManager.currentWorld;
        const settings = world?.settings.ui.contextMenu;
        const items: Map<string, { label: string, callback: () => void }> = new Map();

        settings?.items.forEach((item) => {
            switch (item) {
                case ContextMenuItemType.copyCoords:
                    items.set('copyCoords', {
                        label: this._pl3xmap.settings!.lang.contextMenu.copyCoords
                            .replace(/<x>/g, "" + x)
                            .replace(/<y>/g, "" + y)
                            .replace(/<z>/g, "" + z),
                        callback: () => navigator.clipboard.writeText(coords),
                    });
                    break;
                case ContextMenuItemType.copyLink:
                    items.set('copyLink', {
                        label: this._pl3xmap.settings!.lang.contextMenu.copyLink,
                        callback: () => navigator.clipboard.writeText(
                            window.location.href +
                            this._pl3xmap.controlManager.linkControl?.getUrlFromCoords(
                                x, z,
                                this._pl3xmap.map.getCurrentZoom(),
                                world
                            )
                        ),
                    });
                    break;
                case ContextMenuItemType.centerMap:
                    items.set('centerMap', {
                        label: this._pl3xmap.settings!.lang.contextMenu.centerMap,
                        callback: () => this._pl3xmap.map.panTo(e.latlng),
                    });
                    break;
            }
        });

        return items;
    }
}
