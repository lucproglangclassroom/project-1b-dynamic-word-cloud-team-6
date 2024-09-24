
import org.apache.commons.collections4.queue.CircularFifoQueue
import scala.language.unsafeNulls

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
      if (args.length >=1){
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
    } catch{
      case _: NumberFormatException =>
        System.err.println("The arguments should be natural numbers")
        System.exit(4)
    }

    // Set up input Scanner
    val lines = scala.io.Source.stdin.getLines
    val words = 
      lines.flatMap(l => l.split("(?U)[^\\p{Alpha}0-9']+")).map(_.toLowerCase) //.map(_.toLowerCase) satisfies EC for case-insensitivity

    val queue = new CircularFifoQueue[String](window_size)
    words.filter(_.length >= length_at_least).foreach(queue.add)
    // pseudo-code for functionality:
    // Read words ((length >= l) and (EC:not in "ignore_list")) from input into a FIFO queue of length w.
    // After queue fills up with w valid words, count the frequency of unique words in the queue.
    // Output the top c words/frequencies (EC: only words with frequency >= f) in the format "w1:f1 w2:f2", where w1 is the first word and f1 is the corresponding frequency for w1.
    // 



end Main
