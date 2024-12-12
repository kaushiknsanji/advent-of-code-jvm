/**
 * Problem: Day12: Garden Groups
 * https://adventofcode.com/2024/day/12
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseFileHandler
import extensions.toIntRanges
import utils.grid.ILattice
import utils.grid.Lattice
import utils.grid.Point2d
import utils.grid.TransverseDirection
import utils.grid.TransverseDirection.*
import utils.grid.TransverseDirection as Direction

private class Day12 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSampleType1(1)     // 140
    println("=====")
    solveSampleType2(1)     // 772
    println("=====")
    solveSampleType3(1)     // 1930
    println("=====")
    solveActual(1)      // 1424006
    println("=====")
    solveSampleType1(2)     // 80
    println("=====")
    solveSampleType2(2)     // 436
    println("=====")
    solveSampleType3(2)     // 1206
    println("=====")
    solveSamplePart2Type1()     // 236
    println("=====")
    solveSamplePart2Type2()     // 368
    println("=====")
    solveActual(2)      // 858684
    println("=====")
}

private fun solveSampleType1(executeProblemPart: Int) {
    execute(Day12.getSampleFile("_1").readLines(), executeProblemPart)
}

private fun solveSampleType2(executeProblemPart: Int) {
    execute(Day12.getSampleFile("_2").readLines(), executeProblemPart)
}

private fun solveSampleType3(executeProblemPart: Int) {
    execute(Day12.getSampleFile("_3").readLines(), executeProblemPart)
}

private fun solveSamplePart2Type1(executeProblemPart: Int = 2) {
    execute(Day12.getSampleFile("_part2_1").readLines(), executeProblemPart)
}

private fun solveSamplePart2Type2(executeProblemPart: Int = 2) {
    execute(Day12.getSampleFile("_part2_2").readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day12.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    GardenFenceAnalyzer.parse(input)
        .getTotalFencingPriceForAllRegions()
        .also(::println)
}

private fun doPart2(input: List<String>) {
    GardenFenceAnalyzer.parse(input)
        .getTotalFencingPriceForAllRegions(isDiscounted = true)
        .also(::println)
}

private class GardenPlot(x: Int, y: Int) : Point2d<Int>(x, y)

private class GardenPlotGrid(
    plotPattern: List<String>
) : Lattice<GardenPlot, Char>(plotPattern) {

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value location's row
     * @param column [Int] value location's column
     */
    override fun provideLocation(row: Int, column: Int): GardenPlot =
        GardenPlot(row, column)

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar Char found at a location in the input pattern
     */
    override fun provideValue(locationChar: Char): Char = locationChar

}

private class GardenFenceAnalyzer private constructor(
    private val gardenPlotGrid: GardenPlotGrid
) : ILattice<GardenPlot, Char> by gardenPlotGrid {

    companion object {

        fun parse(input: List<String>): GardenFenceAnalyzer = GardenFenceAnalyzer(GardenPlotGrid(input))
    }

    /**
     * Returns plant type at [this] in [gardenPlotGrid].
     */
    private fun GardenPlot.toPlant(): Char = gardenPlotGrid[this]

    // Map of Garden plots for each plant type
    private val plantTypesToPlotsMap: Map<Char, List<GardenPlot>> by lazy {
        getAllLocations().groupBy { it.toPlant() }
    }

    /**
     * Returns clustered Regions in [this] list of Garden plots of the given [plant] type using Depth-First Search.
     */
    private fun List<GardenPlot>.toRegions(plant: Char): List<List<GardenPlot>> {
        // For the clustered regions found
        val regions: MutableList<List<GardenPlot>> = mutableListOf()

        // Set of plots visited during traversal
        val visitedSet: MutableSet<GardenPlot> = mutableSetOf()

        /**
         * Internal recursive function to Depth-First Search connecting Garden plots of [plot]
         * and update the plots found to [region].
         */
        fun dfs(plot: GardenPlot, region: MutableList<GardenPlot>) {
            // Return when base conditions are met
            if (visitedSet.contains(plot) || plot.toPlant() != plant) {
                // Return if current [plot] is already visited or is not of the same plant type
                return
            }

            // Track the visit
            visitedSet.add(plot)
            // Update 'region' with current [plot] found
            region.add(plot)

            // Go over the neighbours of current [plot] and repeat this search recursively
            plot.getAllNeighbours().forEach { neighbourPlot: GardenPlot ->
                dfs(neighbourPlot, region)
            }
        }

        // Iterate over all Garden plots of the same plant type to group them based on connectivity
        this.forEach { currentPlot: GardenPlot ->
            // Process if 'currentPlot' not already visited
            if (!visitedSet.contains(currentPlot)) {
                // New region group for the current search
                val region = mutableListOf<GardenPlot>()
                // Search connected plots and update region group recursively
                dfs(currentPlot, region)

                // Update 'regions' when some connected plots were found for the 'currentPlot'
                if (region.isNotEmpty()) {
                    regions.add(region)
                }
            }
        }

        return regions
    }

    /**
     * Returns Perimeter of [this] Garden plot determined by its border count with their neighbours
     * of different plant type.
     */
    private fun GardenPlot.toPerimeter(): Int =
        Direction.entries.map { direction -> getNeighbour(direction) }
            .count { plot: GardenPlot? -> plot == null || plot.toPlant() != this.toPlant() }

    /**
     * Returns Area of [this] region determined by its number of plots.
     *
     * @throws IllegalArgumentException when [this] region is found to contain a plant of some other type.
     */
    private fun List<GardenPlot>.regionToArea(): Int {
        require(this.map { it.toPlant() }.distinct().count() == 1) {
            "Area Error: Region $this seems to also contain some other plant"
        }

        return count()
    }

    /**
     * Returns Perimeter of [this] region determined by the border count of every plot with their neighbours
     * of different plant type.
     *
     * @throws IllegalArgumentException when [this] region is found to contain a plant of some other type.
     */
    private fun List<GardenPlot>.regionToPerimeter(): Int {
        require(this.map { it.toPlant() }.distinct().count() == 1) {
            "Perimeter Error: Region $this seems to also contain some other plant"
        }

        return this.sumOf { plot: GardenPlot -> plot.toPerimeter() }
    }

    /**
     * Returns number of Sides formed by [this] region of Garden plots.
     */
    private fun List<GardenPlot>.regionToSides(): Int =
        this.first().toPlant().let { plant ->
            // Pick only the plots that are bordering with their neighbours of different plant type
            this.filter { plot: GardenPlot ->
                Direction.entries.any { direction ->
                    plot.getNeighbour(direction) == null ||
                            plot.getNeighbour(direction)!!.toPlant() != plant
                }
            }.flatMap { plot: GardenPlot ->
                // Get directions where the border is with respect to the plot and return as pairs
                Direction.entries.filter { direction ->
                    plot.getNeighbour(direction) == null ||
                            plot.getNeighbour(direction)!!.toPlant() != plant
                }.map { direction ->
                    direction to plot
                }
            }.groupBy { (direction, _) ->
                // Group resulting pairs by their direction
                direction
            }.mapValues { (_, pair: List<Pair<TransverseDirection, GardenPlot>>) ->
                // Convert pairs to list of plots for the direction
                pair.map { it.second }
            }.mapValues { (direction, plots: List<GardenPlot>) ->
                // Convert to number of sides in each direction
                when (direction) {
                    TOP, BOTTOM -> {
                        // For Top and Bottom sides, adjacent plots are found along the x-coordinate.
                        // Hence, group by x-coordinate
                        plots.groupBy { plot: GardenPlot -> plot.xPos }
                            .values.sumOf { xPlots: List<GardenPlot> ->
                                // For the current x-coordinate plots, get their y-coordinate values and then
                                // get a count of their ranges. This will result in the number of sides found
                                // for this x-coordinate plots.
                                xPlots.map { it.yPos }.toIntRanges().count()
                            }
                    }

                    RIGHT, LEFT -> {
                        // For Right and Left sides, adjacent plots are found along the y-coordinate.
                        // Hence, group by y-coordinate
                        plots.groupBy { plot: GardenPlot -> plot.yPos }
                            .values.sumOf { yPlots: List<GardenPlot> ->
                                // For the current y-coordinate plots, get their x-coordinate values and then
                                // get a count of their ranges. This will result in the number of sides found
                                // for this y-coordinate plots.
                                yPlots.map { it.xPos }.toIntRanges().count()
                            }
                    }
                }
            }.values.sum() // Return total of all sides found in all directions
        }

    /**
     * [Solution for Part 1 and 2]
     *
     * Return the total price for Fencing all regions of Garden plots found.
     *
     * @param isDiscounted [Boolean] to indicate if the Fence prices are discounted. For Part-1, this is `false`,
     * in which case a Region's Perimeter is used for price calculation. For Part-2, this is `true`, in which case
     * a Region's Side count is used for price calculation.
     */
    fun getTotalFencingPriceForAllRegions(isDiscounted: Boolean = false): Int =
        with(
            plantTypesToPlotsMap.mapValues { (plant: Char, singlePlantTypePlots: List<GardenPlot>) ->
                singlePlantTypePlots.toRegions(plant)
            }.values.flatten() // With the Regions of Garden plots
        ) {
            if (isDiscounted) {
                // Part-2, returns price calculated by the total of the product of every Region's Area
                // with its side count
                sumOf { singlePlantTypeRegions: List<GardenPlot> ->
                    singlePlantTypeRegions.regionToArea() * singlePlantTypeRegions.regionToSides()
                }
            } else {
                // Part-1, returns price calculated by the total of the product of every Region's Area
                // with its Perimeter
                sumOf { singlePlantTypeRegions: List<GardenPlot> ->
                    singlePlantTypeRegions.regionToArea() * singlePlantTypeRegions.regionToPerimeter()
                }
            }
        }

}