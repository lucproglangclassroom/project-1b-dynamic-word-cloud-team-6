package topwords

import org.apache.commons.collections4.queue.CircularFifoQueue
import scala.language.unsafeNulls
import scala.collection.mutable
import sun.misc.{Signal, SignalHandler} 
import mainargs.{main, arg, ParserForMethods, Flag}
import org.log4s._
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.chart.ChartFactory
import javax.imageio.ImageIO
import java.io.File


object Main:

  // Default values for arguments


  def argValidation(cloud_size: Int, length_at_least: Int, window_size: Int, min_frequency: Int, every_K: Int): Unit = {
  if (cloud_size<1 || length_at_least<1 || window_size<1 || min_frequency<1 ||every_K<1) {
    throw new NumberFormatException("Arguments should be natural numbers")
    }
  }

  def main(args: Array[String]): Unit = {
    try {
      ParserForMethods(this).runOrExit(args.toIndexedSeq)
    } catch {
      case e: NumberFormatException =>
        System.err.println(e.getMessage)
        System.exit(4)
    }
  }

  @main
  def run( 
    @arg(short = 'c', doc = "size of the sliding word cloud") cloud_size: Int = 10,
    @arg(short = 'l', doc = "minimum word length to be considere") length_at_least: Int = 6,
    @arg(short = 'w', doc = "size of the sliding FIFO queue") window_size: Int = 1000,
    @arg(short = 'k', doc = "number of steps between word cloud updates") every_K: Int = 10,
    @arg(short = 'f', doc = "minimum frequency for a word to be included in the cloud") min_frequency: Int = 3,
    @arg(short = 'i', doc = "path to ignore file") ignore_file: Option[String] = None,
    @arg(short = 't', doc = "path to input text file") input_file: Option[String] = None) = {

    // Handle SIGPIPE signal by exiting 
    try {
      Signal.handle(new Signal("PIPE"), _ => {
        System.err.println("SIGPIPE detected. Terminating.")
        System.exit(0)
      })
    } catch {
      case e: IllegalArgumentException =>
        System.err.println("Signal handling not supported on this platform.")
    }

    // Ignore file
    val ignore: Set[String] = try {
      ignore_file match {
        case Some(path) => scala.io.Source.fromFile(path).getLines().map(_.toLowerCase).toSet
        case None => Set.empty[String]
      }
    } catch {
      case e: Exception =>
        System.err.println(s"Error reading ignore file: ${e.getMessage}")
        Set.empty[String]
    }

    // Set up input Scanner
    val lines = input_file match {
      case Some(path) => scala.io.Source.fromFile(path).getLines()
      case None => scala.io.Source.stdin.getLines()
    }
    val words = 
      lines.flatMap(l => l.split("(?U)[^\\p{Alpha}0-9']+")).map(_.toLowerCase).filter(word => !ignore.contains(word))

    // Call WordCloud with given words and arguments
    WordCloud.processing(words=words, cloud_size=cloud_size, length_at_least=length_at_least, window_size=window_size, every_K=every_K, min_frequency=min_frequency, outputSink=WordCloud.myOutputSink)

    val logger = org.log4s.getLogger
    logger.debug(f"Cloud Size = $cloud_size Length At Leasts = $length_at_least Window Size = $window_size Every K = $every_K Min Frequency = $min_frequency")
  }

end Main


object WordCloud {

  def processing(words: Iterator[String], cloud_size: Int, length_at_least: Int, window_size: Int, every_K: Int, min_frequency: Int, outputSink: OutputSink): Unit = {

    words.filter(_.length >= length_at_least).scanLeft((0,List.empty[String])) {case ((steps,queue),word) =>
      val newQueue = (queue :+ word).takeRight(window_size)
      val newSteps = steps+1

      if (newQueue.size >= window_size && newSteps >= every_K) {
        fullQueue(newQueue,cloud_size,min_frequency,outputSink)
        (0,newQueue) // Resets steps
      } else {
        (newSteps,newQueue)
      }

    }
    .foreach { _ => () }
  }

  // Separate I/O and logic by creating OutputSink
  trait OutputSink{
    def doOutput(value: Seq[(String,Int)]): Unit
  }

  // Create OutputSink instance that prints the output of fullQueue. This separates I/O from logic
  object myOutputSink extends OutputSink {
    def doOutput(value: Seq[(String, Int)]) = {
      try {
        val out = value.map {case (word,count) => s"$word: $count" }.mkString(" ")
        println(out)
        visualization(value) // call barchart visualization
      } catch {
        case _: java.io.IOException =>
          System.err.println("Broken pipe error. Exiting")
          System.exit(0)
      }
    }
  }

  // Function to process full queue
  def fullQueue(queue: List[String], cloud_size: Int, min_frequency: Int, output:OutputSink): Unit = {

    // Create a variable 'frequency', which contains a map with identical words grouped together as the key and the value as the value/frequency of the grouped words
    val frequency = queue.groupBy(identity).view.mapValues(_.size).toMap 

    // Sort by descending frequency and take the first c pairs
    val sortedfrequency: Seq[(String, Int)] = frequency.toSeq.sortBy(-_._2).filter{case (_,count) => count >=min_frequency}.take(cloud_size)

    output.doOutput(sortedfrequency)

  }

    def visualization(wordCounts: Seq[(String,Int)]): Unit = {

    val data = new DefaultCategoryDataset() //Initialize dataset to hold words and frequencies

    //Add words and their frequencies to the dataset
    wordCounts.foldLeft(data) { (d,wordCount) =>
      val (word,count) = wordCount
      d.addValue(count, "Word", word)
      d
    }

    // Create the barchart 
    val barchart = ChartFactory.createBarChart("Word Cloud Barchart","Words","Frequency",data)

    val image = barchart.createBufferedImage(1000,800)

    // Write an image to the docs folder
    ImageIO.write(image, "png", new File("docs/barchart.png"))

  }

}
