package me.rfprojects.airy.resolver;

import me.rfprojects.airy.core.NioBuffer;

import java.lang.reflect.Type;

public class StringResolver implements Resolver {

    private String charsetName;

    public StringResolver() {
        this("UTF-8");
    }

    public StringResolver(String charsetName) {
        this.charsetName = charsetName;
    }

    @Override
    public boolean checkType(Class<?> type) {
        return type == String.class;
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        buffer.putString((String) object, charsetName);
        return true;
    }

    @Override
    public Object readObject(NioBuffer buffer, Class<?> reference, Type... generics) {
        return buffer.getString(charsetName);
    }
}
