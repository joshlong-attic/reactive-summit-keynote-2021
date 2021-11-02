package com.example.edge


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.http.ResponseEntity
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.retrieveMono
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@SpringBootApplication
open class EdgeApplication {

    @Bean
    open fun httpClient(webClient: WebClient.Builder) = webClient.build()

    @Bean
    open fun rSocketClient(rSocket: RSocketRequester.Builder) =
        rSocket.tcp("localhost", 8181)
}

fun main(args: Array<String>) {
    runApplication<EdgeApplication>(*args)
}

data class Profile(val id: Int, val registered: Date)
data class Customer(val id: Int, val name: String)
data class CustomerProfile(val customer: Customer, val profile: Profile)

@Component
class CrmClient(
    val httpClient: WebClient,
    val rSocketClient: RSocketRequester
) {

    fun customers(): Flux<Customer> =
        this.httpClient.get().uri("http://localhost:8080/customers").retrieve()
            .bodyToFlux()

    fun profileForCustomer(customerId: Int): Mono<Profile> =
        this.rSocketClient.route("profiles.{cid}", customerId).retrieveMono()

    fun customerProfiles(): Flux<CustomerProfile> =
        this.customers()
            .flatMap {
                Mono.zip(
                    Mono.just(it),
                    profileForCustomer(it.id)
                )
            }
            .map { tuple2 -> CustomerProfile(tuple2.t1, tuple2.t2) }

}

@Controller
class CrmGraphQLController(val crm: CrmClient) {

    @SchemaMapping(typeName = "Profile")
    fun registered(p: Profile) = p.registered.toGMTString()

    @QueryMapping
    fun customers(): Flux<Customer> =
        if (Math.random() > .5)
            this.crm.customers()
        else Flux.error {
            IllegalAccessException(
                "No!"
            )
        }

    @SchemaMapping(typeName = "Customer")
    fun profile(customer: Customer) = this.crm.profileForCustomer(customer.id)
}