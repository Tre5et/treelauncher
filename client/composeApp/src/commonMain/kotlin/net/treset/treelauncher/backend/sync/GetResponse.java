package net.treset.minecraftlauncher.sync;

import net.treset.mc_version_loader.json.GenericJsonParsable;
import net.treset.mc_version_loader.json.SerializationException;

import java.util.List;

public class GetResponse extends GenericJsonParsable {
    private int version;
    private List<String> difference;

    public GetResponse(int version, List<String> difference) {
        this.version = version;
        this.difference = difference;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<String> getDifference() {
        return difference;
    }

    public void setDifference(List<String> difference) {
        this.difference = difference;
    }

    public static GetResponse fromJson(String json) throws SerializationException {
        return GenericJsonParsable.fromJson(json, GetResponse.class);
    }
}
