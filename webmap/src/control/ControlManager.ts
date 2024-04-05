import {Pl3xMap} from "../Pl3xMap";
import {BlockInfoControl} from "./BlockInfoControl";
import {CoordsControl} from "./CoordsControl";
import {LinkControl} from "./LinkControl";
import SidebarControl from "./SidebarControl";

export class ControlManager {
    private readonly _pl3xmap: Pl3xMap;

    constructor(pl3xmap: Pl3xMap) {
        this._pl3xmap = pl3xmap;
    }

    private _sidebarControl?: SidebarControl

    get sidebarControl(): SidebarControl | undefined {
        return this._sidebarControl;
    }

    set sidebarControl(control: SidebarControl | undefined) {
        this._sidebarControl?.remove();
        this._sidebarControl = control;
        this._sidebarControl?.addTo(this._pl3xmap.map);
    }

    private _blockInfoControl?: BlockInfoControl;

    get blockInfoControl(): BlockInfoControl | undefined {
        return this._blockInfoControl;
    }

    set blockInfoControl(control: BlockInfoControl | undefined) {
        this._blockInfoControl?.remove();
        this._blockInfoControl = control;
        this._blockInfoControl?.addTo(this._pl3xmap.map);
    }

    private _coordsControl?: CoordsControl;

    get coordsControl(): CoordsControl | undefined {
        return this._coordsControl;
    }

    set coordsControl(control: CoordsControl | undefined) {
        this._coordsControl?.remove();
        this._coordsControl = control;
        this._coordsControl?.addTo(this._pl3xmap.map);
    }

    private _linkControl?: LinkControl;

    get linkControl(): LinkControl | undefined {
        return this._linkControl;
    }

    set linkControl(control: LinkControl | undefined) {
        this._linkControl?.remove();
        this._linkControl = control;
        this._linkControl?.addTo(this._pl3xmap.map);
    }
}
