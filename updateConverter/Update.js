"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.UpdateFile = exports.Update = void 0;
var Update = /** @class */ (function () {
    function Update(available, version, requires, files, updaterUrl) {
        this.available = available;
        this.version = version;
        this.requires = requires;
        this.files = files;
        this.updaterUrl = updaterUrl;
    }
    return Update;
}());
exports.Update = Update;
var UpdateFile = /** @class */ (function () {
    function UpdateFile(remove, update, url) {
        this.remove = remove;
        this.update = update;
        this.url = url;
    }
    return UpdateFile;
}());
exports.UpdateFile = UpdateFile;
//# sourceMappingURL=Update.js.map