import {Settings} from "./settings/Settings";
import {ControlManager} from "./control/ControlManager";
import {PlayerManager} from "./player/PlayerManager";
import {WorldManager} from "./world/WorldManager";
import {getJSON} from "./util/Util";
import SidebarControl from "./control/SidebarControl";
import Pl3xMapLeafletMap from "./map/Pl3xMapLeafletMap";
import "./scss/styles.scss";
import {Player} from "./player/Player";

window.onload = function (): void {
    window.pl3xmap = new Pl3xMap();
};

/**
 * Represents the main Pl3xMap class.
 */
export class Pl3xMap {
    private static _instance: Pl3xMap;

    private readonly _map: Pl3xMapLeafletMap;

    private readonly _controlManager: ControlManager;
    private readonly _playerManager: PlayerManager;
    private readonly _worldManager: WorldManager;

    private _eventSource?: EventSource;

    private _langPalette: Map<string, string> = new Map();
    private _settings?: Settings;

    private _timestamp: number = (new Date()).getTime();
    private _timer: NodeJS.Timeout | undefined;

    constructor() {
        Pl3xMap._instance = this;

        this._map = new Pl3xMapLeafletMap(this);
        
        window.addEventListener('beforeunload', function () {
            if (Pl3xMap.instance.eventSource != undefined) {
                Pl3xMap.instance.eventSource.close();
            }
        });

        // update map size when window size, scale, or orientation changes
        'orientationchange resize'.split(' ').forEach((event: string): void => {
            window.addEventListener(event, (): void => {
                this._map.updateSizeToWindow();
            }, {passive: true});
        });

        this._controlManager = new ControlManager(this);
        this._playerManager = new PlayerManager(this);
        this._worldManager = new WorldManager(this);

        getJSON('tiles/settings.json').then((json) => {
            this._settings = json as Settings;
            document.title = this._settings.lang.title;
            //this.map.options.zoomSnap = json.zoom.snap;
            //this.map.options.zoomDelta = json.zoom.delta;
            //this.map.options.wheelPxPerZoomLevel = json.zoom.wheel;
            getJSON('lang/' + this._settings.lang.langFile).then((json): void => {
                Object.entries(json).forEach((data: [string, unknown]): void => {
                    this._langPalette.set(data[0], <string>data[1]);
                });
            });
            this.controlManager.sidebarControl = new SidebarControl(this);
            const promise: Promise<void> = this.worldManager.init(this._settings);
            this._eventSource = this.initSSE();
            this.update();
            return promise;
        });
    }

    public static get instance(): Pl3xMap {
        return Pl3xMap._instance;
    }

    private update(): void {
        if (this._eventSource?.readyState === EventSource.OPEN && (new Date()).getTime() - this._timestamp < 1000) {
            this._timer = setTimeout(() => this.update(), 1000);
            return;
        }
        getJSON('tiles/settings.json').then( (json): void => {
            this._settings = json as Settings;

            this.playerManager.update(this._settings.players);

            this._timer = setTimeout(() => this.update(), 1000);
        });
    }

    private initSSE(): EventSource {
        const eventSource = new EventSource("sse");

        eventSource.addEventListener("settings", (ev: Event) => {
            this._timestamp = (new Date()).getTime();
            const messageEvent = (ev as MessageEvent);
            const json: any = JSON.parse(messageEvent.data);
            this._settings = json as Settings;
            this.playerManager.update(this._settings.players);
        });

        return eventSource;
    }

    get map(): Pl3xMapLeafletMap {
        return this._map;
    }

    get controlManager(): ControlManager {
        return this._controlManager;
    }

    get playerManager(): PlayerManager {
        return this._playerManager;
    }

    get worldManager(): WorldManager {
        return this._worldManager;
    }

    get eventSource(): EventSource | undefined {
        return this._eventSource;
    }

    get langPalette(): Map<string, string> {
        return this._langPalette;
    }

    get settings(): Settings | undefined {
        return this._settings;
    }

    get timestamp(): number {
        return this._timestamp;
    }
}
