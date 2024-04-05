/**
 * Represents language settings.
 */
export class Lang {
    private readonly _title: string;
    private readonly _langFile: string;
    private readonly _coords: Label;
    private readonly _blockInfo: BlockInfo;
    private readonly _layers: Label;
    private readonly _link: Label;
    private readonly _markers: Label;
    private readonly _players: Label;
    private readonly _worlds: Label;

    constructor(title: string, langFile: string, coords: Label, blockInfo: BlockInfo, layers: Label, link: Label, markers: Label, players: Label, worlds: Label) {
        this._title = title;
        this._langFile = langFile;
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

    get coords(): Label {
        return this._coords;
    }

    get blockInfo(): BlockInfo {
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
    private readonly _label: string;
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

/**
 * Represents 'unknown' values for BlockInfo.
 */
export class BlockInfoUnknown {
    private readonly _block: string;
    private readonly _biome: string;

    constructor(block: string, biome: string) {
        this._block = block;
        this._biome = biome;
    }

    get block(): string {
        return this._block;
    }

    get biome(): string {
        return this._biome;
    }
}

/**
 * Represents a label and a value, with 'unknown' values.
 */
export class BlockInfo extends Label {
    private readonly _unknown: BlockInfoUnknown;

    constructor(label: string, value: string, unknown: BlockInfoUnknown) {
        super(label, value);
        this._unknown = unknown;
    }

    get unknown(): BlockInfoUnknown {
        return this._unknown;
    }
}
