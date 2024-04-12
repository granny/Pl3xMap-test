import * as L from "leaflet";
import {Pl3xMap} from "../Pl3xMap";
import {ContextMenuItemType} from "../settings/WorldSettings";
import {insertCss, removeCss} from "../util/Util";

type ContextMenuCallback = {
    label: () => string;
    callback: (e: L.LeafletMouseEvent) => void;
};

export default class ContextMenuControl extends L.Control {
    private readonly _pl3xmap: Pl3xMap;
    private _dom: HTMLDivElement = L.DomUtil.create('div');
    private _id: string = 'pl3xmap-contextmenu';
    private _items: Map<ContextMenuItemType, ContextMenuCallback> = new Map(
        [
            [ContextMenuItemType.copyCoords, {
                label: () => {
                    const {x, y, z} = this._pl3xmap.controlManager.coordsControl ?? {x: 0, y: 0, z: 0};
                    return this._pl3xmap.settings!.lang.contextMenu.copyCoords
                        .replace(/<x>/g, x.toString())
                        .replace(/<y>/g, y?.toString() ?? '???')
                        .replace(/<z>/g, z.toString())
                },
                callback: () => {
                    const {x, y, z} = this._pl3xmap.controlManager.coordsControl ?? {x: 0, y: 0, z: 0};
                    const coords = `(${x}, ${y ?? '???'}, ${z})`;
                    navigator.clipboard.writeText(coords)
                },
            }],
            [ContextMenuItemType.copyLink, {
                label: () => this._pl3xmap.settings!.lang.contextMenu.copyLink,
                callback : () => {
                    const {x, z} = this._pl3xmap.controlManager.coordsControl ?? {x: 0, y: 0, z: 0};
                    const world = this._pl3xmap.worldManager.currentWorld;
                    navigator.clipboard.writeText(
                        window.location.href +
                        this._pl3xmap.controlManager.linkControl?.getUrlFromCoords(
                            x, z,
                            this._pl3xmap.map.getCurrentZoom(),
                            world
                        )
                    )
                },
            }],
            [ContextMenuItemType.centerMap, {
                label: () => this._pl3xmap.settings!.lang.contextMenu.centerMap,
                callback: (event: L.LeafletMouseEvent) => {
                    this._pl3xmap.map.panTo(event.latlng);
                },
            }]
        ]
    );

    constructor(pl3xmap: Pl3xMap) {
        super();
        this._pl3xmap = pl3xmap;
        if (this._pl3xmap.worldManager.currentWorld?.settings.ui.contextMenu.enabled) {
            this._init();
        }
    }

    private _init(): void {
        this._pl3xmap.map.on('contextmenu', this._show, this);
        this._pl3xmap.map.on('click', this._hide, this);

        const css = this._pl3xmap.worldManager.currentWorld?.settings.ui.contextMenu.css;
        if (css !== undefined) {
            insertCss(css, this._id);
        }
    }

    onAdd(): HTMLDivElement {
        this._dom = L.DomUtil.create('div', 'leaflet-control leaflet-control-contextmenu');
        this._dom.dataset.label = this._pl3xmap.settings!.lang.contextMenu.label;
        return this._dom;
    }


    onRemove(): void {
        removeCss(this._id);
    }

    private _show(event: L.LeafletMouseEvent): void {
        // Ignore right-clicks on controls (https://github.com/JLyne/LiveAtlas/blob/0819cdf2728b49d361f9adfda09ff08311a59337/src/components/map/MapContextMenu.vue#L188-L194)
        if(event.originalEvent.target && (event.originalEvent.target as HTMLElement).closest('.leaflet-control')) {
            return;
        }

        event.originalEvent.stopImmediatePropagation();
        event.originalEvent.preventDefault();

        this._dom.style.visibility = 'visible';

        this._dom.innerHTML = '';

        const world = this._pl3xmap.worldManager.currentWorld;
        world?.settings.ui.contextMenu?.items?.forEach(itemType => {
            const item: ContextMenuCallback | undefined = this._items.get(itemType);
            if (item === undefined) return;

            const menuItem = L.DomUtil.create('button', 'leaflet-control-contextmenu-item', this._dom);
            menuItem.innerHTML = item.label();
            L.DomEvent.on(menuItem, 'click', (ev) => {
                L.DomEvent.stopPropagation(ev);
                item.callback(event);
                this._hide();
            });
        });

        // Don't position offscreen (https://github.com/JLyne/LiveAtlas/blob/0819cdf2728b49d361f9adfda09ff08311a59337/src/components/map/MapContextMenu.vue#L123-L135)
        const x = Math.min(
                window.innerWidth - this._dom.offsetWidth - 10,
                event.originalEvent.clientX
            ),
            y = Math.min(
                window.innerHeight - this._dom.offsetHeight - 10,
                event.originalEvent.clientY
            );

        this._dom.style.transform = `translate(${x}px, ${y}px)`;
    }

    private _hide(): void {
        this._dom.style.visibility = 'hidden';
        this._dom.style.left = '-1000';
    }
}
