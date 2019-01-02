package net.ramptors.asincro;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import static net.ramptors.asincro.Util.getMensaje;
import static net.ramptors.asincro.Util.leeString;

public class SincroTaskLoader extends AsyncTaskLoader<JSONObject> {
  private final String URL_SINCRO = "https://sincro.000webhostapp.com/servicio/sincro.php";
  private final String tag = getClass().getName();
  private final JSONArray todos;

  public SincroTaskLoader(Context context, JSONArray todos) {
    super(context);
    this.todos = todos;
  }

  @Override
  protected void onStartLoading() {
    forceLoad();
  }

  @Override
  public JSONObject loadInBackground() {
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
        return new JSONObject(texto);
      } else {
        throw new Exception(c.getResponseMessage());
      }
    } catch (Exception e) {
      try {
        Log.e(tag, "Error en la conexión.", e);
        JSONObject respuesta = new JSONObject();
        respuesta.put("error", getMensaje(e));
        return respuesta;
      } catch (JSONException e2) {
        Log.e(tag, "Error procesando error de JSON.", e);
        return null;
      }
    } finally {
      if (c != null) {
        c.disconnect();
      }
    }
  }
}