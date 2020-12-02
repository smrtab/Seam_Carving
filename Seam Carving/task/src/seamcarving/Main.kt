package seamcarving

import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) {

    try {

        InputOptions.parse(args)

        val image = Image(InputOptions.inputFile)
        val reducedImage = image.reduce(InputOptions.width, InputOptions.height)

        ImageIO.write(reducedImage.getValue(), "png", File(InputOptions.outputFile))

    } catch (e: SeamcarvingException) {
        println(e.message)
    }
}