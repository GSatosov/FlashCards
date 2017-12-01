import services.ItemParser
import models.Exceptions.ItemParsingException
import org.scalatest._

class ItemParserSpec extends FunSuite {

  //To unfold json string, hover over \n, press Alt+Enter and select Convert to """String"""
  val correctJSON: String = "{\n\t\"Level01\":[\n\t\t{\n\t\t\t\"text\":\"蚕\",\n\t\t\t\"translationVariants\": [\"silkworm\"],\n\t\t\t\"readingVariants\": [\"さん\"],\n\t\t\t\"precedence\": 0\n\t\t},\n\t\t{\n\t\t\t\"text\":\"韻\",\n\t\t\t\"translationVariants\":[\"rhyme\", \"elegance\", \"tone\"],\n\t\t\t\"readingVariants\":[\"いん\"],\n\t\t\t\"precedence\": 0\n\t\t}],\n\t\"Level03\":[\n\t\t{\n\t\t\"text\":\"謁\",\n\t\t\"translationVariants\":[\"audience\", \"audience with king\"],\n\t\t\"readingVariants\":[],\n\t\t\"precedence\": 0\n\t\t}\n\t]\n}"
  val JSONWithNoText = "{\n\t\"Level01\":[\n\t\t{\n\t\t\"text\":\"\",\n\t\t\"translationVariants\":[\"audience\", \"audience with king\"],\n\t\t\"readingVariants\":[\"えっ\"],\n\t\t\"precedence\": 1\n\t\t}\n\t]\n}"
  val JSONWithNothingToTest = "{\n\t\"Level01\":[\n\t\t{\n\t\t\"text\":\"謁\",\n\t\t\"translationVariants\":[],\n\t\t\"readingVariants\":[\"\"],\n\t\t\"precedence\": 1\n\t\t}\n\t]\n}"

  test("Printing results of test sample№1") {
    println(ItemParser.makeJSONOutOfString(correctJSON))
  }

  test("passing a faulty json should produce an error") {
    assert(ItemParser.makeJSONOutOfString("randomString").isLeft)
  }
  //TODO figure out how to check types elegantly
  test("Passing an item with no text should result in item exception") {
    assert(ItemParser.makeJSONOutOfString(JSONWithNoText) == Right(Vector((None, Some(ItemParsingException)))))
  }

  test("Passing an item with text but with no fields that can be checked should results in item exception ") {
    assert(ItemParser.makeJSONOutOfString(JSONWithNothingToTest) == Right(Vector(None, Some(ItemParsingException))))
  }
}
