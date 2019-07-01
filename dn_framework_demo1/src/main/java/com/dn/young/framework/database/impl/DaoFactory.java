package com.dn.young.framework.database.impl;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class DaoFactory<T extends BaseDao<E>, E> {

    private static DaoFactory daoFactory;

    private Map<String, T> daoMap;

    public synchronized static DaoFactory getInstance() {
        if (daoFactory == null) {
            daoFactory = new DaoFactory();
        }
        return daoFactory;
    }

    private DaoFactory() {
        daoMap = new HashMap<>();
    }

    public synchronized T getDao(Class<T> daoClass, Class<E> clazz) {
        try {
            if (daoMap.containsKey(clazz.getSimpleName())) {
                return daoMap.get(clazz.getSimpleName());
            }

            Constructor<T> constructor = daoClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            T t = constructor.newInstance();
            t.init(clazz);
            daoMap.put(clazz.getSimpleName(), t);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
