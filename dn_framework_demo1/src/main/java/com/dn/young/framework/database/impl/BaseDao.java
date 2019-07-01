package com.dn.young.framework.database.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.dn.young.framework.annotation.DbField;
import com.dn.young.framework.annotation.DbTable;
import com.dn.young.framework.database.IBaseDao;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BaseDao<T> implements IBaseDao<T> {

    //持有数据库对象的引用
    protected SQLiteDatabase sqLiteDatabase;

    //缓存数据库字段名和类成员变量的映射关系
    private Map<String, Field> fieldMap;

    //持有操作数据库对应的java类型
    private Class<T> entity;

    //数据库表名
    private String tableName;

    private boolean isInit = false;

    private BaseDao() {
        //初始化数据库所在路径
        String dbPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Dn2018" + File.separator;
        File dirFile = new File(dbPath);

        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        try {
            File dbFile = new File(dbPath + "young.db");
            if (!dbFile.exists()) {
                dbFile.createNewFile();
            }
            //打开数据库
            sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void init(Class<T> clazz) {
        this.entity = clazz;

        //如果数据库没有打开
        if (!sqLiteDatabase.isOpen()) {
            return;
        }

        //数据库表初始化
        if (!isInit) {
            //执行建表操作：create table if not exist tableName(id integer, name text, age integer, mobile text);
            sqLiteDatabase.execSQL(createTableSql());
            initCacheMap();
            isInit = true;
        }
    }

    /**
     * @return 创建建表sql语句
     */
    private String createTableSql() {
        Field[] declaredFields = entity.getDeclaredFields();

        //初始化数据库表名
        tableName = entity.isAnnotationPresent(DbTable.class) ? entity.getAnnotation(DbTable.class).value() : entity.getSimpleName().toLowerCase();
        StringBuilder sb = new StringBuilder();
        sb.append("create table if not exists ");
        sb.append(tableName);
        sb.append("(");
        sb.append("id integer primary key autoincrement, ");

        if (declaredFields.length > 0) {
            for (Field field : declaredFields) {
                //根据成员变量名称生成表中的字段名
                String columnName = field.isAnnotationPresent(DbField.class) ? field.getAnnotation(DbField.class).value() : field.getName().toLowerCase();
                sb.append(columnName).append(" ");
                if (field.getType() == String.class) {
                    sb.append("text, ");
                } else if (field.getType() == Integer.class) {
                    sb.append("integer, ");
                } else if (field.getType() == Long.class) {
                    sb.append("bigint, ");
                } else if (field.getType() == Double.class) {
                    sb.append("double, ");
                } else if (field.getType() == byte[].class) {
                    sb.append("blob, ");
                }
            }
        }

        sb.deleteCharAt(sb.lastIndexOf(", "));
        sb.append(")");
        return sb.toString();
    }

    //初始化Map
    private void initCacheMap() {
        if (fieldMap == null) {
            fieldMap = new HashMap<>();
        }

        //1.获取所有成员变量
        Field[] declaredFields = entity.getDeclaredFields();

        //2.获取数据库表中所有列名
        String sql = "select * from " + tableName + " limit 1,0";//空表，可以获取到该表中所有的列名
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        String[] columnNames = cursor.getColumnNames();
        cursor.close();

        //3.映射数据库列明和成员变量
        if (columnNames.length > 0 && declaredFields.length > 0) {
            for (Field field : declaredFields) {
                field.setAccessible(true);
                for (String columnName : columnNames) {
                    String fieldName = field.isAnnotationPresent(DbField.class) ? field.getAnnotation(DbField.class).value() : field.getName().toLowerCase();
                    if (columnName.equals(fieldName)) {
                        fieldMap.put(columnName, field);
                        break;
                    }
                }
            }
        }

    }

    @Override
    public long insert(T entity) {
//        插入操作
        return sqLiteDatabase.insert(tableName, null, getContentValues(getMapValues(entity)));
    }

    @Override
    public int delete(T where) {
//      删除语句：sqLiteDatabase.delete(String table, String whereClause, String[] whereArgs);
        int result;
        Condition condition = new Condition(getMapValues(where));
        result = sqLiteDatabase.delete(tableName, condition.getWhereClause(), condition.getWhereArgs());
        return result;
    }

    @Override
    public int update(T entity, T where) {
//      更新语句：sqLiteDatabase.update(String table, ContentValues values, String whereClause, String[] whereArgs)
        int result;
        ContentValues contentValues = getContentValues(getMapValues(entity));
        Condition condition = new Condition(getMapValues(where));
        String whereClause = condition.getWhereClause();
        String[] whereArgs = condition.getWhereArgs();
        result = sqLiteDatabase.update(tableName, contentValues, whereClause, whereArgs);
        return result;
    }

    @Override
    public List<T> query(T where) {
        return query(where, null, null, null);
    }

    @Override
    public List<T> query(T where, String orderBy, Integer startIndex, Integer limit) {
//      查询语句：sqLiteDatabase.query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        Map<String, String> map = getMapValues(where);
        Condition condition = new Condition(map);
        String limitString = (startIndex == null || limit == null) ? null : startIndex + "," + limit;
        Cursor cursor = sqLiteDatabase.query(tableName, null, condition.getWhereClause(), condition.getWhereArgs(), null, null, orderBy, limitString);
        return handleQueryCursor(cursor, where);
    }

    @Override
    public List<T> query(String sql) {

        return null;
    }

    private List<T> handleQueryCursor(Cursor cursor, T entity) {
        List<T> result = new ArrayList<>();

        T object;
        while (cursor.moveToNext()) {
            try {
                object = (T) entity.getClass().newInstance();
                Iterator<Map.Entry<String, Field>> iterator = fieldMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Field> entry = iterator.next();
                    int columnIndex = cursor.getColumnIndex(entry.getKey());
                    if (columnIndex != -1) {
                        Field field = entry.getValue();
                        Class type = field.getType();
                        if (type == String.class) {
                            field.set(object, cursor.getString(columnIndex));
                        } else if (type == Double.class) {
                            field.set(object, cursor.getDouble(columnIndex));
                        } else if (type == Float.class) {
                            field.set(object, cursor.getFloat(columnIndex));
                        } else if (type == Integer.class) {
                            field.set(object, cursor.getInt(columnIndex));
                        } else if (type == Long.class) {
                            field.set(object, cursor.getLong(columnIndex));
                        } else if (type == byte[].class) {
                            field.set(object, cursor.getBlob(columnIndex));
                        }
                    }
                }
                result.add(object);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

        }
        cursor.close();
        return result;
    }

    /**
     * 根据T类型获取value并获取对应字段名称保存到map对象中并返回
     *
     * @return contentValues
     */
    private Map<String, String> getMapValues(T entity) {
        Map<String, String> map = new HashMap<>();

        //获取缓存Map中所有成员变量
        Set<Map.Entry<String, Field>> entrySet = fieldMap.entrySet();
        Iterator<Map.Entry<String, Field>> iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Field> entry = iterator.next();
            String key = entry.getKey();
            Field field = entry.getValue();
            field.setAccessible(true);

            try {
                Object object = field.get(entity);
                if (object != null) {
                    String value = object.toString();
                    map.put(key, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return map;
    }

    /**
     * Map对象转换成ContentValues对象
     *
     * @return contentValues
     */
    private ContentValues getContentValues(Map<String, String> map) {
        ContentValues contentValues = new ContentValues();
        //获取缓存Map中所有成员变量
        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();
            contentValues.put(key, value);
        }
        return contentValues;
    }

    private class Condition {

        private String whereClause;

        private String[] whereArgs;

        public Condition(Map<String, String> whereClauseMap) {
            StringBuilder sb = new StringBuilder();
            sb.append("1 = 1");
            List<String> args = new ArrayList<>();
            if (whereClauseMap != null) {
                Set<Map.Entry<String, String>> entrySet = whereClauseMap.entrySet();
                Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> entry = iterator.next();
                    String key = entry.getKey();
                    String value = entry.getValue();
                    sb.append(" and " + key + "=?");
                    args.add(value);
                }

                whereClause = sb.toString();
                whereArgs = args.toArray(new String[args.size()]);
            }
        }

        public String getWhereClause() {
            return whereClause;
        }

        public String[] getWhereArgs() {
            return whereArgs;
        }
    }

}
