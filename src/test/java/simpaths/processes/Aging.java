package simpaths.processes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import simpaths.experiment.SimPathsCollector;
import simpaths.model.Person;
import simpaths.model.SimPathsModel;

@Nested
@DisplayName("Aging process")
class AgingProcessTest {
    @BeforeEach
    void setup() {
    }

    @Test
    @DisplayName("A person can age")
    void personCanAge() {
        var person = new Person();
        person.setDag(10);
        person.onEvent(Person.Processes.Aging);

        assertEquals(person.getDag(), 11);
    }
}
