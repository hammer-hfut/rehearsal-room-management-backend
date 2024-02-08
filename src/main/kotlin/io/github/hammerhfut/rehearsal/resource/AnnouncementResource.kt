package io.github.hammerhfut.rehearsal.resource

import io.github.hammerhfut.rehearsal.exception.BusinessError
import io.github.hammerhfut.rehearsal.exception.ErrorCode
import io.github.hammerhfut.rehearsal.model.Paging
import io.github.hammerhfut.rehearsal.model.db.Announcement
import io.github.hammerhfut.rehearsal.model.db.copy
import io.github.hammerhfut.rehearsal.model.db.dto.AnnouncementSaveDto
import io.github.hammerhfut.rehearsal.model.db.fetchBy
import jakarta.ws.rs.*
import org.babyfish.jimmer.Page
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.jboss.resteasy.reactive.RestPath
import java.time.LocalDateTime

@Path("/announcement")
class AnnouncementResource(
    private val sqlClient: KSqlClient
) {
    @GET
    fun pageAnnouncements(@BeanParam paging: Paging): Page<Announcement> {
        return sqlClient.createQuery(Announcement::class) {
            select(table.fetchBy {
                allScalarFields()
                author {
                    username()
                    realname()
                }
            })
        }.fetchPage(paging.pageIndex, paging.pageSize)
    }

    @PUT
    fun saveAnnouncement(input: AnnouncementSaveDto) {
        sqlClient.save(input.toEntity().copy {
            createTime = LocalDateTime.now()
            author().apply {
                id = 1
            }
        })
    }

    @Path("/{id}")
    @DELETE
    fun deleteAnnouncement(@RestPath id: Long) {
        if (sqlClient.deleteById(Announcement::class, id).totalAffectedRowCount != 1) {
            throw BusinessError(ErrorCode.NOT_FOUND, "未找到 id 为 $id 的公告")
        }
        // 插入日志...
    }
}