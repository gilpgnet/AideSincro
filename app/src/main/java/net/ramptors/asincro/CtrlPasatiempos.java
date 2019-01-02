package net.ramptors.asincro;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.ListView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import static net.ramptors.asincro.Util.muestraError;
import static net.ramptors.asincro.Util.EXTRA_ID;
import static net.ramptors.asincro.Util.muestraMensaje;

public class CtrlPasatiempos extends AppCompatActivity
    implements OnItemClickListener, LoaderManager.LoaderCallbacks<JSONObject> {
  private final String tag = getClass().getName();
  private final BdSincroHelper helper = new BdSincroHelper(this);
  private final Timer timer = new Timer();
  private ListView lista;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.form_listado);
    lista = findViewById(R.id.lista);
    lista.setOnItemClickListener(this);
    try {
      consulta();
      getSupportLoaderManager().initLoader(0, null, this);
    } catch (Exception e) {
      muestraError(this, tag, "Error buscando.", e);
    }
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        runOnUiThread(new Runnable() {
          public void run() {
            try {
              consulta();
              getSupportLoaderManager().restartLoader(0, null, CtrlPasatiempos.this);
            } catch (Exception e) {
              muestraError(CtrlPasatiempos.this, tag, "Error buscando.", e);
            }
          }
        });
      }
    }, 25000, 25000);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_listado, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.action_agrega:
      startActivity(new Intent(this, CtrlPasatiempoNuevo.class));
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    final Intent intent = new Intent(this, CtrlPasatiempo.class);
    intent.putExtra(EXTRA_ID, id);
    startActivity(intent);
  }

  @Override
  protected void onDestroy() {
    helper.close();
    timer.cancel();
    super.onDestroy();
  }

  private void consulta() {
    final SQLiteDatabase db = helper.getReadableDatabase();
    final Cursor cursor = db.rawQuery("SELECT _id, PAS_NOMBRE FROM PASATIEMPO WHERE PAS_ELIMINADO = 0", null);
    lista.setAdapter(new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor,
        new String[] { "PAS_NOMBRE" }, new int[] { android.R.id.text1 }, 0));
  }

  /** Envía todos los refranes para sincronización. */
  @Override
  public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
    final JSONArray todos = consultaTodos();
    return new SincroTaskLoader(this, todos);
  }

  private JSONArray consultaTodos() {
    Cursor cursor = null;
    try {
      final SQLiteDatabase db = helper.getReadableDatabase();
      cursor = db.rawQuery("SELECT PAS_UUID, PAS_NOMBRE, PAS_MODIFICACION, PAS_ELIMINADO FROM PASATIEMPO", null);
      final JSONArray arr = new JSONArray();
      while (cursor.moveToNext()) {
        final JSONObject obj = new JSONObject();
        obj.put("uuid", cursor.getString(cursor.getColumnIndexOrThrow("PAS_UUID")));
        obj.put("nombre", cursor.getString(cursor.getColumnIndexOrThrow("PAS_NOMBRE")));
        obj.put("modificacion", cursor.getLong(cursor.getColumnIndexOrThrow("PAS_MODIFICACION")));
        obj.put("eliminado", cursor.getInt(cursor.getColumnIndexOrThrow("PAS_ELIMINADO")));
        arr.put(obj);
      }
      return arr;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  @Override
  public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
    try {
      if (data == null) {
        throw new Exception(getString(R.string.error_procesando_respuesta));
      } else if (!data.isNull("lista")) {
        reemplaza(data.optJSONArray("lista"));
        consulta();
      } else if (!data.isNull("error")) {
        throw new Exception(data.optString("error"));
      }      
    } catch (Exception e) {
      muestraError(this, tag, "Error procesando respuesta.", e);
    }
  }

  @Override
  public void onLoaderReset(Loader<JSONObject> loader) {
  }

  private void reemplaza(JSONArray arr) throws JSONException {
    final SQLiteDatabase db = helper.getWritableDatabase();
    db.beginTransaction();
    try {
      db.delete("PASATIEMPO", null, null);
      for (int i = 0, length = arr.length(); i < length; i++) {
        final JSONObject obj = arr.getJSONObject(i);
        final ContentValues modelo = new ContentValues();
        modelo.put("PAS_UUID", obj.getString("uuid"));
        modelo.put("PAS_NOMBRE", obj.getString("nombre"));
        modelo.put("PAS_MODIFICACION", obj.getLong("modificacion"));
        modelo.put("PAS_ELIMINADO", obj.getInt("eliminado"));
        db.insert("PASATIEMPO", null, modelo);
      }
      db.setTransactionSuccessful();
    } finally {
      db.endTransaction();
    }
  }
}