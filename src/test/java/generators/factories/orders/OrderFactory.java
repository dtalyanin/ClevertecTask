package generators.factories.orders;

import generators.builders.OrderBuilder;
import ru.clevertec.ecl.models.Order;

import java.util.List;

import static generators.factories.users.UserFactory.getSimpleUser3;

public class OrderFactory {

    public static Order getSimpleOrder() {
        return OrderBuilder.builder().build();
    }

    public static Order getSimpleOrder2() {
        return OrderBuilder.builder()
                .id(2L)
                .quantity(10)
                .totalPrice(10000L)
                .build();
    }

    public static Order getSimpleOrder3() {
        return OrderBuilder.builder()
                .id(3L)
                .user(getSimpleUser3())
                .price(2000L)
                .quantity(10)
                .totalPrice(20000L)
                .build();
    }

    public static Order getSimpleOrderBeforeSaving() {
        return OrderBuilder.builder()
                .id(null)
                .totalPrice(null)
                .orderDate(null)
                .build();
    }

    public static List<Order> getDifferentOrders() {
        return List.of(getSimpleOrder(), getSimpleOrder2());
    }
}
