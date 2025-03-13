package ui;

import java.util.Collection;
import java.util.function.Function;

public record UserInterfaceOption(Collection<String> invokeOptions, String description,
                                  Function<String[], UserInterfaceCommandOutput> callback) {
}
