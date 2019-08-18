package io.github.seckillPro.database

import java.util.Properties

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.github.seckillPro.config.ConfigLoader
import scalikejdbc.{ConnectionPool, DataSourceConnectionPool}

/**
 * 数据源与配置获取
 *
 * @author 梦境迷离
 * @time 2019-08-18
 * @version v1.0
 */
trait DataSourceSupport extends LazyLogging {

  private final lazy val defaultConfig = ConfigLoader.getConfig

  /**
   * 数据库连接池，启动服务时需要执行init方法初始化数据库
   */
  def init(config: Config = defaultConfig): Unit = {
    logger.info("Init connection pool from config scalike")
    val dataSourceConfig = getScalikeDatasourceProperties("seckill", config)
    val _config = new HikariConfig(dataSourceConfig)
    val dataSource = new HikariDataSource(_config)
    ConnectionPool.singleton(new DataSourceConnectionPool(dataSource))
  }

  def getScalikeDatasourceProperties(databaseName: String, config: Config): Properties = {
    import scala.collection.JavaConverters._
    val properties = new Properties()
    properties.setProperty("dataSourceClassName", config.getString("scalike.dataSourceClassName"))
    val databaseConfig = config.getConfig("scalike").getConfig(databaseName)
    databaseConfig.entrySet().asScala.foreach { e =>
      properties.setProperty(e.getKey, e.getValue.unwrapped().toString)
    }
    properties
  }
}