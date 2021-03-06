package com.bigchaindb.ocean.cli.modules.assets;

import com.bigchaindb.ocean.cli.models.CommandResult;
import com.bigchaindb.ocean.cli.AssetsCLI;
import com.bigchaindb.ocean.cli.models.exceptions.CLIException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.oceanprotocol.squid.exceptions.DDOException;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.asset.AssetMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "import",
        description = "Import an asset using the metadata in JSON format ")
public class AssetsImport implements Callable {

    private static final Logger log = LogManager.getLogger(AssetsImport.class);

    @CommandLine.ParentCommand
    AssetsCLI parent;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @CommandLine.Parameters(index = "0")
    String metadataFile;

    CommandResult importAsset() throws CLIException {

        DDO ddo;
        try {
            System.out.println("Importing asset using file " + metadataFile);

            parent.cli.progressBar.start();

            ddo = parent.cli.getOceanAPI().getAssetsAPI()
                    .create(assetMetadataBuilder(metadataFile), parent.serviceEndpointsBuilder());

            System.out.println("Asset Created: " + ddo.getDid().toString());

        } catch (IOException e) {
            throw new CLIException("Error parsing metadata " + e.getMessage());
        } catch (DDOException e) {
            throw new CLIException("Error with DDO " + e.getMessage());
        } finally {
            parent.cli.progressBar.doStop();
        }
        return CommandResult.successResult().setResult(ddo);

    }


    AssetMetadata assetMetadataBuilder(String filePath) throws IOException {
        String jsonContent =  new String(Files.readAllBytes(Paths.get(filePath)));
        return DDO.fromJSON(new TypeReference<AssetMetadata>() {}, jsonContent);
    }

    @Override
    public Object call() throws CLIException {
        return importAsset();
    }
}
