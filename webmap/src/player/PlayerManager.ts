import {Pl3xMap} from "../Pl3xMap";
import {Player} from "./Player";
import {Settings} from "../settings/Settings";
import {fireCustomEvent, toCenteredLatLng} from "../util/Util";
import {WorldManager} from "../world/WorldManager";
import Pl3xMapLeafletMap from "../map/Pl3xMapLeafletMap";
import {World} from "../world/World";
import {LatLng} from "leaflet";

export class PlayerManager {
    private readonly _pl3xmap: Pl3xMap;

    constructor(pl3xmap: Pl3xMap) {
        this._pl3xmap = pl3xmap;
    }

    private _players: Map<string, Player> = new Map();

    get players(): Map<string, Player> {
        return this._players;
    }

    private _follow?: Player;

    get follow(): Player | undefined {
        return this._follow;
    }

    set follow(player: Player | undefined) {
        this._follow = player;
        fireCustomEvent("followplayer", player);
    }

    public update(settings: Settings): void {
        const toRemove: Set<string> = new Set(this._players.keys());

        settings.players.forEach((data: Player): void => {
            const existing: Player | undefined = this._players.get(data.uuid);
            if (existing) {
                // update existing
                existing.displayName = data.displayName;
                existing.world = data.world;
                existing.position = data.position;

                // do not remove this player
                toRemove.delete(existing.uuid);
            } else {
                // create new player
                const player: Player = new Player(data.name, data.uuid, data.displayName, data.world, data.position);
                this._players.set(player.uuid, player);

                // inform the events
                fireCustomEvent('playeradded', player);
            }
        });

        toRemove.forEach((uuid: string): void => {
            // remove players not in updated settings file
            const player: Player | undefined = this._players.get(uuid);
            this._players.delete(uuid);
            fireCustomEvent('playerremoved', player);
        });

        // follow
        this.updateFollow();
    }

    public updateFollow(): void {
        if (!this.follow) {
            // not following anyone
            return;
        }
        if (!this.follow.world) {
            // player world is hidden
            return;
        }
        if (!this.follow.position) {
            // player position is hidden
            return;
        }
        const map: Pl3xMapLeafletMap = this._pl3xmap.map;
        const manager: WorldManager = this._pl3xmap.worldManager;
        const world: World | undefined = manager.getWorld(this.follow.world);
        if (!world) {
            // cant find world?!
            return;
        }
        const position: LatLng = toCenteredLatLng(this.follow.position);
        if (manager.currentWorld === world) {
            // player is in viewed world
            map.setView(position, map.getZoom());
            return;
        } else {
            // player is in a different world
            manager.setWorld(world).then((): void => {
                map.setView(position, map.getZoom());
            });
        }
    }
}
