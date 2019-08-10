package io.github.seckillPro.db

import java.util.Properties

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.github.seckillPro.config.ConfigLoader
import scalikejdbc.{ConnectionPool, DB, DBSession, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * 数据库连接以及CRUD事物封装
 *
 * @author 梦境迷离
 * @time 2019-08-01
 * @version v2.0
 */
trait DatabaseSupport extends LazyLogging {

  import DatabaseSupport._

  def readOnly[A](execution: DBSession ⇒ A): Future[A] = concurrent.Future {
    using(getDB) { db: DB =>
      db.readOnly((session: DBSession) => execution(session))
    }
  }

  def localTx[A](execution: DBSession ⇒ A): Future[A] = concurrent.Future {
    using(getDB) { db: DB =>
      db.localTx((session: DBSession) => execution(session))
    }
  }

  @deprecated
  def localTxWithoutFuture[A](execution: DBSession ⇒ A): A =
    using(getDB) { db: DB =>
      db.localTx((session: DBSession) => execution(session))
    }

  def getAutoCommitSession = getDB.autoCommitSession()

  def getReadOnlySession = getDB.readOnlySession()
}

/**
 * 数据库连接池，启动服务时需要执行init方法初始化数据库
 */
object DatabaseSupport extends DatabaseSupport {

  def getConnectionPool: ConnectionPool = {
    ConnectionPool.get()
  }

  def getDB: DB = {
    DB(ConnectionPool.get().borrow())
  }

  private final lazy val defaultConfig = ConfigLoader.defaultConfig

  def init(config: Config = defaultConfig): Unit = {
    logger.info("Init connection pool from config scalike")
    val dataSourceConfig = getScalikeDatasourceProperties("seckill", config)
    val _config = new HikariConfig(dataSourceConfig)
    val dataSource = new HikariDataSource(_config)
    ConnectionPool.singleton(new DataSourceConnectionPool(dataSource))
  }

  private def getScalikeDatasourceProperties(databaseName: String, config: Config): Properties = {
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
