package net.ramptors.asincro;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import static net.ramptors.asincro.Util.leeString;
import static net.ramptors.asincro.Util.muestraError;

public class CtrlSincro {
  private enum Etapa {
    INICIA, SINCRONIZANDO, MOSTRANDO
  }

  private static final String URL_SINCRO = "http://10.0.2.2/sincro/servicio/sincro.php";
  private static final String tag = CtrlSincro.class.getName();
  protected static final Executor executor = Executors.newSingleThreadExecutor();
  private static Etapa etapa = Etapa.INICIA;
  private static JSONObject resultado;
  private static Exception error;
  private static CtrlPasatiempos ctrlPasatiempos;

  public static synchronized void setCtrlPasatiempos(CtrlPasatiempos ctrlPasatiempos) {
    CtrlSincro.ctrlPasatiempos = ctrlPasatiempos;
  }

  public static synchronized void sincroniza(CtrlPasatiempos ctrlPasatiempos) throws JSONException {
    CtrlSincro.ctrlPasatiempos = ctrlPasatiempos;
    switch (etapa) {
    case INICIA:
      final JSONArray todos = consultaTodos(ctrlPasatiempos.helper);
      etapa = Etapa.SINCRONIZANDO;
      sincronizaTodos(todos);
      break;
    case SINCRONIZANDO:
      break;
    case MOSTRANDO:
      if (resultado != null) {
        muestra(resultado);
      } else if (error != null) {
        muestra(error);
      }
    }

  }

  private static JSONArray consultaTodos(BdSincroHelper helper) {
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

  public static void sincronizaTodos(final JSONArray todos) {
    resultado = null;
    error = null;
    executor.execute(new Runnable() {
      @Override
      public void run() {
        HttpURLConnection c = null;
        try {
          c = (HttpURLConnection) new URL(URL_SINCRO).openConnection();
          c.setUseCaches(false);
          c.setDoOutput(true);
          final OutputStream os = c.getOutputStream();
          try {
            os.write(todos.toString().getBytes("UTF-8"));
          } finally {
            os.close();
          }
          final int status = c.getResponseCode();
          if (status >= 200 && status <= 299) {
            final String texto = leeString(c);
            Log.d(tag, "Recibido: " + texto);
            muestra(new JSONObject(texto));
          } else {
            throw new Exception(c.getResponseMessage());
          }
        } catch (Exception e) {
          muestra(e);
        } finally {
          if (c != null) {
            c.disconnect();
          }
        }

      }
    });
  }

  public static synchronized void muestra(final JSONObject resultado) {
    CtrlSincro.resultado = resultado;
    etapa = Etapa.MOSTRANDO;
    if (ctrlPasatiempos != null) {
      ctrlPasatiempos.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          ctrlPasatiempos.muestra(resultado);
        }
      });
      etapa = Etapa.INICIA;
    }
  }

  public static synchronized void muestra(final Exception error) {
    CtrlSincro.error = error;
    etapa = Etapa.MOSTRANDO;
    if (ctrlPasatiempos != null) {
      ctrlPasatiempos.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          muestraError(ctrlPasatiempos, tag, "Error sincronizando.", error);
        }
      });
      etapa = Etapa.INICIA;
    }
  }
}