package net.ramptors.asincro;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import java.util.Date;
import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static net.ramptors.asincro.Util.EXTRA_ID;
import static net.ramptors.asincro.Util.muestraError;
import static net.ramptors.asincro.Util.muestraMensaje;
import static net.ramptors.asincro.Util.isNullOrEmpty;

public class CtrlPasatiempo extends AppCompatActivity {
  private final String tag = getClass().getName();
  private static ContentValues modelo;
  private final BdSincroHelper helper = new BdSincroHelper(this);
  private EditText iuNombre;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    setContentView(R.layout.form_pasatiempo);
    iuNombre = findViewById(R.id.iuNombre);
    final long extra_id = getIntent().getLongExtra(EXTRA_ID, -1);
    if (savedInstanceState == null && extra_id >= 0) {
      Cursor cursor = null;
      try {
        final SQLiteDatabase db = helper.getReadableDatabase();
        cursor = db.rawQuery("SELECT _id, PAS_UUID, PAS_NOMBRE, PAS_MODIFICACION,  PAS_ELIMINADO "
          + "FROM PASATIEMPO "
          + "WHERE _id = ? AND PAS_ELIMINADO = 0",
          new String[] { String.valueOf(extra_id) });
        if (cursor.moveToNext()) {
          modelo = new ContentValues();
          DatabaseUtils.cursorRowToContentValues(cursor, modelo);
          setTitle(modelo.getAsString("PAS_NOMBRE"));
          iuNombre.setText(modelo.getAsString("PAS_NOMBRE"));
        } else {
          muestraMensaje(this, getString(R.string.no_encontrado));
        }
      } catch (Exception e) {
        muestraError(this, tag, "Error buscando.", e);
      } finally {
        if (cursor != null) {
          cursor.close();
        }
      }
    } else {
      muestraMensaje(this, getString(R.string.no_encontrado));
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_detalle, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case android.R.id.home:
      regresa();
      return true;
    case R.id.action_guarda:
      guarda();
      return true;
    case R.id.action_elimina:
      elimina();
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onDestroy() {
    helper.close();
    super.onDestroy();
  }

  private void regresa() {
    NavUtils.navigateUpFromSameTask(this);
  }

  private void guarda() {
    try {
      final String nombre = this.iuNombre.getText().toString().trim();
      Util.valida(this, !isNullOrEmpty(nombre), R.string.falta_el_texto);
      Util.valida(this, nombre.length() <= 255, R.string.mas_de_255);
      modelo.put("PAS_NOMBRE", nombre);
      modelo.put("PAS_MODIFICACION", new Date().getTime());
      final SQLiteDatabase db = helper.getWritableDatabase();
      db.update("PASATIEMPO", modelo, "_id = ?", new String[] { modelo.getAsString("_id") });
      regresa();
    } catch (Exception e) {
      muestraError(this, tag, "Error guardando.", e);
    }
  }

  private void elimina() {
    try {
      modelo.put("PAS_MODIFICACION", new Date().getTime());
      modelo.put("PAS_ELIMINADO", 1);
      final SQLiteDatabase db = helper.getWritableDatabase();
      db.update("PASATIEMPO", modelo, "_id = ?", new String[] { modelo.getAsString("_id") });
      regresa();
    } catch (Exception e) {
      muestraError(this, tag, "Error guardando.", e);
    }
  }
}