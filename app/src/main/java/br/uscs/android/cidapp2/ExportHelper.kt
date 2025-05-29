package br.uscs.android.cidapp2

import android.content.Context
import android.util.Log
import android.widget.Toast
// Para uma implementação real de CSV, você pode precisar de:
// import java.io.File
// import java.io.FileWriter
// import android.os.Environment
// import androidx.core.content.FileProvider // Para compartilhar o arquivo

object ExportHelper {

    private const val TAG = "ExportHelper"

    fun exportarParaCSV(context: Context, cids: List<Cid>) {
        if (cids.isEmpty()) {
            Toast.makeText(context, "Nenhum dado para exportar.", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "Nenhum dado para exportar.")
            return
        }

        // --- Início da Implementação Simplificada (Apenas Log) ---
        Log.i(TAG, "Iniciando exportação simulada para CSV...")
        val stringBuilder = StringBuilder()
        // Cabeçalho do CSV
        stringBuilder.append("ID;Codigo;Descricao;Favorito;Deficiencia;Capitulo;DescricaoCapitulo;Sintomas;Tratamento;Gravidade;Legal;Estatisticas;OutrasInfo\n")

        cids.forEach { cid ->
            stringBuilder.append("${cid.id};")
            stringBuilder.append("\"${cid.codigo.replace("\"", "\"\"")}\";")
            stringBuilder.append("\"${cid.descricao.replace("\"", "\"\"")}\";")
            stringBuilder.append("${if (cid.favorito) 1 else 0};")
            stringBuilder.append("${if (cid.deficiencia) 1 else 0};")
            stringBuilder.append("\"${cid.capitulo.replace("\"", "\"\"")}\";")
            stringBuilder.append("\"${cid.capitulo_descricao.replace("\"", "\"\"")}\";")
            stringBuilder.append("\"${(cid.sintomas ?: "").replace("\"", "\"\"")}\";")
            stringBuilder.append("\"${(cid.tratamento ?: "").replace("\"", "\"\"")}\";")
            stringBuilder.append("\"${(cid.gravidade ?: "").replace("\"", "\"\"")}\";")
            stringBuilder.append("\"${(cid.legal ?: "").replace("\"", "\"\"")}\";")
            stringBuilder.append("\"${(cid.estatisticas ?: "").replace("\"", "\"\"")}\";")
            stringBuilder.append("\"${(cid.outrasInfo ?: "").replace("\"", "\"\"")}\"\n")
        }

        Log.d(TAG, "Dados que seriam exportados para CSV:\n${stringBuilder.toString()}")
        Toast.makeText(context, "Exportação para CSV simulada (ver Logcat).", Toast.LENGTH_LONG).show()
        // --- Fim da Implementação Simplificada ---

        /*
        // --- Início da Implementação Real (Requer Permissões e/ou Storage Access Framework) ---
        // Exemplo básico usando armazenamento externo (LEGADO - pode não funcionar bem em Android 10+)
        // Para Android 10+ é fortemente recomendado usar o Storage Access Framework (SAF)
        // ou MediaStore API.

        val fileName = "cids_exportados.csv"
        try {
            // Para armazenamento público (requer permissão WRITE_EXTERNAL_STORAGE em versões antigas
            // e pode não ser acessível diretamente em versões mais novas sem SAF).
            // val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            // val file = File(path, fileName)

            // Para armazenamento interno do aplicativo (mais simples, mas o arquivo só é acessível pelo app)
            val file = File(context.getExternalFilesDir(null), fileName) // Ou context.filesDir

            FileWriter(file).use { writer ->
                // Cabeçalho
                writer.append("ID;Codigo;Descricao;Favorito;Deficiencia;Capitulo;DescricaoCapitulo;Sintomas;Tratamento;Gravidade;Legal;Estatisticas;OutrasInfo\n")
                cids.forEach { cid ->
                    writer.append("${cid.id};")
                    writer.append("\"${cid.codigo.replace("\"", "\"\"")}\";")
                    writer.append("\"${cid.descricao.replace("\"", "\"\"")}\";")
                    writer.append("${if (cid.favorito) 1 else 0};")
                    writer.append("${if (cid.deficiencia) 1 else 0};")
                    writer.append("\"${cid.capitulo.replace("\"", "\"\"")}\";")
                    writer.append("\"${cid.capitulo_descricao.replace("\"", "\"\"")}\";")
                    writer.append("\"${(cid.sintomas ?: "").replace("\"", "\"\"")}\";")
                    writer.append("\"${(cid.tratamento ?: "").replace("\"", "\"\"")}\";")
                    writer.append("\"${(cid.gravidade ?: "").replace("\"", "\"\"")}\";")
                    writer.append("\"${(cid.legal ?: "").replace("\"", "\"\"")}\";")
                    writer.append("\"${(cid.estatisticas ?: "").replace("\"", "\"\"")}\";")
                    writer.append("\"${(cid.outrasInfo ?: "").replace("\"", "\"\"")}\"\n")
                }
                writer.flush()
            }
            Log.i(TAG, "Dados exportados para: ${file.absolutePath}")
            Toast.makeText(context, "Dados exportados para ${file.name}", Toast.LENGTH_LONG).show()

            // Para tornar o arquivo acessível por outros apps, você pode usar FileProvider
            // e uma Intent ACTION_VIEW ou ACTION_SEND. Exemplo:
            // val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            // val intent = Intent(Intent.ACTION_VIEW).apply {
            //    setDataAndType(uri, "text/csv")
            //    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // }
            // context.startActivity(Intent.createChooser(intent, "Abrir CSV com"))

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao exportar para CSV", e)
            Toast.makeText(context, "Erro ao exportar: ${e.message}", Toast.LENGTH_LONG).show()
        }
        // --- Fim da Implementação Real ---
        */
    }
}