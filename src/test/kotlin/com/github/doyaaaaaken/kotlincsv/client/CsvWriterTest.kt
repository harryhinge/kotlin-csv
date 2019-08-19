package com.github.doyaaaaaken.kotlincsv.client

import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext
import com.github.doyaaaaaken.kotlincsv.dsl.context.WriteQuoteMode
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.github.doyaaaaaken.kotlincsv.util.Const
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import java.io.File
import java.nio.charset.Charset

/**
 * @author doyaaaaaken
 */
class CsvWriterTest : WordSpec() {

    private val testFileName = "test.csv"

    override fun afterTest(testCase: TestCase, result: TestResult) {
        File(testFileName).delete()
        super.afterTest(testCase, result)
    }

    private fun readTestFile(charset: Charset = Charsets.UTF_8): String {
        return File(testFileName).readText(charset)
    }

    init {
        "CsvWriter class constructor" should {
            "be created with no argument" {
                val writer = CsvWriter()
                writer.charset shouldBe Const.defaultCharset
            }
            "be created with CsvWriterContext argument" {
                val context = CsvWriterContext().apply {
                    charset = Charsets.ISO_8859_1
                    delimiter = '\t'
                    nullCode = "NULL"
                    lineTerminator = "\n"
                    quote {
                        quoteChar = '\''
                        quoteMode = WriteQuoteMode.NONE
                    }
                }
                val writer = CsvWriter(context)
                writer.charset shouldBe Charsets.ISO_8859_1
                writer.delimiter shouldBe '\t'
                writer.nullCode shouldBe "NULL"
                writer.lineTerminator shouldBe "\n"
                writer.quote.quoteChar = '\''
                writer.quote.quoteMode = WriteQuoteMode.NONE
            }
        }

        "writeTo method" should {
            val row1 = listOf("a", "b", null)
            val row2 = listOf("d", "2", "1.0")
            val expected = "a,b,\r\nd,2,1.0\r\n"

            "write simple csv data into file with writing each rows" {
                csvWriter().writeTo(testFileName) {
                    writeRow(row1)
                    writeRow(row2)
                }
                val actual = readTestFile()
                actual shouldBe expected
            }

            "write simple csv data into file with writing all at one time" {
                csvWriter().writeTo(testFileName) { writeAll(listOf(row1, row2)) }
                val actual = readTestFile()
                actual shouldBe expected
            }

            "write simple csv data to the tail of existing file with append = true" {
                val writer = csvWriter()
                writer.writeTo(File(testFileName), true) {
                    writeAll(listOf(row1, row2))
                }
                writer.writeTo(File(testFileName), true) {
                    writeAll(listOf(row1, row2))
                }
                val actual = readTestFile()
                actual shouldBe expected + expected
            }

            "overwrite simple csv data with append = false" {
                val writer = csvWriter()
                writer.writeTo(File(testFileName), false) {
                    writeAll(listOf(row2, row2, row2))
                }
                writer.writeTo(File(testFileName), false) {
                    writeAll(listOf(row1, row2))
                }
                val actual = readTestFile()
                actual shouldBe expected
            }
        }

        "Customized CsvWriter" should {
            "write csv with SJIS charset" {
                val sjis = Charset.forName("SJIS")
                csvWriter{
                    charset = sjis
                }.writeTo(File(testFileName)) {
                    writeAll(listOf(listOf("あ", "い")))
                }
                val actual = readTestFile(sjis)
                actual shouldBe "あ,い\r\n"
            }
            "write csv with '|' demimiter" {
                val row1 = listOf("a", "b")
                val row2 = listOf("c", "d")
                val expected = "a|b\r\nc|d\r\n"
                csvWriter{
                    delimiter = '|'
                }.writeTo(File(testFileName)) {
                    writeAll(listOf(row1, row2))
                }
                val actual = readTestFile()
                actual shouldBe expected
            }
            "write null with customized null code" {
                val row = listOf(null, null)
                csvWriter {
                    nullCode = "NULL"
                }.writeTo(testFileName) {
                    writeRow(row)
                }
                val actual = readTestFile()
                actual shouldBe "NULL,NULL\r\n"
            }
            "write csv with \n line terminator" {
                val row1 = listOf("a", "b")
                val row2 = listOf("c", "d")
                val expected = "a,b\nc,d\n"
                csvWriter{
                    lineTerminator = "\n"
                }.writeTo(File(testFileName)) {
                    writeAll(listOf(row1, row2))
                }
                val actual = readTestFile()
                actual shouldBe expected
            }
        }
    }
}
