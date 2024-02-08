package io.github.hammerhfut.rehearsal.model

import org.jboss.resteasy.reactive.RestQuery

data class Paging(
    @field:RestQuery
    var pageIndex: Int = 0,
    @field:RestQuery
    var pageSize: Int = 0
)
