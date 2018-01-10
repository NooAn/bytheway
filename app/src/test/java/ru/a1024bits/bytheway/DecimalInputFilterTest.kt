package ru.a1024bits.bytheway

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import ru.a1024bits.bytheway.util.DecimalInputFilter

/**
 * Created by tikhon.osipov on 25.11.17
 */
@RunWith(JUnit4::class)
class DecimalInputFilterTest {

    @Test
    @Throws(Exception::class)
    fun testDecimalInputFilterRegex() {
        Assert.assertTrue("1".matches(DecimalInputFilter.regex.toRegex()))
        Assert.assertTrue("12".matches(DecimalInputFilter.regex.toRegex()))
        Assert.assertTrue("123".matches(DecimalInputFilter.regex.toRegex()))
        Assert.assertTrue("1234".matches(DecimalInputFilter.regex.toRegex()))

        Assert.assertFalse("0".matches(DecimalInputFilter.regex.toRegex()))
        Assert.assertFalse("01".matches(DecimalInputFilter.regex.toRegex()))
        Assert.assertFalse("0012".matches(DecimalInputFilter.regex.toRegex()))
        Assert.assertFalse("00123".matches(DecimalInputFilter.regex.toRegex()))
        Assert.assertFalse("001234".matches(DecimalInputFilter.regex.toRegex()))
    }
}