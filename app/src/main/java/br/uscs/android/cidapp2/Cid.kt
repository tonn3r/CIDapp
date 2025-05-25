package br.uscs.android.cidapp2

data class Cid(
    val id: Int,
    val codigo: String,
    val descricao: String,
    var favorito: Boolean = false,
    var deficiencia: Boolean = false,
    val capitulo: String,
    val capitulo_descricao: String
)