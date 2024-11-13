package com.bloodpressure.app.data.repository.bloodsugar

import com.bloodpressure.app.data.model.BloodSugarRecord
import com.bloodpressure.app.screen.bloodsugar.type.BloodSugarStateType
import com.bloodpressure.app.screen.bloodsugar.type.TargetRange
import com.bloodpressure.app.utils.BloodSugarUnit
import com.bloodpressure.app.utils.TextFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.java.KoinJavaComponent
import java.util.Calendar

class MockBloodSugarRepository: IBloodSugarRepository {
    private var bloodSugarRecord: List<BloodSugarRecord> = initBloodSugarRecords()
    private fun initBloodSugarRecords(): List<BloodSugarRecord> {
        val textFormatter: TextFormatter by KoinJavaComponent.inject(TextFormatter::class.java)
        val cal = Calendar.getInstance()
        val formattedTime = textFormatter.formatTime(cal.time)
        val formattedDate = textFormatter.formatDate(cal.timeInMillis)
        return (1..5).mapIndexed { index, i ->
            BloodSugarRecord(
                bloodSugar = i * 40f,
                time = formattedTime,
                date = formattedDate,
                bloodSugarStateType = BloodSugarStateType.values().random(),
                targetRanges = BloodSugarStateType.values().map {
                    val minValue = (40..80).random()
                    val maxValue = (80..120).random()
                    TargetRange(
                        bloodSugarStateType = it,
                        isChecked = index % 2 == 0,
                        normalRangeMin = minValue.toFloat(),
                        normalRangeMax = maxValue.toFloat(),
                        diabetesValue = (maxValue + 1).toFloat()
                    )
                },
                notes = setOf(),
                rowId = (1L..20L).random(),
                bloodSugarUnit = BloodSugarUnit.MILLIMOLES_PER_LITRE
            )
        }
    }

    override suspend fun insertRecord(record: BloodSugarRecord) {
        TODO("Not yet implemented")
    }

    override suspend fun updateRecord(record: BloodSugarRecord) {
        TODO("Not yet implemented")
    }

    override fun getRecordById(id: Long): Flow<BloodSugarRecord?> {
        TODO("Not yet implemented")
    }

    override fun getAllRecords(): Flow<List<BloodSugarRecord>> {
        return flow { bloodSugarRecord }
    }

    override suspend fun deleteRecord(record: BloodSugarRecord) {
        bloodSugarRecord = bloodSugarRecord.filter { it != record }
    }
}