package br.uscs.android.cidapp2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class CidDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "cids.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_CIDS = "cids"
        private const val COLUMN_ID = "id"
        private const val COLUMN_CODIGO = "codigo"
        private const val COLUMN_DESCRICAO = "descricao"
        private const val COLUMN_FAVORITO = "favorito"
        private const val COLUMN_DEFICIENCIA = "deficiencia"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSql = """
            CREATE TABLE $TABLE_CIDS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CODIGO TEXT NOT NULL,
                $COLUMN_DESCRICAO TEXT NOT NULL,
                $COLUMN_FAVORITO INTEGER DEFAULT 0,
                $COLUMN_DEFICIENCIA INTEGER DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTableSql)
        Log.d("DB_SETUP", "Tabela $TABLE_CIDS criada.")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.w("DB_SETUP", "Atualizando banco de dados da versão $oldVersion para $newVersion. Todos os dados antigos serão perdidos.")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CIDS")
        onCreate(db)
    }

    fun getAllCids(): List<Cid> {
        val list = mutableListOf<Cid>()
        val selectQuery = "SELECT * FROM $TABLE_CIDS"
        readableDatabase.rawQuery(selectQuery, null).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    list.add(Cid(
                        id          = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        codigo      = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CODIGO)),
                        descricao   = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRICAO)),
                        favorito    = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAVORITO)) == 1,
                        deficiencia = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DEFICIENCIA)) == 1
                    ))
                } while (cursor.moveToNext())
            } else {
                Log.d("DB_QUERY", "Nenhum CID encontrado na tabela $TABLE_CIDS.")
            }
        }
        Log.d("DB_QUERY", "getAllCids retornou ${list.size} registros.")
        return list
    }

    fun updateCidFlags(id: Int, favorito: Boolean, deficiencia: Boolean): Boolean {
        val values = ContentValues().apply {
            put(COLUMN_FAVORITO,    if (favorito) 1 else 0)
            put(COLUMN_DEFICIENCIA, if (deficiencia) 1 else 0)
        }
        val rowsAffected = writableDatabase.update(
            TABLE_CIDS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
        return rowsAffected > 0
    }

    fun updateCid(cid: Cid): Boolean {
        val sucesso = updateCidFlags(cid.id, cid.favorito, cid.deficiencia)
        Log.d("DB_UPDATE", "ID: ${cid.id}, Favorito: ${cid.favorito}, Deficiência: ${cid.deficiencia}, Sucesso: $sucesso")
        return sucesso
    }

    fun popularSeVazio() {
        // 1. Verifica se a tabela já tem dados
        val countQuery = "SELECT COUNT(*) FROM $TABLE_CIDS"
        var count = 0
        readableDatabase.rawQuery(countQuery, null).use { cursor ->
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
            }
        }

        // 2. Se já existem dados, não faz nada
        if (count > 0) {
            Log.d("DB_POPULATE", "Banco de dados já populado com $count registros. Não fazendo nada.")
            return
        }

        // 3. Se a tabela está vazia, popula com os dados iniciais
        Log.d("DB_POPULATE", "Banco de dados vazio. Populando com dados iniciais...")
        val db = writableDatabase // Agora pega a instância para escrita
        val cids = listOf(
            // O ID 0 é passado, mas será ignorado pelo AUTOINCREMENT.
            // O SQLite gerará os IDs corretos.
            Cid(0, "A00", "Cólera"),
            Cid(0, "A01", "Febre tifóide e paratifóide"),
            Cid(0, "A02", "Outras infecções por Salmonella"),
            Cid(0, "B20", "Doença pelo vírus da imunodeficiência humana [HIV]"),
            Cid(0, "C34", "Neoplasia maligna dos brônquios e do pulmão"),
            Cid(0, "D50", "Anemia por deficiência de ferro"),
            Cid(0, "E10", "Diabetes mellitus insulino-dependente"),
            Cid(0, "E11", "Diabetes mellitus não-insulino-dependente"),
            Cid(0, "F32", "Episódios depressivos"),
            Cid(0, "G40", "Epilepsia"),
            Cid(0, "H52", "Transtornos da refração e da acomodação"),
            Cid(0, "I10", "Hipertensão essencial (primária)"),
            Cid(0, "J45", "Asma"),
            Cid(0, "K35", "Apendicite aguda"),
            Cid(0, "L20", "Dermatite atópica"),
            Cid(0, "M54", "Dorsalgia"),
            Cid(0, "N39", "Outros transtornos do trato urinário"),
            Cid(0, "O80", "Parto único espontâneo"),
            Cid(0, "P07", "Transtornos relacionados à curta gestação e baixo peso ao nascer"),
            Cid(0, "Q90", "Síndrome de Down"),
            Cid(0, "R50", "Febre de origem desconhecida"),
            Cid(0, "S06", "Traumatismo intracraniano"),
            Cid(0, "T78", "Reações adversas a alimentos, não classificadas em outra parte"),
            Cid(0, "Z00", "Exame geral e investigação de pessoas sem queixas ou diagnósticos relatados")
        )

        db.beginTransaction()
        try {
            for (cid in cids) {
                val values = ContentValues().apply {
                    put(COLUMN_CODIGO, cid.codigo)
                    put(COLUMN_DESCRICAO, cid.descricao)
                    // Os valores padrão para favorito e deficiencia (0)
                    // já são definidos na criação da tabela.
                    // Se preferir ser explícito:
                    // put(COLUMN_FAVORITO, 0)
                    // put(COLUMN_DEFICIENCIA, 0)
                }
                db.insert(TABLE_CIDS, null, values)
            }
            db.setTransactionSuccessful()
            Log.d("DB_POPULATE", "Banco de dados populado com ${cids.size} registros.")
        } catch (e: Exception) {
            Log.e("DB_POPULATE", "Erro ao popular o banco de dados", e)
        } finally {
            db.endTransaction()
        }
    }
}