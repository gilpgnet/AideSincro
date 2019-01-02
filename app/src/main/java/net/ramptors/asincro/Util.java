package net.ramptors.asincro;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.widget.Toast;
import android.util.Log;
import android.view.View;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import static android.widget.Toast.LENGTH_LONG;

import java.io.BufferedReader;

class Util {
  static final String EXTRA_ID = "net.ramptors.asincro.ID";

  static String texto(final String s) {
    return s == null ? "" : s;
  }

  static boolean isNullOrEmpty(final String s) {
    return s == null || s.isEmpty();
  }

  static String getMensaje(final Exception e) {
    final String localizedMessage = e.getLocalizedMessage();
    return isNullOrEmpty(localizedMessage) ? e.toString() : localizedMessage;
  }

  static void muestraError(Context context, String tag, String error, Exception e) {
    if (e != null) {
      Log.e(tag, error, e);
      muestraMensaje(context, getMensaje(e));
    }
  }

  static void muestraMensaje(Context context, String mensaje) {
    if (context != null) {
      Toast.makeText(context, mensaje, LENGTH_LONG).show();
    }
  }

  public static void valida(Context context, boolean condicion, @StringRes int mensaje) {
    if (!condicion) {
      throw new IllegalArgumentException(context.getString(mensaje));
    }
  }

  public static String leeString(HttpURLConnection c) throws IOException {
    final BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
    try {
      final char[] buffer = new char[1024];
      final StringBuilder out = new StringBuilder();
      int size = in.read(buffer, 0, buffer.length);
      while (size != -1) {
        out.append(buffer, 0, size);
        size = in.read(buffer, 0, buffer.length);
      }
      return out.toString();
    } finally {
      in.close();
    }
  }

  private Util() {
  }
}