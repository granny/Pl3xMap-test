import * as L from "leaflet";
import {TileEvent} from "leaflet";
import {Pl3xMap} from "../Pl3xMap";
import {Label} from "../settings/Lang";
import {Renderer, World} from "../world/World";

export class ReversedZoomTileLayer extends L.TileLayer {
    private readonly _world: World;
    private readonly _renderer: Label;

    constructor(pl3xmap: Pl3xMap, world: World, renderer: Renderer) {
        super(`tiles/${world.directoryName}/{z}/${renderer.label}/{x}_{y}.${pl3xmap.settings?.format}`, {
            // tile sizes match regions sizes (512 blocks x 512 blocks)
            tileSize: 512,
            // dont wrap tiles at edges
            noWrap: true,
            // the closest zoomed in possible (without stretching)
            // this is always 0. no exceptions!
            minNativeZoom: 0,
            // the farthest possible zoom out possible
            maxNativeZoom: world.zoom.maxOut,
            // for extra zoom in, make higher than maxNativeZoom
            // this is the stretched tiles to zoom in further
            maxZoom: world.zoom.maxOut + world.zoom.maxIn,
            // we need to counter effect the higher maxZoom here
            // maxZoom + zoomOffset = maxNativeZoom
            zoomOffset: -world.zoom.maxIn
        });

        this._world = world!;
        this._renderer = renderer;

        // when tiles load we need to load extra block info
        this.addEventListener("tileload", (event: TileEvent): void => {
            const zoom: number = world.settings.zoom.maxOut - event.coords.z;
            world.loadBlockInfo(zoom, event.coords.x, event.coords.y);
        });

        // when tiles unload we need to remove the extra block info from memory
        this.addEventListener("tileunload", (event: TileEvent): void => {
            const zoom: number = world.settings.zoom.maxOut - event.coords.z;
            world.unsetBlockInfo(zoom, event.coords.x, event.coords.y);
        });

        // push this layer to the back (leaflet defaults it to 1)
        this.setZIndex(0);
    }

    get world(): World {
        return this._world;
    }

    get renderer(): Label {
        return this._renderer;
    }

    _getZoomForUrl(): number {
        const zoom: number = this._tileZoom!,
            maxZoom: number = this.options.maxZoom!,
            offset: number = this.options.zoomOffset!;
        return (maxZoom - zoom) + offset;
    }

    // @method createTile(coords: Object, done?: Function): HTMLElement
    // Called only internally, overrides GridLayer's [`createTile()`](#gridlayer-createtile)
    // to return an `<img>` HTML element with the appropriate image URL given `coords`. The `done`
    // callback is called when the tile has been loaded.
    createTile(coords: L.Coords, done: L.DoneCallback): HTMLImageElement {
        const tile: HTMLImageElement = L.DomUtil.create('img');

        L.DomEvent.on(tile, 'load', (): void => {
            // Once image has loaded revoke the object URL as we don't need it anymore
            URL.revokeObjectURL(tile.src);
            this._tileOnLoad(done, tile)
        });
        L.DomEvent.on(tile, 'error', L.Util.bind(this._tileOnError, this, done, tile));

        if (this.options.crossOrigin || this.options.crossOrigin === '') {
            tile.crossOrigin = this.options.crossOrigin === true ? '' : this.options.crossOrigin;
        }

        tile.alt = '';
        tile.setAttribute('role', 'presentation');

        // Retrieve image via a fetch instead of just setting the src
        // This works around the fact that browsers usually don't make a request for an image that was previously loaded,
        // without resorting to changing the URL (which would break caching).
        fetch(this.getTileUrl(coords))
            .then((res: Response): void => {
                // Call leaflet's error handler if request fails for some reason
                if (!res.ok) {
                    this._tileOnError(done, tile, new Error(res.statusText));
                    return;
                }

                // Get image data and convert into object URL, so it can be used as a src
                // Leaflet's onload listener will take it from here
                res.blob().then((blob: Blob): void => {
                    // don't use URL.createObjectURL, it creates memory leak
                    const reader: FileReader = new FileReader();
                    reader.readAsDataURL(blob);
                    reader.onload = () => tile.src = String(reader.result);
                });
            }).catch((e) => this._tileOnError(done, tile, e));

        return tile;
    }
}
