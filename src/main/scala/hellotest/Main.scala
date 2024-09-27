
import org.apache.commons.collections4.queue.CircularFifoQueue
import scala.language.unsafeNulls
import scala.collection.mutable
import mainargs.{main, arg, ParserForMethods, Flag}

object Main:

  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args.toIndexedSeq)

  @main
  def run(
    @arg(short = 'c', doc = "size of the sliding word cloud") cloud_size: Int = 10,
    @arg(short = 'l', doc = "minimum word length to be considere") length_at_least: Int = 6,
    @arg(short = 'w', doc = "size of the sliding FIFO queue") window_size: Int = 1000,
    @arg(short = 's', doc = "number of steps between word cloud updates") every_K: Int = 10,
    @arg(short = 'f', doc = "minimum frequency for a word to be included in the cloud") min_frequency: Int = 3) = {
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
  }
    

end Main
