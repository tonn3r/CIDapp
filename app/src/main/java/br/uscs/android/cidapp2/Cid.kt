package br.uscs.android.cidapp2

data class Cid(
    val id: Int,
    val codigo: String,
    var descricao: String, // 'var' se a descrição pode mudar, 'val' caso contrário
    var favorito: Boolean,
    var deficiencia: Boolean,
    val capitulo: String,
    val capitulo_descricao: String,
    val nomeNormalizado: String,  // campo para busca, preenchido no banco
    // Campos para detalhes da IA, inicializados como string vazia ou nula
    var sintomas: String? = "",
    var tratamento: String? = "",
    var gravidade: String? = "",
    var legal: String? = "",
    var estatisticas: String? = "",
    var outrasInfo: String? = ""
)
