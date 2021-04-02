package kim.hsl.dex;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        /*
            验证 Application 是否替换成功
            打印 Application , ApplicationContext , ApplicationInfo
         */
        Log.i("octopus.MyProvider", "Application : " + getContext());
        Log.i("octopus.MyProvider", "ApplicationContext : " + getContext().getApplicationContext());
        Log.i("octopus.MyProvider", "ApplicationInfo.className : " + getContext().getApplicationInfo().className);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.e("octopus.MyProvider", "MyProvider delete : " + getContext());
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
