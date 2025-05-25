package br.uscs.android.cidapp2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets // Import para Charsets.UTF_8

class CidDatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "cids.db"
        // IMPORTANTE: Se você alterou a estrutura da tabela (adicionou colunas)
        // ou precisa que a população do CSV seja refeita com a nova leitura UTF-8,
        // DESINSTALE o app ou INCREMENTE esta versão.
        private const val DATABASE_VERSION = 1 // Mude para 2 se necessário

        private const val TABLE_CIDS = "cids"
        private const val COLUMN_ID = "id"
        private const val COLUMN_CODIGO = "codigo"
        private const val COLUMN_DESCRICAO = "descricao"
        private const val COLUMN_FAVORITO = "favorito"
        private const val COLUMN_DEFICIENCIA = "deficiencia"
        private const val COLUMN_CAPITULO = "capitulo" // Coluna que estava faltando antes
        private const val COLUMN_CAPITULO_DESCRICAO = "capitulo_descricao" // Coluna que estava faltando antes
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSql = """
            CREATE TABLE $TABLE_CIDS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CODIGO TEXT NOT NULL,
                $COLUMN_DESCRICAO TEXT NOT NULL,
                $COLUMN_FAVORITO INTEGER DEFAULT 0,
                $COLUMN_DEFICIENCIA INTEGER DEFAULT 0,
                $COLUMN_CAPITULO TEXT,
                $COLUMN_CAPITULO_DESCRICAO TEXT
            )
        """.trimIndent()
        db.execSQL(createTableSql)
        Log.d("DB_SETUP", "Tabela $TABLE_CIDS criada.")
        // A população agora é chamada explicitamente pela MainActivity através de popularSeVazio
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.w("DB_SETUP", "Atualizando banco da v$oldVersion para v$newVersion; dados da tabela $TABLE_CIDS serão perdidos.")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CIDS")
        onCreate(db)
    }

    fun popularSeVazio() {
        if (isDatabaseEmpty()) {
            Log.d("DB_POPULATE", "Banco vazio — populando a partir de res/raw/cids.csv...")
            val db = writableDatabase
            popularBancoComCsv(db, this.context)
        } else {
            Log.d("DB_POPULATE", "Banco de dados já contém dados. Nenhuma população necessária.")
        }
    }

    private fun isDatabaseEmpty(): Boolean {
        readableDatabase.rawQuery("SELECT COUNT(*) FROM $TABLE_CIDS", null).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) == 0
            }
        }
        return true // Se o cursor não puder se mover, considere vazio ou erro.
    }

    private fun popularBancoComCsv(db: SQLiteDatabase, appContext: Context) {
        db.beginTransaction()
        var recordsAdded = 0
        try {
            val inputStream = appContext.resources.openRawResource(R.raw.cids)
            // Especificar Charsets.UTF_8 para garantir a leitura correta de caracteres especiais
            BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
                var line = reader.readLine() // Opcional: Ler e descartar cabeçalho
                // Se seu CSV não tem cabeçalho, comente a linha acima
                // ou ajuste a lógica se o cabeçalho for a primeira linha de dados.

                line = reader.readLine() // Começa a ler as linhas de dados
                while (line != null) {
                    val cols = line.split(';', limit = 8) // Ajuste 'limit' conforme o número máximo de campos esperados
                    if (cols.size >= 8) { // Verifique se tem colunas suficientes para todos os campos
                        val codigo = cols[0].trim()
                        val descricao = cols[1].trim()
                        // Assumindo que as colunas 2, 3, 4 são ignoradas
                        val capitulo = cols[5].trim()
                        val capDesc = cols[6].trim()
                        val defFlagStr = cols[7].trim()

                        val defFlag = defFlagStr.equals("true", ignoreCase = true) || defFlagStr == "1"

                        val values = ContentValues().apply {
                            put(COLUMN_CODIGO, codigo)
                            put(COLUMN_DESCRICAO, descricao)
                            put(COLUMN_FAVORITO, 0) // Padrão
                            put(COLUMN_DEFICIENCIA, if (defFlag) 1 else 0)
                            put(COLUMN_CAPITULO, capitulo)
                            put(COLUMN_CAPITULO_DESCRICAO, capDesc)
                        }
                        db.insert(TABLE_CIDS, null, values)
                        recordsAdded++
                    } else {
                        Log.w("DB_POPULATE_CSV", "Linha ignorada no CSV por ter ${cols.size} colunas (esperado >= 8): \"$line\"")
                    }
                    line = reader.readLine()
                }
            }
            db.setTransactionSuccessful()
            Log.d("DB_POPULATE", "População do CSV concluída. $recordsAdded registros adicionados.")
        } catch (e: Exception) {
            Log.e("DB_POPULATE", "Erro ao popular banco a partir do CSV: ${e.message}", e)
        } finally {
            db.endTransaction()
        }
    }

    fun getAllCids(): List<Cid> {
        val list = mutableListOf<Cid>()
        // Selecionar todas as colunas explicitamente é uma boa prática
        val selectQuery = "SELECT $COLUMN_ID, $COLUMN_CODIGO, $COLUMN_DESCRICAO, $COLUMN_FAVORITO, $COLUMN_DEFICIENCIA, $COLUMN_CAPITULO, $COLUMN_CAPITULO_DESCRICAO FROM $TABLE_CIDS"
        readableDatabase.rawQuery(selectQuery, null).use { cursor -> // 'use' garante que o cursor seja fechado
            val idCol = cursor.getColumnIndexOrThrow(COLUMN_ID)
            val codigoCol = cursor.getColumnIndexOrThrow(COLUMN_CODIGO)
            val descricaoCol = cursor.getColumnIndexOrThrow(COLUMN_DESCRICAO)
            val favoritoCol = cursor.getColumnIndexOrThrow(COLUMN_FAVORITO)
            val deficienciaCol = cursor.getColumnIndexOrThrow(COLUMN_DEFICIENCIA)
            val capituloCol = cursor.getColumnIndexOrThrow(COLUMN_CAPITULO)
            val capituloDescCol = cursor.getColumnIndexOrThrow(COLUMN_CAPITULO_DESCRICAO)

            if (cursor.moveToFirst()) {
                do {
                    list.add(Cid(
                        id = cursor.getInt(idCol),
                        codigo = cursor.getString(codigoCol),
                        descricao = cursor.getString(descricaoCol),
                        favorito = cursor.getInt(favoritoCol) == 1,
                        deficiencia = cursor.getInt(deficienciaCol) == 1,
                        // Usar ?: "" para fornecer um valor padrão caso a coluna seja NULL no banco
                        capitulo = cursor.getString(capituloCol) ?: "",
                        capitulo_descricao = cursor.getString(capituloDescCol) ?: ""
                    ))
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    fun updateCid(cid: Cid): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FAVORITO, if (cid.favorito) 1 else 0)
            put(COLUMN_DEFICIENCIA, if (cid.deficiencia) 1 else 0)
            // Não estamos atualizando outros campos como codigo, descricao, capitulo aqui
            // pois eles geralmente vêm do CSV e não são editáveis pelo usuário dessa forma.
        }
        val rowsAffected = db.update(
            TABLE_CIDS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(cid.id.toString())
        )
        return rowsAffected > 0
    }
}