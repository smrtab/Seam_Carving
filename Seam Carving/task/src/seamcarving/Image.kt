package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.pow
import kotlin.math.sqrt

class Image {

    private val value: BufferedImage
    private var energyMap: List<DoubleArray> = emptyList()
    private var maxEnergyValue: Double = 0.0

    constructor(inputFile: String) {
        val input = File(inputFile)
        this.value = ImageIO.read(input)
    }

    constructor(image: BufferedImage) {
        this.value = image
    }

    /**
     * Rotate the matrix along the main diagonal
     * @return an {@code Image}
     */
    fun transpose(): Image {
        val image = BufferedImage(this.value.height, this.value.width, BufferedImage.TYPE_INT_RGB)
        for (row in 0 until this.value.width) {
            for (column in 0 until this.value.height) {
                image.setRGB(column, row, this.value.getRGB(row, column))
            }
        }
        return Image(image)
    }

    /**
     * Negate the object, i.e. subtract each pixels color from 255
     * @return a {@code Image}
     */
    fun negate(): Image {

        val image = BufferedImage(this.value.width, this.value.height, BufferedImage.TYPE_INT_RGB)

        for (i in 0 until this.value.height) {
            for (j in 0 until this.value.width) {
                var pixel = Color(this.value.getRGB(j, i))
                image.setRGB(j, i, Color(255 - pixel.red, 255 - pixel.green, 255 - pixel.blue, 127).rgb)
            }
        }

        return Image(image)
    }

    /**
     * Intensifies an image
     * Uses energy matrix
     * @return a {@code Image}
     */
    fun intensify(): Image {

        this.energyMap = this.createEnergyMap()
        val image = BufferedImage(this.value.width, this.value.height, BufferedImage.TYPE_INT_RGB)

        for (i in 0 until this.value.height) {
            for (j in 0 until this.value.width) {
                val intensity = (255.0 * this.energyMap[i][j] / this.maxEnergyValue).toInt()
                image.setRGB(j, i, Color(intensity, intensity, intensity).rgb)
            }
        }

        return Image(image)
    }

    /**
     * Reduces an image by given width and height
     * @return a {@code Image}
     */
    fun reduce(width: Int, height: Int): Image {

        var image = this.copy()
        for (w in 1..width) {
            image = image.removeBestSeem()
        }

        image = image.transpose()
        for (h in 1..height) {
            image = image.removeBestSeem()
        }

        return image.transpose()
    }

    /**
     * Finds the best seem to remove (the seam with the lowest energy).
     * Paint the seem in red
     * @return an {@code Image}
     */
    fun seem(): Image {

        val image = this.copy()
        val path = this.getBestSeem()
        for (node in path) {
            image.getValue().setRGB(node!!.x, node!!.y, Color(255, 0, 0).rgb)
        }

        return image
    }

    fun getValue() = this.value
    fun getWidth() = this.value.width
    fun getHeight() = this.value.height
    fun getEnergyMap() = this.energyMap

    private fun getY(i: Int): Int {
        return when (i) {
            0 -> i + 1
            this.value.height - 1 -> i - 1
            else -> i
        }
    }
    private fun getX(j: Int): Int {
        return when (j) {
            0 -> j + 1
            this.value.width - 1 -> j - 1
            else -> j
        }
    }

    /**
     * Copies the image
     * @return an {@code Image}
     */
    private fun copy(): Image {
        val image = BufferedImage(this.value.width, this.value.height, BufferedImage.TYPE_INT_RGB)
        for (i in 0 until image.height) {
            for (j in 0 until image.width) {
                image.setRGB(j, i, this.value.getRGB(j, i))
            }
        }

        return Image(image)
    }

    /**
     * Builds graph and applies Dijkstra algorithm.
     * Returns a path consisted of pixels
     * @return a {@code Array<Pixel?>}
     */
    private fun getBestSeem(): Array<Pixel?> {

        this.energyMap= this.createEnergyMap()
        val graph = Graph(this)

        for (i in this.energyMap.indices) {
            for (j in this.energyMap[i].indices) {
                val pixel = graph.getPixel(j, i)!!

                graph.getPixel(j - 1, i - 1)?.addNeighbor(pixel)
                graph.getPixel(j, i - 1)?.addNeighbor(pixel)
                graph.getPixel(j + 1, i - 1)?.addNeighbor(pixel)
            }
        }

        return graph.applyDijkstra()
    }

    /**
     * Removes the best seem found
     * In each row corresponding pixel is skipped
     * @return an {@code Image}
     */
    private fun removeBestSeem(): Image {

        val image = BufferedImage(this.value.width - 1, this.value.height, BufferedImage.TYPE_INT_RGB)
        val path = this.getBestSeem()

        for (y in 0 until this.value.height) {
            val pixel = path[y]!!
            var x = -1
            inner@for (j in 0 until this.value.width) {
                if (pixel.x == j)
                    continue@inner
                x++
                image.setRGB(x, y, this.value.getRGB(j, y))
            }
        }

        return Image(image)
    }

    /**
     * Creates a matrix of pixels' energies
     * @return a {@code List<DoubleArray>}
     */
    private fun createEnergyMap(cached: Boolean = true): List<DoubleArray> {

        if (cached && this.energyMap.isNotEmpty()) {
            return this.energyMap
        }

        var energies = List(this.value.height) { DoubleArray(this.value.width) }
        for (i in 0 until this.value.height) {

            val y = this.getY(i)
            for (j in 0 until this.value.width) {

                val x = this.getX(j)

                val nextX = Color(this.value.getRGB(x + 1, i))
                val prevX = Color(this.value.getRGB(x - 1, i))
                val nextY = Color(this.value.getRGB(j, y + 1))
                val prevY = Color(this.value.getRGB(j, y - 1))

                val dX = (prevX.red - nextX.red).toDouble().pow(2) +
                        (prevX.green - nextX.green).toDouble().pow(2) +
                        (prevX.blue - nextX.blue).toDouble().pow(2)

                val dY = (prevY.red - nextY.red).toDouble().pow(2) +
                        (prevY.green - nextY.green).toDouble().pow(2) +
                        (prevY.blue - nextY.blue).toDouble().pow(2)

                val energy = sqrt(dX + dY)
                if (energy > this.maxEnergyValue)
                    this.maxEnergyValue = energy

                energies[i][j] = energy
            }
        }

        return energies
    }
}