import * as L from "leaflet";
import {Point} from "../../util/Point";
import {getOrCreatePane, isset} from "../../util/Util";

export interface PopupOptions {
    content?: string;
    pane?: string;
    offset?: Point;
    maxWidth?: number;
    minWidth?: number;
    maxHeight?: number;
    autoPan?: boolean;
    autoPanPaddingTopLeft?: Point;
    autoPanPaddingBottomRight?: Point;
    autoPanPadding?: Point;
    keepInView?: boolean;
    closeButton?: boolean;
    autoClose?: boolean;
    closeOnEscapeKey?: boolean;
    closeOnClick?: boolean;
}

export class Popup {
    private readonly _content: string;
    private readonly _properties: L.PopupOptions;

    constructor(data: PopupOptions) {
        this._content = isset(data.content) ? data.content! : "";

        let props: L.PopupOptions = {};
        if (isset(data.offset)) props.offset = [data.offset!.x, data.offset!.z];

        this.applySize(data, props);
        this.applyPanning(data, props);
        this.applyClosing(data, props);

        if (isset(data.pane)) {
            const dom: HTMLElement = getOrCreatePane(data.pane!);
            props = {
                ...props,
                pane: dom.className.split(" ")[1].split("-")[1]
            };
        }

        this._properties = props;
    }

    get content(): string {
        return this._content;
    }

    get properties(): L.PopupOptions {
        return this._properties;
    }

    applySize(data: PopupOptions, props: L.PopupOptions): void {
        if (isset(data.maxWidth)) props.maxWidth = data.maxWidth;
        if (isset(data.minWidth)) props.minWidth = data.minWidth;
        if (isset(data.maxHeight)) props.maxHeight = data.maxHeight;
    }

    applyPanning(data: PopupOptions, props: L.PopupOptions): void {
        if (isset(data.autoPan)) props.autoPan = data.autoPan;
        if (isset(data.autoPanPaddingTopLeft)) props.autoPanPaddingTopLeft = [data.autoPanPaddingTopLeft!.x, data.autoPanPaddingTopLeft!.z];
        if (isset(data.autoPanPaddingBottomRight)) props.autoPanPaddingBottomRight = [data.autoPanPaddingBottomRight!.x, data.autoPanPaddingBottomRight!.z];
        if (isset(data.autoPanPadding)) props.autoPanPadding = [data.autoPanPadding!.x, data.autoPanPadding!.z];
        if (isset(data.keepInView)) props.keepInView = data.keepInView;
    }

    applyClosing(data: PopupOptions, props: L.PopupOptions): void {
        if (isset(data.closeButton)) props.closeButton = data.closeButton;
        if (isset(data.autoClose)) props.autoClose = data.autoClose;
        if (isset(data.closeOnEscapeKey)) props.closeOnEscapeKey = data.closeOnEscapeKey;
        if (isset(data.closeOnClick)) props.closeOnClick = data.closeOnClick;
    }
}
