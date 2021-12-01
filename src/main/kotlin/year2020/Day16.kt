/**
 * Problem: Day16: Ticket Translation
 * https://adventofcode.com/2020/day/16
 *
 * @author Kaushik N Sanji
 */

package year2020

import base.BaseFileHandler
import extensions.product

private class Day16 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)
    println("=====")
    solveActual(1)
    println("=====")
    solvePart2Sample()
    println("=====")
    solveActual(2)
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day16.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day16.getActualTestFile().readLines(), executeProblemPart)
}

private fun solvePart2Sample() {
    execute(Day16.getSampleFile("_part2").readLines(), 3)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
        3 -> doPart3(input)
    }
}

private fun doPart1(input: List<String>) {
    TicketTranslator.parse(input)
        .getTicketErrorRateOfInvalidNearByTickets()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    TicketTranslator.parse(input)
        .identifyAndPrintFieldsWithValuesFromYourTicket()
        .getDepartureBasedFieldsProductValueFromYourTicket()
        .also { println(it) }
}

private fun doPart3(input: List<String>) {
    TicketTranslator.parse(input)
        .identifyAndPrintFieldsWithValuesFromYourTicket()
}

private class TicketTranslator private constructor(
    val fieldValueMap: Map<String, List<Int>>,
    val yourTicketNumbers: List<Int>,
    val nearByTicketNumbers: List<List<Int>>
) {
    companion object {
        const val LINE_YOUR_TICKET = "your ticket:"
        const val LINE_NEARBY_TICKETS = "nearby tickets:"
        const val LINE_FIELDS = "fields"
        const val DEPARTURE_FIELDS = "departure"

        private val fieldNumberPattern get() = """(\d+-\d+)""".toRegex()

        fun parse(input: List<String>): TicketTranslator {
            val fieldValueMap = mutableMapOf<String, List<Int>>()
            val yourTicketNumbers = mutableListOf<Int>()
            val nearByTicketNumbers = mutableListOf<List<Int>>()
            var lineInfoControl = LINE_FIELDS

            input.forEach { line ->
                if (line.contains(LINE_YOUR_TICKET)) {
                    lineInfoControl = LINE_YOUR_TICKET
                } else if (line.contains(LINE_NEARBY_TICKETS)) {
                    lineInfoControl = LINE_NEARBY_TICKETS
                } else {
                    when (lineInfoControl) {
                        LINE_FIELDS -> {
                            line.split(":")
                                .zipWithNext { field: String, valueStr: String ->
                                    field to
                                            fieldNumberPattern.findAll(valueStr)
                                                .flatMap {
                                                    it.groupValues[1].split("-")
                                                        .zipWithNext { numberStr1: String, numberStr2: String ->
                                                            numberStr1.toInt()..numberStr2.toInt()
                                                        }
                                                }
                                }
                                .associateTo(fieldValueMap) { (field: String, valueSequence: Sequence<IntRange>) ->
                                    field to
                                            valueSequence.flatMap { range: IntRange ->
                                                range.asSequence()
                                            }.toList()
                                }
                        }

                        LINE_YOUR_TICKET -> {
                            line.split(",").filterNot { it.isEmpty() || it.isBlank() }
                                .mapTo(yourTicketNumbers) { it.toInt() }
                        }

                        LINE_NEARBY_TICKETS -> {
                            line.split(",").filterNot { it.isEmpty() || it.isBlank() }
                                .map { it.toInt() }
                                .let {
                                    nearByTicketNumbers.add(it)
                                }
                        }
                    }
                }
            }

            return TicketTranslator(
                fieldValueMap,
                yourTicketNumbers,
                nearByTicketNumbers
            )
        }
    }

    private val allValidValues = fieldValueMap.values.flatten()

    private val validNearByTicketNumbers: List<List<Int>> = nearByTicketNumbers.filter { ticketNumbers ->
        ticketNumbers.all { it in allValidValues }
    }

    private val identifiedFieldsWithPositionsMap: MutableMap<String, Int> = mutableMapOf()

    fun getTicketErrorRateOfInvalidNearByTickets(): Int = nearByTicketNumbers.sumOf { ticketNumbers ->
        ticketNumbers.filter { it !in allValidValues }.sum()
    }

    fun identifyAndPrintFieldsWithValuesFromYourTicket(): TicketTranslator = this.apply {
        // Stores the fields already identified with their position
        val identifiedFieldsSet = mutableSetOf<String>()

        // Begin with indexing the ticket numbers and then group them by their position
        // to get all the ticket numbers falling in a given index
        validNearByTicketNumbers.flatMap { validNearByNumbers -> validNearByNumbers.withIndex() }
            .groupBy { indexedValidNearByNumbers -> indexedValidNearByNumbers.index }
            .mapValues { (_, indexedValidNearByNumbers) -> indexedValidNearByNumbers.map { it.value } }
            .mapValues { (_, validNearByNumbers) ->
                // Map ticket numbers to probable fields
                fieldValueMap.filterValues { validFieldValues -> validNearByNumbers.all { it in validFieldValues } }.keys
            }
            .map { (index, validFieldsSet) ->
                // Change to pair for sorting later by fields size
                validFieldsSet to index
            }
            .sortedBy { (validFieldsSet, _) -> validFieldsSet.size }
            .runningReduce { acc: Pair<Set<String>, Int>, next: Pair<Set<String>, Int> ->
                // Add all the fields previously identified
                identifiedFieldsSet.addAll(acc.first)
                // Get remaining fields from the "next" by removing already identified fields
                val remainingFieldsSet: Set<String> = next.first - identifiedFieldsSet
                // Remaining field is mapped to the identified position,
                // which will become the accumulator value for the following reduce operation
                remainingFieldsSet to next.second
            }
            .associateTo(identifiedFieldsWithPositionsMap) { (validFieldsSet, index) ->
                // Change to a Map of Field with its Position
                validFieldsSet.single() to index
            }
            .mapValues { (_, index) ->
                // Map positions to the ticket numbers from your ticket
                yourTicketNumbers[index]
            }
            .also { println(it) }
    }

    fun getDepartureBasedFieldsProductValueFromYourTicket(): Long =
        identifiedFieldsWithPositionsMap.mapValues { (_, index) -> yourTicketNumbers[index].toLong() }
            .filterKeys { field -> field.startsWith(DEPARTURE_FIELDS) }
            .values
            .product()
}