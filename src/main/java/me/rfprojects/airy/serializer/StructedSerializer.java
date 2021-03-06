package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.NioBuffer;

import java.util.Map;

public interface StructedSerializer extends Serializer {

    Map<String, Integer> getStructMap(NioBuffer buffer, Class<?> type);
}
