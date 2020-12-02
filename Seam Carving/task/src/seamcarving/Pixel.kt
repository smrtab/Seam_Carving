package seamcarving

class Pixel(
        val x: Int,
        val y: Int,
        var energy: Double = Double.MAX_VALUE,
        private var processed: Boolean = false
) {

    private val neighbors = mutableListOf<Pixel>()
    var from: Pixel? = null

    companion object {
        fun comparator(): java.util.Comparator<Pixel> {
            return Comparator<Pixel> { px1, px2 ->
                when {
                    px1.energy < px2.energy -> -1
                    px1.energy > px2.energy -> 1
                    else -> 0
                }
            }
        }
    }

    fun addNeighbor(node: Pixel) {
        this.neighbors.add(node)
    }

    fun getNeighbors(): MutableList<Pixel> = this.neighbors
    fun isNotQueued(): Boolean = this.energy == Double.MAX_VALUE
    fun isProcessed(): Boolean = this.processed
    fun setIsProcessed(flag: Boolean) {
        this.processed = flag
    }
}