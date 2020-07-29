package uk.nhs.adaptors.scr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class SampleTest {

    @Test
    public void shouldPass() {
        assertThat(true).isEqualTo(true);
    }

    @Test
    public void shouldFail() {
        assertThat(true).isEqualTo(false);
    }
}
