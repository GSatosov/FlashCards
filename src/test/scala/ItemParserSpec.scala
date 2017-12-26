
import models.exceptions.JsonParsingException._
import services.ItemService
import org.scalatest._
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global

class ItemParserSpec extends FunSuite with Matchers {
  //TODO rewrite everything here
  implicit val db: slick.jdbc.PostgresProfile.backend.Database = Database.forConfig("development")
  val itemService = new ItemService

  test("Printing results of valid test sample№1") {
    var results = itemService.getItemsOutOfString(validJSON).right.get
    println(results)
  }

  test("Passing a faulty json should produce an error") {
    assert(itemService.getItemsOutOfString("randomString").isLeft)
  }
  test("Validating a correct item should not produce an exception") {
    val item = itemService.getItemsOutOfString(CorrectItem).right.get
 //   assert(results.head._1.contains(item.head) && results.head._2.isEmpty)
  }
  test("Validating an item without important field should produce a minor exception") {
    val item = itemService.getItemsOutOfString(ItemWithoutADescrption).right.get
  //  val results = itemService.validateItems(item)
  //  assert(results.head._1.contains(item.head))
   // results.head._2.get shouldBe a[MinorItemParsingException]
  }
  test("Validating items without necessary fields should produce major exceptions") {
    val items = itemService.getItemsOutOfString(itemsThatShouldRaiseMajorExceptions).right.get
  //  val results = itemService.validateItems(items)
  //  assert(results.forall(_._1.isEmpty))
  //  results.foreach(_._2.get shouldBe a[MajorItemParsingException])
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
  val CorrectItem =
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
  val ItemWithoutADescrption =
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

  val itemsThatShouldRaiseMajorExceptions =
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
