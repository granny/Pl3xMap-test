import {Pl3xMap} from "../Pl3xMap";
import {BlockInfoControl} from "./BlockInfoControl";
import {CoordsControl} from "./CoordsControl";
import {LinkControl} from "./LinkControl";
import ContextMenuControl from "./ContextMenuControl";
import SidebarControl from "./SidebarControl";

export class ControlManager {
    private readonly _pl3xmap: Pl3xMap;

    private _contextMenuControl?: ContextMenuControl;
    private _sidebarControl?: SidebarControl
    private _blockInfoControl?: BlockInfoControl;
    private _coordsControl?: CoordsControl;
    private _linkControl?: LinkControl;

    constructor(pl3xmap: Pl3xMap) {
        this._pl3xmap = pl3xmap;
    }

    get contextMenuControl(): ContextMenuControl | undefined {
        return this._contextMenuControl;
    }

    set contextMenuControl(menu: ContextMenuControl | undefined) {
        this._contextMenuControl?.remove();
        this._contextMenuControl = menu;
        this._contextMenuControl?.addTo(this._pl3xmap.map);
    }

    get sidebarControl(): SidebarControl | undefined {
        return this._sidebarControl;
    }

    set sidebarControl(control: SidebarControl | undefined) {
        this._sidebarControl?.remove();
        this._sidebarControl = control;
        this._sidebarControl?.addTo(this._pl3xmap.map);
    }

    get blockInfoControl(): BlockInfoControl | undefined {
        return this._blockInfoControl;
    }

    set blockInfoControl(control: BlockInfoControl | undefined) {
        this._blockInfoControl?.remove();
        this._blockInfoControl = control;
        this._blockInfoControl?.addTo(this._pl3xmap.map);
    }

    get coordsControl(): CoordsControl | undefined {
        return this._coordsControl;
    }

    set coordsControl(control: CoordsControl | undefined) {
        this._coordsControl?.remove();
        this._coordsControl = control;
        this._coordsControl?.addTo(this._pl3xmap.map);
    }

    get linkControl(): LinkControl | undefined {
        return this._linkControl;
    }

    set linkControl(control: LinkControl | undefined) {
        this._linkControl?.remove();
        this._linkControl = control;
        this._linkControl?.addTo(this._pl3xmap.map);
    }
}
