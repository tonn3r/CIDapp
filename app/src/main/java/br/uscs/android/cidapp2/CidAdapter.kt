package br.uscs.android.cidapp2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import java.util.Locale

enum class FilterState {
    ALL,
    FAVORITES,
    DISABILITIES
}

class CidAdapter(
    private var cidsCompletaOriginal: MutableList<Cid>,
    private val onUpdate: (Cid) -> Unit
) : RecyclerView.Adapter<CidAdapter.CidViewHolder>() {

    private var cidsFiltradosPorTexto: MutableList<Cid> = ArrayList(cidsCompletaOriginal)
    private var cidsExibida: MutableList<Cid> = ArrayList(cidsFiltradosPorTexto)

    private var filtroTextoAtual: String? = null
    private var filtroModoAtual: FilterState = FilterState.ALL // Estado inicial

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CidViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cid, parent, false)
        return CidViewHolder(view, cidsCompletaOriginal, onUpdate)
    }

    override fun onBindViewHolder(holder: CidViewHolder, position: Int) {
        holder.bind(cidsExibida[position])
    }

    override fun getItemCount(): Int = cidsExibida.size

    private fun aplicarFiltros() {
        // 1. Aplicar filtro de texto
        cidsFiltradosPorTexto.clear()
        val queryLower = filtroTextoAtual?.lowercase(Locale.getDefault())?.trim()
        if (queryLower.isNullOrEmpty()) {
            cidsFiltradosPorTexto.addAll(cidsCompletaOriginal)
        } else {
            cidsCompletaOriginal.forEach { cid ->
                if (cid.codigo.lowercase(Locale.getDefault()).contains(queryLower) ||
                    cid.descricao.lowercase(Locale.getDefault()).contains(queryLower)
                ) {
                    cidsFiltradosPorTexto.add(cid)
                }
            }
        }

        // 2. Aplicar filtro de modo (ALL, FAVORITES, DISABILITIES)
        cidsExibida.clear()
        when (filtroModoAtual) {
            FilterState.ALL -> {
                cidsExibida.addAll(cidsFiltradosPorTexto)
            }
            FilterState.FAVORITES -> {
                cidsFiltradosPorTexto.forEach { cid ->
                    if (cid.favorito) {
                        cidsExibida.add(cid)
                    }
                }
            }
            FilterState.DISABILITIES -> {
                cidsFiltradosPorTexto.forEach { cid ->
                    if (cid.deficiencia) { // Supondo que seu Cid.kt tem um campo 'deficiencia' (Boolean)
                        cidsExibida.add(cid)
                    }
                }
            }
        }
        notifyDataSetChanged()
    }

    fun filtrarPorTexto(query: String?) {
        filtroTextoAtual = query
        aplicarFiltros()
    }

    fun definirModoFiltro(modo: FilterState) {
        filtroModoAtual = modo
        aplicarFiltros()
    }

    fun atualizarListaOriginal(novaLista: List<Cid>) {
        cidsCompletaOriginal.clear()
        cidsCompletaOriginal.addAll(novaLista)
        aplicarFiltros()
    }

    // ViewHolder permanece o mesmo da sua versão anterior,
    // apenas certifique-se de que o Cid.kt tem o campo 'deficiencia'
    class CidViewHolder(
        itemView: View,
        private val listaOriginalCompleta: List<Cid>,
        private val onUpdateCallback: (Cid) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val txtCodigo: TextView = itemView.findViewById(R.id.txtCodigo)
        private val txtDescricao: TextView = itemView.findViewById(R.id.txtDescricao)
        private val checkFavoritoItem: MaterialCheckBox = itemView.findViewById(R.id.checkFavorito)
        private val checkDeficiencia: MaterialCheckBox = itemView.findViewById(R.id.checkDeficiencia)

        fun bind(cidAtualExibido: Cid) {
            txtCodigo.text = cidAtualExibido.codigo
            txtDescricao.text = cidAtualExibido.descricao

            checkFavoritoItem.setOnCheckedChangeListener(null)
            checkFavoritoItem.isChecked = cidAtualExibido.favorito
            checkFavoritoItem.setOnCheckedChangeListener { _, isChecked ->
                val cidNaListaOriginal = listaOriginalCompleta.find { it.id == cidAtualExibido.id }
                cidNaListaOriginal?.let {
                    it.favorito = isChecked
                    onUpdateCallback(it)
                }
            }

            checkDeficiencia.setOnCheckedChangeListener(null)
            checkDeficiencia.isChecked = cidAtualExibido.deficiencia
            checkDeficiencia.setOnCheckedChangeListener { _, isChecked ->
                val cidNaListaOriginal = listaOriginalCompleta.find { it.id == cidAtualExibido.id }
                cidNaListaOriginal?.let {
                    it.deficiencia = isChecked // Supondo que seu Cid.kt tem este campo
                    onUpdateCallback(it)
                }
            }
        }
    }
}