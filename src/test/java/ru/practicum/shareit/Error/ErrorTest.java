package ru.practicum.shareit.Error;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.model.ErrorResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ErrorTest {

    @Test
    void errorResponseTest() {
        ErrorResponse errorResponse = new ErrorResponse("ValidationException");

        assertThat(errorResponse.getError(), equalTo("ValidationException"));
    }

}
