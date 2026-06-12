package de.robv.android.xposed;

import java.io.File;
import java.util.Map;

public class XSharedPreferences {
    public XSharedPreferences(String packageName) {}
    public XSharedPreferences(String packageName, String prefFileName) {}
    public XSharedPreferences(File prefFile) {}
    public void makeWorldReadable() {}
    public void reload() {}
    public String getString(String key, String defValue) { return defValue; }
    public int getInt(String key, int defValue) { return defValue; }
    public long getLong(String key, long defValue) { return defValue; }
    public float getFloat(String key, float defValue) { return defValue; }
    public boolean getBoolean(String key, boolean defValue) { return defValue; }
    public Map<String, ?> getAll() { return null; }
    public boolean contains(String key) { return false; }
}
