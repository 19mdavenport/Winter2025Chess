package serialize;

import com.google.gson.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

import java.lang.reflect.Type;

public class UserGameCommandDeserializer implements JsonDeserializer<UserGameCommand> {

    @Override
    public UserGameCommand deserialize(JsonElement el, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        try {
            String commandValue = el.getAsJsonObject().get("commandType").getAsString();
            UserGameCommand.CommandType command = UserGameCommand.CommandType.valueOf(commandValue);
            if (command == UserGameCommand.CommandType.MAKE_MOVE) {
                return ctx.deserialize(el, MakeMoveCommand.class);
            }
            return new Gson().fromJson(el, UserGameCommand.class);
        } catch (Exception e) {
            throw new JsonParseException("Invalid commandType", e);
        }
    }
}
