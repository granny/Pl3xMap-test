/**
 * Represents language settings.
 */
export class Lang {
    private readonly _title: string;
    private readonly _langFile: string;
    private readonly _unknownBiome: string;
    private readonly _unknownBlock: string;
    private readonly _coords: Label;
    private readonly _blockInfo: Label;
    private readonly _layers: Label;
    private readonly _link: Label;
    private readonly _markers: Label;
    private readonly _players: Label;
    private readonly _worlds: Label;

    constructor(title: string, langFile: string, unknownBiome: string, unknownBlock: string, coords: Label, blockInfo: Label, layers: Label, link: Label, markers: Label, players: Label, worlds: Label) {
        this._title = title;
        this._langFile = langFile;
        this._unknownBiome = unknownBiome;
        this._unknownBlock = unknownBlock;
        this._coords = coords;
        this._blockInfo = blockInfo;
        this._layers = layers;
        this._link = link;
        this._markers = markers;
        this._players = players;
        this._worlds = worlds;
    }

    get title(): string {
        return this._title;
    }

    get langFile(): string {
        return this._langFile;
    }

    get unknownBiome(): string {
        return this._unknownBiome;
    }

    get unknownBlock(): string {
        return this._unknownBlock;
    }

    get coords(): Label {
        return this._coords;
    }

    get blockInfo(): Label {
        return this._blockInfo;
    }

    get layers(): Label {
        return this._layers;
    }

    get link(): Label {
        return this._link;
    }

    get markers(): Label {
        return this._markers;
    }

    get players(): Label {
        return this._players;
    }

    get worlds(): Label {
        return this._worlds;
    }
}

/**
 * Represents a label and value.
 */
export class Label {
    private readonly _label: string
    private readonly _value: string;

    constructor(label: string, value: string) {
        this._label = label;
        this._value = value;
    }

    get label(): string {
        return this._label;
    }

    get value(): string {
        return this._value;
    }
}
