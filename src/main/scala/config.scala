import com.example.spark.spring.Application
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.springframework.context.ApplicationContext

object config {
  private var _context: ApplicationContext = _

  def context: ApplicationContext = {
    if (_context == null) {
      Application.main(Array[String]())
      _context = Application.context
    }
    _context
  }

  implicit def sc: SparkContext = context.getBean(classOf[SparkContext])
  implicit def ssc: StreamingContext = context.getBean(classOf[StreamingContext])
  implicit def spark: SparkSession = context.getBean(classOf[SparkSession])
  implicit def stream: DStream[ConsumerRecord[String, String]] = context.getBean(classOf[DStream[ConsumerRecord[String, String]]])
}