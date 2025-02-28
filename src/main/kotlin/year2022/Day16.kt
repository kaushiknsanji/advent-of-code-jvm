/**
 * Problem: Day16: Proboscidea Volcanium
 * https://adventofcode.com/2022/day/16
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2022

import base.BaseProblemHandler
import extensions.generateCombinations
import utils.Constants.COMMA_STRING
import java.util.*

class Day16 : BaseProblemHandler() {

    /**
     * Returns the Package name of this problem class
     */
    override fun getCurrentPackageName(): String = this::class.java.packageName

    /**
     * Returns the Class name of this problem class
     */
    override fun getClassName(): String = this::class.java.simpleName

    /**
     * Executes "Part-1" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart1(input: List<String>, otherArgs: Array<out Any?>): Any =
        VolcanoTunnelsAnalyzer.parse(input)
            .getMostPressureReleased(1, 30)

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        VolcanoTunnelsAnalyzer.parse(input)
            .getMostPressureReleased(2, 26)

    /**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        solveSample(1, false, 0, 1651)
        solveActual(1, false, 0, 2077)
        solveSample(2, false, 0, 1707)
        solveActual(2, false, 0, 2741)
    }

}

fun main() {
    Day16().start()
}

/**
 * Class for Pressure-release [Valve]s.
 *
 * @property name [String] name of the [Valve]
 * @property flowRate [Int] value of the pressure per minute when [Valve] is opened
 */
private class Valve(
    val name: String,
    val flowRate: Int
) {

    // List of Valves connecting to this Valve along the tunnels
    private val tunnelsToValves: MutableList<Valve> = mutableListOf()

    /**
     * Updates [tunnelsToValves] with given [List of connecting Valves][valves]
     */
    fun updateConnectingValves(valves: List<Valve>) {
        tunnelsToValves.addAll(valves)
    }

    /**
     * Returns [List] of connecting [Valve]s
     */
    fun getConnectingValves(): List<Valve> = tunnelsToValves

}

/**
 * Class to parse the input, analyze and solve the problem at hand.
 *
 * @property startValve The Starting [Valve]
 * @property valves [Set] of all [Valve]s
 */
private class VolcanoTunnelsAnalyzer private constructor(
    private val startValve: Valve,
    private val valves: Set<Valve>
) {

    companion object {
        private const val START_VALVE_NAME = "AA"

        private val valveDataLineRegex =
            """Valve ([A-Z]{2}) has flow rate=(\d+); (?:tunnel|tunnels) (?:lead|leads) to (?:valve|valves) ((?:[A-Z]{2},?\s?)+)""".toRegex()

        fun parse(input: List<String>): VolcanoTunnelsAnalyzer = input.associate { line ->
            valveDataLineRegex.matchEntire(line)!!.groupValues.drop(1).let { groupValues: List<String> ->
                Valve(groupValues[0], groupValues[1].toInt()) to groupValues[2]
            }
        }.let { valveDataMap: Map<Valve, String> ->
            valveDataMap.keys.forEach { valve: Valve ->
                valveDataMap[valve]!!.split(COMMA_STRING).map { nextValveName: String ->
                    valveDataMap.keys.first { valve: Valve -> valve.name == nextValveName.trim() }
                }.let(valve::updateConnectingValves)
            }

            VolcanoTunnelsAnalyzer(
                startValve = valveDataMap.keys.first { it.name == START_VALVE_NAME },
                valves = valveDataMap.keys
            )
        }
    }

    /**
     * Nested Data class for [Agent]'s [Worker] whose work is to visit connecting [Valve]s from the current [valve]
     * and open them to release pressure.
     *
     * @property id [Int] identifier of the [Worker]
     * @property valve Current [Valve]
     * @property timeRemaining [Int] value of the countdown Timer
     * @property valvesDistanceMap [Map] of [Pair] of [Valve]s to their shortest [Int] distance between them
     * @property pressureReleased [Int] value of the pressure released so far. Starts with 0.
     * @property openedValves [List] of [Valve]s with flow that are opened so far. Starts as an [emptyList].
     * @constructor Creates a [Worker]
     */
    private data class Worker(
        val id: Int,
        val valve: Valve,
        val timeRemaining: Int,
        private val valvesDistanceMap: MutableMap<Pair<Valve, Valve>, Int>,
        val pressureReleased: Int = 0,
        val openedValves: List<Valve> = emptyList()
    ) {

        // Total Flow Rate of all Valves opened so far by this Worker
        private val releasedFlowRate: Int by lazy {
            openedValves.sumOf(Valve::flowRate)
        }

        // Total Pressure that will be released when time elapses based on the current pressure released so far
        // and the total flow rate of all Valves opened
        val projectedPressureReleased: Int by lazy {
            pressureReleased + timeRemaining * releasedFlowRate
        }

        /**
         * Returns shortest distance or time equivalent required to reach [toValve] from [fromValve].
         *
         * Shortest distance is obtained by following [Dijkstra](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm)
         * algorithm using [PriorityQueue] which prioritizes on the distance accumulated so far.
         */
        private fun getShortestDistance(
            fromValve: Valve,
            toValve: Valve
        ): Int =
            valvesDistanceMap.getOrPut(fromValve to toValve) {
                // Return distance from the Map if present, else compute, save and return

                // A PriorityQueue based Frontier that prioritizes on the distance accumulated so far, for minimizing
                // the time taken to reach `toValve`
                val frontier = PriorityQueue<Pair<Valve, Int>>(
                    compareBy { it.second }
                ).apply {
                    // Begin with `fromValve` at a distance of 0
                    add(fromValve to 0)
                }

                // Map of Valve reached to distance taken
                val distanceMap: MutableMap<Valve, Int> = mutableMapOf(fromValve to 0)

                // Set of Valves visited so far
                val visitedSet: MutableSet<Valve> = mutableSetOf(fromValve)

                // Repeat till the PriorityQueue based Frontier becomes empty
                while (frontier.isNotEmpty()) {
                    // Get the Top Valve-Distance pair
                    val current = frontier.poll()

                    // Bail out when `toValve` is reached
                    if (current.first == toValve) break

                    // Retrieve Next Valves
                    current.first.getConnectingValves()
                        .filterNot { nextValve ->
                            // Exclude Next Valve when it is not `toValve` and is already visited
                            nextValve != toValve && nextValve in visitedSet
                        }.forEach { nextValve ->
                            // New distance to Next Valve from current
                            val newDistance = current.second + 1

                            // Mark this Next Valve as visited
                            visitedSet.add(nextValve)

                            // When distance to Next Valve is the best so far, update distance map for this Next Valve,
                            // and add this Next Valve-Distance pair to the Frontier for further processing
                            if (newDistance < distanceMap.getOrDefault(nextValve, Int.MAX_VALUE)) {
                                distanceMap[nextValve] = newDistance
                                frontier.add(nextValve to newDistance)
                            }
                        }
                }

                // Save and return the shortest distance required to reach `toValve` from `fromValve`
                distanceMap[toValve]!!
            }

        /**
         * Returns next [List] of [Worker]s to open remaining valves.
         *
         * @param valvesToOpen [List] of flow [Valve]s remaining to be opened
         */
        fun getNextWorkers(valvesToOpen: List<Valve>): List<Worker> =
            valvesToOpen.mapNotNull { nextValve ->
                // For every Valve that needs to be opened

                // Get the distance or time equivalent required to reach this Next Valve
                val distanceToNextValve = getShortestDistance(valve, nextValve)

                timeRemaining.takeIf { it >= distanceToNextValve + 1 }?.let {
                    // When there is enough time to reach this Next Valve and also to open,
                    // then construct a new Worker for this Next Valve with new time remaining,
                    // updated pressure released so far and updated list of valves opened
                    copy(
                        valve = nextValve,
                        timeRemaining = timeRemaining - distanceToNextValve - 1,
                        pressureReleased = pressureReleased + (distanceToNextValve + 1) * releasedFlowRate,
                        openedValves = openedValves + nextValve
                    )
                }

            }.ifEmpty {
                // When all flow Valves are opened or time has elapsed

                // Construct a new Worker for the same current valve, with 0 minutes of time remaining
                // and `projectedPressureReleased` as pressure released
                listOf(
                    copy(
                        timeRemaining = 0,
                        pressureReleased = projectedPressureReleased
                    )
                )
            }

    }

    /**
     * Nested Data class for managing [Worker]s.
     *
     * @property workers [List] of [Worker]s being managed by the [Agent]
     * @property valvesWithFlow [List] of [Valve]s that have a [Valve.flowRate] greater than 0
     * @constructor Creates an [Agent] for managing the given [workers].
     */
    private data class Agent(
        val workers: List<Worker>,
        private val valvesWithFlow: List<Valve>
    ) {

        /**
         * Creates an [Agent] with requested [number of workers][workerCount].
         *
         * @param workerCount [Int] count of workers needed
         * @param startValve [Valve] to start from
         * @param startTime [Int] value of countdown start time
         * @param valvesWithFlow [List] of [Valve]s that have a [Valve.flowRate] greater than 0
         * @param valvesDistanceMap [Map] of [Pair] of [Valve]s to their shortest [Int] distance between them.
         * Starts empty and is being shared across current and future [Worker]s.
         */
        constructor(
            workerCount: Int,
            startValve: Valve,
            startTime: Int,
            valvesWithFlow: List<Valve>,
            valvesDistanceMap: MutableMap<Pair<Valve, Valve>, Int> = mutableMapOf()
        ) : this(
            workers = (0 until workerCount).map { index ->
                Worker(index, startValve, startTime, valvesDistanceMap)
            },
            valvesWithFlow
        )

        // Maximum time remaining across Workers
        val timeRemaining: Int by lazy {
            workers.maxOf(Worker::timeRemaining)
        }

        // Total Pressure released by all Workers
        val totalPressureReleased: Int by lazy {
            workers.sumOf(Worker::pressureReleased)
        }

        // List of Valves opened by all Workers
        private val allOpenedValves: List<Valve> by lazy {
            workers.flatMap(Worker::openedValves)
        }

        // List of Flow Valves to be Opened
        val valvesToOpen: List<Valve> by lazy {
            valvesWithFlow.filterNot { valve -> valve in allOpenedValves }
        }

        // Future Potential Pressure Release based on Flow Valves yet to be opened and the remaining time
        private val futurePotentialPressureRelease: Int by lazy {
            timeRemaining * valvesToOpen.sumOf(Valve::flowRate)
        }

        /**
         * Returns Prioritization value required by [PriorityQueue], by dynamically
         * adjusting based on [futurePotentialPressureRelease], for maximizing the amount of pressure that can be
         * released within the given time.
         *
         * When [futurePotentialPressureRelease] is greater than the current [totalPressureReleased], then it
         * prioritizes on [timeRemaining] as there is enough time to try and open as many flow valves; but when
         * [futurePotentialPressureRelease] becomes lesser than the current [totalPressureReleased], then it
         * prioritizes on [totalPressureReleased] instead of [timeRemaining] as there is less time to try and open
         * remaining valves.
         */
        fun getPriority(): Int = if (futurePotentialPressureRelease > totalPressureReleased) {
            timeRemaining
        } else {
            totalPressureReleased
        }

        /**
         * Returns next [List] of [Agent]s with new [Worker]s
         */
        fun getNextAgents(): List<Agent> =
            workers.flatMap { worker ->
                // Get Next Workers to open remaining valves
                worker.getNextWorkers(valvesToOpen)
            }.groupBy { worker ->
                // Group Next Workers by their Identifier in order to combine them uniquely
                // based on their Identifier and the Valve they are at
                worker.id
            }.values.generateCombinations { combination: List<Worker>, item: Worker ->
                // Combine `item` worker when the Valve it is at, is unique to the `combination`
                item.valve !in combination.map(Worker::valve)
            }.map { workersCombination: List<Worker> ->
                // Construct a new Agent with this workers combination
                copy(
                    workers = workersCombination
                )
            }

    }

    /**
     * [Solution for Part 1 and 2]
     *
     * Returns the Most pressure that can be released within the given [time][startTime] using
     * given [number of workers][workerCount].
     *
     * Most pressure that can be released is determined by following
     * [Dijkstra](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm) algorithm using [PriorityQueue].
     */
    fun getMostPressureReleased(workerCount: Int, startTime: Int): Int {
        // A PriorityQueue based Frontier that prioritizes dynamically based on future potential pressure release,
        // for maximizing the amount of pressure that can be released within the given time
        val frontier = PriorityQueue<Agent>(
            compareByDescending { it.getPriority() }
        ).apply {
            // Begin with an Agent having the required number of workers
            add(
                Agent(
                    workerCount,
                    startValve,
                    startTime,
                    valvesWithFlow = valves.filter { valve -> valve.flowRate > 0 }
                )
            )
        }

        // Map of Time remaining to Pressure released for each Worker. Start with `startTime` to 0 pressure released
        // for each Worker
        val timePressureMaps: List<MutableMap<Int, Int>> = List(workerCount) { mutableMapOf(startTime to 0) }

        // Lambda to decide if this Agent needs to be picked for further processing
        val pickCandidate: (timeRemaining: Int, totalPressureReleased: Int, workers: List<Worker>) -> Boolean =
            { timeRemaining, totalPressureReleased, workers ->

                if (timeRemaining > 0) {
                    // When there is still some time left for the current Agent

                    // Test if any Worker's pressure released is equal to or better than the best so far
                    val currentPressureTest = workers.any { worker ->
                        worker.pressureReleased >= timePressureMaps[worker.id].getOrDefault(
                            timeRemaining,
                            Int.MIN_VALUE
                        )
                    }

                    // Test if any Worker's projected pressure released is equal to or better than the best so far
                    val projectedPressureTest = workers.any { worker ->
                        worker.projectedPressureReleased >= timePressureMaps[worker.id].getOrDefault(
                            0,
                            Int.MIN_VALUE
                        )
                    }

                    // Pick this Agent if any of the above test result is true
                    currentPressureTest || projectedPressureTest
                } else {
                    // When time has elapsed for the current Agent

                    // Pick this Agent if the Total pressure released by all of its Workers
                    // is equal to or better than the best so far
                    totalPressureReleased >= timePressureMaps.sumOf {
                        it.getOrDefault(timeRemaining, Int.MIN_VALUE)
                    }
                }
            }

        // Lambda to update `timePressureMaps` of each Worker for the pressure released by them at the given time left
        val updateMaxPressure: (timeRemaining: Int, workers: List<Worker>) -> Unit = { timeRemaining, workers ->

            if (timeRemaining > 0) {
                // When there is still some time left for the current Agent

                // Update `timePressureMaps` for the Worker when the Worker's pressure released
                // is better than the best so far at the same remaining time
                workers.filter { worker ->
                    worker.pressureReleased > timePressureMaps[worker.id].getOrDefault(timeRemaining, Int.MIN_VALUE)
                }.forEach { worker ->
                    timePressureMaps[worker.id][timeRemaining] = worker.pressureReleased
                }
            } else {
                // When time has elapsed for the current Agent

                // Update `timePressureMaps` for all Workers with its pressure released. No need to check before
                // updating as this lambda is called only when the call to `pickCandidate` lambda returns `true`
                workers.forEach { worker ->
                    timePressureMaps[worker.id][timeRemaining] = worker.pressureReleased
                }
            }
        }

        // Repeat till the PriorityQueue based Frontier becomes empty
        while (frontier.isNotEmpty()) {
            // Get the Top Agent
            val currentAgent = frontier.poll()

            if (currentAgent.timeRemaining == 0) {
                // When time has elapsed for the current Agent

                // Update `timePressureMaps` for all Workers of the current Agent when its total pressure released
                // is equal to or better than the best so far
                if (
                    pickCandidate(
                        currentAgent.timeRemaining,
                        currentAgent.totalPressureReleased,
                        currentAgent.workers
                    )
                ) {
                    updateMaxPressure(currentAgent.timeRemaining, currentAgent.workers)
                }
            } else {
                // When there is still some time left for the current Agent

                // Retrieve Next Agents with new Workers
                currentAgent.getNextAgents().forEach { nextAgent ->
                    // Check if this Next Agent needs to be picked for further processing
                    val pickNextCandidate = pickCandidate(
                        nextAgent.timeRemaining,
                        nextAgent.totalPressureReleased,
                        nextAgent.workers
                    )

                    // If Next Agent has opened all Valves or is picked for further processing,
                    // then add it to the Frontier
                    if (nextAgent.valvesToOpen.isEmpty() || pickNextCandidate) {
                        // If picked for further processing, then update `timePressureMaps` of each Worker
                        // for the pressure released by them at the given time left
                        if (pickNextCandidate) {
                            updateMaxPressure(
                                nextAgent.timeRemaining,
                                nextAgent.workers
                            )
                        }
                        frontier.add(nextAgent)
                    }
                }
            }
        }

        // Return total pressure released by all Workers at elapsed time
        return timePressureMaps.sumOf { it.getOrDefault(0, 0) }
    }

}