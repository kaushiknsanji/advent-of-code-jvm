/**
 * Problem: Day24: Never Tell Me The Odds
 * https://adventofcode.com/2023/day/24
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import extensions.distinctPairs
import kotlin.math.roundToLong

private class Day24 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1, 7.00, 27.00)      // 2
    println("=====")
    solveActual(1, 200000000000000.00, 400000000000000.00)  // 18098
    println("=====")
    solveSample(2)      // 47
    println("=====")
    solveActual(2)      // 886858737029295
    println("=====")
}

private fun solveSample(executeProblemPart: Int, lowerBound: Double = 0.0, upperBound: Double = 0.0) {
    execute(Day24.getSampleFile().readLines(), executeProblemPart, lowerBound, upperBound)
}

private fun solveActual(executeProblemPart: Int, lowerBound: Double = 0.0, upperBound: Double = 0.0) {
    execute(Day24.getActualTestFile().readLines(), executeProblemPart, lowerBound, upperBound)
}

private fun execute(input: List<String>, executeProblemPart: Int, lowerBound: Double, upperBound: Double) {
    when (executeProblemPart) {
        1 -> doPart1(input, lowerBound, upperBound)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>, lowerBound: Double, upperBound: Double) {
    TrajectoryAnalyzer.parse(input)
        .getCountOfIntersectionsWithinTestArea(lowerBound..upperBound)
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    TrajectoryAnalyzer.parse(input)
        .getCoordinateSumOfTheRockPosition()
        .also { println(it) }
}

/**
 * Class for Hailstone linear trajectory and velocity information.
 *
 * @property x [Long] position of Hailstone in X-plane.
 * @property y [Long] position of Hailstone in Y-plane.
 * @property z [Long] position of Hailstone in Z-plane.
 * @property velocityX [Int] value of Hailstone's velocity in X-plane.
 * @property velocityY [Int] value of Hailstone's velocity in Y-plane.
 * @property velocityZ [Int] value of Hailstone's velocity in Z-plane.
 */
private class Hailstone private constructor(
    val x: Long,
    val y: Long,
    val z: Long,
    val velocityX: Int,
    val velocityY: Int,
    val velocityZ: Int
) {
    companion object {
        private val numbersRegex = """(-?\d+)""".toRegex()

        fun create(inputLine: String): Hailstone =
            numbersRegex.findAll(inputLine).map { it.groupValues[1] }.toList()
                .let { extractedNumbersList: List<String> ->
                    Hailstone(
                        x = extractedNumbersList[0].toLong(),
                        y = extractedNumbersList[1].toLong(),
                        z = extractedNumbersList[2].toLong(),
                        velocityX = extractedNumbersList[3].toInt(),
                        velocityY = extractedNumbersList[4].toInt(),
                        velocityZ = extractedNumbersList[5].toInt()
                    )
                }
    }

    // Slope of the linear trajectory of Hailstone
    private val xySlope: Double = velocityY.toDouble() / velocityX.toDouble()

    // Intercept of the linear trajectory of Hailstone
    private val interceptYOfX: Double = y - xySlope * x

    /**
     * Returns the [Intersection] data of one [Hailstone] with the [other].
     *
     * Can be `null` if both of the Hailstones' trajectory have the same [slope][xySlope]
     * or when either of the Hailstones' slope is invalid
     * or when their computed intersection data is invalid
     * or when Hailstones have crossed paths in the past for either of the hailstones.
     */
    fun intersect(other: Hailstone): Intersection? {
        // Return null when slopes are invalid or found to be equal
        // in the case of Hailstones moving in parallel trajectory
        if (xySlope.isNaN() || other.xySlope.isNaN() || xySlope == other.xySlope) return null

        // Find X-Intersect = (c2 - c1) / (m1 - m2)
        // Derived from two slope-intercept equations considering both 'Y' and both 'X' to be the same for intersection
        val xIntersect = (other.interceptYOfX - this.interceptYOfX) / (this.xySlope - other.xySlope)

        // Find Y-Intersect = m * xIntersect + c
        val yIntersect = xySlope * xIntersect + interceptYOfX

        // Find time of intersection for "this" Hailstone using linear velocity formula
        val thisTime = (xIntersect - x) / velocityX

        // Find time of intersection for "other" Hailstone using linear velocity formula
        val otherTime = (xIntersect - other.x) / other.velocityX

        // Return null when the computed intersection data is invalid
        if (xIntersect.isNaN() || yIntersect.isNaN() || thisTime.isNaN() || otherTime.isNaN()) return null

        // Return null when hailstones have crossed paths in the past for either of the hailstones
        if (thisTime < 0 || otherTime < 0) return null

        // Return Intersection data
        return Intersection(xIntersect, yIntersect, thisTime)
    }

    /**
     * Changes velocity in X and Y plane by delta of [deltaVelocityX] and [deltaVelocityY] respectively
     * in order to compute intersection with the rock thrown at a later point in time.
     */
    fun nudgeXYVelocity(deltaVelocityX: Int, deltaVelocityY: Int): Hailstone =
        Hailstone(
            x,
            y,
            z,
            velocityX + deltaVelocityX,
            velocityY + deltaVelocityY,
            velocityZ
        )

    /**
     * Computes and returns the new Z-position based on its Z-velocity [velocityZ] and
     * the change in Z-velocity [deltaVelocityZ] along with the time taken to reach intersection [timeElapsed]
     * or in other words, the travelling time needed to move from initial Z-position [z] to reach the
     * returned new Z-position.
     */
    fun recomputeZ(deltaVelocityZ: Double, timeElapsed: Double): Double =
        z + (velocityZ + deltaVelocityZ) * timeElapsed

    override fun toString(): String =
        "Hailstone: $x, $y, $z @ $velocityX, $velocityY, $velocityZ"

}

/**
 * Class for intersection data of two [Hailstone]s crossing paths based on their trajectories.
 *
 * @property x [Double] value of X-Intersect position.
 * @property y [Double] value of Y-Intersect position.
 * @property time [Double] value of the time of intersection or time taken from the initial position to reach intersection.
 */
private class Intersection(val x: Double, val y: Double, val time: Double) {

    /**
     * Finds and returns the change in Z-velocity for the current [Intersection] of a common [Hailstone]
     * with the [currentHailstone] and [other] Intersection of the same common [Hailstone] with the [otherHailstone].
     *
     * This formula is derived from the equation which computes the new Z-position based on its Z-velocity (`vz`)
     * and the change in Z-velocity (`dvz`) along with the time taken to reach intersection
     * from initial Z-position, i.e,
     * ```
     * z<new> = z<initial> + (vz + dvz) * time
     * ```
     *
     * Both `z<new>` and both `dvz` resulting from two intersections of a common [Hailstone] with [currentHailstone]
     * and the same with [other] Hailstone are considered to be same for arriving at the following equation used
     * to compute the change in Z-velocity (`dvz`).
     *
     * If `t<01>` is the time of intersection of common [Hailstone] - (`h0`) with [currentHailstone] - (`h1`) and
     * `t<02>` is the time of intersection of common [Hailstone] - (`h0`) with [other] - (`h2`) Hailstone, then
     * ```
     * z1<new> = z1<initial> + (vz1 + dvz1) * t<01>
     * z2<new> = z2<initial> + (vz2 + dvz2) * t<02>
     * ```
     * where `z1` is from `h1` and `z2` is from `h2`.
     *
     * Considering `z1<new> = z2<new> = z<new>` and `dvz1 = dvz2 = dvz`, we can equate them to compute
     * the change in Z-velocity (`dvz`)-
     * ```
     * z1<initial> + (vz1 + dvz) * t<01> = z2<initial> + (vz2 + dvz) * t<02>
     * z1<initial> - z2<initial> + vz1 * t<01> - vz2 * t<02> = (t<02> - t<01>) * dvz
     * dvz = (z1<initial> - z2<initial> + vz1 * t<01> - vz2 * t<02>) / (t<02> - t<01>)
     * ```
     *
     * By treating `z1<new> = z2<new> = z<new>`, we have ensured that `z<new>` which is the intersection in Z-plane
     * remains same for both the intersections just like [x] intersect and [y] intersect positions.
     *
     * Returned `dvz` can be later used in one of the `z*<new>` equation, i.e.,
     * ```
     * z1<new> = z1<initial> + (vz1 + dvz) * t<01>
     * ```
     * to compute the new Z position which will be the Z-intersect position.
     */
    fun findDeltaVelocityZ(other: Intersection, currentHailstone: Hailstone, otherHailstone: Hailstone): Double =
        (currentHailstone.z - otherHailstone.z +
                currentHailstone.velocityZ * time - otherHailstone.velocityZ * other.time) / (other.time - time)

    override fun toString(): String =
        "Intersection: $x, $y @ time $time"
}

private class TrajectoryAnalyzer private constructor(
    private val hailstones: List<Hailstone>
) {
    companion object {
        fun parse(input: List<String>): TrajectoryAnalyzer = TrajectoryAnalyzer(
            hailstones = input.map(Hailstone::create)
        )
    }

    /**
     * Returns [Hailstone]s as distinct [Pair] combinations.
     *
     * If there are `n` Hailstones, then a total of `n*(n-1)/2` distinct Hailstone pairs are returned.
     */
    private val distinctHailstonePairs: Collection<Pair<Hailstone, Hailstone>>
        get() = hailstones.distinctPairs()

    /**
     * [Solution for Part-1]
     *
     * Returns the total number of Hailstones' future intersections that can occur within the [testArea].
     *
     * @param testArea A [ClosedFloatingPointRange] depicting the area where we need to
     * search for possible Hailstones' future intersections.
     */
    fun getCountOfIntersectionsWithinTestArea(testArea: ClosedFloatingPointRange<Double>) =
        distinctHailstonePairs.mapNotNull { (currentHailstone: Hailstone, nextHailstone: Hailstone) ->
            // Find intersection of Hailstones
            currentHailstone.intersect(nextHailstone)
        }.count { intersection: Intersection ->
            // Filter and count those whose intersection is within the area we are checking
            intersection.x in testArea && intersection.y in testArea
        }

    /**
     * [Solution for Part-2]
     *
     * Returns the sum of X, Y, Z-coordinates of the initial position of the Rock, where it needs to be for
     * perfectly colliding with every hailstone.
     */
    fun getCoordinateSumOfTheRockPosition(): Long = sequence {
        // Change in velocities for X and Y planes of the Hailstones
        // will be from negative to positive range of the number of Hailstones present
        val deltaVelocityRange = -hailstones.size..hailstones.size

        while (true) {
            // Solving for rock coordinates requires any three Hailstones data.
            // If selected three Hailstones did not yield the result needed, then
            // we repeat the same process till we find one.
            // Three Hailstones because we are evaluating for three unknown coordinates, derived from two intersections
            // with the first Hailstone from this selected list
            val randomlyPickedHailstones = hailstones.shuffled().take(3)

            // Emit all results
            yield(
                deltaVelocityRange.firstNotNullOfOrNull { deltaVelocityX ->
                    deltaVelocityRange.firstNotNullOfOrNull { deltaVelocityY ->
                        // Change X and Y velocities of all the selected Hailstones by their deltas.
                        // This needs to be done, since hailstones would be moving at some new relative velocity
                        // to the rock being thrown. These deltas are relative velocities and
                        // negative values of the same happens to be the velocity with which the rock needs to be thrown
                        // for colliding with all Hailstones. Hence, we are nudging their velocities till we
                        // find the proper X and Y velocity deltas that would yield intersections / collisions
                        // having the same X and Y positions
                        randomlyPickedHailstones.map { hailstone ->
                            hailstone.nudgeXYVelocity(
                                deltaVelocityX,
                                deltaVelocityY
                            )
                        }.let { velocityNudgedHailstones: List<Hailstone> ->
                            // Perform intersections of all other Hailstones with the first Hailstone
                            // from the selected list
                            velocityNudgedHailstones.drop(1).map { hailstone ->
                                hailstone.intersect(velocityNudgedHailstones[0])
                            }
                        }.takeIf { intersections: List<Intersection?> ->
                            // Select if the intersections data are valid (not null) and are having
                            // the same X and Y positions. Returns null otherwise.
                            intersections.none { it == null }
                                    && intersections.all { it!!.x == intersections[0]!!.x }
                                    && intersections.all { it!!.y == intersections[0]!!.y }
                        }
                    }
                }.takeUnless { it == null }?.filterNotNull()?.let { desiredIntersections: List<Intersection> ->
                    listOf(
                        desiredIntersections[0].x, // X intersect rock position
                        desiredIntersections[0].y, // Y intersect rock position
                        // Obtain Z intersect rock position from second Hailstone
                        randomlyPickedHailstones[1].recomputeZ(
                            // Compute the change in Z velocity using the intersection data of
                            // first Hailstone with the second (desiredIntersections[0]) and
                            // the same first Hailstone with the third (desiredIntersections[1])
                            deltaVelocityZ = desiredIntersections[0].findDeltaVelocityZ(
                                desiredIntersections[1],
                                randomlyPickedHailstones[1], // Second Hailstone
                                randomlyPickedHailstones[2] // Third Hailstone
                            ),
                            // Time required will be given by the intersection of
                            // first Hailstone with the second, which is the first intersection's time
                            timeElapsed = desiredIntersections[0].time
                        )
                    ).sum().roundToLong() // Take the sum of the rock coordinates and round it off to the nearest Long
                }
            )
        }

    }.filterNotNull().first() // return the first non-null value

}