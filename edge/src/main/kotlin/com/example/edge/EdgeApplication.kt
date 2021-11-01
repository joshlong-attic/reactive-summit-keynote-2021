package com.example.edge

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.retrieveFlux
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@SpringBootApplication
class EdgeApplication

@Configuration
class EdgeConfiguration {

    @Bean
    fun http(webClient: WebClient.Builder) = webClient.build()

    @Bean
    fun rSocket(rSocket: RSocketRequester.Builder) =
        rSocket.tcp("localhost", 8181)
}

fun main(args: Array<String>) {
    runApplication<EdgeApplication>(*args)
}

data class Order(val id: Int, val customerId: Int)
data class Customer(val id: Int, val name: String)
data class CustomerOrders(val customer: Customer, val orders: List<Order>)

@Component
class CrmClient(val http: WebClient, val rSocket: RSocketRequester) {

    fun customers() =
        this.http.get().uri("http://localhost:8080/customers").retrieve()
            .bodyToFlux<Customer>()

    fun ordersForCustomer(customerId: Int) =
        this.rSocket.route("orders.{cid}", customerId).retrieveFlux<Order>()

    fun customerOrders(): Flux<CustomerOrders> = this.customers()
        .flatMap {
            Mono.zip(
                Mono.just(it),
                ordersForCustomer(it.id).collectList()
            )
        }
        .map { tuple2 -> CustomerOrders(tuple2.t1, tuple2.t2) }

}

@Controller
class CrmGraphQLController(val crm: CrmClient) {

    @QueryMapping
    fun customers() = this.crm.customers()

    @SchemaMapping(typeName = "Customer")
    fun orders(customer: Customer) = this.crm.ordersForCustomer(customer.id)
}