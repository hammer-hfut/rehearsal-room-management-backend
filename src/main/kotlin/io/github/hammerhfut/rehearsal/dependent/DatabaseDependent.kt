package io.github.hammerhfut.rehearsal.dependent

import io.quarkus.runtime.configuration.ConfigUtils
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.Dependent
import jakarta.enterprise.inject.Produces
import org.babyfish.jimmer.sql.dialect.PostgresDialect
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.newKSqlClient
import org.babyfish.jimmer.sql.runtime.DefaultDatabaseNamingStrategy
import org.babyfish.jimmer.sql.runtime.Executor
import javax.sql.DataSource

@Dependent
class DatabaseDependent {

    @Produces
    @ApplicationScoped
    fun sqlClient(datasource: DataSource): KSqlClient {
        return newKSqlClient {
            setConnectionManager {
                datasource.connection.use {
                    proceed(it)
                }
            }
            setDialect(PostgresDialect())
            setDatabaseNamingStrategy(DefaultDatabaseNamingStrategy.LOWER_CASE)
            if (!ConfigUtils.isProfileActive("prod")) {
                setExecutor(Executor.log())
            }
        }
    }
}