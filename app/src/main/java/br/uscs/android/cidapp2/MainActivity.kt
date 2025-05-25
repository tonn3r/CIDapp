package br.uscs.android.cidapp2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText

// Se FilterState.kt está em um arquivo separado, o import é automático se no mesmo pacote.
// Se você moveu para outro pacote, ajuste o import.
// import br.uscs.android.cidapp2.FilterState // Desnecessário se no mesmo pacote.

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: CidDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CidAdapter
    private lateinit var editTextSearch: TextInputEditText
    private lateinit var buttonFilterMode: ImageButton

    private var currentFilterState = FilterState.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextSearch = findViewById(R.id.editTextSearch)
        buttonFilterMode = findViewById(R.id.buttonFilterMode)
        recyclerView = findViewById(R.id.recyclerCids)

        dbHelper = CidDatabaseHelper(this)
        // Tentativa de popular o banco de dados se estiver vazio.
        // A lógica de verificar se está vazio está dentro de popularSeVazio().
        dbHelper.popularSeVazio()


        val cidListCompleta = dbHelper.getAllCids().toMutableList()
        if (cidListCompleta.isEmpty()) {
            Log.w("MainActivity", "A lista de CIDs está vazia após tentar popular e carregar do banco.")
            // Você pode querer mostrar uma mensagem para o usuário ou lidar com este caso.
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CidAdapter(cidListCompleta) { cidAtualizado ->
            val success = dbHelper.updateCid(cidAtualizado)
            if (success) {
                Log.d("MainActivity", "CID ID ${cidAtualizado.id} atualizado no banco.")
                // A lista original `cidListCompleta` contém o objeto que foi modificado,
                // então a refiltragem deve pegar o estado mais recente.
            } else {
                Log.w("MainActivity", "Falha ao atualizar CID ID ${cidAtualizado.id} no banco.")
            }
            // Reaplicar filtros para refletir a mudança (ex: favorito/deficiência) na UI.
            adapter.definirModoFiltro(currentFilterState)
        }
        recyclerView.adapter = adapter

        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filtrarPorTexto(s?.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        buttonFilterMode.setOnClickListener {
            cicloModoFiltro()
            atualizarIconeBotaoFiltro()
            adapter.definirModoFiltro(currentFilterState)
        }

        // Definir estado inicial do filtro no adapter e atualizar ícone
        adapter.definirModoFiltro(currentFilterState)
        atualizarIconeBotaoFiltro()
    }

    private fun cicloModoFiltro() {
        currentFilterState = when (currentFilterState) {
            FilterState.ALL -> FilterState.FAVORITES
            FilterState.FAVORITES -> FilterState.DISABILITIES
            FilterState.DISABILITIES -> FilterState.ALL
        }
        Log.d("MainActivity", "Modo de filtro alterado para: $currentFilterState")
    }

    private fun atualizarIconeBotaoFiltro() {
        val iconRes = when (currentFilterState) {
            FilterState.ALL -> R.drawable.ic_list // Substitua pelo seu ícone
            FilterState.FAVORITES -> R.drawable.ic_favorite_all // Substitua pelo seu ícone
            FilterState.DISABILITIES -> R.drawable.ic_disability_filled // Substitua pelo seu ícone
        }
        buttonFilterMode.setImageDrawable(ContextCompat.getDrawable(this, iconRes))
    }
}