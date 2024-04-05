import {Pl3xMap} from "../Pl3xMap";
import {MarkerLayer} from "../layergroup/MarkerLayer";
import {BlockInfo} from "../palette/BlockInfo";
import {Label} from "../settings/Lang";
import {Center, Spawn, WorldSettings, Zoom} from "../settings/WorldSettings";
import {DoubleTileLayer} from "../tilelayer/DoubleTileLayer";
import {WorldManager} from "./WorldManager";
import {fireCustomEvent, getBytes, getJSON, getLangName} from "../util/Util";

/**
 * Represents a loaded world.
 */
export class World {
    private readonly _pl3xmap: Pl3xMap;
    private readonly _settings: WorldSettings;
    private _rendererLayers: Map<Renderer, DoubleTileLayer> = new Map();
    private _loaded: boolean = false;
    private _timer: NodeJS.Timeout | undefined;

    constructor(pl3xmap: Pl3xMap, worldManager: WorldManager, settings: WorldSettings) {
        this._pl3xmap = pl3xmap;
        this._settings = settings;
    }

    private _currentRenderer?: Renderer;

    get currentRenderer(): Renderer | undefined {
        return this._currentRenderer;
    }

    private _currentRendererLayer?: DoubleTileLayer;

    get currentRendererLayer(): DoubleTileLayer | undefined {
        return this._currentRendererLayer;
    }

    private _markerLayers: MarkerLayer[] = [];

    get markerLayers(): MarkerLayer[] {
        return this._markerLayers;
    }

    private _biomePalette: Map<number, string> = new Map();

    get biomePalette(): Map<number, string> {
        return this._biomePalette;
    }

    private _blockInfo: Map<number, Map<string, BlockInfo>> = new Map();

    get blockInfo(): Map<number, Map<string, BlockInfo>> {
        return this._blockInfo;
    }

    get settings(): WorldSettings {
        return this._settings;
    }

    get name(): string {
        return this.settings.name;
    }

    get displayName(): string {
        return this.settings.displayName;
    }

    get type(): string {
        return this.settings.type;
    }

    get order(): number {
        return this.settings.order;
    }

    get renderers(): Renderer[] {
        return this.settings.renderers;
    }

    get spawn(): Spawn {
        return this.settings.spawn;
    }

    get center(): Center {
        return this.settings.center;
    }

    get zoom(): Zoom {
        return this.settings.zoom;
    }

    get background(): string {
        switch (this.type) {
            case "nether":
                return "url('images/sky/nether.png')";
            case "the_end":
                return "url('images/sky/the_end.png')";
            case "normal":
            default:
                return "url('images/sky/overworld.png')";
        }
    }

    public load(): Promise<World> {
        if (this._loaded) {
            return Promise.resolve(this);
        }

        getJSON(`tiles/${this.name}/biomes.gz`).then((json): void => {
            Object.entries(json).forEach((data: [string, unknown]): void => {
                let name: string = <string>data[1];
                name = getLangName("biome", name);
                this._biomePalette.set(Number(data[0]), name);
            });
        });

        //TODO: Handle errors
        return new Promise((resolve): void => {
            getJSON(`tiles/${this.name}/settings.json`)
                .then((settings: WorldSettings): void => {
                    this._loaded = true;

                    // copy settings values
                    this.settings.spawn = settings.spawn;
                    this.settings.center = settings.center;
                    this.settings.zoom = settings.zoom;
                    this.settings.tileUpdateInterval = settings.tileUpdateInterval;
                    this.settings.ui = settings.ui;

                    // setup renderers
                    this.settings.renderers.forEach((renderer: Renderer): void => {
                        this._rendererLayers.set(renderer, new DoubleTileLayer(this._pl3xmap, this, renderer));
                    });

                    resolve(this);
                });
        });
    }

    public unload(): void {
        clearTimeout(this._timer);
        // unload and clear markers
        this._markerLayers.forEach((layer: MarkerLayer) => layer.unload())
        this._markerLayers = [];
        // unload renderer layer
        this._currentRendererLayer?.remove();
        this._currentRendererLayer = undefined;
        this._currentRenderer = undefined;
    }

    public loadMarkers(): void {
        getJSON(`tiles/${this.name}/markers.json`)
            .then((json): void => {
                (json as MarkerLayer[]).forEach((layer: MarkerLayer): void => {
                    const markerLayer: MarkerLayer = new MarkerLayer(layer.key, layer.label, layer.updateInterval, layer.showControls, layer.defaultHidden, layer.priority, layer.zIndex, layer.pane, layer.css);
                    this._markerLayers.push(markerLayer);
                    markerLayer.update(this);
                });
            });
    }

    public loadBlockInfo(zoom: number, x: number, z: number): void {
        if (!this.settings.ui.blockinfo) {
            return;
        }
        getBytes(`tiles/${this.name}/${zoom}/blockinfo/${x}_${z}.pl3xmap.gz`)
            .then((buffer?: ArrayBuffer): void => {
                this.setBlockInfo(zoom, x, z, buffer);
            });
    }

    public getBlockInfo(zoom: number, x: number, z: number): BlockInfo | undefined {
        return this.blockInfo.get(zoom < 0 ? 0 : zoom)?.get(`${x}_${z}`);
    }

    public setBlockInfo(zoom: number, x: number, z: number, buffer?: ArrayBuffer): void {
        let infoMap: Map<string, BlockInfo> | undefined = this.blockInfo.get(zoom < 0 ? 0 : zoom);
        if (infoMap == undefined) {
            infoMap = new Map<string, BlockInfo>();
            this.blockInfo.set(zoom, infoMap);
        }

        const blockInfo: null | BlockInfo = buffer == undefined ? null : new BlockInfo(new Uint8Array(buffer));

        if (blockInfo == null) {
            infoMap.delete(`${x}_${z}`);
        } else {
            infoMap.set(`${x}_${z}`, blockInfo);
        }
    }

    public unsetBlockInfo(zoom: number, x: number, z: number): void {
        this.blockInfo.get(zoom)?.delete(`${x}_${z}`);
    }

    public getRendererLayer(renderer: Renderer): DoubleTileLayer | undefined {
        return this._rendererLayers.get(renderer);
    }

    public setRenderer(renderer: Renderer | string): void {
        clearTimeout(this._timer);

        if (!(renderer instanceof Renderer)) {
            this.renderers.forEach((label: Renderer): void => {
                if (label.label == renderer) {
                    renderer = label;
                }
            });
        }

        this._currentRendererLayer?.remove();
        this._currentRenderer = this.settings.renderers.indexOf(renderer as Renderer) > -1 ? renderer as Renderer : this.settings.renderers[0];
        this._currentRendererLayer = this._rendererLayers.get(this._currentRenderer);
        this._currentRendererLayer!.addTo(this._pl3xmap.map);

        this.tick();

        fireCustomEvent('rendererselected', this);
    }

    public resetRenderer(renderer?: Renderer | string): void {
        this.setRenderer(renderer ?? this.settings.renderers[0]);
    }

    private tick(): void {
        this.currentRendererLayer?.updateTileLayer();
        this._timer = setTimeout(() => this.tick(), this.settings.tileUpdateInterval * 1000);
    }
}

export class Renderer extends Label {
    private readonly _icon: string;

    constructor(label: string, value: string, icon: string) {
        super(label, value);
        this._icon = icon;
    }

    get icon(): string {
        return this._icon;
    }
}
