package br.uscs.android.cidapp2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: CidDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CidAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Instancia o helper do banco
        dbHelper = CidDatabaseHelper(this)

        // 2. Popula a base, se estiver vazia
        dbHelper.popularSeVazio()

        // 3. Busca a lista de CIDs no banco
        val cidList = dbHelper.getAllCids().toMutableList() // precisa ser mutável

        // 4. Configura o RecyclerView
        recyclerView = findViewById(R.id.recyclerCids)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 5. Cria o adapter com a função de atualização
        adapter = CidAdapter(cidList) { cidAtualizado ->
            dbHelper.updateCid(cidAtualizado) // salva favorito/deficiência
        }

        // 6. Define o adapter no RecyclerView
        recyclerView.adapter = adapter
    }
}
