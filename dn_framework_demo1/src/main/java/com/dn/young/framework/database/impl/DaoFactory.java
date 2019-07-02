package com.dn.young.framework.database.impl;

//import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DaoFactory<T extends BaseDao<E>, E> {

    private static DaoFactory daoFactory;

    //定义一个数据库连接池
    protected Map<String, T> daoMap;

    //
//    private SQLiteDatabase sqLiteDatabaseSplit;

    public synchronized static DaoFactory getInstance() {
        if (daoFactory == null) {
            daoFactory = new DaoFactory();
        }
        return daoFactory;
    }

    private DaoFactory() {
        daoMap = Collections.synchronizedMap(new HashMap<String, T>());
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
