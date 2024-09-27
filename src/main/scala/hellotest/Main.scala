
import org.apache.commons.collections4.queue.CircularFifoQueue
import scala.language.unsafeNulls
import scala.collection.mutable
import mainargs.{main, arg, ParserForMethods, Flag}

object Main:

  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args.toIndexedSeq)

  @main
  def run(
    // Set arguments and default values
    @arg(short = 'c', doc = "size of the sliding word cloud") cloud_size: Int = 10,
    @arg(short = 'l', doc = "minimum word length to be considere") length_at_least: Int = 6,
    @arg(short = 'w', doc = "size of the sliding FIFO queue") window_size: Int = 1000,
    @arg(short = 's', doc = "number of steps between word cloud updates") every_K: Int = 10,
    @arg(short = 'f', doc = "minimum frequency for a word to be included in the cloud") min_frequency: Int = 3) = {
      // Validate arguments
      try {
        if (cloud_size < 1) {
          throw new NumberFormatException()
        }
       
        if (length_at_least < 1) {
          throw new NumberFormatException()
        }
        
        if (window_size < 1) {
          throw new NumberFormatException()
        }
     
        if (min_frequency < 1) {
          throw new NumberFormatException()
        }
    
        if (every_K < 1) {
          throw new NumberFormatException()
        }
      }
     
    catch{
      case _: NumberFormatException =>
        System.err.println("The arguments should be natural numbers")
        System.exit(4)
    }
    // Separate I/O and logic by creating OutputSink
    trait OutputSink{
      def doOutput(value: String): Unit
    }

    // Create OutputSink instance that prints the output of fullQueue. This separates I/O from logic
    object myOutputSink extends OutputSink{
      def doOutput(value: String) = {
        println(value)
      }
    }

    // Function to process full queue
    def fullQueue(queue: CircularFifoQueue[String], output:OutputSink): Unit = {

      // Create a variable 'frequency', which is a mutable map of a string and integer
      val frequency = mutable.Map[String, Int]() 

      // For each word in the current queue: if the string is not in 'frequency', set the word frequency to 0. Add 1 to the frequency.
      queue.forEach {word => 
        frequency(word) = frequency.getOrElse(word,0) + 1
      }

      // Sort by descending frequency and take the first c pairs
      val sortedfrequency: Seq[(String, Int)] = frequency.toSeq.sortBy(-_._2).filter{case (_,count) => count >=min_frequency}.take(cloud_size)


      val out = sortedfrequency.map {case (word,count) => s"$word: $count" }.mkString(" ")
      output.doOutput(out)

    }

    
    // Set up input Scanner
    val lines = scala.io.Source.stdin.getLines
    val words = 
      lines.flatMap(l => l.split("(?U)[^\\p{Alpha}0-9']+")).map(_.toLowerCase) //.map(_.toLowerCase) satisfies EC for case-insensitivity

    val queue = new CircularFifoQueue[String](window_size)

    var steps = 0 // Initialize to count steps
    words.filter(_.length >= length_at_least).foreach {word =>

      // Add the word to the queue
      queue.add(word)
      steps += 1 // Increment steps by 1 after word added to queue
      
      // If the queue is full after adding the word AND steps >= k, call the fullQueue function on the queue. Additionally, reset steps.
      if ((queue.isAtFullCapacity) & (steps >= every_K))  {
        steps = 0
        fullQueue(queue,myOutputSink)
      }
    }

    // pseudo-code for functionality:
    // Read words ((length >= l) and (EC:not in "ignore_list")) from input into a FIFO queue of length w.
    // After queue fills up with w valid words, count the frequency of unique words in the queue.
    // Output the top c words/frequencies (EC: only words with frequency >= f) in the format "w1:f1 w2:f2", where w1 is the first word and f1 is the corresponding frequency for w1.
    // 


  }
    

end Main
