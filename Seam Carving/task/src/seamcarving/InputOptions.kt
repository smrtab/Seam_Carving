package seamcarving

object InputOptions {

    var inputFile: String = ""
    var outputFile: String = ""
    var width: Int = 0
    var height: Int = 0

    fun parse(args: Array<String>) {

        val inputFileIndex = args.indexOf("-in")
        val outputFileIndex = args.indexOf("-out")
        val widthIndex = args.indexOf("-width")
        val heightIndex = args.indexOf("-height")

        if (inputFileIndex != -1) {
            val inputFile = args.getOrNull(inputFileIndex + 1)
            inputFile ?: throw SeamcarvingException("No input file defined!")
            InputOptions.inputFile = inputFile
        }
        if (outputFileIndex != -1) {
            val outputFile = args.getOrNull(outputFileIndex + 1)
            outputFile ?: throw SeamcarvingException("No output file defined!")
            InputOptions.outputFile = outputFile
        }
        if (widthIndex != -1) {
            val width = args.getOrNull(widthIndex + 1)
            width ?: throw SeamcarvingException("No width defined!")
            InputOptions.width = width.toInt()
        }
        if (heightIndex != -1) {
            val height = args.getOrNull(heightIndex + 1)
            height ?: throw SeamcarvingException("No height defined!")
            InputOptions.height = height.toInt()
        }

        args.forEach {
            if (it.matches(Regex("-[a-z]+")) && it != "-in" && it != "-out" && it != "-width" && it != "-height")
                println("\"$it\" is not a valid parameter. It will be skipped.")
        }
    }
}