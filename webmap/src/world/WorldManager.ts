import {Pl3xMap} from "../Pl3xMap";
import {BlockInfoControl} from "../control/BlockInfoControl";
import {CoordsControl} from "../control/CoordsControl";
import {LinkControl} from "../control/LinkControl";
import {Settings} from "../settings/Settings";
import {Renderer, World} from "./World";
import {fireCustomEvent, getUrlParam} from "../util/Util";
import {UI, WorldSettings} from "../settings/WorldSettings";

/**
 * The world manager. Manages all loaded worlds.
 */
export class WorldManager {
    private readonly _pl3xmap: Pl3xMap;

    private _worlds: Map<string, World> = new Map();

    constructor(pl3xmap: Pl3xMap) {
        this._pl3xmap = pl3xmap;
    }

    private _currentWorld?: World;

    get currentWorld(): World | undefined {
        return this._currentWorld;
    }

    public async init(settings: Settings): Promise<void> {
        // build world objects
        const worlds: any[] = [];
        settings.worldSettings.forEach((worldSettings: WorldSettings): void => {
            const world: World = new World(this._pl3xmap, this, worldSettings);
            worlds.push(world);
        });

        // sort and store worlds
        worlds.sort((w1: World, w2: World) => w1.settings.order - w2.settings.order).forEach((world: World): void => {
            this._worlds.set(world.name, world);
            fireCustomEvent('worldadded', world);
        });

        // get world name from url, or first world from settings
        const worldName: string = getUrlParam('world', this._worlds.keys().next().value);

        // load world
        const world: World | undefined = this._worlds.get(worldName);
        if (world) {
            await this.setWorld(world, getUrlParam('renderer', undefined));
        }
    }

    public getWorld(world: string): World | undefined {
        world = world.replace(/:/g, "-");
        return this._worlds.get(world);
    }

    public async setWorld(world: World, renderer?: Renderer | string, resetCoords?: boolean): Promise<void> {
        return world.load().then((): void => {
            if (world === this._currentWorld && renderer === this._currentWorld.currentRenderer) {
                return;
            }

            if (world !== this._currentWorld) {
                this._currentWorld?.unload();
                this._currentWorld = world;
                world.loadMarkers();
                fireCustomEvent('worldselected', world);
                document.getElementById("map")!.style.background = world.background;
            }

            world.resetRenderer(renderer);

            const xCoord = world.center.x === -1 ? world.spawn.x : world.center.x;
            const zCoord = world.center.z === -1 ? world.spawn.z : world.center.z;
            this._pl3xmap.map.centerOn(
                resetCoords ? xCoord : getUrlParam('x', xCoord),
                resetCoords ? zCoord : getUrlParam('z', zCoord),
                resetCoords ? world.zoom.default : getUrlParam('zoom', world.zoom.default)
            );

            const ui: UI = world.settings.ui;
            this._pl3xmap.controlManager.linkControl = ui.link ? new LinkControl(this._pl3xmap, ui.link) : undefined;
            this._pl3xmap.controlManager.coordsControl = ui.coords ? new CoordsControl(this._pl3xmap, ui.coords) : undefined;
            this._pl3xmap.controlManager.blockInfoControl = ui.blockinfo ? new BlockInfoControl(this._pl3xmap, ui.blockinfo) : undefined;

            const attributeDom: HTMLElement | undefined = this._pl3xmap.map.attributionControl.getContainer();
            if (attributeDom) {
                attributeDom.style.display = ui.attribution ? "inline-block" : "none";
            }
        });
    }
}
