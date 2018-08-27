import java.util.Properties

import com.example.spark.spring.{Application, DatabaseProperties}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, SparkSession}
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

  def time(action: () => Any): Any = {
    val start = System.currentTimeMillis()
    val result = action()
    println(s"Elapsed: ${(System.currentTimeMillis() - start)/1000}s")
    result
  }

  def table(table: String): DataFrame = {
    val configuration = context.getBean(classOf[DatabaseProperties])
    val props = new Properties()
    props.setProperty("user", configuration.getUsername)
    props.setProperty("password", configuration.getPassword)
    spark.read.jdbc(configuration.getJdbcUrl, table, props)
  }

  def sc: SparkContext = context.getBean(classOf[SparkContext])
  def ssc: StreamingContext = context.getBean(classOf[StreamingContext])
  def spark: SparkSession = context.getBean(classOf[SparkSession])
  def stream: DStream[ConsumerRecord[String, String]] = context.getBean(classOf[DStream[ConsumerRecord[String, String]]])
}