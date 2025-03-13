package ui;

import exception.ResponseException;

@FunctionalInterface
public interface CommandCallback {
    UserInterfaceCommandOutput execute(Object[] args) throws ResponseException;
}
