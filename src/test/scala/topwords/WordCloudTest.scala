package topwords

import org.scalatest.funsuite.AnyFunSuite
import scala.collection.mutable.Map
import scala.language.unsafeNulls



class TestOutputSink extends WordCloud.OutputSink {
    val freqMap: Map[String,Int] = Map()
    
    def doOutput(value: Seq[(String, Int)]) = {
      freqMap.clear()
      freqMap ++= value.toMap
    }
  }

class WordCloudTest extends AnyFunSuite {


  test("WordCloud calculates word frequencies as expected") {

    val words = Iterator("hello","hello","world","hello","world","today")
    val output = new TestOutputSink
    WordCloud.processing(words=words, cloud_size=3, length_at_least=2, window_size=6, every_K=1, min_frequency=1, outputSink=output)

    assert(output.freqMap("hello") == 3, "Frequency of 'hello' should be 3")
    assert(output.freqMap("world") == 2, "Frequency of 'world' should be 2")
    assert(output.freqMap("today") == 1, "Frequency of 'today' should be 1")
  }

  test("Processing handles empty input") {
    val words = Iterator.empty[String]
    val output = new TestOutputSink
    WordCloud.processing(
      words = words,
      cloud_size = 3,
      length_at_least = 1,
      window_size = 3,
      every_K = 1,
      min_frequency = 1,
      outputSink = output
    )

    assert(output.freqMap.isEmpty, "Output should be empty for empty input")
  }

  test("Ignores words shorter than length_at_least") {
    val words = Iterator("a", "b", "c", "aa", "bb", "cc","aa","bb")
    val output = new TestOutputSink()
    WordCloud.processing(words=words, cloud_size=3,length_at_least=2, window_size=5, every_K=1, min_frequency=1, outputSink=output)

    assert(!output.freqMap.contains("a"),"'a' should not be in the output because its length is < 2")
    assert(!output.freqMap.contains("b"),"'b' should not be in the output because its length is < 2")
    assert(output.freqMap("aa") == 2,"Frequency of 'aa' should be 2")
    assert(output.freqMap("bb") == 2,"Frequency of 'bb' should be 2")
  }

  test("Window size works with FIFO queue") {
    val words = Iterator("dd","bb","cc","aa","bb","ee")
    val output = new TestOutputSink()
    WordCloud.processing(words=words, cloud_size=4,length_at_least=2, window_size=5, every_K=1, min_frequency=1, outputSink=output)
    assert(output.freqMap.contains("ee"),"'ee' should be in the queue")
    assert(!output.freqMap.contains("dd"),"'dd' should be pushed out of the queue")

  }

  test("Length requirement not met") {
    val words = Iterator("a","b","c","d","e","f")
    val output = new TestOutputSink()
    WordCloud.processing(words=words, cloud_size=4,length_at_least=2, window_size=5, every_K=1, min_frequency=1, outputSink=output)

    assert(output.freqMap.isEmpty,"No words should be in the queue because they are all length < 2")

  }

  test("Minimum frequency requirement not met") {
    val words = Iterator("aa","bb","cc","dd","ee","ff")
    val output = new TestOutputSink()
    WordCloud.processing(words=words, cloud_size=4,length_at_least=2, window_size=5, every_K=1, min_frequency=5, outputSink=output)

    assert(output.freqMap.isEmpty,"No words should be in the queue because they all appear < 5 times")

  }

  test("Main handles NumberFormatException for invalid args") {
    intercept[NumberFormatException] {
      Main.argValidation(-1,5,100,1,2)
    }
  }


  test("Words with non-alphanumeric characters are processed correctly") {
    val words = Iterator("hello!", "@world", "hello", "#scala", "world?", "hello")
      .flatMap(_.split("(?U)[^\\p{Alpha}0-9']+"))
      .map(_.toLowerCase)
    val output = new TestOutputSink()
    WordCloud.processing(
      words = words,
      cloud_size = 3,
      length_at_least = 1,
      window_size = 6,
      every_K = 1,
      min_frequency = 1,
      outputSink = output
    )

    assert(output.freqMap("hello") == 3, "Frequency of 'hello' should be 3")
    assert(output.freqMap("world") == 2, "Frequency of 'world' should be 2")
    assert(output.freqMap("scala") == 1, "Frequency of 'scala' should be 1")
  }

  test("Words with apostrophes are processed correctly") {
    val words = Iterator("don't", "can't", "won't", "can't", "don't")
    val output = new TestOutputSink()
    WordCloud.processing(
      words = words,
      cloud_size = 3,
      length_at_least = 3,
      window_size = 5,
      every_K = 1,
      min_frequency = 1,
      outputSink = output
    )

    assert(output.freqMap("don't") == 2, "Frequency of 'don't' should be 2")
    assert(output.freqMap("can't") == 2, "Frequency of 'can't' should be 2")
    assert(output.freqMap("won't") == 1, "Frequency of 'won't' should be 1")
  }

  test("Words are counted in a case-insensitive manner") {
    val words = Iterator("Hello", "world", "HELLO", "World", "hello")
      .map(_.toLowerCase)
    val output = new TestOutputSink()
    WordCloud.processing(
      words = words,
      cloud_size = 2,
      length_at_least = 1,
      window_size = 5,
      every_K = 1,
      min_frequency = 1,
      outputSink = output
    )

    assert(output.freqMap("hello") == 3, "Frequency of 'hello' should be 3")
    assert(output.freqMap("world") == 2, "Frequency of 'world' should be 2")
  }


}
