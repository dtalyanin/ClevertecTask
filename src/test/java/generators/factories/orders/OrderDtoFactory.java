package generators.factories.orders;

import generators.builders.OrderDtoBuilder;
import ru.clevertec.ecl.dto.orders.OrderDto;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static generators.factories.certificates.GiftCertificateDtoForOrderFactory.getSimpleGiftCertificateDtoForOrder2;

public class OrderDtoFactory {

    public static OrderDto getSimpleOrderDto() {
        return OrderDtoBuilder.builder().build();
    }

    public static OrderDto getSimpleOrderDto2() {
        return OrderDtoBuilder.builder()
                .quantity(10)
                .totalPrice(BigDecimal.valueOf(10000L, 2))
                .build();
    }

    public static OrderDto getSimpleOrderDto3() {
        return OrderDtoBuilder.builder()
                .certificate(getSimpleGiftCertificateDtoForOrder2())
                .quantity(10)
                .price(BigDecimal.valueOf(2000L, 2))
                .totalPrice(BigDecimal.valueOf(20000L, 2))
                .build();
    }

    public static List<OrderDto> getDifferentOrderDtos() {
        return List.of(getSimpleOrderDto(), getSimpleOrderDto2());
    }

    public static List<OrderDto> getOrderDtosWithSize1() {
        return List.of(getSimpleOrderDto());
    }

    public static List<OrderDto> getEmptyListOrderDtos() {
        return Collections.emptyList();
    }
}
