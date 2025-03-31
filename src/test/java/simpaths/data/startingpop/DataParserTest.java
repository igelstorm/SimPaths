package simpaths.data.startingpop;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

import simpaths.data.Parameters;
import simpaths.model.enums.Country;

class DataParserTest {
    private static Connection conn;

    @BeforeEach
    void setupDatabase() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
    }

    @AfterEach
    void tearDownDatabase() throws Exception {
        if (conn != null) {
            conn.close();
        }
    }

    @Test
    void testCreateDatabaseForPopulationInitialisationByYearFromCSV() throws Exception {
        String inputDirectory = Paths.get("src/test/resources/").toAbsolutePath().toString() + File.separator;
        Country country = Country.UK;

        try (MockedStatic<Parameters> parameters = Mockito.mockStatic(Parameters.class)) {
            parameters.when(Parameters::getInputDirectoryInitialPopulations)
                .thenReturn(inputDirectory);
			parameters.when(Parameters::getPopulationInitialisationInputFileName)
                .thenReturn("population_initial_subset_UK");
			parameters.when(Parameters::getMinStartYear)
                .thenReturn(2019);
			parameters.when(Parameters::getMaxStartYear)
                .thenReturn(2019);
            DataParser.databaseFromCSV(country, false, conn);
        }
    }
}