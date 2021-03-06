package me.rfprojects.airy.resolver;

import me.rfprojects.airy.core.NioBuffer;
import me.rfprojects.airy.internal.Misc;
import me.rfprojects.airy.internal.Null;
import me.rfprojects.airy.serializer.Serializer;

import java.lang.reflect.Array;
import java.lang.reflect.Type;

public class ArrayResolver implements Resolver {

    private Serializer serializer;

    public ArrayResolver(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public boolean checkType(Class<?> type) {
        return type != null && type.isArray();
    }

    @Override
    public boolean writeObject(NioBuffer buffer, Object object, Class<?> reference, Type... generics) {
        reference = Misc.getComponentType(reference);
        Class<?> componentType = Misc.getComponentType(object.getClass());
        serializer.getRegistry().writeClass(buffer, componentType != reference ? componentType : null);

        Object array = object;
        do {
            int length = Array.getLength(array);
            buffer.putUnsignedVarint(length);

            for (int i = 0; i < length; i++) {
                array = Array.get(array, i);
                if (array != null)
                    break;
            }
        } while (array != null && array.getClass().isArray());

        deepIterate(buffer, object, 1, componentType);
        buffer.asByteBuffer().put((byte) 0);
        return true;
    }

    private int deepIterate(NioBuffer buffer, Object array, int indexer, Class<?> componentType) {
        boolean isArray = array.getClass().getComponentType().isArray();
        boolean isPrimitive = !isArray && componentType.isPrimitive();
        for (int i = 0, length = Array.getLength(array); i < length; i++, indexer++) {
            if (isArray) {
                Object subArray = Array.get(array, i);
                if (subArray != null)
                    indexer = deepIterate(buffer, subArray, indexer, componentType);
            } else {
                Object value = Array.get(array, i);
                if (!Null.isNull(value, componentType)) {
                    buffer.putUnsignedVarint(indexer);
                    if (!isPrimitive) {
                        Class<?> type = value.getClass();
                        serializer.getRegistry().writeClass(buffer, type != componentType ? type : null);
                    }
                    serializer.serialize(buffer, value, false);
                }
            }
        }
        return indexer - 1;
    }

    @Override
    public Object readObject(NioBuffer buffer, Class<?> reference, Type... generics) {
        reference = serializer.getRegistry().readClass(buffer, reference);

        int dimension = 1;
        while ((reference = reference.getComponentType()).isArray())
            dimension++;
        int[] dimensions = new int[dimension];
        for (int i = 0; i < dimension; i++)
            dimensions[i] = (int) buffer.getUnsignedVarint();

        int indexer;
        Object array = Array.newInstance(reference, dimensions);
        boolean isPrimitive = reference.isPrimitive();
        while ((indexer = (int) buffer.getUnsignedVarint()) != 0) {
            Class<?> type = isPrimitive ? reference : serializer.getRegistry().readClass(buffer, reference);
            setArray(array, dimensions, indexer, serializer.deserialize(buffer, type));
        }
        return array;
    }

    private void setArray(Object array, int[] dimensions, int indexer, Object value) {
        int dimension = dimensions.length;
        int[] indexes = new int[dimension];

        for (int i = 0; i < dimension; i++) {
            double factor = 1.0;
            for (int f = dimension - 1; f > i; f--)
                factor *= dimensions[f];
            indexes[i] = (int) Math.ceil(indexer / factor);
            indexer -= (indexes[i] - 1) * factor;
        }

        for (int i = 0; i < dimension - 1; i++)
            array = Array.get(array, indexes[i] - 1);
        Array.set(array, indexes[dimension - 1] - 1, value);
    }
}
