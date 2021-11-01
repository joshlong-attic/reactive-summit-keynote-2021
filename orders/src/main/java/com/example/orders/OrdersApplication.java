package com.example.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class OrdersApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrdersApplication.class, args);
	}

}

@Controller
class OrderRSocketController {

	private final Map<Integer, Collection<Order>> db = new ConcurrentHashMap<>();

	OrderRSocketController() {
		for (var customerId = 1; customerId <= 8; customerId++) {
			var ordersList = new ArrayList<Order>();
			var max = (int) (Math.random() * 100);
			for (var orderId = 1; orderId <= max; orderId++) {
				ordersList.add(new Order(orderId, customerId));
			}
			this.db.put(customerId, ordersList);
		}
	}

	@MessageMapping("orders.{customerId}")
	Flux<Order> getOrdersFor(@DestinationVariable Integer customerId) {
		var list = this.db.get(customerId);
		return Flux.fromIterable(list);
	}
}

record Order(Integer id, Integer customerId) {
}
