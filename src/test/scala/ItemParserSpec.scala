
import models.exceptions.JsonParsingException._
import services.ItemParser
import org.scalatest._


class ItemParserSpec extends FunSuite with Matchers {
  //TODO rewrite everything here
  test("Printing results of valid test sample№1") {
    val results = ItemParser.getItemsOutOfString(validJSON).right.get
    println(results)
  }

  test("Passing a faulty json should produce an error") {
    assert(ItemParser.getItemsOutOfString("randomString").isLeft)
  }
  test("Validating a correct item should not produce an exception") {
    val validatedItem = ItemParser.getItemsOutOfString(CorrectItem).right.get
    assert(validatedItem._1.nonEmpty && validatedItem._2.isEmpty)
  }
  test("Validating an item without important field should produce a minor exception") {
    val validatedItem = ItemParser.getItemsOutOfString(ItemWithoutADescription).right.get
    assert(validatedItem._1.nonEmpty)
    validatedItem._2.foreach(_ shouldBe a[MinorItemParsingException])
  }
  test("Validating items without necessary fields should produce major exceptions") {
    val validatedItems = ItemParser.getItemsOutOfString(itemsThatShouldRaiseMajorExceptions).right.get
    assert(validatedItems._1.isEmpty)
    validatedItems._2.foreach(_ shouldBe a[MajorItemParsingException])
  }
  val validJSON: String =
    """[
      |    {
      |		"text":"蚕",
      |		"meaningVariants": ["silkworm"],
      |		"readingVariants": ["さん"],
      |		"precedence": 0,
      |		"level": 1
      |	},
      |	{
      |		"text":"韻",
      |		"translationVariants":["rhyme", "elegance", "tone"],
      |		"readingVariants":["いん"],
      |		"precedence": 0,
      |		"level": 1
      |	},
      |	{
      |		"text":"謁",
      |		"translationVariants":["audience", "audience with king"],
      |		"readingVariants":[],
      |		"precedence": 0,
      |		"level": 3
      |  	}
      |]""".stripMargin
  val CorrectItem: String =
    """
      |[
      | {
      |		"text":"蚕",
      |		"meaningVariants": ["silkworm"],
      |		"readingVariants": ["さん"],
      |		"precedence": 0,
      |   "description":"Last item in G6",
      |		"level": 1
      |	}
      | ]
    """.stripMargin
  val ItemWithoutADescription: String =
    """
      |[
      |    {
      |		"text":"韻",
      |		"translationVariants":["rhyme", "elegance", "tone"],
      |		"readingVariants":["いん"],
      |		"precedence": 0,
      |		"level": 1
      |	}
      | ]
    """.stripMargin

  val itemsThatShouldRaiseMajorExceptions: String =
    """[
      |		{
      |		"text":"",
      |		"translationVariants":["audience", "audience with king"],
      |		"readingVariants":["えっ"],
      |		"precedence": 1
      |		},
      |  {
      |		"text":"謁",
      |		"precedence": 1
      |		}
      |]
      |""".stripMargin
}
