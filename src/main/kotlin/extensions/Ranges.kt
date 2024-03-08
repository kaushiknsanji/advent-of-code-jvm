/**
 * Kotlin file for working with Kotlin's Ranges.
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package extensions

import utils.difference

/**
 * Extension function on [Int] to create an [IntRange] starting with the given [this] number
 * having a length of [countOfNumbers].
 */
fun Int.createRange(countOfNumbers: Int): IntRange = this until (this + countOfNumbers)

/**
 * Extension function on [Long] to create a [LongRange] starting with the given [this] number
 * having a length of [countOfNumbers].
 */
fun Long.createRange(countOfNumbers: Long): LongRange = this until (this + countOfNumbers)

/**
 * Extension function on [IntRange] to return its size/length in [Int].
 */
fun IntRange.rangeLength(): Int = last - first + 1

/**
 * Extension function on [LongRange] to return its size/length in [Long].
 */
fun LongRange.rangeLength(): Long = last - first + 1

/**
 * Extension function on [Iterable] of [IntRange] to find and return
 * the common / intersecting [IntRange] among the given ranges.
 *
 * When intersecting [IntRange] is found to be empty or when [this] is empty, it returns [IntRange.EMPTY].
 */
fun Iterable<IntRange>.intersectRange(): IntRange =
    when (count()) {
        0 -> IntRange.EMPTY
        1 -> single()
        else -> {
            (maxOf { range -> range.first }..minOf { range -> range.last }).let { intersectedRange: IntRange ->
                if (intersectedRange.first > intersectedRange.last) {
                    IntRange.EMPTY
                } else {
                    intersectedRange
                }
            }
        }
    }

/**
 * Extension function on [Iterable] of [LongRange] to find and return
 * the common / intersecting [LongRange] among the given ranges.
 *
 * When intersecting [LongRange] is found to be empty or when [this] is empty, it returns [LongRange.EMPTY].
 */
fun Iterable<LongRange>.intersectRange(): LongRange =
    when (count()) {
        0 -> LongRange.EMPTY
        1 -> single()
        else -> {
            (maxOf { range -> range.first }..minOf { range -> range.last }).let { intersectedRange: LongRange ->
                if (intersectedRange.first > intersectedRange.last) {
                    LongRange.EMPTY
                } else {
                    intersectedRange
                }
            }
        }
    }

/**
 * Extension function on [IntRange] to find and return the intersection / common [IntRange] with [other] range.
 */
fun IntRange.intersectRange(other: IntRange): IntRange = listOf(this, other).intersectRange()

/**
 * Extension function on [LongRange] to find and return the intersection / common [LongRange] with [other] range.
 */
fun LongRange.intersectRange(other: LongRange): LongRange = listOf(this, other).intersectRange()

/**
 * Extension function on [Iterable] of [Int] to sort and group these [Int]s into a [Collection] of [IntRange].
 */
fun Iterable<Int>.toIntRanges(): Collection<IntRange> =
    this.sortedBy { it }.takeUnless(List<Int>::isEmpty)?.let { sortedNumbers ->
        sortedNumbers.windowed(size = 2, step = 1) // Window current and next numbers to get their difference
            .takeUnless { it.isEmpty() } // Will be empty when there is only one Int number in the list
            ?.map { windowedNumbers ->
                windowedNumbers.reversed().difference()
            }?.withIndex()
            ?.toMutableList()?.apply {
                // When there are only two Int numbers in the list, it will result in only one difference number
                // rendering rest of the code useless as we are going to use window again. Hence, we simply
                // add a copy of the same difference to make the code work.
                if (this.size == 1) {
                    this.add(this[0])
                }
            }
            ?.windowed(size = 2, step = 1) // Window previous and next differences to group the numbers
            ?.fold(mutableListOf(mutableListOf())) { acc: MutableList<MutableList<Int>>, previousNextDifferences: List<IndexedValue<Int>> ->
                // Grouping numbers in a List of lists accumulator to generate their range later
                acc.apply {
                    // Get previous and next differences
                    val previousDifference = previousNextDifferences.first()
                    val nextDifference = previousNextDifferences.last()

                    // Include the first number of the previous difference in the current grouped list
                    this.last().add(sortedNumbers[previousDifference.index])

                    if (previousDifference == nextDifference) {
                        // This is for the case where we added a copy of the same difference.
                        // Hence, previous and next difference instances will be same.

                        // Include the second number of the previous difference based on previous difference value
                        if (previousDifference.value > 1) {
                            // Start a new list when the previous difference is more than 1 and also include
                            // the second number of the previous difference
                            this.add(mutableListOf(sortedNumbers[previousDifference.index + 1]))
                        } else {
                            // Include the second number of the previous difference in the current group list when
                            // previous difference is less than or equal to 1
                            this.last().add(sortedNumbers[previousDifference.index + 1])
                        }
                    } else {
                        // When there are more than two Int numbers in the list, we will have
                        // different previous and next difference instances

                        if (previousDifference.value > 1) {
                            // Start a new list when the previous difference is more than 1
                            this.add(mutableListOf())
                        }

                        if (nextDifference.index + 1 == sortedNumbers.lastIndex) {
                            // When we are at the end of the original sorted numbers list

                            // Include the first number of the next difference in the current grouped list
                            this.last().add(sortedNumbers[nextDifference.index])

                            // Include the second number of the next difference based on next difference value
                            if (nextDifference.value > 1) {
                                // Start a new list when the next difference is more than 1 and also include
                                // the second number of the next difference
                                this.add(mutableListOf(sortedNumbers[nextDifference.index + 1]))
                            } else {
                                // Include the second number of the next difference in the current grouped list when
                                // next difference is less than or equal to 1
                                this.last().add(sortedNumbers[nextDifference.index + 1])
                            }
                        }
                    }
                }
            }
            ?.map { groupedNumbers: Iterable<Int> ->
                // Generate range using the first and last numbers of each grouped list
                groupedNumbers.first()..groupedNumbers.last()
            }
            ?: listOf(this.first()..this.first()) // If only one number was present in the list, then return that as range
    } ?: emptyList() // If no numbers were present in the list, then return an empty list

/**
 * Extension function on [Iterable] of [Long] to sort and group these [Long]s into a [Collection] of [LongRange].
 */
fun Iterable<Long>.toLongRanges(): Collection<LongRange> =
    this.sortedBy { it }.takeUnless(List<Long>::isEmpty)?.let { sortedNumbers ->
        sortedNumbers.windowed(size = 2, step = 1) // Window current and next numbers to get their difference
            .takeUnless { it.isEmpty() } // Will be empty when there is only one Long number in the list
            ?.map { windowedNumbers ->
                windowedNumbers.reversed().difference()
            }?.withIndex()
            ?.toMutableList()?.apply {
                // When there are only two Long numbers in the list, it will result in only one difference number
                // rendering rest of the code useless as we are going to use window again. Hence, we simply
                // add a copy of the same difference to make the code work.
                if (this.size == 1) {
                    this.add(this[0])
                }
            }
            ?.windowed(size = 2, step = 1) // Window previous and next differences to group the numbers
            ?.fold(mutableListOf(mutableListOf())) { acc: MutableList<MutableList<Long>>, previousNextDifferences: List<IndexedValue<Long>> ->
                // Grouping numbers in a List of lists accumulator to generate their range later
                acc.apply {
                    // Get previous and next differences
                    val previousDifference = previousNextDifferences.first()
                    val nextDifference = previousNextDifferences.last()

                    // Include the first number of the previous difference in the current grouped list
                    this.last().add(sortedNumbers[previousDifference.index])

                    if (previousDifference == nextDifference) {
                        // This is for the case where we added a copy of the same difference.
                        // Hence, previous and next difference instances will be same.

                        // Include the second number of the previous difference based on previous difference value
                        if (previousDifference.value > 1L) {
                            // Start a new list when the previous difference is more than 1 and also include
                            // the second number of the previous difference
                            this.add(mutableListOf(sortedNumbers[previousDifference.index + 1]))
                        } else {
                            // Include the second number of the previous difference in the current group list when
                            // previous difference is less than or equal to 1
                            this.last().add(sortedNumbers[previousDifference.index + 1])
                        }
                    } else {
                        // When there are more than two Long numbers in the list, we will have
                        // different previous and next difference instances

                        if (previousDifference.value > 1L) {
                            // Start a new list when the previous difference is more than 1
                            this.add(mutableListOf())
                        }

                        if (nextDifference.index + 1 == sortedNumbers.lastIndex) {
                            // When we are at the end of the original sorted numbers list

                            // Include the first number of the next difference in the current grouped list
                            this.last().add(sortedNumbers[nextDifference.index])

                            // Include the second number of the next difference based on next difference value
                            if (nextDifference.value > 1L) {
                                // Start a new list when the next difference is more than 1 and also include
                                // the second number of the next difference
                                this.add(mutableListOf(sortedNumbers[nextDifference.index + 1]))
                            } else {
                                // Include the second number of the next difference in the current grouped list when
                                // next difference is less than or equal to 1
                                this.last().add(sortedNumbers[nextDifference.index + 1])
                            }
                        }
                    }
                }
            }
            ?.map { groupedNumbers: Iterable<Long> ->
                // Generate range using the first and last numbers of each grouped list
                groupedNumbers.first()..groupedNumbers.last()
            }
            ?: listOf(this.first()..this.first()) // If only one number was present in the list, then return that as range
    } ?: emptyList() // If no numbers were present in the list, then return an empty list

/**
 * Extension function on [Iterable] of [IntRange] to sort and merge [this] ranges into a [Collection] of [IntRange].
 */
fun Iterable<IntRange>.mergeIntRanges(): Collection<IntRange> =
    this.filterNot(IntRange::isEmpty).sortedBy { range -> range.first }
        .takeUnless(List<IntRange>::isEmpty)?.let { sortedRanges: List<IntRange> ->
            sortedRanges.windowed(size = 2, step = 1) // Window current and next ranges to get their gap / difference
                .takeUnless { it.isEmpty() } // Will be empty when there is only one IntRange in the list
                ?.map { windowedRanges: List<IntRange> ->
                    windowedRanges.last().first - windowedRanges.first().last
                }?.withIndex()
                ?.toMutableList()?.apply {
                    // When there are only two IntRanges in the list, it will result in only one difference number
                    // rendering rest of the code useless as we are going to use window again. Hence, we simply
                    // add a copy of the same difference to make the code work.
                    if (this.size == 1) {
                        this.add(this[0])
                    }
                }
                ?.windowed(size = 2, step = 1) // Window previous and next differences to group the IntRanges
                ?.fold(mutableListOf(mutableListOf())) { acc: MutableList<MutableList<IntRange>>, previousNextDifferences: List<IndexedValue<Int>> ->
                    // Grouping IntRanges in a List of lists accumulator to Merge their ranges later
                    acc.apply {
                        // Get previous and next differences
                        val previousDifference = previousNextDifferences.first()
                        val nextDifference = previousNextDifferences.last()

                        /**
                         * Lambda to find and return the maximum [IntRange.last] number from the ranges
                         * present in the current grouped list.
                         */
                        val maxLast: () -> Int = {
                            this.last().maxOf { range -> range.last }
                        }

                        // Include the first range of the previous difference in the current grouped list
                        this.last().add(sortedRanges[previousDifference.index])

                        if (previousDifference == nextDifference) {
                            // This is for the case where we added a copy of the same difference.
                            // Hence, previous and next difference instances will be same.

                            // Include the second range of the previous difference based on previous difference value
                            if (previousDifference.value > 1) {
                                // Start a new list when the previous difference is more than 1 and also include
                                // the second range of the previous difference
                                this.add(mutableListOf(sortedRanges[previousDifference.index + 1]))
                            } else {
                                // Include the second range of the previous difference in the current group list when
                                // previous difference is less than 1
                                this.last().add(sortedRanges[previousDifference.index + 1])
                            }
                        } else {
                            // When there are more than two IntRanges in the list, we will have
                            // different previous and next difference instances

                            if (previousDifference.value > 1
                                && (sortedRanges[previousDifference.index + 1].first - maxLast()) > 1
                            ) {
                                // Start a new list when the previous difference is more than 1
                                // and when the difference of first number of the second range of the previous difference
                                // with the maximum [IntRange.last] number found from the ranges present
                                // in the current grouped list is more than 1
                                this.add(mutableListOf())
                            }

                            if (nextDifference.index + 1 == sortedRanges.lastIndex) {
                                // When we are at the end of the original sorted ranges list

                                // Include the first range of the next difference in the current grouped list
                                this.last().add(sortedRanges[nextDifference.index])

                                // Include the second range of the next difference based on next difference value
                                // and the difference of first number of the second range of the next difference with the
                                // maximum [IntRange.last] number found from the ranges present in the current grouped list
                                if (nextDifference.value > 1
                                    && (sortedRanges[nextDifference.index + 1].first - maxLast()) > 1
                                ) {
                                    // Start a new list when the computed difference is more than 1 and also include
                                    // the second range of the next difference
                                    this.add(mutableListOf(sortedRanges[nextDifference.index + 1]))
                                } else {
                                    // Otherwise, include the second range of the next difference
                                    // in the current grouped list
                                    this.last().add(sortedRanges[nextDifference.index + 1])
                                }
                            }
                        }
                    }
                }
                ?.map { groupedRanges: Iterable<IntRange> ->
                    // Merge ranges using the minimum first number and maximum last number
                    // of the ranges of each grouped list
                    with(groupedRanges) {
                        minOf { range -> range.first }..maxOf { range -> range.last }
                    }
                } ?: listOf(this.first()) // If only one range was present in the list, then return the same
        } ?: emptyList() // If no ranges were present in the list, then return an empty list

/**
 * Extension function on [Iterable] of [LongRange] to sort and merge [this] ranges into a [Collection] of [LongRange].
 */
fun Iterable<LongRange>.mergeLongRanges(): Collection<LongRange> =
    this.filterNot(LongRange::isEmpty).sortedBy { range -> range.first }
        .takeUnless(List<LongRange>::isEmpty)?.let { sortedRanges: List<LongRange> ->
            sortedRanges.windowed(size = 2, step = 1) // Window current and next ranges to get their gap / difference
                .takeUnless { it.isEmpty() } // Will be empty when there is only one LongRange in the list
                ?.map { windowedRanges: List<LongRange> ->
                    windowedRanges.last().first - windowedRanges.first().last
                }?.withIndex()
                ?.toMutableList()?.apply {
                    // When there are only two LongRanges in the list, it will result in only one difference number
                    // rendering rest of the code useless as we are going to use window again. Hence, we simply
                    // add a copy of the same difference to make the code work.
                    if (this.size == 1) {
                        this.add(this[0])
                    }
                }
                ?.windowed(size = 2, step = 1) // // Window previous and next differences to group the LongRanges
                ?.fold(mutableListOf(mutableListOf())) { acc: MutableList<MutableList<LongRange>>, previousNextDifferences: List<IndexedValue<Long>> ->
                    // Grouping LongRanges in a List of lists accumulator to Merge their ranges later
                    acc.apply {
                        // Get previous and next differences
                        val previousDifference = previousNextDifferences.first()
                        val nextDifference = previousNextDifferences.last()

                        /**
                         * Lambda to find and return the maximum [LongRange.last] number from the ranges
                         * present in the current grouped list.
                         */
                        val maxLast: () -> Long = {
                            this.last().maxOf { range -> range.last }
                        }

                        // Include the first range of the previous difference in the current grouped list
                        this.last().add(sortedRanges[previousDifference.index])

                        if (previousDifference == nextDifference) {
                            // This is for the case where we added a copy of the same difference.
                            // Hence, previous and next difference instances will be same.

                            // Include the second range of the previous difference based on previous difference value
                            if (previousDifference.value > 1L) {
                                // Start a new list when the previous difference is more than 1 and also include
                                // the second range of the previous difference
                                this.add(mutableListOf(sortedRanges[previousDifference.index + 1]))
                            } else {
                                // Include the second range of the previous difference in the current group list when
                                // previous difference is less than 1
                                this.last().add(sortedRanges[previousDifference.index + 1])
                            }
                        } else {
                            // When there are more than two LongRanges in the list, we will have
                            // different previous and next difference instances

                            if (previousDifference.value > 1L
                                && (sortedRanges[previousDifference.index + 1].first - maxLast()) > 1L
                            ) {
                                // Start a new list when the previous difference is more than 1
                                // and when the difference of first number of the second range of the previous difference
                                // with the maximum [LongRange.last] number found from the ranges present
                                // in the current grouped list is more than 1
                                this.add(mutableListOf())
                            }

                            if (nextDifference.index + 1 == sortedRanges.lastIndex) {
                                // When we are at the end of the original sorted ranges list

                                // Include the first range of the next difference in the current grouped list
                                this.last().add(sortedRanges[nextDifference.index])

                                // Include the second range of the next difference based on next difference value
                                // and the difference of first number of the second range of the next difference with the
                                // maximum [LongRange.last] number found from the ranges present in the current grouped list
                                if (nextDifference.value > 1L
                                    && (sortedRanges[nextDifference.index + 1].first - maxLast()) > 1L
                                ) {
                                    // Start a new list when the computed difference is more than 1 and also include
                                    // the second range of the next difference
                                    this.add(mutableListOf(sortedRanges[nextDifference.index + 1]))
                                } else {
                                    // Otherwise, include the second range of the next difference
                                    // in the current grouped list
                                    this.last().add(sortedRanges[nextDifference.index + 1])
                                }
                            }
                        }
                    }
                }
                ?.map { groupedRanges: Iterable<LongRange> ->
                    // Merge ranges using the minimum first number and maximum last number
                    // of the ranges of each grouped list
                    with(groupedRanges) {
                        minOf { range -> range.first }..maxOf { range -> range.last }
                    }
                } ?: listOf(this.first()) // If only one range was present in the list, then return the same
        } ?: emptyList() // If no ranges were present in the list, then return an empty list

/**
 * Extension function on [IntRange] to find and return a [Collection] of [IntRange] containing
 * ranges from the original [this] range except the given [other] range.
 */
fun IntRange.minusRange(other: IntRange): Collection<IntRange> =
    buildList {
        if (this@minusRange.intersectRange(other).isEmpty()) {
            if (!this@minusRange.isEmpty()) {
                add(this@minusRange)
            }
        } else {
            if (this@minusRange.first < other.first) {
                add(this@minusRange.first until other.first)
            }

            if (this@minusRange.last > other.last) {
                add(other.last + 1..this@minusRange.last)
            }
        }
    }

/**
 * Extension function on [LongRange] to find and return a [Collection] of [LongRange] containing
 * ranges from the original [this] range except the given [other] range.
 */
fun LongRange.minusRange(other: LongRange): Collection<LongRange> =
    buildList {
        if (this@minusRange.intersectRange(other).isEmpty()) {
            if (!this@minusRange.isEmpty()) {
                add(this@minusRange)
            }
        } else {
            if (this@minusRange.first < other.first) {
                add(this@minusRange.first until other.first)
            }

            if (this@minusRange.last > other.last) {
                add(other.last + 1..this@minusRange.last)
            }
        }
    }

/**
 * Extension function on [IntRange] to find and return a [Collection] of [IntRange] containing ranges from the
 * original [this] range except the given [subtractByRanges] ranges.
 */
fun IntRange.minusRanges(subtractByRanges: Collection<IntRange>): Collection<IntRange> =
    subtractByRanges.mergeIntRanges() // Sort and merge subtract by IntRanges, and then do the minus
        .let(this::minusRangesMerged)

/**
 * Extension function on [LongRange] to find and return a [Collection] of [LongRange] containing ranges from the
 * original [this] range except the given [subtractByRanges] ranges.
 */
fun LongRange.minusRanges(subtractByRanges: Collection<LongRange>): Collection<LongRange> =
    subtractByRanges.mergeLongRanges() // Sort and merge subtract by LongRanges, and then do the minus
        .let(this::minusRangesMerged)

/**
 * Extension function on [IntRange] to find and return a [Collection] of [IntRange] containing ranges from the
 * original [this] range except the given [subtractByMergedRanges] ranges.
 *
 * @param subtractByMergedRanges [Collection] of [IntRange] that are already sorted and merged.
 */
fun IntRange.minusRangesMerged(
    subtractByMergedRanges: Collection<IntRange>
): Collection<IntRange> =
    subtractByMergedRanges.fold(listOf(this)) { acc: Collection<IntRange>, mergedRange: IntRange ->
        acc.flatMap { fromRange: IntRange ->
            fromRange.minusRange(mergedRange)
        }
    }.filterNot(IntRange::isEmpty)

/**
 * Extension function on [LongRange] to find and return a [Collection] of [LongRange] containing ranges from the
 * original [this] range except the given [subtractByMergedRanges] ranges.
 *
 * @param subtractByMergedRanges [Collection] of [LongRange] that are already sorted and merged.
 */
fun LongRange.minusRangesMerged(
    subtractByMergedRanges: Collection<LongRange>
): Collection<LongRange> =
    subtractByMergedRanges.fold(listOf(this)) { acc: Collection<LongRange>, mergedRange: LongRange ->
        acc.flatMap { fromRange: LongRange ->
            fromRange.minusRange(mergedRange)
        }
    }.filterNot(LongRange::isEmpty)

/**
 * Extension function on [Iterable] of [IntRange] to find and return a [Collection] of [IntRange] containing ranges
 * from the supplied ranges of [this] except the given [subtractByRanges] ranges.
 */
fun Iterable<IntRange>.minusIntRanges(subtractByRanges: Collection<IntRange>): Collection<IntRange> =
    subtractByRanges.mergeIntRanges() // Sort and merge subtract by IntRanges, and then do the minus
        .let { subtractByMergedRanges: Collection<IntRange> ->
            // Sort and merge [this] IntRanges, and then do the minus
            this.mergeIntRanges().flatMap { fromMergedRange: IntRange ->
                fromMergedRange.minusRangesMerged(subtractByMergedRanges)
            }
        }

/**
 * Extension function on [Iterable] of [LongRange] to find and return a [Collection] of [LongRange] containing ranges
 * from the supplied ranges of [this] except the given [subtractByRanges] ranges.
 */
fun Iterable<LongRange>.minusLongRanges(subtractByRanges: Collection<LongRange>): Collection<LongRange> =
    subtractByRanges.mergeLongRanges() // Sort and merge subtract by LongRanges, and then do the minus
        .let { subtractByMergedRanges: Collection<LongRange> ->
            // Sort and merge [this] LongRanges, and then do the minus
            this.mergeLongRanges().flatMap { fromMergedRange: LongRange ->
                fromMergedRange.minusRangesMerged(subtractByMergedRanges)
            }
        }