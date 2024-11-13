package com.bloodpressure.app.data.local

import android.content.Context
import android.net.Uri
import com.bloodpressure.app.data.repository.BMIRepository
import com.bloodpressure.app.data.repository.BpRepository
import com.bloodpressure.app.data.repository.HeartRateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.OutputStream

class DatabaseExporter(
    private val context: Context,
    private val repository: BpRepository,
    private val heartRateRepository: HeartRateRepository,
    private val bmiRepository: BMIRepository
) {
    suspend fun exportRecordsToCsv(fileUri: Uri): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val csvRecords = repository.getAllRecords().firstOrNull()?.map { record ->
                    RecordCsv(
                        systolic = record.systolic,
                        diastolic = record.diastolic,
                        pulse = record.pulse,
                        time = record.time,
                        date = record.date,
                        typeName = record.typeName,
                        note = "#${record.notes.joinToString(separator = NoteConverters.JOIN_CHAR)}",
                        createdAt = record.createdAt
                    )
                }
                val fileOutputStream = context.contentResolver.openOutputStream(fileUri, "w")
                fileOutputStream?.writeCsv(csvRecords!!)
                fileOutputStream?.close()
                fileUri
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun exportHeartRAteRecordsToCsv(fileUri: Uri): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val csvHeartRateRecords = heartRateRepository.getAllRecords().firstOrNull()?.map { record ->
                    HeartRateRecordCsv(
                        heartRate = record.heartRate,
                        time = record.time,
                        date = record.date,
                        typeName = record.typeName,
                        note = "#${record.notes.joinToString(separator = NoteConverters.JOIN_CHAR)}",
                        genderType = context.getString(record.genderType.nameRes),
                        createdAt = record.createdAt
                    )
                }
                val fileOutputStream = context.contentResolver.openOutputStream(fileUri, "w")
                fileOutputStream?.writeHeartRateCsv(csvHeartRateRecords!!)
                fileOutputStream?.close()
                fileUri
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun exportBmiRecordsToCsv(fileUri: Uri): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val bmiRecordCsvs = bmiRepository.getAllRecords().firstOrNull()?.map { record ->
                    BMIRecordCsv(
                        createdAt = record.createdAt,
                        date = record.date,
                        time = record.time,
                        weight = record.weight,
                        height = record.height,
                        bmi = record.bmi,
                        typeName = record.typeName,
                        note = "#${record.notes.joinToString(separator = NoteConverters.JOIN_CHAR)}",
                    )
                }
                val fileOutputStream = context.contentResolver.openOutputStream(fileUri, "w")
                fileOutputStream?.writeBMICsv(bmiRecordCsvs!!)
                fileOutputStream?.close()
                fileUri
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun OutputStream.writeCsv(recordCsvs: List<RecordCsv>) {
        val writer = bufferedWriter()
        writer.write(""""CREATED_AT","DATE","TIME","DIASTOLIC","SYSTOLIC","PULSE","NOTE","TYPE"""")
        writer.newLine()
        recordCsvs.forEach {
            writer.write(""""${it.createdAt}","${it.date}","${it.time}","${it.diastolic}","${it.systolic}","${it.pulse}","${it.note}","${it.typeName}"""")
            writer.newLine()
        }
        writer.flush()
    }

    private fun OutputStream.writeHeartRateCsv(heartRateRecordCsvs: List<HeartRateRecordCsv>) {
        val writer = bufferedWriter()
        writer.write(""""CREATED_AT","DATE","TIME","HEART_RATE","NOTE","TYPE","GENDER"""")
        writer.newLine()
        heartRateRecordCsvs.forEach {

            writer.write(""""${it.createdAt}","${it.date}","${it.time}","${it.heartRate}","${it.note}","${it.typeName}","${it.genderType}"""")
            writer.newLine()
        }
        writer.flush()
    }

    private fun OutputStream.writeBMICsv(bmiRecordCsvs: List<BMIRecordCsv>) {
        val writer = bufferedWriter()
        writer.write(""""CREATED_AT","DATE","TIME","WEIGHT","HEIGHT","BMI","NOTE","TYPE"""")
        writer.newLine()
        bmiRecordCsvs.forEach {
            writer.write(""""${it.createdAt}","${it.date}","${it.time}","${it.weight}","${it.height}","${it.bmi}","${it.note}","${it.typeName}"""")
            writer.newLine()
        }
        writer.flush()
    }
}
