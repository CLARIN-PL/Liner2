package g419.spatial.action

import g419.toolbox.files.Unzip
import org.apache.commons.io.FileUtils
import spock.lang.Specification

class ActionPrintSpatialObjectsTest extends Specification {

    def "should print a valid list of spatial objects with their attributes"() {
        given:
            def name = "pst20_00120445"
            def zipName = name + ".zip"
            def tmpFolder = File.createTempDir("spock_", "")
            def tmpZip = new File(tmpFolder, zipName)
            def targetPath = new File(tmpFolder, name)
            final InputStream zip = getClass().getClassLoader().getResourceAsStream(zipName)
            FileUtils.copyInputStreamToFile(zip, tmpZip)
            Unzip.unzip(tmpZip.toString(), tmpFolder.toString())

            def buffer = new ByteArrayOutputStream()
            System.out = new PrintStream(buffer)

            String[] args = [
                    "print-spatial-objects",
                    "-i",
                    "tei:gz",
                    "-f",
                    targetPath.toString()
            ]

            def action = new ActionPrintSpatialObjects()
            action.parseOptions(args)

        when:
            action.run()

        then:
            buffer.toString().split("\n").length == 21

        cleanup:
            FileUtils.deleteDirectory(tmpFolder)
    }

}
