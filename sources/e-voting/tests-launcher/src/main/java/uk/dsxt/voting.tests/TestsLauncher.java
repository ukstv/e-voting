package uk.dsxt.voting.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.joda.time.Instant;
import uk.dsxt.voting.client.VotingClientMain;
import uk.dsxt.voting.common.datamodel.Voting;
import uk.dsxt.voting.common.networking.RegistriesServer;
import uk.dsxt.voting.common.networking.RegistriesServerImpl;
import uk.dsxt.voting.common.utils.PropertiesHelper;
import uk.dsxt.voting.masterclient.VotingMasterClientMain;
import uk.dsxt.voting.registriesserver.RegistriesServerMain;
import uk.dsxt.voting.resultsbuilder.ResultsBuilderMain;

import java.util.Properties;

@Log4j2
public class TestsLauncher {
    private static final String MODULE_NAME = "tests-launcher";

    @FunctionalInterface
    public interface SimpleRequest {
        void run();
    }

    public static void main(String[] args) {
        try {
            log.debug("Starting module {}...", MODULE_NAME);
            ObjectMapper mapper = new ObjectMapper();
            //read configuration
            Properties properties = PropertiesHelper.loadProperties(MODULE_NAME);
            int votingDuration = Integer.valueOf(properties.getProperty("voting.duration.minutes"));

            String testingType = properties.getProperty("testing.type");
            log.info("Testing type is {}", testingType);

            String registriesServerUrl = properties.getProperty("register.server.url");
            int connectionTimeout = Integer.parseInt(properties.getProperty("http.connection.timeout"));
            int readTimeout = Integer.parseInt(properties.getProperty("http.read.timeout"));
            //json file configuration for clients
            String configFileName = properties.getProperty("client.config.file");
            String resourceJson = PropertiesHelper.getResourceString(String.format(configFileName, testingType));
            ClientConfiguration[] configurations = mapper.readValue(resourceJson, ClientConfiguration[].class);

            //starting single modules
            startSingleModule(RegistriesServerMain.MODULE_NAME, () -> RegistriesServerMain.main(new String[] {testingType, String.valueOf(votingDuration)}));
            startSingleModule(ResultsBuilderMain.MODULE_NAME, () -> ResultsBuilderMain.main(null));
            startSingleModule(VotingMasterClientMain.MODULE_NAME, () -> VotingMasterClientMain.main(null));

            //starting clients
            long start = Instant.now().getMillis();
            log.debug("Starting {} instances of {}", configurations.length, VotingClientMain.MODULE_NAME);
            for (int i = 0; i < configurations.length; i++) {
                ClientConfiguration conf = configurations[i];
                VotingClientMain.main(new String[] {conf.getPublicKey(), conf.getPrivateKey(), conf.getVote(), String.valueOf(conf.isHonestParticipant())});
            }
            log.info("{} instances of {} started in {} ms", configurations.length, RegistriesServerMain.MODULE_NAME, Instant.now().getMillis() - start);

            //need to wait until voting is complete
            RegistriesServer regServer = new RegistriesServerImpl(registriesServerUrl, connectionTimeout, readTimeout);
            Voting[] votings = regServer.getVotings();
            if (votings.length > 1) {
                log.error("There is more than one voting. Stopping launcher");
                return;
            }
            Voting currentVoting = regServer.getVotings()[0];
            long sleepPeriod = currentVoting.getEndTimestamp() - Instant.now().getMillis();
            log.info("Waiting {} seconds while voting ends", sleepPeriod / 1000);
            Thread.sleep(sleepPeriod);

            //TODO: start to ask results builder for voting results

            //TODO: after receiving results print them

            //stop jetty servers
            RegistriesServerMain.shutdown();
            ResultsBuilderMain.shutdown();

            log.info("Testing finished");
        } catch (Exception e) {
            log.error("Error occurred in module {}", MODULE_NAME, e);
        }
    }

    private static void startSingleModule(String name, SimpleRequest request) {
        log.debug("Starting {}", name);
        long start = Instant.now().getMillis();
        request.run();
        log.info("{} started in {} ms", name, Instant.now().getMillis() - start);
    }

}
