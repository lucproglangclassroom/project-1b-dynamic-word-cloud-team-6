
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


end Main
