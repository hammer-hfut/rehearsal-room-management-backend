package io.github.hammerhfut.rehearsal.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.hammerhfut.rehearsal.model.db.LogNotice
import io.github.hammerhfut.rehearsal.model.db.ModuleName
import io.github.hammerhfut.rehearsal.model.db.OperationLog
import io.github.hammerhfut.rehearsal.model.db.by
import io.quarkus.narayana.jta.QuarkusTransaction
import jakarta.inject.Singleton
import org.babyfish.jimmer.kt.new
import org.babyfish.jimmer.sql.kt.KSqlClient
import java.time.LocalDateTime

@Singleton
class OperationLogService(
    private val sqlClient: KSqlClient,
    private val objectMapper: ObjectMapper
) {
    /**
     * 有任何增加、删除、修改的操作时调用本方法。
     *
     * - 当操作为增加时，[dataBefore] 为 null，[dataAfter] 为新增的数据。
     * - 当操作为删除时，[dataBefore] 为被删除的数据，[dataAfter] 为 null。
     * - 当操作为修改时，[dataBefore] 为修改前的数据，[dataAfter] 为修改后的数据。
     */
    fun <T> newOperationLog(
        operatorId: Long,
        targetModuleName: ModuleName,
        dataBefore: T?,
        dataAfter: T?,
        vararg noticeTargetIds: Long
    ) {
        QuarkusTransaction.requiringNew().run {
            val newLog = sqlClient.insert(new(OperationLog::class).by {
                this.targetModuleName = targetModuleName
                this.dataBefore = dataBefore?.let { objectMapper.writeValueAsString(it) }
                this.dataAfter = dataAfter?.let { objectMapper.writeValueAsString(it) }
                createTime = LocalDateTime.now()
                operator().apply {
                    id = operatorId
                }
            }).modifiedEntity
            noticeTargetIds.forEach {
                sqlClient.insert(new(LogNotice::class).by {
                    isRead = false
                    log().apply {
                        id = newLog.id
                    }
                    receiver().apply {
                        id = it
                    }
                })
            }
        }
    }
}