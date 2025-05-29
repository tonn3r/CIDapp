package br.uscs.android.cidapp2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox

class CidAdapter(
    novaListaOriginal: List<Cid>,
    private val onUpdate: (Cid) -> Unit,
    private val onItemClicked: (Cid) -> Unit
) : ListAdapter<CidAdapter.NormalizedCid, CidAdapter.CidViewHolder>(CidDiffCallback()) {

    private var filtroTextoAtual: String? = null
    private var filtroModoAtual: FilterState = FilterState.ALL

    // Usa diretamente o campo nomeNormalizado de Cid, não normaliza aqui
    private var listaOriginalNormalizada: List<NormalizedCid> = novaListaOriginal.map {
        NormalizedCid(it, it.nomeNormalizado)
    }

    data class NormalizedCid(val cid: Cid, val textoNormalizado: String)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CidViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cid, parent, false)
        return CidViewHolder(view, onUpdate, onItemClicked)
    }

    override fun onBindViewHolder(holder: CidViewHolder, position: Int) {
        holder.bind(getItem(position).cid)
    }

    // O texto recebido aqui já deve estar normalizado externamente antes da busca
    fun filtrarPorTexto(query: String?) {
        filtroTextoAtual = query
        aplicarFiltros()
    }

    fun definirModoFiltro(modo: FilterState, textoDeBuscaAtual: String?) {
        filtroModoAtual = modo
        filtroTextoAtual = textoDeBuscaAtual
        aplicarFiltros()
    }

    fun atualizarListaOriginal(novaLista: List<Cid>) {
        listaOriginalNormalizada = novaLista.map {
            NormalizedCid(it, it.nomeNormalizado)
        }
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val palavrasBusca = filtroTextoAtual
            ?.lowercase()
            ?.split("\\s+".toRegex())
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        val filtradosPorTexto = if (palavrasBusca.isEmpty()) {
            listaOriginalNormalizada
        } else {
            listaOriginalNormalizada.filter { normalizedCid ->
                palavrasBusca.all { normalizedCid.textoNormalizado.contains(it) }
            }
        }

        val filtradosPorModo = when (filtroModoAtual) {
            FilterState.ALL -> filtradosPorTexto
            FilterState.FAVORITES -> filtradosPorTexto.filter { it.cid.favorito }
            FilterState.DISABILITIES -> filtradosPorTexto.filter { it.cid.deficiencia }
        }

        submitList(filtradosPorModo)
    }

    fun getCidsExibida(): List<Cid> {
        return currentList.map { it.cid }
    }

    class CidViewHolder(
        itemView: View,
        private val onUpdateCallback: (Cid) -> Unit,
        private val onItemClickCallback: (Cid) -> Unit
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
                if (cidAtualExibido.favorito != isChecked) {
                    cidAtualExibido.favorito = isChecked
                    onUpdateCallback(cidAtualExibido)
                }
            }

            checkDeficiencia.setOnCheckedChangeListener(null)
            checkDeficiencia.isChecked = cidAtualExibido.deficiencia
            checkDeficiencia.setOnCheckedChangeListener { _, isChecked ->
                if (cidAtualExibido.deficiencia != isChecked) {
                    cidAtualExibido.deficiencia = isChecked
                    onUpdateCallback(cidAtualExibido)
                }
            }

            itemView.setOnClickListener {
                onItemClickCallback(cidAtualExibido)
            }
        }
    }

    class CidDiffCallback : DiffUtil.ItemCallback<NormalizedCid>() {
        override fun areItemsTheSame(oldItem: NormalizedCid, newItem: NormalizedCid): Boolean {
            return oldItem.cid.id == newItem.cid.id
        }

        override fun areContentsTheSame(oldItem: NormalizedCid, newItem: NormalizedCid): Boolean {
            return oldItem.cid == newItem.cid
        }
    }
}
