package org.example.plain.common.service;

import java.util.UUID;

public class UUIDService {

    public static String makeUUID(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
