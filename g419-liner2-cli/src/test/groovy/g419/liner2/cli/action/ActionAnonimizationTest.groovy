package g419.liner2.cli.action

import org.apache.commons.io.FileUtils
import spock.lang.Specification

class ActionAnonimizationTest extends Specification {

    def "should anonymize given document and generate a valid output"() {
        given:
            File document = File.createTempFile("document", ".xml")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/anonimization/document.xml"), document)

            File dictionary = File.createTempFile("dictionary", ".txt")
            FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("/anonimization/dictionary.txt"), dictionary)

            File output = File.createTempFile("output", ".xml")
            String outputExpected = getClass().getResourceAsStream("/anonimization/output.xml").text.trim()

            String[] options = ["-i", "ccl",
                                "-f", document.getAbsolutePath(),
                                "-d", dictionary.getAbsolutePath(),
                                "-o", "ccl",
                                "-t", output.getAbsolutePath()];

            ActionAnonimization action = new ActionAnonimization()
            action.parseOptions(options)

        when:
            action.run()

        then:
            outputExpected == FileUtils.readFileToString(output, "UTF-8").trim()

        cleanup:
            FileUtils.deleteQuietly(document)
            FileUtils.deleteQuietly(dictionary)
            FileUtils.deleteQuietly(output)

    }


}
