package generators.factories.users;

import ru.clevertec.ecl.dto.UserDto;

import java.util.Collections;
import java.util.List;

public class UserDtoFactory {
    public static UserDto getSimpleUserDto() {
        return new UserDto("Ivan Ivanov", "ivan@ivanov.com");
    }

    public static UserDto getSimpleUserDto2() {
        return new UserDto("Petr Petrov", "petr@petrov.com");
    }

    public static UserDto getSimpleUserDto3() {
        return new UserDto("Alexandr Alexandrov", "alexandr@alexandrov.com");
    }

    public static List<UserDto> getSimpleUsersDtos() {
        return List.of(getSimpleUserDto(), getSimpleUserDto2(), getSimpleUserDto3());
    }

    public static List<UserDto> getUserDtosWithSize1() {
        return List.of(getSimpleUserDto());
    }

    public static List<UserDto> getEmptyListUserDtos() {
        return Collections.emptyList();
    }
}
