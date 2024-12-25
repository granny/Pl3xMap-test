export class Block {
    private readonly _block: number;
    private readonly _biome: number;
    private readonly _yPos: number;
    private readonly _minY: number;

    constructor(packed: number, minY: number) {
        // 11111111111111111111111111111111 - 32 bits - (4294967295)
        // 11111111111                      - 11 bits - block (2047)
        //            111111111             -  9 bits - biome (511)
        //                     111111111111 - 12 bits - yPos  (4095)
        this._block = packed >>> 21;
        this._biome = (packed & 0b00000000000_111111111_000000000000) >>> 12;
        this._yPos = packed & 0b00000000000_000000000_111111111111;
        this._minY = minY;
    }

    get block(): number {
        return this._block;
    }

    get biome(): number {
        return this._biome;
    }

    get yPos(): number {
        return this._yPos + this._minY;
    }
}
