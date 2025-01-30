package serialize;

import chess.moves.extra.ExtraMoveCalculator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Serializer {

    static final Gson GSON;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ExtraMoveCalculator.class, new ExtraRulesetAdapter());
        GSON = gsonBuilder.create();
    }

    public static String serialize(Object object) {
        return GSON.toJson(object);
    }

    public static <T> T deserialize(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

}
