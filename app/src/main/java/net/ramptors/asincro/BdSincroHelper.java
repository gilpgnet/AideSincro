package net.ramptors.asincro;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

public class BdSincroHelper extends SQLiteOpenHelper {
  public BdSincroHelper(Context context) {
    super(context, "sincro", null, 1);
  }
  public void onCreate(SQLiteDatabase db) {
    db.execSQL("CREATE TABLE PASATIEMPO ("
        + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
        + "PAS_UUID TEXT UNIQUE,"
        + "PAS_NOMBRE TEXT,"
        + "PAS_MODIFICACION INTEGER,"
        + "PAS_ELIMINADO INTEGER)");
  }
  public void onUpgrade(SQLiteDatabase db, int oldVersion,
      int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS PASATIEMPO");
    onCreate(db);
  }
}