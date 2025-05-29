package br.uscs.android.cidapp2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.text.Normalizer
import java.util.Locale

class CidDatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "cids.db"
        private const val DATABASE_VERSION = 3 // Incrementado devido à adição de coluna

        private const val TABLE_CIDS = "cids"
        private const val COLUMN_ID = "id"
        private const val COLUMN_CODIGO = "codigo"
        private const val COLUMN_DESCRICAO = "descricao"
        private const val COLUMN_NOME_NORMALIZADO = "nome_normalizado"
        private const val COLUMN_FAVORITO = "favorito"
        private const val COLUMN_DEFICIENCIA = "deficiencia"
        private const val COLUMN_CAPITULO = "capitulo"
        private const val COLUMN_CAPITULO_DESCRICAO = "capitulo_descricao"
        private const val COLUMN_SINTOMAS = "sintomas"
        private const val COLUMN_TRATAMENTO = "tratamento"
        private const val COLUMN_GRAVIDADE = "gravidade"
        private const val COLUMN_LEGAL = "legal"
        private const val COLUMN_ESTATISTICAS = "estatisticas"
        private const val COLUMN_OUTRAS_INFO = "outras_info"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSql = """
            CREATE TABLE $TABLE_CIDS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CODIGO TEXT NOT NULL UNIQUE,
                $COLUMN_DESCRICAO TEXT NOT NULL,
                $COLUMN_NOME_NORMALIZADO TEXT,
                $COLUMN_FAVORITO INTEGER DEFAULT 0,
                $COLUMN_DEFICIENCIA INTEGER DEFAULT 0,
                $COLUMN_CAPITULO TEXT,
                $COLUMN_CAPITULO_DESCRICAO TEXT,
                $COLUMN_SINTOMAS TEXT,
                $COLUMN_TRATAMENTO TEXT,
                $COLUMN_GRAVIDADE TEXT,
                $COLUMN_LEGAL TEXT,
                $COLUMN_ESTATISTICAS TEXT,
                $COLUMN_OUTRAS_INFO TEXT
            )
        """.trimIndent()
        db.execSQL(createTableSql)
        Log.d("DB_SETUP", "Tabela $TABLE_CIDS criada com novas colunas incluindo nome_normalizado.")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.w("DB_SETUP", "Atualizando banco da v$oldVersion para v$newVersion; dados da tabela $TABLE_CIDS serão perdidos.")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CIDS")
        onCreate(db)
    }

    fun popularSeVazio() {
        if (isDatabaseEmpty()) {
            Log.i("DB_POPULATE", "Banco de dados vazio. Tentando popular a partir de res/raw/cids.csv...")
            writableDatabase.use { db ->
                popularBancoComCsv(db, this.context)
            }
        } else {
            Log.i("DB_POPULATE", "Banco de dados já contém dados. Nenhuma população necessária.")
        }
    }

    private fun isDatabaseEmpty(): Boolean {
        readableDatabase.use { db ->
            db.rawQuery("SELECT COUNT(*) FROM $TABLE_CIDS", null).use { cursor ->
                if (cursor.moveToFirst()) {
                    val count = cursor.getInt(0)
                    return count == 0
                }
            }
        }
        return true
    }

    private fun popularBancoComCsv(db: SQLiteDatabase, appContext: Context) {
        db.beginTransaction()
        var recordsAdded = 0
        var lineNumber = 1
        try {
            val inputStream = appContext.resources.openRawResource(R.raw.cids)
            BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
                reader.readLine() // ignora cabeçalho
                var line = reader.readLine()
                while (line != null) {
                    lineNumber++
                    val cols = line.split(';', limit = 8)
                    if (cols.size >= 8) {
                        val codigo = cols[0].trim()
                        val descricao = cols[1].trim()
                        val capitulo = cols[5].trim()
                        val capDesc = cols[6].trim()
                        val defFlagStr = cols[7].trim().removeSuffix(";")
                        val defFlagIntValue = if (defFlagStr.equals("1", true) || defFlagStr.equals("true", true)) 1 else 0
                        val nomeNormalizado = normalizarTexto("$codigo $descricao")

                        val values = ContentValues().apply {
                            put(COLUMN_CODIGO, codigo)
                            put(COLUMN_DESCRICAO, descricao)
                            put(COLUMN_NOME_NORMALIZADO, nomeNormalizado)
                            put(COLUMN_FAVORITO, 0)
                            put(COLUMN_DEFICIENCIA, defFlagIntValue)
                            put(COLUMN_CAPITULO, capitulo)
                            put(COLUMN_CAPITULO_DESCRICAO, capDesc)
                            put(COLUMN_SINTOMAS, "")
                            put(COLUMN_TRATAMENTO, "")
                            put(COLUMN_GRAVIDADE, "")
                            put(COLUMN_LEGAL, "")
                            put(COLUMN_ESTATISTICAS, "")
                            put(COLUMN_OUTRAS_INFO, "")
                        }

                        val rowId = db.insert(TABLE_CIDS, null, values)
                        if (rowId != -1L) recordsAdded++
                        else Log.e("DB_POPULATE_CSV", "Falha ao inserir linha $lineNumber. Código: $codigo")
                    }
                    line = reader.readLine()
                }
            }
            db.setTransactionSuccessful()
            Log.i("DB_POPULATE", "$recordsAdded registros adicionados.")
        } catch (e: Exception) {
            Log.e("DB_POPULATE_CSV", "Erro ao popular banco na linha $lineNumber: ${e.message}", e)
        } finally {
            db.endTransaction()
        }
    }

    fun getAllCids(): List<Cid> {
        val listaCids = mutableListOf<Cid>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CIDS,
            arrayOf(
                COLUMN_ID,
                COLUMN_CODIGO,
                COLUMN_DESCRICAO,
                COLUMN_FAVORITO,
                COLUMN_DEFICIENCIA,
                COLUMN_CAPITULO,
                COLUMN_CAPITULO_DESCRICAO,
                COLUMN_NOME_NORMALIZADO,
                COLUMN_SINTOMAS,
                COLUMN_TRATAMENTO,
                COLUMN_GRAVIDADE,
                COLUMN_LEGAL,
                COLUMN_ESTATISTICAS,
                COLUMN_OUTRAS_INFO
            ),
            null, null, null, null, null
        )

        cursor.use {
            while (it.moveToNext()) {
                val cid = Cid(
                    id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID)),
                    codigo = it.getString(it.getColumnIndexOrThrow(COLUMN_CODIGO)),
                    descricao = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRICAO)),
                    favorito = it.getInt(it.getColumnIndexOrThrow(COLUMN_FAVORITO)) != 0,
                    deficiencia = it.getInt(it.getColumnIndexOrThrow(COLUMN_DEFICIENCIA)) != 0,
                    capitulo = it.getString(it.getColumnIndexOrThrow(COLUMN_CAPITULO)),
                    capitulo_descricao = it.getString(it.getColumnIndexOrThrow(COLUMN_CAPITULO_DESCRICAO)),
                    nomeNormalizado = it.getString(it.getColumnIndexOrThrow(COLUMN_NOME_NORMALIZADO)),
                    sintomas = it.getString(it.getColumnIndexOrThrow(COLUMN_SINTOMAS)),
                    tratamento = it.getString(it.getColumnIndexOrThrow(COLUMN_TRATAMENTO)),
                    gravidade = it.getString(it.getColumnIndexOrThrow(COLUMN_GRAVIDADE)),
                    legal = it.getString(it.getColumnIndexOrThrow(COLUMN_LEGAL)),
                    estatisticas = it.getString(it.getColumnIndexOrThrow(COLUMN_ESTATISTICAS)),
                    outrasInfo = it.getString(it.getColumnIndexOrThrow(COLUMN_OUTRAS_INFO))
                )
                listaCids.add(cid)
            }
        }
        return listaCids
    }


    private fun normalizarTexto(texto: String): String {
        val textoNormalizado = Normalizer.normalize(texto, Normalizer.Form.NFD)
        return textoNormalizado.replace("\\p{Mn}+".toRegex(), "").lowercase(Locale.getDefault())
    }

}
