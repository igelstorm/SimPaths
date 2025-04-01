package simpaths.data.startingpop;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import static java.util.Arrays.asList;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

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
    void testDatabaseFromCSV() throws Exception {
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

        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SCRIPT");
            StringBuilder result = new StringBuilder();
            while (rs.next()) {
                result.append(rs.getString(1).replaceAll("\\r", "")).append("\n");
            }

            Path outputPath = Paths.get("src/test/resources/expected-db-state.sql");
            String expected = null;
            if (Files.exists(outputPath)) {
                expected = Files.readString(outputPath).replaceAll("\\r", "");
            }

            if (expected == null) {
                // First run - save as baseline
                Files.writeString(outputPath, result.toString());
                System.out.println("Baseline created at: " + outputPath);
            } else {
                System.out.println(expected.split("\n")[3]);
                System.out.println(result.toString().split("\n")[3]);
                Patch<String> patch = DiffUtils.diff(asList(expected.split("\n")), asList(result.toString().split("\n")));
                for (AbstractDelta<String> delta : patch.getDeltas()) {
                    System.out.println(delta);
                }
                if (!patch.getDeltas().isEmpty()) {
                    fail("Differences!");
                }
            }
        }
    }
}