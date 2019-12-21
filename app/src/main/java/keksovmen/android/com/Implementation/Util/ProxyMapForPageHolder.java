package keksovmen.android.com.Implementation.Util;

import androidx.annotation.Nullable;

import java.util.LinkedHashMap;

public class ProxyMapForPageHolder extends LinkedHashMap<String, PageHolder> {

    @Nullable
    @Override
    public PageHolder put(String key, PageHolder value) {
        value.setIndexOnPane(size());
        return super.put(key, value);
    }

    @Nullable
    @Override
    public PageHolder remove(@Nullable Object key) {
        PageHolder remove = super.remove(key);
        if (remove != null) {
            int index = 0;
            for (PageHolder holder : values()) {
                holder.setIndexOnPane(index);
                index++;
            }
        }
        return remove;
    }
}
