import org.apache.commons.collections4.queue.CircularFifoQueue
import scala.collection.mutable
import scala.language.unsafeNulls
import scala.io.Source

object Main:

  // Default values for arguments
  val CLOUD_SIZE = 10
  val LENGTH_AT_LEAST = 6
  val WINDOW_SIZE = 1000

  def main(args: Array[String]) = 

    // Argument validity checking
    if (args.length > 3) {
      System.err.nn.println("usage: ./target/universal/stage/bin/main [cloud-size] [length-at-least] [window-size]")
      System.exit(2)
    }

    // Parse the command-line argument or use the default value
    var cloud_size = CLOUD_SIZE
    var length_at_least = LENGTH_AT_LEAST
    var window_size = WINDOW_SIZE

    try {
      if (args.length >= 1) {
        cloud_size = args(0).toInt
        if (cloud_size < 1) throw new NumberFormatException()
      }
      if (args.length >= 2) {
        length_at_least = args(1).toInt
        if (length_at_least < 1) throw new NumberFormatException()
      }
      if (args.length == 3) {
        window_size = args(2).toInt
        if (window_size < 1) throw new NumberFormatException()
      }
    } catch {
      case _: NumberFormatException =>
        Console.err.println("The arguments should be natural numbers")
        System.exit(4)
    }

    // Set up input from stdin and process words
    val lines = scala.io.Source.stdin.getLines
    val words = lines.flatMap(l => l.split("(?U)[^\\p{Alpha}0-9']+")).map(_.toLowerCase)

    // Circular buffer to hold the most recent words
    val queue = new CircularFifoQueue[String](window_size)

    // Process words and update word cloud
    words.filter(_.length >= length_at_least).foreach { word =>
      queue.add(word) // Add word to the queue

      // Only start processing once the queue is filled with `window_size` words
      if (queue.size == window_size) {
        // Compute word frequencies
        val wordCount = mutable.Map[String, Int]()
        queue.forEach { w =>
          wordCount(w) = wordCount.getOrElse(w, 0) + 1
        }

        // Sort by frequency (descending) and alphabetically in case of ties
        val sortedWords = wordCount.toSeq.sortBy { case (word, count) => (-count, word) }

        // Take the top `cloud_size` words
        val topWords = sortedWords.take(cloud_size)

        // Print the word cloud in the required format
        println(topWords.map { case (word, count) => s"$word: $count" }.mkString(" "))
      }
    }

end Main

