package net.ramptors.asincro;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import java.util.UUID;
import java.util.Date;
import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static net.ramptors.asincro.Util.muestraError;
import static net.ramptors.asincro.Util.isNullOrEmpty;

public class CtrlPasatiempoNuevo extends AppCompatActivity {
  private final String tag = getClass().getName();
  private final BdSincroHelper helper = new BdSincroHelper(this);
  private EditText iuNombre;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    setContentView(R.layout.form_pasatiempo);
    iuNombre = findViewById(R.id.iuNombre);
  }
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    final int menuRes;
    getMenuInflater().inflate(R.menu.menu_nuevo, menu);
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
      final String nombre = iuNombre.getText().toString().trim();
      Util.valida(this, !isNullOrEmpty(nombre), R.string.falta_el_texto);
      Util.valida(this, nombre.length() <= 255, R.string.mas_de_255);
      final ContentValues modelo = new ContentValues();
      modelo.put("PAS_UUID", UUID.randomUUID().toString());
      modelo.put("PAS_NOMBRE", nombre);
      modelo.put("PAS_MODIFICACION", new Date().getTime());
      modelo.put("PAS_ELIMINADO", 0);
      final SQLiteDatabase db = helper.getWritableDatabase();
      db.insert("PASATIEMPO", null, modelo);
      regresa();
    } catch (Exception e) {
      muestraError(this, tag, "Error guardando.", e);
    }
  }
}