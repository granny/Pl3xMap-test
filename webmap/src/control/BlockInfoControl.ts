import * as L from "leaflet";
import { Pl3xMap } from "../Pl3xMap";
import { Block } from "../palette/Block";
import { ControlBox } from "./ControlBox";
import { getJSON, getLangName } from "../util/Util";
import Pl3xMapLeafletMap from "../map/Pl3xMapLeafletMap";
import { CoordsControl } from "./CoordsControl";
import { BlockInfo } from "../palette/BlockInfo";

export class BlockInfoControl extends ControlBox {
    private _dom: HTMLDivElement = L.DomUtil.create('div');

    private _blockPalette: Map<number, string> = new Map();

    constructor(pl3xmap: Pl3xMap, position: string) {
        super(pl3xmap, position);

        getJSON('tiles/blocks.gz').then((json): void => {
            Object.entries(json).forEach((data: [string, unknown]): void => {
                let name: string = <string>data[1];
                name = getLangName("block", name);
                this._blockPalette.set(Number(data[0]), name);
            });
        });
    }

    onAdd(map: Pl3xMapLeafletMap): HTMLDivElement {
        this._dom = L.DomUtil.create('div', 'leaflet-control leaflet-control-panel leaflet-control-blockinfo');
        this._dom.dataset.label = this._pl3xmap.settings!.lang.blockInfo.label;
        this.update(map);
        return this._dom;
    }

    public update(map: Pl3xMapLeafletMap): void {
        const coords: CoordsControl = this._pl3xmap.controlManager.coordsControl!;
        const zoom: number = map.getCurrentZoom() < 0 ? 0 : map.getCurrentZoom();
        const x: number = coords.x;
        const z: number = coords.z;
        const step: number = 1 << zoom;
        const regionX: number = x >> 9;
        const regionZ: number = z >> 9;
        const fileX: number = Math.floor(regionX / step);
        const fileZ: number = Math.floor(regionZ / step);
        const tileX: number = (x / step) & 511;
        const tileZ: number = (z / step) & 511;

        let blockName: string = this._pl3xmap.settings!.lang.blockInfo.unknown.block ?? 'Unknown block';
        let biomeName: string = this._pl3xmap.settings!.lang.blockInfo.unknown.biome ?? 'Unknown biome';
        let y: number | undefined;

        const blockInfo: BlockInfo | undefined = this._pl3xmap.worldManager.currentWorld?.getBlockInfo(zoom, fileX, fileZ);
        if (blockInfo !== undefined) {
            const block: Block = blockInfo.getBlock(tileZ * 512 + tileX);
            if (block != null) {
                if (block.block != 0) {
                    blockName = this._blockPalette.get(block.block) ?? blockName;
                }
                if (block.biome != 0) {
                    biomeName = this._pl3xmap.worldManager.currentWorld?.biomePalette.get(block.biome) ?? biomeName;
                }

                if (block.block != 0) {
                    y = block.yPos + 1;
                }
            }
        }

        coords.y = y;
        this._dom.innerHTML = this._pl3xmap.settings!.lang.blockInfo.value
            .replace(/<block>/g, blockName!.padEnd(15, ' '))
            .replace(/<biome>/g, biomeName!.padEnd(15, ' '));
    }
}
