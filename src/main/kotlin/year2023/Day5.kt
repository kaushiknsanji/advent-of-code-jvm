/**
 * Problem: Day5: If You Give A Seed A Fertilizer
 * https://adventofcode.com/2023/day/5
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import extensions.*

private class Day5 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 35
    println("=====")
    solveActual(1)      // 318728750
    println("=====")
    solveSample(2)      // 46
    println("=====")
    solveActual(2)      // 37384986
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day5.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day5.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    GardeningAlmanac.parse(input)
        .getClosestLocationNumber()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    GardeningAlmanac.parse(input)
        .getClosestLocationNumber(seedNumbersAreRange = true)
        .also { println(it) }
}

/**
 * Class for mapping source number/range to destination number/range.
 *
 * @property sourceRange [LongRange] of source numbers.
 * @property sourceToDestinationAdder [Long] value of the number to be added to a source number
 * to get its corresponding destination number.
 */
private class SourceDestination private constructor(
    val sourceRange: LongRange,
    val sourceToDestinationAdder: Long
) {
    companion object {
        fun create(input: String): SourceDestination =
            input.trim().split("""\s+""".toRegex()).takeIf { it.size == 3 }?.let { numberStrings ->
                numberStrings.map { it.toLong() }.let { numbers ->
                    SourceDestination(
                        sourceRange = numbers[1].createRange(numbers[2]),
                        sourceToDestinationAdder = numbers[0] - numbers[1]
                    )
                }
            } ?: throw IllegalArgumentException(
                "$input should only contain 3 numbers"
            )
    }

}

@JvmInline
private value class Seed(val number: Long)

@JvmInline
private value class Soil(val number: Long)

@JvmInline
private value class Fertilizer(val number: Long)

@JvmInline
private value class Water(val number: Long)

@JvmInline
private value class Light(val number: Long)

@JvmInline
private value class Temperature(val number: Long)

@JvmInline
private value class Humidity(val number: Long)

@JvmInline
private value class Location(val number: Long)

private interface ISeedToSoil {
    fun Seed.toSoil(): Soil

    fun Iterable<LongRange>.toSoilRange(): Iterable<LongRange>
}

private interface ISoilToFertilizer {
    fun Soil.toFertilizer(): Fertilizer

    fun Iterable<LongRange>.toFertilizerRange(): Iterable<LongRange>
}

private interface IFertilizerToWater {
    fun Fertilizer.toWater(): Water

    fun Iterable<LongRange>.toWaterRange(): Iterable<LongRange>
}

private interface IWaterToLight {
    fun Water.toLight(): Light

    fun Iterable<LongRange>.toLightRange(): Iterable<LongRange>
}

private interface ILightToTemperature {
    fun Light.toTemperature(): Temperature

    fun Iterable<LongRange>.toTemperatureRange(): Iterable<LongRange>
}

private interface ITemperatureToHumidity {
    fun Temperature.toHumidity(): Humidity

    fun Iterable<LongRange>.toHumidityRange(): Iterable<LongRange>
}

private interface IHumidityToLocation {
    fun Humidity.toLocation(): Location

    fun Iterable<LongRange>.toLocationRange(): Iterable<LongRange>
}

/**
 * Class to build source category to destination category mapping via [SourceDestination] for various
 * categories of Almanac and also to find the required destination and category for a source in its category.
 */
private class SourceDestinationRule : ISeedToSoil, ISoilToFertilizer, IFertilizerToWater, IWaterToLight,
    ILightToTemperature, ITemperatureToHumidity, IHumidityToLocation {

    // List of [SourceDestination] for the categories involved
    private val sourceDestinationRules: MutableList<SourceDestination> = mutableListOf()

    /**
     * Builds [SourceDestination] mapping for the categories.
     *
     * @param input [String] containing information to create [SourceDestination].
     */
    fun append(input: String) {
        sourceDestinationRules.add(SourceDestination.create(input))
    }

    /**
     * Converts a source number given by [this] into its destination number based on [sourceDestinationRules].
     */
    private fun Long.toDestination(): Long =
        sourceDestinationRules.singleOrNull { sourceDestination ->
            // Find if the source is mapped
            this in sourceDestination.sourceRange
        }?.let { sourceDestinationFound ->
            // If mapped, return its destination number
            this + sourceDestinationFound.sourceToDestinationAdder
        } ?: this // If not mapped, return the same number as its destination number

    /**
     * Converts an [Iterable] of source [LongRange] given by [this] into its [Iterable] of destination [LongRange]
     * based on [sourceDestinationRules].
     */
    private fun Iterable<LongRange>.toDestinationRange(): Iterable<LongRange> = buildList {
        this@toDestinationRange.forEach { fromRange ->
            // For each range from the source, build a list of destination ranges for part of the source range
            // that is mapped to the destination along with those parts of the source range that are not mapped

            sourceDestinationRules.associateWith { sourceDestination: SourceDestination ->
                // Find mapped source ranges through intersection with destination's source range
                fromRange.intersectRange(sourceDestination.sourceRange)
            }.filterValues { mappedFromRange: LongRange ->
                // Eliminate any non-intersecting source ranges
                !mappedFromRange.isEmpty()
            }.onEach { (sourceDestination: SourceDestination, mappedFromRange: LongRange) ->
                // Add destination ranges for mapped source ranges
                add(
                    mappedFromRange.first + sourceDestination.sourceToDestinationAdder..
                            mappedFromRange.last + sourceDestination.sourceToDestinationAdder
                )
            }.values.let { mappedFromRange: Collection<LongRange> ->
                // Add all unmapped source ranges by excluding those that are mapped
                addAll(
                    fromRange.minusRanges(mappedFromRange)
                )
            }
        }

    }.mergeLongRanges() // Sort and merge destination ranges

    override fun Seed.toSoil(): Soil = Soil(this.number.toDestination())

    override fun Iterable<LongRange>.toSoilRange(): Iterable<LongRange> = this.toDestinationRange()

    override fun Soil.toFertilizer(): Fertilizer = Fertilizer(this.number.toDestination())

    override fun Iterable<LongRange>.toFertilizerRange(): Iterable<LongRange> = this.toDestinationRange()

    override fun Fertilizer.toWater(): Water = Water(this.number.toDestination())

    override fun Iterable<LongRange>.toWaterRange(): Iterable<LongRange> = this.toDestinationRange()

    override fun Water.toLight(): Light = Light(this.number.toDestination())

    override fun Iterable<LongRange>.toLightRange(): Iterable<LongRange> = this.toDestinationRange()

    override fun Light.toTemperature(): Temperature = Temperature(this.number.toDestination())

    override fun Iterable<LongRange>.toTemperatureRange(): Iterable<LongRange> = this.toDestinationRange()

    override fun Temperature.toHumidity(): Humidity = Humidity(this.number.toDestination())

    override fun Iterable<LongRange>.toHumidityRange(): Iterable<LongRange> = this.toDestinationRange()

    override fun Humidity.toLocation(): Location = Location(this.number.toDestination())

    override fun Iterable<LongRange>.toLocationRange(): Iterable<LongRange> = this.toDestinationRange()

}

private class GardeningAlmanac private constructor(
    private val seeds: List<Seed>,
    private val seedToSoilRule: SourceDestinationRule,
    private val soilToFertilizerRule: SourceDestinationRule,
    private val fertilizerToWaterRule: SourceDestinationRule,
    private val waterToLightRule: SourceDestinationRule,
    private val lightToTemperatureRule: SourceDestinationRule,
    private val temperatureToHumidityRule: SourceDestinationRule,
    private val humidityToLocationRule: SourceDestinationRule
) : ISeedToSoil by seedToSoilRule, ISoilToFertilizer by soilToFertilizerRule,
    IFertilizerToWater by fertilizerToWaterRule,
    IWaterToLight by waterToLightRule, ILightToTemperature by lightToTemperatureRule,
    ITemperatureToHumidity by temperatureToHumidityRule, IHumidityToLocation by humidityToLocationRule {

    companion object {
        private const val MAP = "map"
        private const val COLON = ":"

        private enum class AlmanacEnum(val ruleKey: String) {
            SEED_TO_SOIL("seed-to-soil"),
            SOIL_TO_FERTILIZER("soil-to-fertilizer"),
            FERTILIZER_TO_WATER("fertilizer-to-water"),
            WATER_TO_LIGHT("water-to-light"),
            LIGHT_TO_TEMPERATURE("light-to-temperature"),
            TEMPERATURE_TO_HUMIDITY("temperature-to-humidity"),
            HUMIDITY_TO_LOCATION("humidity-to-location")
        }

        private val buildRule: (rules: Collection<String>) -> SourceDestinationRule = { rules ->
            SourceDestinationRule().apply {
                rules.forEach { rule: String ->
                    append(rule)
                }
            }
        }

        fun parse(input: List<String>): GardeningAlmanac =
            input.splitWhen { line -> line.isEmpty() || line.isBlank() }
                .partition { groupedInput: Iterable<String> ->
                    groupedInput.first().contains(MAP)
                }.let { (mapsInput: List<Iterable<String>>, seedsInput: List<Iterable<String>>) ->
                    val sourceDestinationRuleMap = mapsInput.associate { groupedInput: Iterable<String> ->
                        groupedInput.drop(1).let { rules ->
                            val firstLine = groupedInput.first().substringBefore(MAP).trim()
                            when (AlmanacEnum.entries.first { almanacEnum: AlmanacEnum ->
                                almanacEnum.ruleKey == firstLine
                            }) {
                                AlmanacEnum.SEED_TO_SOIL -> {
                                    AlmanacEnum.SEED_TO_SOIL to buildRule(rules)
                                }

                                AlmanacEnum.SOIL_TO_FERTILIZER -> {
                                    AlmanacEnum.SOIL_TO_FERTILIZER to buildRule(rules)
                                }

                                AlmanacEnum.FERTILIZER_TO_WATER -> {
                                    AlmanacEnum.FERTILIZER_TO_WATER to buildRule(rules)
                                }

                                AlmanacEnum.WATER_TO_LIGHT -> {
                                    AlmanacEnum.WATER_TO_LIGHT to buildRule(rules)
                                }

                                AlmanacEnum.LIGHT_TO_TEMPERATURE -> {
                                    AlmanacEnum.LIGHT_TO_TEMPERATURE to buildRule(rules)
                                }

                                AlmanacEnum.TEMPERATURE_TO_HUMIDITY -> {
                                    AlmanacEnum.TEMPERATURE_TO_HUMIDITY to buildRule(rules)
                                }

                                AlmanacEnum.HUMIDITY_TO_LOCATION -> {
                                    AlmanacEnum.HUMIDITY_TO_LOCATION to buildRule(rules)
                                }
                            }
                        }
                    }

                    GardeningAlmanac(
                        seeds = seedsInput.single().first().substringAfter(COLON).trim()
                            .split("""\s+""".toRegex())
                            .map(String::toLong)
                            .map(::Seed),
                        seedToSoilRule = sourceDestinationRuleMap[AlmanacEnum.SEED_TO_SOIL]!!,
                        soilToFertilizerRule = sourceDestinationRuleMap[AlmanacEnum.SOIL_TO_FERTILIZER]!!,
                        fertilizerToWaterRule = sourceDestinationRuleMap[AlmanacEnum.FERTILIZER_TO_WATER]!!,
                        waterToLightRule = sourceDestinationRuleMap[AlmanacEnum.WATER_TO_LIGHT]!!,
                        lightToTemperatureRule = sourceDestinationRuleMap[AlmanacEnum.LIGHT_TO_TEMPERATURE]!!,
                        temperatureToHumidityRule = sourceDestinationRuleMap[AlmanacEnum.TEMPERATURE_TO_HUMIDITY]!!,
                        humidityToLocationRule = sourceDestinationRuleMap[AlmanacEnum.HUMIDITY_TO_LOCATION]!!
                    )

                }

    }

    /**
     * [Solution for Part 1 & 2]
     *
     * Returns the lowest location number that corresponds to any of the initial seed numbers.
     *
     * @param seedNumbersAreRange [Boolean] set to `true` for Part-2 to indicate that the line describing seed numbers
     * are ranges instead.
     */
    fun getClosestLocationNumber(seedNumbersAreRange: Boolean = false): Long = if (seedNumbersAreRange) {
        // Part-2: When seed numbers are ranges

        seeds.chunked(2) { chunkedNumbers: List<Seed> ->
            // Create range for each chunk of Seed start number followed by its length
            chunkedNumbers[0].number.createRange(chunkedNumbers[1].number)
        }.toSoilRange().toFertilizerRange().toWaterRange()
            .toLightRange().toTemperatureRange().toHumidityRange()
            .toLocationRange()
            .first() // Minimum will be the first value of the first range
            .first   // since at every conversion we are sorting and merging ranges
    } else {
        // Part-1: When seed numbers are NOT ranges

        seeds.minOf { seed: Seed ->
            seed.toSoil().toFertilizer().toWater().toLight().toTemperature().toHumidity().toLocation().number
        }
    }

}