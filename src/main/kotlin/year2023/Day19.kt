/**
 * Problem: Day19: Aplenty
 * https://adventofcode.com/2023/day/19
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import extensions.*
import utils.product
import year2023.Workflow.Accept.name
import year2023.Workflow.Reject.name

private class Day19 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 19114
    println("=====")
    solveActual(1)      // 287054
    println("=====")
    solveSample(2)      // 167409079868000
    println("=====")
    solveActual(2)      // 131619440296497
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day19.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day19.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    PartsOrganizer.parse(input)
        .getTotalAcceptedRatings(startWorkflowName = "in")
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    PartsOrganizer.parse(input)
        .getTotalDistinctCombinationsOfAcceptedRatings(
            startWorkflowName = "in",
            allCategoryRatingRange = 1..4000
        )
        .also { println(it) }
}

/**
 * Class for rating parts in each of the four categories - 'x','m','a' and 's'.
 *
 * @property categoryMetadata [Map] of category names with its values, accessed through delegation.
 */
private class PartRating private constructor(private val categoryMetadata: Map<String, Any>) {
    constructor(categoryPairs: Array<Pair<String, Any>>) : this(hashMapOf(*categoryPairs))

    companion object {

        /**
         * Enum class to get reference to the required category for accessing its value.
         *
         * @property category Lambda to obtain the required category value from [PartRating].
         */
        private enum class CategoryEnum(val category: (PartRating) -> Int) {
            X(PartRating::x),
            M(PartRating::m),
            A(PartRating::a),
            S(PartRating::s)
        }

        /**
         * Enum class to get reference to the required category for accessing its range.
         *
         * @property categoryRange Lambda to obtain the required category range from [PartRating].
         */
        private enum class CategoryRangeEnum(val categoryRange: (PartRating) -> IntRange) {
            X(PartRating::xRange),
            M(PartRating::mRange),
            A(PartRating::aRange),
            S(PartRating::sRange)
        }

        /**
         * Returns a reference to the required [category] for accessing its value.
         */
        fun partCategorySelector(category: String) = CategoryEnum.valueOf(category.uppercase()).category

        /**
         * Returns a reference to the required [category] for accessing its range.
         */
        fun partCategoryRangeSelector(category: String) = CategoryRangeEnum.valueOf(category.uppercase()).categoryRange
    }

    /**
     * [PartRating] inner class for the Result of executing a [WorkflowRule] condition on the [category] range.
     *
     * @param category [String] containing the Name of the category on which the condition was executed. Can be `null`
     * for a [WorkflowRule] independent of category.
     * @param range [IntRange] of the [category] range that either satisfies [WorkflowRule] condition
     * or the range that does not satisfy. Can be `null` for a [WorkflowRule] independent of category.
     * @param nextWorkflowOnTrue [String] containing the Name of the next [Workflow] when the category [range]
     * satisfies [WorkflowRule] condition, and also when the [WorkflowRule] is independent of category; otherwise `null`.
     */
    class RangeResult(val category: String?, val range: IntRange?, val nextWorkflowOnTrue: String?) {
        override fun toString(): String = "'$category' = $range : $nextWorkflowOnTrue"
    }

    // Categories as delegated properties read from the [categoryMetadata] Map
    private val x: Int by categoryMetadata
    private val xRange: IntRange by categoryMetadata
    private val m: Int by categoryMetadata
    private val mRange: IntRange by categoryMetadata
    private val a: Int by categoryMetadata
    private val aRange: IntRange by categoryMetadata
    private val s: Int by categoryMetadata
    private val sRange: IntRange by categoryMetadata

    /**
     * Executes given [testCondition] of a [WorkflowRule] on the [category] value
     * and then returns its [Boolean] result.
     *
     * @param category Lambda to obtain the required category value from [this][PartRating].
     */
    fun testRating(
        category: (PartRating) -> Int,
        testCondition: Int.() -> Boolean
    ): Boolean =
        category(this).testCondition()

    /**
     * Executes given [testRangeCondition] of a [WorkflowRule] on the [categoryRange] range
     * and then returns its [List] of [RangeResult] result.
     *
     * @param categoryRange Lambda to obtain the required category range from [this][PartRating].
     */
    fun testRatingRange(
        categoryRange: (PartRating) -> IntRange,
        testRangeCondition: IntRange.() -> List<RangeResult>
    ): List<RangeResult> =
        categoryRange(this).testRangeCondition()

    /**
     * Returns a total of all the category values (excluding those that are ranges).
     */
    fun ratingTotal(): Int = categoryMetadata.values.filterIsInstance<Int>().sum()
}

/**
 * Sealed class of [Workflow]s.
 *
 * @property name [String] containing the Name of the Workflow.
 * @property rules [List] of [WorkflowRule] that make a Workflow.
 */
private sealed class Workflow(val name: String, val rules: List<WorkflowRule>) {
    companion object {
        private const val ACCEPT = "A"
        private const val REJECT = "R"
    }

    /**
     * [Workflow] subclass for an Intermediate Workflow.
     */
    class IntermediateWorkflow(name: String, rules: List<WorkflowRule>) : Workflow(name, rules)

    /**
     * [Workflow] object instance for Terminal Workflow with [name] as [ACCEPT].
     */
    data object Accept : Workflow(ACCEPT, emptyList())

    /**
     * [Workflow] object instance for Terminal Workflow with [name] as [REJECT].
     */
    data object Reject : Workflow(REJECT, emptyList())
}

/**
 * Class for rules in a [Workflow].
 *
 * @property category Lambda to the required [category] for accessing its value. Can be `null`
 * for a [WorkflowRule] independent of category.
 * @property categoryRange Lambda to the required category for accessing its range. Can be `null`
 * for a [WorkflowRule] independent of category.
 * @property testCondition Lambda Condition to be executed on [category]. Can be `null`
 * for a [WorkflowRule] independent of category.
 * @property testRangeCondition Lambda Condition to be executed on [categoryRange]. Can be `null`
 * for a [WorkflowRule] independent of category.
 * @property nextWorkflowOnTrue [String] containing the Name of the next [Workflow] when
 * the [category] or [categoryRange] satisfies its test condition, and also when
 * the [WorkflowRule] is independent of category.
 */
private class WorkflowRule(
    val category: ((PartRating) -> Int)? = null,
    val categoryRange: ((PartRating) -> IntRange)? = null,
    val testCondition: (Int.() -> Boolean)? = null,
    val testRangeCondition: (IntRange.() -> List<PartRating.RangeResult>)? = null,
    val nextWorkflowOnTrue: String
) {
    companion object {
        private const val GREATER = ">"

        fun create(input: List<String>): WorkflowRule = input.takeIf { it.size == 4 }?.let {
            // When WorkflowRule is dependent on category
            WorkflowRule(
                category = PartRating.partCategorySelector(input[0]),
                categoryRange = PartRating.partCategoryRangeSelector(input[0]),
                testCondition = {
                    if (input[1] == GREATER) {
                        // When the condition is for Greater
                        this > input[2].toInt()
                    } else {
                        // When the condition is for Lesser
                        this < input[2].toInt()
                    }
                },
                testRangeCondition = {
                    if (input[1] == GREATER) {
                        // When the condition is for Greater

                        // Get the first value that happens to be Greater
                        val firstTrue = this.first { it > input[2].toInt() }
                        listOf(
                            // Result for the Range that satisfies the condition
                            PartRating.RangeResult(
                                category = input[0],
                                range = firstTrue..this.last,
                                nextWorkflowOnTrue = input[3]
                            ),

                            // Result for the Range that does NOT satisfy the condition
                            // 'nextWorkflowOnTrue' will be null in this case
                            PartRating.RangeResult(
                                category = input[0],
                                range = this.first until firstTrue,
                                nextWorkflowOnTrue = null
                            )
                        )
                    } else {
                        // When the condition is for Lesser

                        // Get the last value that happens to be Lesser
                        val lastTrue = this.last { it < input[2].toInt() }
                        listOf(
                            // Result for the Range that satisfies the condition
                            PartRating.RangeResult(
                                category = input[0],
                                range = this.first..lastTrue,
                                nextWorkflowOnTrue = input[3]
                            ),

                            // Result for the Range that does NOT satisfy the condition
                            // 'nextWorkflowOnTrue' will be null in this case
                            PartRating.RangeResult(
                                category = input[0],
                                range = lastTrue + 1..this.last,
                                nextWorkflowOnTrue = null
                            )
                        )
                    }
                },
                nextWorkflowOnTrue = input[3]
            )
        } ?: WorkflowRule(
            // When WorkflowRule is independent of category
            nextWorkflowOnTrue = input[0]
        )

    }

}

private class PartsOrganizer private constructor(
    private val workflows: Map<String, Workflow>,
    private val partRatings: List<PartRating>
) {
    companion object {

        private const val X = "x"
        private const val X_RANGE = "xRange"
        private const val M = "m"
        private const val M_RANGE = "mRange"
        private const val A = "a"
        private const val A_RANGE = "aRange"
        private const val S = "s"
        private const val S_RANGE = "sRange"
        private const val EQUALS = "="

        private val workflowInputRegex = """(?:([xmas])([<>])(\d+):)?([a-zA-Z]+)""".toRegex()

        private val partRatingInputRegex = """([xmas])=(\d+)""".toRegex()

        fun parse(input: List<String>): PartsOrganizer =
            input.splitWhen { it.isEmpty() || it.isBlank() }
                .partition { groupedStrings: Iterable<String> ->
                    groupedStrings.first().contains(EQUALS)
                }.let { (partRatings: List<Iterable<String>>, workflows: List<Iterable<String>>) ->
                    PartsOrganizer(
                        workflows = workflows.single().associate { workflowString: String ->
                            workflowInputRegex.findAll(workflowString).map { matchResult ->
                                matchResult.groupValues.drop(1).filterNot(String::isEmpty)
                            }.toList().let { extractedData: List<List<String>> ->
                                extractedData[0].first().let { workflowName: String ->
                                    workflowName to Workflow.IntermediateWorkflow(
                                        workflowName,
                                        extractedData.drop(1).map(WorkflowRule::create)
                                    )
                                }
                            }
                        }.toMutableMap<String, Workflow>().apply {
                            put(Workflow.Accept.name, Workflow.Accept)
                            put(Workflow.Reject.name, Workflow.Reject)
                        },

                        partRatings = partRatings.single().map { partRatingString ->
                            partRatingInputRegex.findAll(partRatingString).map { matchResult ->
                                matchResult.groupValues.drop(1)
                            }.toList()
                        }.map { partRatingsList: List<List<String>> ->
                            PartRating(
                                partRatingsList.map { categoryRatingData: List<String> ->
                                    categoryRatingData[0] to categoryRatingData[1].toInt()
                                }.toTypedArray()
                            )
                        }
                    )
                }
    }

    /**
     * Converts [Workflow] name given in [this] to corresponding [Workflow] instance read from [workflows] Map.
     */
    private fun String.toWorkflow() = workflows[this]!!

    /**
     * Executes [WorkflowRule]s of each [Workflow] starting from the given workflow [startWorkflowName] on each of the
     * category ratings of the given Part [this] based on the rule to see if the Part needs to be Accepted or Rejected.
     *
     * Returns any of the Terminal Workflow reached, that is, [Workflow.Accept] or [Workflow.Reject].
     */
    private fun PartRating.doAcceptanceTest(startWorkflowName: String): Workflow {
        // Get the starting workflow
        var currentWorkflow = startWorkflowName.toWorkflow()

        // Repeat till we reach any of the Terminal workflow
        while (currentWorkflow != Workflow.Accept && currentWorkflow != Workflow.Reject) {
            for (rule in currentWorkflow.rules) {
                // For every rule in the Workflow
                if (rule.category != null && rule.testCondition != null) {
                    // If the rule is dependent on category, then execute the condition on the category
                    if (this.testRating(rule.category, rule.testCondition)) {
                        // If the category rating satisfies the condition, then pick the next workflow
                        currentWorkflow = rule.nextWorkflowOnTrue.toWorkflow()
                        // Bail out to continue with the next workflow
                        break
                    }
                    // (We continue to the next rule when the category rating did NOT satisfy the condition)
                } else {
                    // If the rule is independent of category, then pick the next workflow
                    currentWorkflow = rule.nextWorkflowOnTrue.toWorkflow()
                }
            }
        }

        // Return the Terminal workflow reached
        return currentWorkflow
    }

    /**
     * Executes [WorkflowRule]s of each [Workflow] starting from the given workflow [startWorkflowName] on each of the
     * category range ratings of the given Part [this] based on the rule to see if the Part gets Accepted or Rejected.
     *
     * Returns results of each [WorkflowRule] as [PartRating.RangeResult] till the Terminal workflow [Workflow.Accept]
     * is reached, and since there will be several ways to reach Terminal workflow [Workflow.Accept], it returns
     * a [List] of the same obtained through the use of Breadth-First-Search technique.
     */
    private fun PartRating.getAcceptedResults(startWorkflowName: String): List<List<PartRating.RangeResult>> {
        // Using two lists instead of a Queue as it is faster for items that are already initialized
        // and since Queue would be just holding Results of WorkflowRules that are at a distance of 'd' and 'd+1' only.
        // List of Results obtained from executing WorkflowRules that are at a distance of 'd'
        var currentResults: MutableList<PartRating.RangeResult> =
            mutableListOf(
                // Result for the Starting Workflow
                PartRating.RangeResult(
                    null,
                    null,
                    startWorkflowName
                )
            )

        // List of Results obtained from executing WorkflowRules that are at a distance of 'd + 1'
        val nextResults: MutableList<PartRating.RangeResult> = mutableListOf()

        // Map that saves Result of past WorkflowRule we came from for the new Result. This facilitates to build
        // Result sequence without the need for storing them in a List of Lists for Result of each WorkflowRule explored.
        val cameFromMap: MutableMap<PartRating.RangeResult, PartRating.RangeResult> =
            mutableMapOf(
                // Add Result for the Starting Workflow as the past and current Result to begin with
                PartRating.RangeResult(
                    null,
                    null,
                    startWorkflowName
                ) to PartRating.RangeResult(
                    null,
                    null,
                    startWorkflowName
                )
            )

        // List of Result sequences that end up being Accepted
        val acceptedResults: MutableList<List<PartRating.RangeResult>> = mutableListOf()

        /**
         * Generates a sequence of Results obtained by backtracking from the given `current` Result. Sequence generated
         * will be in the reverse direction till the Result for Starting Workflow. Result for Starting Workflow
         * is also included.
         */
        val resultSequence: (
            current: PartRating.RangeResult
        ) -> Sequence<PartRating.RangeResult> = {
            var currentResult: PartRating.RangeResult = it
            sequence {
                while (currentResult.nextWorkflowOnTrue != startWorkflowName) {
                    yield(currentResult)
                    currentResult = cameFromMap[currentResult]!!
                }
                yield(currentResult)
            }
        }

        /**
         * Adds list of Results that ended with the Terminal workflow [Workflow.Accept] given by `nextResult`
         * to `acceptedResults` list. List of Results are obtained by backtracking from either the given
         * `previousRuleResult` if present or the `currentResult`. `previousRuleResult` is the Result coming
         * from the previous [WorkflowRule] of the same [Workflow].
         */
        val addAcceptedResult: (
            currentResult: PartRating.RangeResult,
            nextResult: PartRating.RangeResult,
            previousRuleResult: PartRating.RangeResult?
        ) -> Unit = { currentResult, nextResult, previousRuleResult ->

            if (previousRuleResult != null) {
                resultSequence(previousRuleResult)
            } else {
                resultSequence(currentResult)
            }.reversed().toMutableList().apply {
                // Include terminal result in the end after reversing the sequence of Results
                // obtained through backtracking
                add(nextResult)
            }.let(acceptedResults::add)
        }

        /**
         * Updates `cameFromMap` with the Result of past [WorkflowRule] we came from for the new Result.
         * `nextResult` is the new Result, while `previousRuleResult` and `currentResult`
         * are Results of the past [WorkflowRule].
         */
        val updateCameFromMap: (
            currentResult: PartRating.RangeResult,
            nextResult: PartRating.RangeResult,
            previousRuleResult: PartRating.RangeResult?
        ) -> Unit = { currentResult, nextResult, previousRuleResult ->

            // If `previousRuleResult` is present, then that will be the past Result; otherwise it is `currentResult`
            if (previousRuleResult != null) {
                cameFromMap[nextResult] = previousRuleResult
            } else {
                cameFromMap[nextResult] = currentResult
            }
        }

        /**
         * Called when the category range rating satisfies the condition of the [WorkflowRule]. Picks the next workflow
         * and updates either the `acceptedResults` or the `cameFromMap` along with `nextResults`, based on
         * the next workflow.
         */
        val pickNextWorkflow: (
            currentResult: PartRating.RangeResult,
            nextResult: PartRating.RangeResult,
            previousRuleResult: PartRating.RangeResult?
        ) -> Unit = { currentResult, nextResult, previousRuleResult ->

            if (nextResult.nextWorkflowOnTrue == Workflow.Accept.name) {
                // If the next workflow is the Terminal Workflow Accept, then add
                // list of Results to `acceptedResults`
                addAcceptedResult(currentResult, nextResult, previousRuleResult)
            } else if (nextResult.nextWorkflowOnTrue != Workflow.Reject.name) {
                // If the next workflow is an Intermediate Workflow, then update `cameFromMap`
                // and add next result to `nextResults` for processing later
                updateCameFromMap(currentResult, nextResult, previousRuleResult)
                nextResults.add(nextResult)
            }
        }

        // Repeat till the List holding Results at distance of 'd' becomes empty
        while (currentResults.isNotEmpty()) {
            currentResults.forEach { currentResult ->
                // To save previous rule's result during the current result's next workflow
                var previousRuleResult: PartRating.RangeResult? = null

                // Delve into the next workflow's rules
                currentResult.nextWorkflowOnTrue!!.toWorkflow().rules.forEach { rule ->

                    if (rule.categoryRange != null && rule.testRangeCondition != null) {
                        // If the rule is dependent on category range, then execute the condition on the category range
                        this.testRatingRange(rule.categoryRange, rule.testRangeCondition).forEach { nextResult ->
                            if (nextResult.nextWorkflowOnTrue != null) {
                                // If the category range rating satisfies the condition, then pick the next workflow
                                pickNextWorkflow(currentResult, nextResult, previousRuleResult)
                            } else {
                                // If the category range rating does NOT satisfy the condition, then
                                // update `cameFromMap` and save this next result as previous rule result
                                // to make it available for the next rule evaluation
                                updateCameFromMap(currentResult, nextResult, previousRuleResult)
                                previousRuleResult = nextResult
                            }
                        }
                    } else {
                        // If the rule is independent of category range, then consider the rule's next workflow
                        // as the next result and pick the next workflow
                        val nextResult = PartRating.RangeResult(
                            null,
                            null,
                            rule.nextWorkflowOnTrue
                        )

                        pickNextWorkflow(currentResult, nextResult, previousRuleResult)
                    }
                }
            }

            // Copy over to the `currentResults` and clear `nextResults`
            currentResults = nextResults.toMutableList()
            nextResults.clear()
        }

        // Return list of Accepted Result sequences
        return acceptedResults
    }

    /**
     * [Solution for Part-1]
     *
     * Returns the sum of all the category rating values of all the [parts][partRatings] that get Accepted.
     *
     * @param startWorkflowName [String] containing the name of [Workflow] to start with.
     */
    fun getTotalAcceptedRatings(startWorkflowName: String): Int =
        partRatings.associateWith { partRating -> partRating.doAcceptanceTest(startWorkflowName) }
            .filterValues { workflow: Workflow -> workflow == Workflow.Accept }
            .keys
            .sumOf { partRating: PartRating -> partRating.ratingTotal() }

    /**
     * [Solution for Part-2]
     *
     * Returns the Total number of Distinct combinations of category ratings that gets Accepted
     * by the [workflows] given.
     *
     * @param startWorkflowName [String] containing the name of [Workflow] to start with.
     * @param allCategoryRatingRange [IntRange] of values for all category ratings of a part.
     */
    fun getTotalDistinctCombinationsOfAcceptedRatings(
        startWorkflowName: String,
        allCategoryRatingRange: IntRange
    ): Long =
        PartRating(
            arrayOf(
                X_RANGE to allCategoryRatingRange,
                M_RANGE to allCategoryRatingRange,
                A_RANGE to allCategoryRatingRange,
                S_RANGE to allCategoryRatingRange
            )
        ).getAcceptedResults(startWorkflowName) // Get results of category range ratings that are Accepted
            .map { rangeResults: List<PartRating.RangeResult> ->
                rangeResults.groupBy { rangeResult: PartRating.RangeResult ->
                    // Group results by category
                    rangeResult.category
                }.mapValues { (_: String?, rangeResults: List<PartRating.RangeResult>) ->
                    // Convert all ranges found for a category into a single common intersecting range
                    rangeResults.mapNotNull { rangeResult: PartRating.RangeResult ->
                        rangeResult.range
                    }.let(List<IntRange>::intersectRange)
                }
            }.sumOf { acceptedRangeMap: Map<String?, IntRange> ->
                listOf(X, M, A, S).map { category: String ->
                    // Convert range to its size for each category
                    acceptedRangeMap.getOrDefault(category, allCategoryRatingRange)
                        .rangeLength().toLong()
                }.product() // Product gives distinct combinations
            }

}