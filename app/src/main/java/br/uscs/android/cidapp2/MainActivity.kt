package br.uscs.android.cidapp2

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.uscs.android.cidapp2.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var cidAdapter: CidAdapter
    private lateinit var dbHelper: CidDatabaseHelper
    private var allCids: MutableList<Cid> = mutableListOf()
    private var currentFilterState: FilterState = FilterState.ALL
    private var currentSearchQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura Toolbar
        setSupportActionBar(binding.toolbar)

        // Configura DrawerLayout e NavigationView
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)

        // Inicializa banco de dados e popula se vazio
        dbHelper = CidDatabaseHelper(this)
        dbHelper.popularSeVazio()

        // Configura RecyclerView
        binding.recyclerCids.layoutManager = LinearLayoutManager(this)

        // Carrega dados do banco
        loadCidsFromDatabase()

        // Inicializa adapter
        cidAdapter = CidAdapter(
            allCids,
            onUpdate = { cid ->
                // Removida chamada inexistente a updateCid
                // Atualiza lista local e adapter sem acesso ao banco
                val index = allCids.indexOfFirst { it.id == cid.id }
                if (index != -1) {
                    allCids[index] = cid
                    cidAdapter.atualizarListaOriginal(ArrayList(allCids))
                }
            },
            onItemClicked = { cid ->
                val intent = Intent(this, CidDetailActivity::class.java)
                intent.putExtra(CidDetailActivity.EXTRA_CID_ID, cid.id)
                startActivity(intent)
            }
        )
        binding.recyclerCids.adapter = cidAdapter

        // Configura TextInputEditText para busca
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()
                cidAdapter.definirModoFiltro(currentFilterState, currentSearchQuery)
            }
            override fun afterTextChanged(s: Editable?) { }
        })

        // Botão de filtro alterna modo de filtro e atualiza ícone
        binding.buttonFilterMode.setOnClickListener {
            cicloModoFiltro()
            atualizarIconeBotaoFiltro()
            cidAdapter.definirModoFiltro(currentFilterState, currentSearchQuery)
        }

        // Aplica filtro inicial e ícone do botão
        cidAdapter.definirModoFiltro(currentFilterState, currentSearchQuery)
        atualizarIconeBotaoFiltro()
    }

    private fun cicloModoFiltro() {
        currentFilterState = when (currentFilterState) {
            FilterState.ALL -> FilterState.FAVORITES
            FilterState.FAVORITES -> FilterState.DISABILITIES
            FilterState.DISABILITIES -> FilterState.ALL
        }
    }

    private fun atualizarIconeBotaoFiltro() {
        val iconRes = when (currentFilterState) {
            FilterState.ALL -> R.drawable.ic_list
            FilterState.FAVORITES -> R.drawable.ic_favorite_all
            FilterState.DISABILITIES -> R.drawable.ic_disability_filled
        }
        binding.buttonFilterMode.setImageDrawable(ContextCompat.getDrawable(this, iconRes))
    }

    private fun loadCidsFromDatabase() {
        allCids.clear()
        allCids.addAll(dbHelper.getAllCids())  // carregando do banco

        if (::cidAdapter.isInitialized) {
            cidAdapter.atualizarListaOriginal(ArrayList(allCids))
        }
    }


    override fun onResume() {
        super.onResume()
        loadCidsFromDatabase()
        if (::cidAdapter.isInitialized) {
            cidAdapter.definirModoFiltro(currentFilterState, currentSearchQuery)
        }
    }

    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item_all -> aplicarFiltro(FilterState.ALL, "Exibindo Todos")
            R.id.nav_item_favorites -> aplicarFiltro(FilterState.FAVORITES, "Exibindo Favoritos")
            R.id.nav_item_disabilities -> aplicarFiltro(FilterState.DISABILITIES, "Exibindo Deficiências")
            R.id.nav_item_export -> ExportHelper.exportarParaCSV(this, cidAdapter.getCidsExibida())
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun aplicarFiltro(filtro: FilterState, mensagem: String) {
        currentFilterState = filtro
        cidAdapter.definirModoFiltro(filtro, currentSearchQuery)
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
