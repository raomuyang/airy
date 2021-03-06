package me.rfprojects.airy.resolver;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;

public class EnumResolver implements Resolver {

    @Override
    public boolean checkType(Class<?> type) {
        return type != null && type.isEnum();
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        buffer.putUnsignedVarint(((Enum) object).ordinal());
        return true;
    }

    @Override
    public Object readObject(NioBuffer buffer, Class<?> reference, Type... generics) {
        return reference.getEnumConstants()[(int) buffer.getUnsignedVarint()];
    }
}
