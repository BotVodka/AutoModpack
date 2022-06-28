package pl.skidam.automodpack.client;

import org.apache.commons.io.FileDeleteStrategy;
import pl.skidam.automodpack.utils.ShityCompressor;
import pl.skidam.automodpack.utils.ShityDeCompressor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static pl.skidam.automodpack.AutoModpackMain.LOGGER;

public class DeleteModpack {

    private static boolean modsDeleted;
    private static boolean configsDeleted;

    public DeleteModpack() {

        LOGGER.warn("Deleting modpack...");

        modsDeleted = true;
        configsDeleted = true;

        int tryCountMods = 1;
        int tryCountConfigs = 1;

        // unzip modpack.zip
        new ShityDeCompressor(new File("./AutoModpack/modpack.zip"), new File("./AutoModpack/modpack/"), true, "none");

        deleteMods();
        deleteConfigs();

        while (true) {
            if (tryCountMods == 10) {
                LOGGER.error("MAKE ME LOUDA Z TYMI MODAMI");
                break;
            }
            if (!modsDeleted) {
                tryCountMods++;
                LOGGER.warn("Trying to delete mods again... " + tryCountMods);
                modsDeleted = true;
                deleteMods();
            }
            if (modsDeleted) {
                break;
            }
        }

        while (true) {
            if (tryCountConfigs == 10) {
                LOGGER.error("MAKE ME LOUDA Z TYMI CONFIGAMI");
                break;
            }
            if (!configsDeleted) {
                tryCountConfigs++;
                LOGGER.warn("Trying to delete configs again... " + tryCountConfigs);
                configsDeleted = true;
                deleteConfigs();
            }
            if (configsDeleted) {
                break;
            }
        }

        // delete unzipped modpack dir, modpack.zip and modpack-link.txt
        try {
            FileDeleteStrategy.FORCE.delete(new File("./AutoModpack/modpack/"));
            FileDeleteStrategy.FORCE.delete(new File("./AutoModpack/modpack.zip"));
            FileDeleteStrategy.FORCE.delete(new File("./AutoModpack/modpack-link.txt"));
        } catch (Exception e) { // ignore it
        }

        LOGGER.info("Finished deleting modpack!");
        LOGGER.info("Restart your game!");

    }

    private static void deleteMods() {
        // MODS
        // make array of file names "./AutoModpack/modpack/mods/" folder
        File[] modpackModsFiles = new File("./AutoModpack/modpack/mods/").listFiles();

        // loop to delete all names in ./mods/ folder of names in files in "./AutoModpack/modpack/mods/"
        for (File modpackModName : modpackModsFiles) {
            String modName = modpackModName.getName();
            File modFile = new File("./mods/" + modName);

            if (modFile.exists()) {

                if (modFile.exists()) {
                    LOGGER.info("Deleting: " + modName);
                    try {
                        FileDeleteStrategy.FORCE.delete(modFile);
                    } catch (IOException ignored) {
                    }
                }

                if (modFile.exists()) { // if mod to delete still exists
                    try {
                        new ShityCompressor(new File("./AutoModpack/TrashMod/"), modFile);
                    } catch (IOException ignored) {
                    }
                    try {
                        FileWriter fw = new FileWriter("./AutoModpack/trashed-mods.txt", true);
                        fw.write(modName + "\n");
                        fw.close();
                    } catch (IOException ignored) {
                    }
                }

                if (modFile.exists()) {
                    try {
                        FileDeleteStrategy.FORCE.delete(modFile);
                    } catch (IOException ignored) {
                    }
                }

                if (!modFile.exists()) {
                    LOGGER.info("Successfully deleted: " + modName);
                } else if (modFile.exists() && modFile.length() == 16681) {
                    LOGGER.info("Successfully trashed: " + modName);
                } else {
                    LOGGER.info("Failed to delete: " + modName);
                    modsDeleted = false;
                }
            }
        }
    }

    private static void deleteConfigs() {

        // CONFIGS
        // make array of file names "./AutoModpack/modpack/config/" folder
        File[] modpackConfigFiles = new File("./AutoModpack/modpack/config/").listFiles();

        // loop to delete all names in ./config/ folder of names in files in "./AutoModpack/modpack/config/"
        for (File modpackConfigName : modpackConfigFiles) {

            String configName = modpackConfigName.getName();
            File configFile = new File("./config/" + configName);

            if (configFile.exists()) {
                try {
                    if (configFile.exists()) {
                        LOGGER.info("Deleting: " + configName);
                        FileDeleteStrategy.FORCE.delete(configFile);
                    }
                    LOGGER.info("Successfully deleted: " + configName);
                } catch (IOException e) { // ignore
                    configsDeleted = false;
                    e.printStackTrace();
                }
            }
        }
    }
}