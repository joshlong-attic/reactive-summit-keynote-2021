package com.example.edge

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.retrieveAndAwait
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlow
import org.springframework.web.reactive.function.client.bodyToFlux
import reactor.core.publisher.Flux
import java.util.*

@SpringBootApplication
class EdgeApplication

fun main(args: Array<String>) {
    runApplication<EdgeApplication>(*args)
}

data class Customer(val id: Int, val name: String)
data class Profile(val id: Int, val registered: Date)
data class CustomerProfile(val customer: Customer, val profile: Profile)

@Configuration
class CrmConfiguration {

    @Bean
    fun rsocket(rsb: RSocketRequester.Builder) = rsb.tcp("localhost", 8181)

    @Bean
    fun http(wcb: WebClient.Builder) = wcb.build()
}

@Component
class CrmClient(
    private val http: WebClient,
    private val rsocket: RSocketRequester
) {

    suspend fun customers(): Flow<Customer> =
        this.http.get().uri("http://localhost:8080/customers").retrieve()
            .bodyToFlow()

    suspend fun profileFor(customerId: Int): Profile =
        this.rsocket.route("profiles.{cid}", customerId)
            .retrieveAndAwait()

    suspend fun customerProfiles(): Flow<CustomerProfile> =
        this.customers()
            .map { customer ->
                val profile = this.profileFor(customer.id)
                CustomerProfile(customer, profile)
            }

}

@Controller
class CrmGraphqlController(private val crm: CrmClient) {

    @SchemaMapping(typeName = "Profile")
    fun registered(p: Profile): String = p.registered.toGMTString()

    @SchemaMapping(typeName = "Customer")
    suspend fun profile(c: Customer) = this.crm.profileFor(c.id)

    @QueryMapping
    suspend fun customers() = this.crm.customers()
}

@Controller
@ResponseBody
class CrmRestController(private val crm: CrmClient) {

    @GetMapping("/cos")
    suspend fun customerProfiles() = this.crm.customerProfiles()
}
