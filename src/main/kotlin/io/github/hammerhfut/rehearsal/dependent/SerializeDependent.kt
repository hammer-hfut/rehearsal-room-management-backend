package io.github.hammerhfut.rehearsal.dependent

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.quarkus.jackson.ObjectMapperCustomizer
import jakarta.enterprise.context.Dependent
import jakarta.inject.Singleton
import org.babyfish.jimmer.jackson.ImmutableModule

@Dependent
class SerializeDependent {
    @Singleton
    class JacksonObjectMapperCustomizer : ObjectMapperCustomizer {
        override fun customize(objectMapper: ObjectMapper) {
            objectMapper.findAndRegisterModules()
                .registerKotlinModule()
                .registerModule(ImmutableModule())
        }
    }
}