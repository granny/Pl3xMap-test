import * as L from "leaflet";
import {Pl3xMap} from "../Pl3xMap";
import {SidebarTab} from "../control/SidebarControl";
import {MarkerLayer} from "../layergroup/MarkerLayer";
import {createSVGIcon} from "../util/Util";
import '../svg/layers.svg';

interface LayerControlInput extends HTMLInputElement {
    layerId: number;
}

interface LayerListItem {
    layer: L.Layer;
    name: string;
}

export default class LayersTab extends L.Control.Layers implements SidebarTab {
    declare protected _map: L.Map;

    declare protected _layerControlInputs: HTMLInputElement[];

    declare protected _layers: LayerListItem[];
    declare protected _onInputClick: (e: Event) => void;
    declare protected _checkDisabledLayers: () => void;

    protected _pl3xmap: Pl3xMap;
    protected _skeleton: HTMLParagraphElement = L.DomUtil.create('p');
    protected _container: HTMLDivElement = L.DomUtil.create('div');
    protected _list: HTMLFieldSetElement = L.DomUtil.create('fieldset', 'menu');

    constructor(pl3xmap: Pl3xMap) {
        super({}, {}, {
            hideSingleBase: true,
            sortLayers: true,
            sortFunction: (layer1: L.Layer, layer2: L.Layer, name1: string, name2: string): number => {
                if (layer1 instanceof MarkerLayer && layer2 instanceof MarkerLayer) {
                    const diff: number = layer1.priority - layer2.priority;
                    if (diff !== 0) {
                        return diff;
                    }
                    name1 = layer1.label;
                    name2 = layer2.label;
                }
                return name1 < name2 ? -1 : (name2 < name1 ? 1 : 0);
            }
        });

        this._pl3xmap = pl3xmap;

        this._button.type = 'button';
        this._button.setAttribute('aria-expanded', 'false');
        this._button.setAttribute('aria-controls', `sidebar__layers`);
        this._button.appendChild(createSVGIcon('layers'));
        this._button.setAttribute('aria-label', pl3xmap.settings!.lang.layers.label);
        this._button.title = pl3xmap.settings!.lang.layers.label;

        this._content.hidden = true;
        this._content.id = `sidebar__layers`;
        this._content.setAttribute('aria-hidden', 'true');

        this.initEvents();
    }

    protected _button: HTMLButtonElement = L.DomUtil.create('button');

    get button(): HTMLElement {
        return this._button;
    }

    protected _content: HTMLDivElement = L.DomUtil.create('div');

    get content(): HTMLElement {
        return this._content;
    }

    get id(): string {
        return 'layers';
    }

    expand(): this {
        //Do nothing
        return this;
    }

    collapse(): this {
        //Do nothing
        return this;
    }

    onActivate(): void {
        if (!this._container.hidden) {
            (this._list.querySelector('input') as HTMLElement)!.focus();
        } else {
            this._skeleton.focus();
        }
    }

    private initEvents(): void {
        window.addEventListener('overlayadded', (e: CustomEvent<MarkerLayer>): void => {
            if (e.detail.showControls) {
                this.addOverlay(e.detail, e.detail.label);
            }
            if (!e.detail.defaultHidden) {
                e.detail.addTo(this._map);
            }
            this._update();
        });

        window.addEventListener('overlayremoved', (e: CustomEvent<MarkerLayer>): void => {
            e.detail.removeFrom(this._map);
            this.removeLayer(e.detail);
            this._update();
        });

        window.addEventListener('worldselected', (): void => {
            this._layers = [];
            this._update();
        });
    }

    private _initLayout(): void {
        const heading: HTMLHeadingElement = L.DomUtil.create('h2', '', this._content);
        heading.innerText = this._pl3xmap.settings!.lang.layers.label;
        heading.id = 'layers-heading';

        this._skeleton.innerText = this._pl3xmap.settings!.lang.layers.value;
        this._skeleton.id = 'layers-skeleton';
        this._skeleton.tabIndex = -1;

        this._list.setAttribute('aria-labelledby', 'overlay-layers-heading');
        this._list.setAttribute('role', 'listbox');

        this._container.appendChild(this._list);

        this._content.appendChild(this._skeleton);
        this._content.appendChild(this._container);
    }

    private _update() {
        if (!this._map) {
            return this;
        }

        L.DomUtil.empty(this._list);

        this._layerControlInputs = [];

        this._layers.forEach((layer: LayerListItem): void => {
            this._addItem(layer);
        });

        const hasLayers: boolean = this._layers.length > 0;
        this._container.hidden = !hasLayers;
        this._skeleton.hidden = hasLayers;

        return this;
    }

    private _addItem(layer: LayerListItem): HTMLLabelElement {
        const label: HTMLLabelElement = L.DomUtil.create('label'),
            input: LayerControlInput = L.DomUtil.create('input') as LayerControlInput;

        input.type = 'checkbox';
        input.name = 'overlays';
        input.layerId = L.Util.stamp(layer.layer);
        input.id = label.htmlFor = `overlay-${input.layerId}`;
        input.defaultChecked = this._map.hasLayer(layer.layer);

        this._layerControlInputs.push(input);

        L.DomEvent.on(input, 'click', this._onInputClick, this);

        label.innerText = layer.name;

        this._list.appendChild(input);
        this._list.appendChild(label);

        this._checkDisabledLayers();
        return label;
    }
}
