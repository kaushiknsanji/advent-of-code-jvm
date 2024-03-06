package extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit test on utility functions provided for working with Ranges `(src/main/kotlin/extensions/Ranges.kt)`.
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */
class RangesTest {

    @Test
    fun testCreateIntRange() {
        // Test for empty range
        assertEquals(
            IntRange.EMPTY,
            1.createRange(0)
        )

        // Test for single number range
        assertEquals(
            (3..3),
            3.createRange(1)
        )

        // Test for two numbers range
        assertEquals(
            (3..4),
            3.createRange(2)
        )

        // Test for bigger range
        assertEquals(
            (1351..2770),
            1351.createRange(1420)
        )
    }

    @Test
    fun testCreateLongRange() {
        // Test for empty range
        assertEquals(
            LongRange.EMPTY,
            1L.createRange(0L)
        )

        // Test for single number range
        assertEquals(
            (3L..3),
            3L.createRange(1L)
        )

        // Test for two numbers range
        assertEquals(
            (3L..4),
            3L.createRange(2L)
        )

        // Test for bigger range
        assertEquals(
            (1351L..2770),
            1351L.createRange(1420L)
        )
    }

    @Test
    fun testIntRangeLength() {
        // Test with empty range
        assertEquals(
            0,
            IntRange.EMPTY.rangeLength()
        )

        // Test with single number range
        assertEquals(
            1,
            (1..1).rangeLength()
        )

        // Test with two numbers range
        assertEquals(
            2,
            (2..3).rangeLength()
        )

        // Test with bigger range
        assertEquals(
            1420,
            (1351..2770).rangeLength()
        )
    }

    @Test
    fun testLongRangeLength() {
        // Test with empty range
        assertEquals(
            0L,
            LongRange.EMPTY.rangeLength()
        )

        // Test with single number range
        assertEquals(
            1L,
            (1L..1).rangeLength()
        )

        // Test with two numbers range
        assertEquals(
            2L,
            (2L..3).rangeLength()
        )

        // Test with bigger range
        assertEquals(
            1420L,
            (1351L..2770).rangeLength()
        )
    }

    @Test
    fun testIntersectUsingMultipleIntRanges() {
        // Test for empty input list
        assertEquals(
            IntRange.EMPTY,
            emptyList<IntRange>().intersectRange()
        )

        // Test for input list with single range
        assertEquals(
            1351..4000,
            listOf(1351..4000).intersectRange()
        )

        // Test for input list with one empty range
        assertEquals(
            IntRange.EMPTY,
            listOf(1351..4000, IntRange.EMPTY).intersectRange()
        )

        // Test for input list with empty ranges
        assertEquals(
            IntRange.EMPTY,
            listOf(IntRange.EMPTY, IntRange.EMPTY).intersectRange()
        )

        // Test for NO intersection
        assertEquals(
            IntRange.EMPTY,
            listOf(5..10, 11..13, 7..12).intersectRange()
        )

        // Test for intersection
        assertEquals(
            2771..3448,
            listOf(1351..4000, 2771..4000, 1..3448).intersectRange()
        )

        // Test for intersection
        assertEquals(
            839..1800,
            listOf(1..1800, 839..4000).intersectRange()
        )

        // Test for intersection
        assertEquals(
            1351..2770,
            listOf(1351..4000, 1..2770).intersectRange()
        )

        // Test for intersection
        assertEquals(
            3449..4000,
            listOf(1351..4000, 2771..4000, 3449..4000).intersectRange()
        )
    }

    @Test
    fun testIntersectUsingMultipleLongRanges() {
        // Test for empty input list
        assertEquals(
            LongRange.EMPTY,
            emptyList<LongRange>().intersectRange()
        )

        // Test for input list with single range
        assertEquals(
            1351L..4000,
            listOf(1351L..4000).intersectRange()
        )

        // Test for input list with one empty range
        assertEquals(
            LongRange.EMPTY,
            listOf(1351L..4000, LongRange.EMPTY).intersectRange()
        )

        // Test for input list with empty ranges
        assertEquals(
            LongRange.EMPTY,
            listOf(LongRange.EMPTY, LongRange.EMPTY).intersectRange()
        )

        // Test for NO intersection
        assertEquals(
            LongRange.EMPTY,
            listOf(5L..10, 11L..13, 7L..12).intersectRange()
        )

        // Test for intersection
        assertEquals(
            2771L..3448,
            listOf(1351L..4000, 2771L..4000, 1L..3448).intersectRange()
        )

        // Test for intersection
        assertEquals(
            839L..1800,
            listOf(1L..1800, 839L..4000).intersectRange()
        )

        // Test for intersection
        assertEquals(
            1351L..2770,
            listOf(1351L..4000, 1L..2770).intersectRange()
        )

        // Test for intersection
        assertEquals(
            3449L..4000,
            listOf(1351L..4000, 2771L..4000, 3449L..4000).intersectRange()
        )
    }

    @Test
    fun testIntersectUsingTwoIntRanges() {
        // Test with one empty range
        assertEquals(
            IntRange.EMPTY,
            (1351..4000).intersectRange(IntRange.EMPTY)
        )

        // Test with both empty ranges
        assertEquals(
            IntRange.EMPTY,
            IntRange.EMPTY.intersectRange(IntRange.EMPTY)
        )

        // Test for intersection
        assertEquals(
            1351..2770,
            (1351..4000).intersectRange(1..2770)
        )

        // Test for intersection
        assertEquals(
            839..1800,
            (1..1800).intersectRange(839..4000)
        )

        // Test for NO intersection
        assertEquals(
            IntRange.EMPTY,
            (5..10).intersectRange(11..13)
        )
    }

    @Test
    fun testIntersectUsingTwoLongRanges() {
        // Test with one empty range
        assertEquals(
            LongRange.EMPTY,
            (1351L..4000).intersectRange(LongRange.EMPTY)
        )

        // Test with both empty ranges
        assertEquals(
            LongRange.EMPTY,
            LongRange.EMPTY.intersectRange(LongRange.EMPTY)
        )

        // Test for intersection
        assertEquals(
            1351L..2770,
            (1351L..4000).intersectRange(1L..2770)
        )

        // Test for intersection
        assertEquals(
            839L..1800,
            (1L..1800).intersectRange(839L..4000)
        )

        // Test for NO intersection
        assertEquals(
            LongRange.EMPTY,
            (5L..10).intersectRange(11L..13)
        )
    }

    @Test
    fun testToIntRanges() {
        // Test with empty input list
        assertEquals(
            emptyList<IntRange>(),
            emptyList<Int>().toIntRanges()
        )

        // Test with one number in the input list
        assertEquals(
            listOf(2..2),
            listOf(2).toIntRanges()
        )

        // Test with two numbers in the input list such that it forms a range
        assertEquals(
            listOf(2..3),
            listOf(3, 2).toIntRanges()
        )

        // Test with two distant numbers in the input list such that it forms individual number ranges
        assertEquals(
            listOf(1..1, 3..3),
            listOf(3, 1).toIntRanges()
        )

        // Test with a mixture of numbers in the input list which forms ranges and individual number ranges: START
        assertEquals(
            listOf(0..2, 4..6, 9..11, 15..15, 19..20),
            listOf(1, 2, 4, 5, 6, 9, 0, 10, 11, 15, 19, 20).shuffled().toIntRanges()
        )

        assertEquals(
            listOf(0..2, 4..6, 9..11, 15..15, 19..19),
            listOf(1, 2, 4, 5, 6, 9, 0, 10, 11, 15, 19).shuffled().toIntRanges()
        )

        assertEquals(
            listOf(0..2, 4..6, 9..11, 15..15),
            listOf(1, 2, 4, 5, 6, 9, 0, 10, 11, 15).shuffled().toIntRanges()
        )

        assertEquals(
            listOf(1..1, 3..6, 9..11, 15..15),
            listOf(1, 3, 4, 5, 6, 9, 10, 11, 15).shuffled().toIntRanges()
        )
        // Test with a mixture of numbers in the input list which forms ranges and individual number ranges: END
    }

    @Test
    fun testToLongRanges() {
        // Test with empty input list
        assertEquals(
            emptyList<LongRange>(),
            emptyList<Long>().toLongRanges()
        )

        // Test with one number in the input list
        assertEquals(
            listOf(2L..2),
            listOf(2L).toLongRanges()
        )

        // Test with two numbers in the input list such that it forms a range
        assertEquals(
            listOf(2L..3),
            listOf(3L, 2).toLongRanges()
        )

        // Test with two distant numbers in the input list such that it forms individual number ranges
        assertEquals(
            listOf(1L..1, 3L..3),
            listOf(3L, 1).toLongRanges()
        )

        // Test with a mixture of numbers in the input list which forms ranges and individual number ranges: START
        assertEquals(
            listOf(0L..2, 4L..6, 9L..11, 15L..15, 19L..20),
            listOf(1L, 2, 4, 5, 6, 9, 0, 10, 11, 15, 19, 20).shuffled().toLongRanges()
        )

        assertEquals(
            listOf(0L..2, 4L..6, 9L..11, 15L..15, 19L..19),
            listOf(1L, 2, 4, 5, 6, 9, 0, 10, 11, 15, 19).shuffled().toLongRanges()
        )

        assertEquals(
            listOf(0L..2, 4L..6, 9L..11, 15L..15),
            listOf(1L, 2, 4, 5, 6, 9, 0, 10, 11, 15).shuffled().toLongRanges()
        )

        assertEquals(
            listOf(1L..1, 3L..6, 9L..11, 15L..15),
            listOf(1L, 3, 4, 5, 6, 9, 10, 11, 15).shuffled().toLongRanges()
        )
        // Test with a mixture of numbers in the input list which forms ranges and individual number ranges: END
    }

    @Test
    fun testMergeIntRanges() {
        // Test with empty input list
        assertEquals(
            emptyList<IntRange>(),
            emptyList<IntRange>().mergeIntRanges()
        )

        // Test with input list having an empty range
        assertEquals(
            emptyList<IntRange>(),
            listOf(IntRange.EMPTY).mergeIntRanges()
        )

        // Test with input list having a single range
        assertEquals(
            listOf(1..3),
            listOf(1..3).mergeIntRanges()
        )

        // Test with input list having a proper range and an empty range
        assertEquals(
            listOf(1..3),
            listOf(1..3, IntRange.EMPTY).mergeIntRanges()
        )

        // Test with input list having two ranges that can be merged
        assertEquals(
            listOf(3..7),
            listOf(5..7, 3..6).mergeIntRanges()
        )

        // Test with input list having two distant ranges that cannot be merged
        assertEquals(
            listOf(5..7, 10..13),
            listOf(5..7, 10..13).mergeIntRanges()
        )

        // Test with input list having two ranges and an empty range that can be merged
        assertEquals(
            listOf(3..7),
            listOf(5..7, IntRange.EMPTY, 3..6).mergeIntRanges()
        )

        // Test with input list having two distant ranges and an empty range that cannot be merged
        assertEquals(
            listOf(5..7, 10..13),
            listOf(5..7, IntRange.EMPTY, 10..13).mergeIntRanges()
        )

        // Test with input list having merge-able and non-merge-able ranges
        assertEquals(
            listOf(1..5, 8..15, 17..20),
            listOf(2..5, 1..3, 8..15, 17..20).shuffled().mergeIntRanges()
        )

        // Test with input list having merge-able ranges, non-merge-able ranges and ranges with same first number: START
        assertEquals(
            listOf(1..5, 8..15, 17..20),
            listOf(2..5, 1..3, 8..15, 17..20, 8..10).shuffled().mergeIntRanges()
        )

        assertEquals(
            listOf(1..5, 8..15, 17..20),
            listOf(1..5, 1..3, 8..15, 17..20, 8..10).shuffled().mergeIntRanges()
        )

        // Test with input list having merge-able ranges, non-merge-able ranges and ranges with same first number: END

        // Test with input list having distant ranges at the start, middle and end of the list: START
        assertEquals(
            listOf(1..3, 8..20, 22..25),
            listOf(1..3, 8..15, 17..20, 13..18, 22..25).shuffled().mergeIntRanges()
        )

        // Test also with a range included in another range
        assertEquals(
            listOf(0..7, 9..11, 15..20),
            listOf(0..2, 4..6, 9..11, 15..15, 19..20, 3..7, 15..18)
                .shuffled().mergeIntRanges()
        )
        // Test with input list having distant ranges at the start, middle and end of the list: END

        // Test with input list having a range included in another range
        assertEquals(
            listOf(0..11, 15..20),
            listOf(0..2, 4..6, 8..11, 15..15, 19..20, 3..7, 15..18)
                .shuffled().mergeIntRanges()
        )
    }

    @Test
    fun testMergeLongRanges() {
        // Test with empty input list
        assertEquals(
            emptyList<LongRange>(),
            emptyList<LongRange>().mergeLongRanges()
        )

        // Test with input list having an empty range
        assertEquals(
            emptyList<LongRange>(),
            listOf(LongRange.EMPTY).mergeLongRanges()
        )

        // Test with input list having a single range
        assertEquals(
            listOf(1L..3),
            listOf(1L..3).mergeLongRanges()
        )

        // Test with input list having a proper range and an empty range
        assertEquals(
            listOf(1L..3),
            listOf(1L..3, LongRange.EMPTY).mergeLongRanges()
        )

        // Test with input list having two ranges that can be merged
        assertEquals(
            listOf(3L..7),
            listOf(5L..7, 3L..6).mergeLongRanges()
        )

        // Test with input list having two distant ranges that cannot be merged
        assertEquals(
            listOf(5L..7, 10L..13),
            listOf(5L..7, 10L..13).mergeLongRanges()
        )

        // Test with input list having two ranges and an empty range that can be merged
        assertEquals(
            listOf(3L..7),
            listOf(5L..7, LongRange.EMPTY, 3L..6).mergeLongRanges()
        )

        // Test with input list having two distant ranges and an empty range that cannot be merged
        assertEquals(
            listOf(5L..7, 10L..13),
            listOf(5L..7, LongRange.EMPTY, 10L..13).mergeLongRanges()
        )

        // Test with input list having merge-able and non-merge-able ranges
        assertEquals(
            listOf(1L..5, 8L..15, 17L..20),
            listOf(2L..5, 1L..3, 8L..15, 17L..20).shuffled().mergeLongRanges()
        )

        // Test with input list having merge-able ranges, non-merge-able ranges and ranges with same first number: START
        assertEquals(
            listOf(1L..5, 8L..15, 17L..20),
            listOf(2L..5, 1L..3, 8L..15, 17L..20, 8L..10).shuffled().mergeLongRanges()
        )

        assertEquals(
            listOf(1L..5, 8L..15, 17L..20),
            listOf(1L..5, 1L..3, 8L..15, 17L..20, 8L..10).shuffled().mergeLongRanges()
        )

        // Test with input list having merge-able ranges, non-merge-able ranges and ranges with same first number: END

        // Test with input list having distant ranges at the start, middle and end of the list: START
        assertEquals(
            listOf(1L..3, 8L..20, 22L..25),
            listOf(1L..3, 8L..15, 17L..20, 13L..18, 22L..25).shuffled().mergeLongRanges()
        )

        // Test also with a range included in another range
        assertEquals(
            listOf(0L..7, 9L..11, 15L..20),
            listOf(0L..2, 4L..6, 9L..11, 15L..15, 19L..20, 3L..7, 15L..18)
                .shuffled().mergeLongRanges()
        )
        // Test with input list having distant ranges at the start, middle and end of the list: END

        // Test with input list having a range included in another range
        assertEquals(
            listOf(0L..11, 15L..20),
            listOf(0L..2, 4L..6, 8L..11, 15L..15, 19L..20, 3L..7, 15L..18)
                .shuffled().mergeLongRanges()
        )
    }

    @Test
    fun testMinusIntRange() {
        // Test with "From" range as empty range
        assertEquals(
            emptyList<IntRange>(),
            IntRange.EMPTY.minusRange(13..15)
        )

        // Test with "Subtract by" range as empty range
        assertEquals(
            listOf(13..15),
            (13..15).minusRange(IntRange.EMPTY)
        )

        // Test with both empty ranges
        assertEquals(
            emptyList<IntRange>(),
            IntRange.EMPTY.minusRange(IntRange.EMPTY)
        )

        // Test with non-intersecting ranges
        assertEquals(
            listOf(13..19),
            (13..19).minusRange(5..9)
        )

        // Test with intersecting ranges: START
        val fromRange = 9..19

        assertEquals(
            listOf(9..12, 16..19),
            fromRange.minusRange(13..15)
        )

        assertEquals(
            listOf(9..12),
            fromRange.minusRange(13..19)
        )

        assertEquals(
            listOf(9..12),
            fromRange.minusRange(13..25)
        )

        assertEquals(
            listOf(16..19),
            fromRange.minusRange(9..15)
        )

        assertEquals(
            emptyList<IntRange>(),
            fromRange.minusRange(9..19)
        )

        assertEquals(
            emptyList<IntRange>(),
            fromRange.minusRange(9..25)
        )

        assertEquals(
            listOf(16..19),
            fromRange.minusRange(5..15)
        )

        assertEquals(
            emptyList<IntRange>(),
            fromRange.minusRange(5..19)
        )

        assertEquals(
            emptyList<IntRange>(),
            fromRange.minusRange(5..25)
        )
        // Test with intersecting ranges: END
    }

    @Test
    fun testMinusLongRange() {
        // Test with "From" range as empty range
        assertEquals(
            emptyList<LongRange>(),
            LongRange.EMPTY.minusRange(13L..15)
        )

        // Test with "Subtract by" range as empty range
        assertEquals(
            listOf(13L..15),
            (13L..15).minusRange(LongRange.EMPTY)
        )

        // Test with both empty ranges
        assertEquals(
            emptyList<LongRange>(),
            LongRange.EMPTY.minusRange(LongRange.EMPTY)
        )

        // Test with non-intersecting ranges
        assertEquals(
            listOf(13L..19),
            (13L..19).minusRange(5L..9)
        )

        // Test with intersecting ranges: START
        val fromRange = 9L..19

        assertEquals(
            listOf(9L..12, 16L..19),
            fromRange.minusRange(13L..15)
        )

        assertEquals(
            listOf(9L..12),
            fromRange.minusRange(13L..19)
        )

        assertEquals(
            listOf(9L..12),
            fromRange.minusRange(13L..25)
        )

        assertEquals(
            listOf(16L..19),
            fromRange.minusRange(9L..15)
        )

        assertEquals(
            emptyList<LongRange>(),
            fromRange.minusRange(9L..19)
        )

        assertEquals(
            emptyList<LongRange>(),
            fromRange.minusRange(9L..25)
        )

        assertEquals(
            listOf(16L..19),
            fromRange.minusRange(5L..15)
        )

        assertEquals(
            emptyList<LongRange>(),
            fromRange.minusRange(5L..19)
        )

        assertEquals(
            emptyList<LongRange>(),
            fromRange.minusRange(5L..25)
        )
        // Test with intersecting ranges: END
    }

    @Test
    fun testMinusIntRangesByMultipleIntRanges() {
        // Test with "From" range as empty range
        assertEquals(
            emptyList<IntRange>(),
            IntRange.EMPTY.minusRanges(listOf(13..15, 3..7))
        )

        // Test with empty "Subtract by" ranges: START
        assertEquals(
            listOf(13..15),
            (13..15).minusRanges(emptyList())
        )

        assertEquals(
            listOf(13..15),
            (13..15).minusRanges(listOf(IntRange.EMPTY, IntRange.EMPTY))
        )
        // Test with empty "Subtract by" ranges: END

        // Test with both empty ranges: START
        assertEquals(
            emptyList<IntRange>(),
            IntRange.EMPTY.minusRanges(emptyList())
        )

        assertEquals(
            emptyList<IntRange>(),
            IntRange.EMPTY.minusRanges(listOf(IntRange.EMPTY, IntRange.EMPTY))
        )
        // Test with both empty ranges: END

        // Test with non-intersecting ranges
        assertEquals(
            listOf(13..19),
            (13..19).minusRanges(listOf(5..9, 20..25, 8..12))
        )

        // Test with intersecting ranges: START
        val fromRange = 9..19

        assertEquals(
            listOf(11..12, 17..17),
            fromRange.minusRanges(
                listOf(13..15, 7..10, 18..21, 8..10, 18..20, 14..16).shuffled()
            )
        )

        assertEquals(
            listOf(11..12, 16..17),
            fromRange.minusRanges(listOf(13..15, 7..10, 18..21).shuffled())
        )

        assertEquals(
            listOf(13..13, 19..19),
            fromRange.minusRanges(listOf(8..12, 14..16, 20..23, 15..18).shuffled())
        )

        assertEquals(
            listOf(13..13, 19..19),
            fromRange.minusRanges(listOf(8..12, 14..18, 20..23, 15..17).shuffled())
        )
        // Test with intersecting ranges: END
    }

    @Test
    fun testMinusLongRangesByMultipleLongRanges() {
        // Test with "From" range as empty range
        assertEquals(
            emptyList<LongRange>(),
            LongRange.EMPTY.minusRanges(listOf(13L..15, 3L..7))
        )

        // Test with empty "Subtract by" ranges: START
        assertEquals(
            listOf(13L..15),
            (13L..15).minusRanges(emptyList())
        )

        assertEquals(
            listOf(13L..15),
            (13L..15).minusRanges(listOf(LongRange.EMPTY, LongRange.EMPTY))
        )
        // Test with empty "Subtract by" ranges: END

        // Test with both empty ranges: START
        assertEquals(
            emptyList<LongRange>(),
            LongRange.EMPTY.minusRanges(emptyList())
        )

        assertEquals(
            emptyList<LongRange>(),
            LongRange.EMPTY.minusRanges(listOf(LongRange.EMPTY, LongRange.EMPTY))
        )
        // Test with both empty ranges: END

        // Test with non-intersecting ranges
        assertEquals(
            listOf(13L..19),
            (13L..19).minusRanges(listOf(5L..9, 20L..25, 8L..12))
        )

        // Test with intersecting ranges: START
        val fromRange = 9L..19

        assertEquals(
            listOf(11L..12, 17L..17),
            fromRange.minusRanges(
                listOf(13L..15, 7L..10, 18L..21, 8L..10, 18L..20, 14L..16)
                    .shuffled()
            )
        )

        assertEquals(
            listOf(11L..12, 16L..17),
            fromRange.minusRanges(listOf(13L..15, 7L..10, 18L..21).shuffled())
        )

        assertEquals(
            listOf(13L..13, 19L..19),
            fromRange.minusRanges(listOf(8L..12, 14L..16, 20L..23, 15L..18).shuffled())
        )

        assertEquals(
            listOf(13L..13, 19L..19),
            fromRange.minusRanges(listOf(8L..12, 14L..18, 20L..23, 15L..17).shuffled())
        )
        // Test with intersecting ranges: END
    }

    @Test
    fun testMinusIntRangesByMultipleIntRangesMerged() {
        // Test with "From" range as empty range
        assertEquals(
            emptyList<IntRange>(),
            IntRange.EMPTY.minusRangesMerged(listOf(13..15, 3..7).mergeIntRanges())
        )

        // Test with empty "Subtract by" ranges: START
        assertEquals(
            listOf(13..15),
            (13..15).minusRangesMerged(emptyList())
        )

        assertEquals(
            listOf(13..15),
            (13..15).minusRangesMerged(listOf(IntRange.EMPTY, IntRange.EMPTY))
        )
        // Test with empty "Subtract by" ranges: END

        // Test with both empty ranges: START
        assertEquals(
            emptyList<IntRange>(),
            IntRange.EMPTY.minusRangesMerged(emptyList())
        )

        assertEquals(
            emptyList<IntRange>(),
            IntRange.EMPTY.minusRangesMerged(listOf(IntRange.EMPTY, IntRange.EMPTY))
        )
        // Test with both empty ranges: END

        // Test with non-intersecting ranges
        assertEquals(
            listOf(13..19),
            (13..19).minusRangesMerged(listOf(5..9, 20..25, 8..12).mergeIntRanges())
        )

        // Test with intersecting ranges: START
        val fromRange = 9..19

        assertEquals(
            listOf(11..12, 17..17),
            fromRange.minusRangesMerged(
                listOf(13..15, 7..10, 18..21, 8..10, 18..20, 14..16)
                    .shuffled().mergeIntRanges()
            )
        )

        assertEquals(
            listOf(11..12, 16..17),
            fromRange.minusRangesMerged(listOf(13..15, 7..10, 18..21).shuffled().mergeIntRanges())
        )

        assertEquals(
            listOf(13..13, 19..19),
            fromRange.minusRangesMerged(
                listOf(8..12, 14..16, 20..23, 15..18).shuffled().mergeIntRanges()
            )
        )

        assertEquals(
            listOf(13..13, 19..19),
            fromRange.minusRangesMerged(
                listOf(8..12, 14..18, 20..23, 15..17).shuffled().mergeIntRanges()
            )
        )
        // Test with intersecting ranges: END
    }

    @Test
    fun testMinusLongRangesByMultipleLongRangesMerged() {
        // Test with "From" range as empty range
        assertEquals(
            emptyList<LongRange>(),
            LongRange.EMPTY.minusRangesMerged(listOf(13L..15, 3L..7).mergeLongRanges())
        )

        // Test with empty "Subtract by" ranges: START
        assertEquals(
            listOf(13L..15),
            (13L..15).minusRangesMerged(emptyList())
        )

        assertEquals(
            listOf(13L..15),
            (13L..15).minusRangesMerged(listOf(LongRange.EMPTY, LongRange.EMPTY))
        )
        // Test with empty "Subtract by" ranges: END

        // Test with both empty ranges: START
        assertEquals(
            emptyList<LongRange>(),
            LongRange.EMPTY.minusRangesMerged(emptyList())
        )

        assertEquals(
            emptyList<LongRange>(),
            LongRange.EMPTY.minusRangesMerged(listOf(LongRange.EMPTY, LongRange.EMPTY))
        )
        // Test with both empty ranges: END

        // Test with non-intersecting ranges
        assertEquals(
            listOf(13L..19),
            (13L..19).minusRangesMerged(listOf(5L..9, 20L..25, 8L..12).mergeLongRanges())
        )

        // Test with intersecting ranges: START
        val fromRange = 9L..19

        assertEquals(
            listOf(11L..12, 17L..17),
            fromRange.minusRangesMerged(
                listOf(13L..15, 7L..10, 18L..21, 8L..10, 18L..20, 14L..16)
                    .shuffled().mergeLongRanges()
            )
        )

        assertEquals(
            listOf(11L..12, 16L..17),
            fromRange.minusRangesMerged(listOf(13L..15, 7L..10, 18L..21).shuffled().mergeLongRanges())
        )

        assertEquals(
            listOf(13L..13, 19L..19),
            fromRange.minusRangesMerged(
                listOf(8L..12, 14L..16, 20L..23, 15L..18).shuffled().mergeLongRanges()
            )
        )

        assertEquals(
            listOf(13L..13, 19L..19),
            fromRange.minusRangesMerged(
                listOf(8L..12, 14L..18, 20L..23, 15L..17).shuffled().mergeLongRanges()
            )
        )
        // Test with intersecting ranges: END
    }

    @Test
    fun testMinusIntRangesByMultipleIntRangesFromMultipleIntRanges() {
        // Test with "From" ranges as empty ranges: START
        assertEquals(
            emptyList<IntRange>(),
            emptyList<IntRange>().minusIntRanges(listOf(13..15, 3..7))
        )

        assertEquals(
            emptyList<IntRange>(),
            listOf(IntRange.EMPTY, IntRange.EMPTY).minusIntRanges(listOf(13..15, 3..7))
        )
        // Test with "From" ranges as empty ranges: END

        // Test with empty "Subtract by" ranges: START
        assertEquals(
            listOf(3..7, 13..15),
            listOf(13..15, 3..7).minusIntRanges(emptyList())
        )

        assertEquals(
            listOf(3..7, 13..15),
            listOf(13..15, 3..7).minusIntRanges(listOf(IntRange.EMPTY, IntRange.EMPTY))
        )
        // Test with empty "Subtract by" ranges: END

        // Test with both empty ranges: START
        assertEquals(
            emptyList<IntRange>(),
            emptyList<IntRange>().minusIntRanges(emptyList())
        )

        assertEquals(
            emptyList<IntRange>(),
            emptyList<IntRange>().minusIntRanges(listOf(IntRange.EMPTY, IntRange.EMPTY))
        )

        assertEquals(
            emptyList<IntRange>(),
            listOf(IntRange.EMPTY, IntRange.EMPTY).minusIntRanges(emptyList())
        )

        assertEquals(
            emptyList<IntRange>(),
            listOf(IntRange.EMPTY, IntRange.EMPTY).minusIntRanges(listOf(IntRange.EMPTY, IntRange.EMPTY))
        )
        // Test with both empty ranges: END

        // Test with non-intersecting ranges
        assertEquals(
            listOf(1..4, 13..19, 26..29, 38..42),
            listOf(13..19, 1..4, 26..29, 38..42)
                .minusIntRanges(listOf(5..9, 20..25, 8..12, 32..36, 30..34))
        )

        // Test with intersecting ranges: START
        val fromRanges =
            listOf(0..2, 4..6, 9..11, 11..13, 15..15, 16..19).shuffled()

        assertEquals(
            listOf(0..2, 4..6, 11..12, 17..17),
            fromRanges.minusIntRanges(
                listOf(13..15, 7..10, 18..21, 8..10, 18..20, 14..16).shuffled()
            )
        )

        assertEquals(
            listOf(0..2, 4..6, 11..12, 16..17),
            fromRanges.minusIntRanges(
                listOf(13..15, 7..10, 18..21).shuffled()
            )
        )

        assertEquals(
            listOf(0..2, 15..17),
            fromRanges.minusIntRanges(
                listOf(11..13, 7..10, 18..21, 3..7).shuffled()
            )
        )

        assertEquals(
            listOf(0..1, 6..6, 13..13, 19..19),
            fromRanges.minusIntRanges(
                listOf(8..12, 14..16, 20..23, 15..18, 2..5).shuffled()
            )
        )

        assertEquals(
            listOf(0..0, 4..6, 13..13, 19..19),
            fromRanges.minusIntRanges(
                listOf(8..12, 14..18, 20..23, 15..17, 1..3).shuffled()
            )
        )
        // Test with intersecting ranges: END
    }

    @Test
    fun testMinusLongRangesByMultipleLongRangesFromMultipleLongRanges() {
        // Test with "From" ranges as empty ranges: START
        assertEquals(
            emptyList<LongRange>(),
            emptyList<LongRange>().minusLongRanges(listOf(13L..15, 3L..7))
        )

        assertEquals(
            emptyList<LongRange>(),
            listOf(LongRange.EMPTY, LongRange.EMPTY).minusLongRanges(listOf(13L..15, 3L..7))
        )
        // Test with "From" ranges as empty ranges: END

        // Test with empty "Subtract by" ranges: START
        assertEquals(
            listOf(3L..7, 13L..15),
            listOf(13L..15, 3L..7).minusLongRanges(emptyList())
        )

        assertEquals(
            listOf(3L..7, 13L..15),
            listOf(13L..15, 3L..7).minusLongRanges(listOf(LongRange.EMPTY, LongRange.EMPTY))
        )
        // Test with empty "Subtract by" ranges: END

        // Test with both empty ranges: START
        assertEquals(
            emptyList<LongRange>(),
            emptyList<LongRange>().minusLongRanges(emptyList())
        )

        assertEquals(
            emptyList<LongRange>(),
            emptyList<LongRange>().minusLongRanges(listOf(LongRange.EMPTY, LongRange.EMPTY))
        )

        assertEquals(
            emptyList<LongRange>(),
            listOf(LongRange.EMPTY, LongRange.EMPTY).minusLongRanges(emptyList())
        )

        assertEquals(
            emptyList<LongRange>(),
            listOf(LongRange.EMPTY, LongRange.EMPTY).minusLongRanges(listOf(LongRange.EMPTY, LongRange.EMPTY))
        )
        // Test with both empty ranges: END

        // Test with non-intersecting ranges
        assertEquals(
            listOf(1L..4, 13L..19, 26L..29, 38L..42),
            listOf(13L..19, 1L..4, 26L..29, 38L..42)
                .minusLongRanges(listOf(5L..9, 20L..25, 8L..12, 32L..36, 30L..34))
        )

        // Test with intersecting ranges: START
        val fromRanges =
            listOf(0L..2, 4L..6, 9L..11, 11L..13, 15L..15, 16L..19).shuffled()

        assertEquals(
            listOf(0L..2, 4L..6, 11L..12, 17L..17),
            fromRanges.minusLongRanges(
                listOf(13L..15, 7L..10, 18L..21, 8L..10, 18L..20, 14L..16)
                    .shuffled()
            )
        )

        assertEquals(
            listOf(0L..2, 4L..6, 11L..12, 16L..17),
            fromRanges.minusLongRanges(
                listOf(13L..15, 7L..10, 18L..21).shuffled()
            )
        )

        assertEquals(
            listOf(0L..2, 15L..17),
            fromRanges.minusLongRanges(
                listOf(11L..13, 7L..10, 18L..21, 3L..7).shuffled()
            )
        )

        assertEquals(
            listOf(0L..1, 6L..6, 13L..13, 19L..19),
            fromRanges.minusLongRanges(
                listOf(8L..12, 14L..16, 20L..23, 15L..18, 2L..5).shuffled()
            )
        )

        assertEquals(
            listOf(0L..0, 4L..6, 13L..13, 19L..19),
            fromRanges.minusLongRanges(
                listOf(8L..12, 14L..18, 20L..23, 15L..17, 1L..3).shuffled()
            )
        )
        // Test with intersecting ranges: END
    }
}