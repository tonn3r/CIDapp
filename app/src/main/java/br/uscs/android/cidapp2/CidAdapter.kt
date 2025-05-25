package br.uscs.android.cidapp2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
// import android.widget.ImageView // Não é mais necessário para checkDeficiencia se ele é um MaterialCheckBox
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox

// Supondo que sua classe Cid seja algo como:
// data class Cid(
//     val id: String, // ou Int, dependendo do seu modelo
//     val codigo: String,
//     val descricao: String,
//     var favorito: Boolean,
//     var deficiencia: Boolean // Agora controla o estado 'checked' do MaterialCheckBox
// )

class CidAdapter(
    private val cids: MutableList<Cid>,
    private val onUpdate: (Cid) -> Unit
) : RecyclerView.Adapter<CidAdapter.CidViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CidViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cid, parent, false) // Certifique-se que R.layout.item_cid é o nome correto
        return CidViewHolder(view) // Linha 28
    }

    override fun onBindViewHolder(holder: CidViewHolder, position: Int) {
        val cid = cids[position]
        holder.bind(cid, onUpdate)
    }

    override fun getItemCount(): Int = cids.size

    class CidViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtCodigo: TextView = itemView.findViewById(R.id.txtCodigo)
        private val txtDescricao: TextView = itemView.findViewById(R.id.txtDescricao)
        private val checkFavorito: MaterialCheckBox = itemView.findViewById(R.id.checkFavorito)

        // ESTA É A LINHA QUE PRECISA SER CORRIGIDA (Linha 44 no seu log de erro)
        // Antes: private val checkDeficiencia: ImageView = itemView.findViewById(R.id.checkDeficiencia)
        // CORREÇÃO:
        private val checkDeficiencia: MaterialCheckBox = itemView.findViewById(R.id.checkDeficiencia)

        fun bind(cid: Cid, onUpdate: (Cid) -> Unit) {
            txtCodigo.text = cid.codigo
            txtDescricao.text = cid.descricao

            // Configuração para o MaterialCheckBox 'checkFavorito'
            checkFavorito.setOnCheckedChangeListener(null)
            checkFavorito.isChecked = cid.favorito
            checkFavorito.setOnCheckedChangeListener { _, isChecked ->
                cid.favorito = isChecked
                onUpdate(cid)
            }

            // Configuração para o MaterialCheckBox 'checkDeficiencia'
            checkDeficiencia.setOnCheckedChangeListener(null)
            checkDeficiencia.isChecked = cid.deficiencia // Usa o boolean 'deficiencia' para marcar/desmarcar
            checkDeficiencia.setOnCheckedChangeListener { _, isChecked ->
                cid.deficiencia = isChecked
                onUpdate(cid)
            }
        }
    }
}