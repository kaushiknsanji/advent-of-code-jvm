/**
 * Problem: Day20: Pulse Propagation
 * https://adventofcode.com/2023/day/20
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import extensions.lcm

private class Day20 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSamplePart1Type1()    // 32000000
    println("=====")
    solveSamplePart1Type2()    // 11687500
    println("=====")
    solveActual(1)      // 879834312
    println("=====")
    solveActual(2)      // 243037165713371
    println("=====")
}

private fun solveSamplePart1Type1(executeProblemPart: Int = 1) {
    execute(Day20.getSampleFile("_part1_1").readLines(), executeProblemPart)
}

private fun solveSamplePart1Type2(executeProblemPart: Int = 1) {
    execute(Day20.getSampleFile("_part1_2").readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day20.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    PulsePropagator.parse(input)
        .getProductOfTotalLowAndHighPulses(1000)
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    PulsePropagator.parse(input)
        .getButtonTriggerCount("rx", PulseType.LOW)
        .also { println(it) }
}

/**
 * Enum class for Pulse being sent by [Communication Modules][CommunicationModule].
 */
private enum class PulseType {
    LOW, HIGH
}

/**
 * Class for a Pulse of type [pulseType] being sent by [sender] Communication Module
 * to [receiver] Communication Module.
 */
private class Pulse(
    val sender: CommunicationModule,
    val receiver: CommunicationModule,
    val pulseType: PulseType
) {
    override fun toString(): String = "${sender.name} -$pulseType-> ${receiver.name}"
}

/**
 * Sealed class of Communication Modules.
 *
 * @property name [String] containing the Name of the Communication Module.
 */
private sealed class CommunicationModule(val name: String) {

    companion object {
        const val BUTTON = "button"
        const val BROADCASTER = "broadcaster"
        private const val PREFIX_FLIP_FLOP = "%"
        private const val PREFIX_CONJUNCTION = "&"

        // Shared Queue for message/pulse relay updated by all communication modules
        // and processed by Button Module only
        private val relayQueue = ArrayDeque<Pulse>()

        // Shared Counter for Low Pulses sent
        private var totalLowPulseSent: Int = 0

        // Shared Counter for High Pulses sent
        private var totalHighPulseSent: Int = 0

        fun create(name: String): CommunicationModule = if (name.startsWith(PREFIX_FLIP_FLOP)) {
            FlipFlop(name.substringAfter(PREFIX_FLIP_FLOP))
        } else if (name.startsWith(PREFIX_CONJUNCTION)) {
            Conjunction(name.substringAfter(PREFIX_CONJUNCTION))
        } else if (name == BROADCASTER) {
            Broadcaster(name)
        } else if (name == BUTTON) {
            Button(name)
        } else {
            Terminal(name)
        }

        /**
         * Static function to increment pulse counters based on [pulseType].
         */
        fun incrementPulseSent(pulseType: PulseType) {
            when (pulseType) {
                PulseType.LOW -> totalLowPulseSent++
                PulseType.HIGH -> totalHighPulseSent++
            }
        }
    }

    // List of Connecting Output Modules of the current or `this` Module
    protected val outputModuleList: MutableList<CommunicationModule> = mutableListOf()

    /**
     * Function to add Connecting Output Modules for `this` Communication Module.
     */
    open fun addOutputModule(communicationModule: CommunicationModule) {
        outputModuleList.add(communicationModule)
    }

    /**
     * Sends Pulse of [pulseType] from `this` Communication Module to [nextModule], and then increments
     * the pulse counter.
     */
    protected fun sendAndIncrementPulse(nextModule: CommunicationModule, pulseType: PulseType) {
        // Update Queue with this Pulse information
        relayQueue.add(
            Pulse(this, nextModule, pulseType)
        )
        // Increment appropriate pulse counter
        incrementPulseSent(pulseType)
    }

    /**
     * Abstract function to be implemented subclasses of [CommunicationModule] to receive
     * and process the [pulse] information.
     */
    abstract fun processPulse(pulse: Pulse)

    override fun toString(): String = "${this::class.simpleName} '${this.name}'"

    /**
     * [CommunicationModule] subclass for Flip-Flops.
     *
     * 1. Maintains state of Flip-Flop as in whether it is `ON` or `OFF`. Initial state is `OFF`.
     * 2. Responds to [PulseType.LOW] pulses only.
     * 3. Sends a [PulseType.HIGH] when turned `ON`; else sends a [PulseType.LOW] when turned `OFF`.
     *
     * @param name [String] containing the Name of this Flip Flop Module.
     */
    class FlipFlop(name: String) : CommunicationModule(name) {
        /**
         * Saves state of the [FlipFlop]. Start state is `false` denoting `OFF`.
         */
        var state: Boolean = false
            private set

        /**
         * Toggles current [state] of [FlipFlop].
         */
        private fun toggleState() {
            state = !state
        }

        /**
         * Receives and processes [pulse] information.
         */
        override fun processPulse(pulse: Pulse) {
            // Send Pulse to connecting Modules only when received Pulse is a Low Pulse
            if (pulse.pulseType == PulseType.LOW) {
                // Toggle current state and then send
                toggleState()

                outputModuleList.forEach { nextModule ->
                    sendAndIncrementPulse(
                        nextModule,
                        if (state) {
                            // Send HIGH when turned ON
                            PulseType.HIGH
                        } else {
                            // Send LOW when turned OFF
                            PulseType.LOW
                        }
                    )
                }
            }
        }

    }

    /**
     * [CommunicationModule] subclass for Conjunctions.
     *
     * 1. Maintains input state for each of its Input [CommunicationModule]. Initial state for each
     * Input [CommunicationModule] is [PulseType.LOW].
     * 2. When it receives a [Pulse], it first updates the input state of the corresponding module and
     * sends out a [PulseType.LOW] when all of its input state is at [PulseType.HIGH]; otherwise sends a [PulseType.HIGH].
     * 3. Basically, it acts like an Inverter which inverts when all of its input state is at [PulseType.HIGH].
     *
     * @param name [String] containing the Name of this Conjunction Module.
     */
    class Conjunction(name: String) : CommunicationModule(name) {
        // Map for last received input state
        private val lastReceivedPulseMap: MutableMap<CommunicationModule, PulseType> = mutableMapOf()

        /**
         * Returns `true` when all of its input state is at [PulseType.HIGH]; otherwise `false`.
         */
        private fun isInputAllHighPulse(): Boolean =
            lastReceivedPulseMap.all { (_: CommunicationModule, receivedPulse: PulseType) ->
                receivedPulse == PulseType.HIGH
            }

        /**
         * Adds [input module][inputModule] with an initial input state of [PulseType.LOW]
         */
        fun addInputModule(inputModule: CommunicationModule) {
            lastReceivedPulseMap[inputModule] = PulseType.LOW
        }

        /**
         * Returns [List] of connecting output [CommunicationModule]s.
         */
        fun getOutputModules(): List<CommunicationModule> = outputModuleList

        /**
         * Receives and processes [pulse] information.
         */
        override fun processPulse(pulse: Pulse) {
            // Save received input
            lastReceivedPulseMap[pulse.sender] = pulse.pulseType

            // Send Pulse to connecting Modules
            outputModuleList.forEach { nextModule ->
                sendAndIncrementPulse(
                    nextModule,
                    if (isInputAllHighPulse()) {
                        PulseType.LOW
                    } else {
                        PulseType.HIGH
                    }
                )
            }
        }

    }

    /**
     * [CommunicationModule] subclass for Broadcaster.
     *
     * Relays the same [PulseType] sent by [Button] module to all its connecting [output modules][outputModuleList].
     *
     * @param name [String] containing the Name of this Broadcaster Module. Typically, this will be [BROADCASTER].
     */
    class Broadcaster(name: String) : CommunicationModule(name) {

        /**
         * Receives and processes [pulse] information.
         */
        override fun processPulse(pulse: Pulse) {
            // Send Pulse to connecting Modules
            outputModuleList.forEach { nextModule ->
                sendAndIncrementPulse(nextModule, pulse.pulseType)
            }
        }

    }

    /**
     * [CommunicationModule] subclass for Button.
     *
     * 1. Communicates only to the [Broadcaster] with a [PulseType.LOW] and begins the relay.
     * 2. There will always be only one [Broadcaster].
     * 3. Additionally, takes care of triggering the processing of input [Pulse] of various
     * receiving [CommunicationModule]s from the [relayQueue], and also provides
     * read access and reset action to pulse counters.
     *
     * @param name [String] containing the Name of this Button Module. Typically, this will be [BUTTON].
     */
    class Button(name: String) : CommunicationModule(name) {

        override fun addOutputModule(communicationModule: CommunicationModule) {
            require(communicationModule::class == Broadcaster::class) {
                "Wrong Module ${communicationModule::class} found. " +
                        "${Button::class} Module only communicates to ${Broadcaster::class} Module"
            }
            super.addOutputModule(communicationModule)
        }

        /**
         * Receives and processes [pulse] information.
         */
        override fun processPulse(pulse: Pulse) {
            // Send Pulse to Broadcaster
            sendAndIncrementPulse(outputModuleList.single(), pulse.pulseType)
        }

        /**
         * Returns total count of Low Pulses sent.
         */
        fun getTotalLowPulseSent(): Int = totalLowPulseSent

        /**
         * Returns total count of High Pulses sent.
         */
        fun getTotalHighPulseSent(): Int = totalHighPulseSent

        /**
         * Clears pulse counters by resetting them to 0.
         */
        fun clearPulseStats() {
            totalLowPulseSent = 0
            totalHighPulseSent = 0
        }

        /**
         * Starts the process of sending pulse to [CommunicationModule]s.
         *
         * @param lookupModuleInputMap [Map] of receiver [CommunicationModule] with an expected [PulseType] message,
         * that needs to be looked up while processing the [relayQueue]. Default is [emptyMap].
         * @param notifyOnFound When a [CommunicationModule] with an expected [PulseType] message mentioned in
         * the [lookupModuleInputMap] is received, this Lambda gets invoked with that [CommunicationModule].
         * Default Lambda does nothing.
         */
        fun push(
            lookupModuleInputMap: Map<CommunicationModule, PulseType> = emptyMap(),
            notifyOnFound: (module: CommunicationModule) -> Unit = {}
        ) {
            // Send Dummy Low Pulse to self start the relay
            processPulse(Pulse(this, this, PulseType.LOW))
            // Process relay queue for all modules' messages
            processRelayQueue(lookupModuleInputMap, notifyOnFound)
        }

        /**
         * Processes [relayQueue] messages to invoke [CommunicationModule.processPulse] of respective
         * receiver [CommunicationModule] in order to process its input [Pulse], until the [relayQueue] becomes empty.
         *
         * @param lookupModuleInputMap [Map] of receiver [CommunicationModule] with an expected [PulseType] message,
         * that needs to be looked up while processing the [relayQueue].
         * @param notifyOnFound When a [CommunicationModule] with an expected [PulseType] message mentioned in
         * the [lookupModuleInputMap] is received, this provided Lambda gets invoked with that [CommunicationModule].
         */
        private fun processRelayQueue(
            lookupModuleInputMap: Map<CommunicationModule, PulseType>,
            notifyOnFound: (module: CommunicationModule) -> Unit
        ) {
            // Process till Relay Queue is empty
            while (relayQueue.isNotEmpty()) {
                // Remove Pulse from first (since this is a Queue) and process
                with(relayQueue.removeFirst()) {
                    // For every Pulse received
                    if (lookupModuleInputMap.isNotEmpty()
                        && lookupModuleInputMap.getOrDefault(receiver, null) == pulseType
                    ) {
                        // If the Pulse received is of the expected Pulse Type for the receiving communication module,
                        // then notify the same by invoking the provided lambda with the receiving communication module.
                        notifyOnFound(receiver)
                    }
                    // Tell the receiver to process its input Pulse
                    receiver.processPulse(this)
                }
            }
        }
    }

    /**
     * Terminal [CommunicationModule] subclass for any other unknown Modules that have no defined function.
     *
     * @param name [String] containing the Name of this Terminal Module.
     */
    class Terminal(name: String) : CommunicationModule(name) {

        override fun processPulse(pulse: Pulse) {
            //NO-OP
        }

    }

}

private class PulsePropagator private constructor(
    private val modulesMap: Map<String, CommunicationModule>
) {
    companion object {
        private const val ARROW = "->"
        private const val COMMA = ","

        fun parse(input: List<String>): PulsePropagator =
            input.map { line ->
                line.split(ARROW).also { splitStrings ->
                    require(splitStrings.size == 2) {
                        "Input seems to contain more than one '$ARROW' on each line"
                    }
                }
            }.map { splitStrings ->
                splitStrings[0].trim() to splitStrings[1].split(COMMA).map(String::trim)
            }.let { moduleToModulesList: List<Pair<String, List<String>>> ->
                // First, create a Map of CommunicationModule instance to the list of Names of Modules it connects to
                val moduleToModulesMap =
                    moduleToModulesList.associate { moduleToModulesPair: Pair<String, List<String>> ->
                        CommunicationModule.create(moduleToModulesPair.first) to moduleToModulesPair.second
                    }

                // From `moduleToModulesMap`, create another Map of Module Name to its CommunicationModule instance
                val modulesMap: MutableMap<String, CommunicationModule> =
                    moduleToModulesMap.keys.associateBy { communicationModule ->
                        communicationModule.name
                    }.toMutableMap().apply {
                        // Create and include Button Module
                        this[CommunicationModule.BUTTON] =
                            CommunicationModule.create(CommunicationModule.BUTTON).also { buttonModule ->
                                // Connect Button to Broadcaster
                                buttonModule.addOutputModule(this[CommunicationModule.BROADCASTER]!!)
                            }
                    }

                // From `moduleToModulesMap`, complete the connections of key Module instances to the instances
                // of Modules it connects to
                moduleToModulesMap.forEach { (keyModule: CommunicationModule, connectingModuleNamesList: List<String>) ->
                    connectingModuleNamesList.forEach { connectingModuleName: String ->
                        // For each key Module, add each of its Connecting Module as its Output Module
                        keyModule.addOutputModule(
                            modulesMap.getOrPut(connectingModuleName) {
                                // Create and save Module if not already present in `modulesMap`
                                CommunicationModule.create(connectingModuleName)
                            }.also { connectingModule: CommunicationModule ->
                                // If the Connecting Module is a Conjunction, then add current key Module
                                // as its Input Module
                                if (connectingModule is CommunicationModule.Conjunction) {
                                    connectingModule.addInputModule(keyModule)
                                }
                            }
                        )
                    }
                }

                // With `modulesMap`, create and return the `PulsePropagator` instance
                PulsePropagator(modulesMap)
            }
    }

    // List of Flip-Flop Modules
    private val flipFlopModules: List<CommunicationModule.FlipFlop> =
        modulesMap.values.filterIsInstance<CommunicationModule.FlipFlop>()

    // List of Conjunction Modules
    private val conjunctionModules: List<CommunicationModule.Conjunction> =
        modulesMap.values.filterIsInstance<CommunicationModule.Conjunction>()

    // Button Module
    private val buttonModule: CommunicationModule.Button =
        modulesMap.values.filterIsInstance<CommunicationModule.Button>().single()

    /**
     * Returns `true` when the [CommunicationModule.FlipFlop.state] of all [flipFlopModules]
     * are `OFF`; otherwise `false`.
     */
    private fun areFlipFlopsOff(): Boolean = flipFlopModules.none { it.state }

    /**
     * Returns a [List] of [Pair] of total [PulseType.LOW] and [PulseType.HIGH] pulses sent
     * for each [CommunicationModule.Button.push] action. [CommunicationModule.Button] will be pushed for
     * the given [buttonTriggerCount] times. During this time, if all [flipFlopModules] turn `OFF` again, then it is
     * said to have completed a cycle. So, this method returns at the earliest of [buttonTriggerCount] elapsing or
     * a cycle completing when all [flipFlopModules] turn `OFF` again.
     */
    private fun getSingleCyclePulseStats(buttonTriggerCount: Int): List<Pair<Int, Int>> = buildList {
        var triggerCount = buttonTriggerCount
        // Repeat till `triggerCount` elapses or when a cycle completes, that is, when all flip-flops turn OFF again
        do {
            triggerCount--
            // Push the Button
            buttonModule.push()
            // Update resulting list with a Pair of current total of Low and High Pulses sent
            add(buttonModule.getTotalLowPulseSent() to buttonModule.getTotalHighPulseSent())
            // Reset Pulse counters
            buttonModule.clearPulseStats()
        } while (triggerCount > 0 && !areFlipFlopsOff())
    }

    /**
     * Triggers [CommunicationModule.Button] until all the key [CommunicationModule]s given in [moduleInputMap]
     * is met with their required input [PulseType] as given by the key's value in [moduleInputMap]. Corresponding
     * trigger counts for each first occurrence is returned as a [List] of [Long] values.
     */
    private fun getButtonTriggerCounts(
        moduleInputMap: MutableMap<CommunicationModule, PulseType>
    ): List<Long> = buildList {
        // Initialize button trigger count
        var triggerCount = 0L
        // Repeat till all Modules are found with their required input pulse type
        while (moduleInputMap.isNotEmpty()) {
            // Increment button trigger
            triggerCount++
            // Push the Button passing in the remaining Modules to be looked up for their required input
            buttonModule.push(moduleInputMap) { communicationModule ->
                // When a Module is found with the desired input, remove it from the map
                // and add the current button trigger count to the resulting list
                moduleInputMap.remove(communicationModule)
                add(triggerCount)
            }
        }
    }

    /**
     * [Solution for Part-1]
     *
     * Returns the product of the total number of [PulseType.LOW] pulses sent and the
     * total number of [PulseType.HIGH] pulses sent for the [buttonModule] being
     * [pushed][CommunicationModule.Button.push] for [buttonTriggerCount] number of times.
     */
    fun getProductOfTotalLowAndHighPulses(buttonTriggerCount: Int): Int =
        getSingleCyclePulseStats(buttonTriggerCount).let { singleCyclePulseStats: List<Pair<Int, Int>> ->
            // With Pulse stats from either the entire single cycle or the entirety of button
            // being pushed for `buttonTriggerCount` number of times

            // Compute the number of times the button is being pushed in a single cycle
            val triggerCountByCycleSize = buttonTriggerCount / singleCyclePulseStats.size

            // Using `triggerCountByCycleSize`, compute the total Low Pulses sent
            val totalCycleLowPulseCount =
                singleCyclePulseStats.sumOf { it.first } * triggerCountByCycleSize

            // Using `triggerCountByCycleSize`, compute the total High Pulses sent
            val totalCycleHighPulseCount =
                singleCyclePulseStats.sumOf { it.second } * triggerCountByCycleSize

            // Return the product of the total Low and High Pulses sent
            totalCycleLowPulseCount * totalCycleHighPulseCount
        }

    /**
     * [Solution for Part-2]
     *
     * Returns a [Long] representing the least number of Button [pushes][CommunicationModule.Button.push]
     * required to deliver a Pulse of [requiredInputPulseType] to the [CommunicationModule] named [moduleName].
     */
    fun getButtonTriggerCount(moduleName: String, requiredInputPulseType: PulseType): Long =
        buildMap {
            // Get the Map of Conjunctions having single Output Module
            val conjunctionToSingleOutputModuleMap: Map<CommunicationModule.Conjunction, CommunicationModule> =
                conjunctionModules.filter { conjunction ->
                    conjunction.getOutputModules().size == 1
                }.associateWith { conjunction ->
                    conjunction.getOutputModules().single()
                }

            // Save current stage's required input pulse
            var currentStageInput = requiredInputPulseType

            // Load [this] resulting Map with given Module and required input pulse
            put(modulesMap[moduleName]!!, currentStageInput)

            /**
             * Lambda that returns `true` only when all the Key Modules
             * of `this` resulting Map have input modules with single output module.
             */
            val haveSingleOutputConjunctionSources: () -> Boolean = {
                keys.all { communicationModule ->
                    communicationModule in conjunctionToSingleOutputModuleMap.values
                }
            }

            /**
             * Lambda that finds and returns a [Set] of [Source Module][CommunicationModule]
             * for the given `outputModule`.
             */
            val findSources: (outputModule: CommunicationModule) -> Set<CommunicationModule> = { outputModule ->
                conjunctionToSingleOutputModuleMap.filterValues { outputCommunicationModule ->
                    outputCommunicationModule == outputModule
                }.keys
            }

            /**
             * Lambda that toggles the given `input` [PulseType] and returns the resulting [PulseType].
             */
            val toggleInput: (input: PulseType) -> PulseType = { input: PulseType ->
                if (input == PulseType.HIGH) {
                    PulseType.LOW
                } else {
                    PulseType.HIGH
                }
            }

            // Repeat while all the Key Modules of the resulting Map have input modules with single output module
            while (haveSingleOutputConjunctionSources()) {
                // Toggle current stage input for the previous stage we are about to find as each stage
                // is a stage of Conjunctions that act like inverters
                currentStageInput = toggleInput(currentStageInput)

                // Find source modules of current stage and make a map of it with the toggled input pulse
                keys.map(findSources)
                    .flatten()
                    .associateWith { currentStageInput }
                    .also {
                        // Clear resulting Map and load this new result
                        this.clear()
                        putAll(it)
                    }
            }
        }.toMutableMap()
            .let(::getButtonTriggerCounts) // Get trigger counts for each Key Module's first occurrence with required input pulse
            .lcm() // LCM of all trigger counts gives us the least number of button pushes required

}