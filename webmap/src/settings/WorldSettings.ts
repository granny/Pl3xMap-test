import {Renderer} from "../world/World";

/**
 * Represents per-world settings.
 */
export class WorldSettings {
    private readonly _name: string;
    private readonly _displayName: string;
    private readonly _directoryName: string;
    private readonly _type: string;
    private readonly _order: number;
    private readonly _renderers: Renderer[];

    private _tileUpdateInterval: number = 5;
    private _spawn: Spawn = new Spawn(0, 0);
    private _center: Center = new Center(-1, -1);
    private _zoom: Zoom = new Zoom(0, 3, 2);
    private _ui: UI = new UI()

    constructor(name: string, directoryName: string, displayName: string, type: string, order: number, renderers: Renderer[]) {
        this._name = name;
        this._directoryName = directoryName;
        this._displayName = displayName;
        this._type = type;
        this._order = order;
        this._renderers = renderers;
    }

    get name(): string {
        return this._name;
    }

    get directoryName(): string {
        return this._directoryName;
    }

    get displayName(): string {
        return this._displayName;
    }

    get type(): string {
        return this._type;
    }

    get order(): number {
        return this._order;
    }

    get renderers(): Renderer[] {
        return this._renderers;
    }

    get tileUpdateInterval(): number {
        return this._tileUpdateInterval;
    }

    set tileUpdateInterval(interval: number) {
        this._tileUpdateInterval = interval;
    }

    get spawn(): Spawn {
        return this._spawn;
    }

    set spawn(spawn: Spawn) {
        this._spawn = spawn;
    }

    get center(): Center {
        return this._center;
    }

    set center(center: Center) {
        this._center = center;
    }

    get zoom(): Zoom {
        return this._zoom;
    }

    set zoom(zoom: Zoom) {
        this._zoom = zoom;
    }

    get ui(): UI {
        return this._ui;
    }

    set ui(ui: UI) {
        this._ui = ui;
    }
}

/**
 * Represents a world's spawn settings.
 */
export class Spawn {
    private readonly _x: number;
    private readonly _z: number;

    constructor(x: number, z: number) {
        this._x = x;
        this._z = z;
    }

    get x(): number {
        return this._x;
    }

    get z(): number {
        return this._z;
    }
}

/**
 * Represents a world's center.
 */
export class Center {
    private readonly _x: number;
    private readonly _z: number;

    constructor(x: number, z: number) {
        this._x = x;
        this._z = z;
    }

    get x(): number {
        return this._x;
    }

    get z(): number {
        return this._z;
    }
}

/**
 * Represents UI settings.
 */
export class UI {
    private _link: string = 'bottomright';
    private _coords: string = 'bottomcenter';
    private _blockinfo: string = 'bottomleft';
    private _attribution: boolean = true;

    get link(): string {
        return this._link;
    }

    set link(value: string) {
        this._link = value;
    }

    get coords(): string {
        return this._coords;
    }

    set coords(value: string) {
        this._coords = value;
    }

    get blockinfo(): string {
        return this._blockinfo;
    }

    set blockinfo(value: string) {
        this._blockinfo = value;
    }

    get attribution(): boolean {
        return this._attribution;
    }

    set attribution(value: boolean) {
        this._attribution = value;
    }
}

/**
 * Represents a world's zoom settings.
 */
export class Zoom {
    private readonly _def: number;
    private readonly _maxOut: number;
    private readonly _maxIn: number;

    constructor(def: number, maxOut: number, maxIn: number) {
        this._def = def;
        this._maxOut = maxOut;
        this._maxIn = maxIn;
    }

    get default(): number {
        return this._def;
    }

    get maxOut(): number {
        return this._maxOut;
    }

    get maxIn(): number {
        return this._maxIn;
    }
}
