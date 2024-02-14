package io.github.hammerhfut.rehearsal.model

import io.quarkus.runtime.annotations.RegisterForReflection
import org.babyfish.jimmer.Page
import org.jboss.resteasy.reactive.RestQuery

@RegisterForReflection(
    targets = [
        // 为 Jimmer Page 注册反射
        Page::class
    ]
)
data class Paging(
    @field:RestQuery
    var pageIndex: Int = 0,
    @field:RestQuery
    var pageSize: Int = 0
)
