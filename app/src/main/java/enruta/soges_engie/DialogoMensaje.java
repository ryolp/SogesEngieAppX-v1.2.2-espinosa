package enruta.soges_engie;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DialogoMensaje {
    private Resultado mResultado = null;
    private Dialog mDialogo = null;
    private Activity mActivity;
    private TextView mTxtMensaje;
    private TextView mTxtDetalle;
    private Button mBtnAceptar;
    private String mMensajeDetalle = "";

    public interface Resultado {
        public void Aceptar(boolean EsOk);
    }

    public DialogoMensaje(Activity activity) {
        mActivity = activity;
    }

    public void setOnResultado(Resultado resultado) {
        mResultado = resultado;
    }

    public void mostrarMensaje(String titulo, String mensajeUsuario, String mensajeDetalle) {
        if (mDialogo == null) {
            mDialogo = new Dialog(mActivity);

            LayoutInflater inflater = mActivity.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialogo_mensaje, null);

            mDialogo.setContentView(dialogView);

            mDialogo.setCancelable(false);

            mTxtMensaje = (TextView) mDialogo.findViewById(R.id.txtMensaje);
            mTxtDetalle = (TextView) mDialogo.findViewById(R.id.txtDetalle);
            mBtnAceptar = (Button) mDialogo.findViewById(R.id.btnAceptar);

            mTxtMensaje.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mMensajeDetalle.trim().equals(""))
                        mTxtDetalle.setVisibility(View.VISIBLE);
                }
            });


            mBtnAceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialogo.cancel();
                    if (mResultado!=null)
                        mResultado.Aceptar(true);
                }
            });
        }

        mMensajeDetalle = mensajeDetalle;
        mDialogo.setTitle(titulo);
        mTxtMensaje.setText(mensajeUsuario);
        mTxtDetalle.setText(mensajeDetalle);
        mTxtDetalle.setVisibility(View.GONE);
        mDialogo.show();
    }
}
