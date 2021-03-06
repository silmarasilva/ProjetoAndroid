package com.example.projetosistemas;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CadLembrete extends AppCompatActivity {

    private Toolbar toolbar;
    final private Calendar myCalendar = Calendar.getInstance();
    final private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", new Locale("pt", "BR"));
    private Usuario usuarioLogado;
    private Lembrete lembreteEdicao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cad_lembrete);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        this.usuarioLogado = (Usuario) intent.getSerializableExtra("usuarioLogado");
        if (intent.hasExtra("lembreteEdicao")) {
            this.lembreteEdicao = (Lembrete) intent.getSerializableExtra("lembreteEdicao");
            ((EditText) findViewById(R.id.txtTitulo)).setText(this.lembreteEdicao.getTitulo());
            ((EditText) findViewById(R.id.txtDescricao)).setText(this.lembreteEdicao.getDescricao());
            ((EditText) findViewById(R.id.txtDataConclusao)).setText(sdf.format(this.lembreteEdicao.getDataConclusao()));
        }
    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    public void onDateClick(View v) {
        new DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateLabel() {
        EditText edittext = findViewById(R.id.txtDataConclusao);
        edittext.setText(sdf.format(myCalendar.getTime()));
    }

    public void BotaoSalvar(View v) throws ParseException {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url;
        int method;

        if (this.lembreteEdicao == null) {
            url = "http://10.0.2.2:3000/api/tarefas";
            method = Request.Method.POST;
        } else {
            url = "http://10.0.2.2:3000/api/tarefas/" + this.lembreteEdicao.getId();
            method = Request.Method.PUT;
        }

        TextView titulo = findViewById(R.id.txtTitulo);
        TextView descricao = findViewById(R.id.txtDescricao);
        TextView dataConclusao = findViewById(R.id.txtDataConclusao);

        Lembrete lembrete = new Lembrete(null, titulo.getText().toString(), descricao.getText().toString(),
                new Date(), sdf.parse(dataConclusao.getText().toString()), this.usuarioLogado);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(method, url, lembrete.toJSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Intent jan = new Intent(CadLembrete.this, ListaLembrete.class);
                        jan.putExtra("usuarioLogado", usuarioLogado);
                        startActivity(jan);
                        finish();
                    }
                }, new Response.ErrorListener() {
            private AlertDialog dialog;

            @Override
            public void onErrorResponse(VolleyError error) {
                dialog = new AlertDialog.Builder(CadLembrete.this)
                        .setTitle("Erro")
                        .setMessage("Erro inesperado ao realizar o cadastro do lembrete")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        });

        queue.add(jsonRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}