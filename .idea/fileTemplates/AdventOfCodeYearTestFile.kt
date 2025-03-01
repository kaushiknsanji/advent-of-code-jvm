#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}
#end

import org.junit.jupiter.api.Test
import utils.test.ProblemClassTester.testDay

#set($author = "${USER}")
#if (${Email} && ${Email.trim()} != "")
	#set($author = "<a href='mailto:${Email}'>${USER}</a>")
#end
/**
 * Unit Test on Day Problem Classes of the Year ${Year}.
 *
 * @author ${author}
 */
class ${NAME} {

    @Test
    fun testDay01() {
        testDay(Day1())
    }

    @Test
    fun testDay02() {
//        testDay(Day2())
    }

    @Test
    fun testDay03() {
//        testDay(Day3())
    }

    @Test
    fun testDay04() {
//        testDay(Day4())
    }

    @Test
    fun testDay05() {
//        testDay(Day5())
    }

    @Test
    fun testDay06() {
//        testDay(Day6())
    }

    @Test
    fun testDay07() {
//        testDay(Day7())
    }

    @Test
    fun testDay08() {
//        testDay(Day8())
    }

    @Test
    fun testDay09() {
//        testDay(Day9())
    }

    @Test
    fun testDay10() {
//        testDay(Day10())
    }

    @Test
    fun testDay11() {
//        testDay(Day11())
    }

    @Test
    fun testDay12() {
//        testDay(Day12())
    }

    @Test
    fun testDay13() {
//        testDay(Day13())
    }

    @Test
    fun testDay14() {
//        testDay(Day14())
    }

    @Test
    fun testDay15() {
//        testDay(Day15())
    }

    @Test
    fun testDay16() {
//        testDay(Day16())
    }

    @Test
    fun testDay17() {
//        testDay(Day17())
    }

    @Test
    fun testDay18() {
//        testDay(Day18())
    }

    @Test
    fun testDay19() {
//        testDay(Day19())
    }

    @Test
    fun testDay20() {
//        testDay(Day20())
    }

    @Test
    fun testDay21() {
//        testDay(Day21())
    }

    @Test
    fun testDay22() {
//        testDay(Day22())
    }

    @Test
    fun testDay23() {
//        testDay(Day23())
    }

    @Test
    fun testDay24() {
//        testDay(Day24())
    }

    @Test
    fun testDay25() {
//        testDay(Day25())
    }

}