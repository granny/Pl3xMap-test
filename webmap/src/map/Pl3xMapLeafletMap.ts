import * as L from "leaflet";
import {Pl3xMap} from "../Pl3xMap";
import {toLatLng} from "../util/Util";

/**
 * Represents the leaflet map.
 */
export default class Pl3xMapLeafletMap extends L.Map {
    declare _controlCorners: { [x: string]: HTMLDivElement; };
    declare _controlContainer?: HTMLElement;
    declare _container?: HTMLElement;

    private readonly _pl3xmap: Pl3xMap;

    constructor(pl3xmap: Pl3xMap) {
        super('map', {
            // simple crs for custom map tiles
            crs: L.Util.extend(L.CRS.Simple, {
                // we need to flip the y-axis correctly
                // https://stackoverflow.com/a/62320569/3530727
                transformation: new L.Transformation(1, 0, 1, 0)
            }),
            // always 0,0 center
            center: [0, 0],
            //zoomSnap: 0.25,
            //zoomDelta: 0.25,
            //wheelPxPerZoomLevel: 120,
            // show the attribution footer
            attributionControl: true,
            // canvas is faster than default svg
            preferCanvas: true
        });

        this.on('mousedown', (): void => {
            pl3xmap.playerManager.follow = undefined;
        });

        // sets the leaflet attribution prefix to our project page
        this.attributionControl.setPrefix("<a href='https://modrinth.com/plugin/pl3xmap/'>Pl3xMap &copy; 2020-2023</a>");

        this._pl3xmap = pl3xmap;

        // stuff to do after the map fully loads
        this.on('load', (): void => this.onLoad());
    }

    onLoad(): void {
        // fix map size on load - fixes android browser url bar pushing page off-screen
        // https://chanind.github.io/javascript/2019/09/28/avoid-100vh-on-mobile-web.html
        this.updateSizeToWindow();
    }

    // https://stackoverflow.com/a/60391674/3530727
    _initControlPos(): void {
        this._controlContainer = L.DomUtil.create('div', 'leaflet-control-container', this._container);

        const corners: { [x: string]: HTMLDivElement; } = this._controlCorners = {},
            top: HTMLDivElement = L.DomUtil.create('div', 'leaflet-control-container-top', this._controlContainer),
            bottom: HTMLDivElement = L.DomUtil.create('div', 'leaflet-control-container-bottom', this._controlContainer);

        function createCorner(vSide: string, hSide: string): void {
            corners[`${vSide}${hSide}`] = L.DomUtil.create('div', `leaflet-${vSide} leaflet-${hSide}`, vSide === 'top' ? top : bottom);
        }

        createCorner('top', 'left');
        createCorner('top', 'center');
        createCorner('top', 'right');
        createCorner('bottom', 'left');
        createCorner('bottom', 'center');
        createCorner('bottom', 'right');
    }

    public centerOn(x: number, z: number, zoom: number): void {
        this.setView(toLatLng([x, z]), this.getMaxZoomOut() - zoom);
    }

    public getMaxZoomOut(): number {
        return this._pl3xmap.worldManager.currentWorld?.zoom.maxOut ?? 0;
    }

    public getCurrentZoom(): number {
        return this.getMaxZoomOut() - this.getZoom();
    }

    public updateSizeToWindow(): void {
        const style: CSSStyleDeclaration = this.getContainer().style;
        style.width = `${window.innerWidth}px`;
        style.height = `${window.innerHeight}px`;
        this.invalidateSize();
    }
}
