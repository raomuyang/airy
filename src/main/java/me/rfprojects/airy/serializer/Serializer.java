package me.rfprojects.airy.serializer;

import me.rfprojects.airy.core.ClassRegistry;
import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.resolver.chain.ResolverChain;

public interface Serializer {

    ClassRegistry getRegistry();

    ResolverChain getResolverChain();

    void serialize(NioBuffer buffer, Object object, boolean writeClass);

    <T> T deserialize(NioBuffer buffer, Class<T> type);
}
