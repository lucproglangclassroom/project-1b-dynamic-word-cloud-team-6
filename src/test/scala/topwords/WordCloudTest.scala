package topwords

import org.scalatest.funsuite.AnyFunSuite
import scala.collection.mutable.Map



class TestOutputSink extends WordCloud.OutputSink {
    val freqMap: Map[String,Int] = Map()
    
    def doOutput(value: Seq[(String, Int)]) = {
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

}
