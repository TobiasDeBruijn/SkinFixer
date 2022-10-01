package dev.array21.skinfixer.reflect.abstractions.player;

import dev.array21.bukkitreflectionlib.ReflectionUtil;
import dev.array21.skinfixer.reflect.ReflectException;

import java.util.Collection;

public record PropertyMap(Object inner) {

    private static final Class<?> FORWARDING_MULTIMAP = com.google.common.collect.ForwardingMultimap.class;

    static PropertyMap getInstance(GameProfile gameProfile) throws ReflectException {
        try {
            Object inner = ReflectionUtil.invokeMethod(gameProfile.inner(), "getProperties");

            return new PropertyMap(inner);
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    public boolean containsTexturesKey() throws ReflectException {
        try {
            return (boolean) ReflectionUtil.invokeMethod(FORWARDING_MULTIMAP, this.inner, "containsKey", new Class<?>[] { Object.class }, new Object[] { "textures" });
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    public void removeTexturesKey() throws ReflectException {
        try {


            Object textures = ReflectionUtil.invokeMethod(FORWARDING_MULTIMAP, this.inner, "get", new Class<?>[] { Object.class }, new Object[] { "textures" });
            Object texturesIter = ReflectionUtil.invokeMethod(Collection.class, textures, "iterator");
            Object iterNext = ReflectionUtil.invokeMethod(texturesIter, "next");

            ReflectionUtil.invokeMethod(FORWARDING_MULTIMAP, this.inner, "remove", new Class<?>[] { Object.class, Object.class }, new Object[] { "textures", iterNext });
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }

    public void putSkinProperty(String skinValue, String skinSignature) throws ReflectException {
        try {
            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            Object newProperty = ReflectionUtil.invokeConstructor(propertyClass, "textures", skinValue, skinSignature);
            ReflectionUtil.invokeMethod(FORWARDING_MULTIMAP, this.inner, "put", new Class<?>[] { Object.class, Object.class }, new Object[] { "textures", newProperty });
        } catch (Exception e) {
            throw new ReflectException(e);
        }
    }
}
