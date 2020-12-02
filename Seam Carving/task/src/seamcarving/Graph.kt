package seamcarving

import java.util.*

class Graph(private val image: Image) {

    private var value: List<List<Pixel>> = List(this.image.getHeight()) { y ->
        List(this.image.getWidth()) { x ->
            Pixel(x, y)
        }
    }

    private var path: Array<Pixel?> = emptyArray()

    fun getPixel(x: Int, y: Int): Pixel? {
        if (this.value.getOrNull(y) == null || this.value[y].getOrNull(x) == null) {
            return null
        }
        return this.value[y][x]
    }

    /**
     * Applies Dijkstra algorithm to find the best seems
     * with the lowest sum of energies.
     * Uses priority queue ADT in order for pixel with the lowest energy
     * to bubble up.
     * @return a {@code Array<Pixel?>}
     */
    fun applyDijkstra(): Array<Pixel?> {

        val queue = PriorityQueue(Pixel.comparator())

        this.value[0].forEach { pixel ->
            pixel.energy = this.image.getEnergyMap()[pixel.y][pixel.x]
            queue.add(pixel)
        }

        while (queue.isNotEmpty()) {

            val node = queue.poll()

            for (neighbor in node.getNeighbors()) {

                if (neighbor.isProcessed())
                    continue

                val weight = this.image.getEnergyMap()[neighbor.y][neighbor.x]
                val energy = node.energy + weight
                val isQueued = !neighbor.isNotQueued()

                if (energy < neighbor.energy) {
                    neighbor.energy = energy
                    neighbor.from = node
                }

                if (!isQueued)
                    queue.add(neighbor)
            }

            node.setIsProcessed(true)
        }

        var target = this.value.last().minByOrNull { it.energy }!!

        this.path = Array(this.image.getHeight()) { null }
        this.path[target.y] = target

        while (target.from != null) {
            this.path[target.from!!.y] = target.from!!
            target = target.from!!
        }

        return this.path
    }
}